package com.machiav3lli.fdroid.work

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Data
import androidx.work.WorkInfo
import com.machiav3lli.fdroid.ARG_PACKAGE_NAME
import com.machiav3lli.fdroid.MainApplication
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Downloaded
import com.machiav3lli.fdroid.service.ActionReceiver
import com.machiav3lli.fdroid.service.InstallerReceiver
import com.machiav3lli.fdroid.service.worker.DownloadState
import com.machiav3lli.fdroid.service.worker.DownloadWorker
import com.machiav3lli.fdroid.utility.downloadNotificationBuilder
import com.machiav3lli.fdroid.utility.extension.text.formatSize
import com.machiav3lli.fdroid.utility.updateWithError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class DownloadStateHandler(
    private val scope: CoroutineScope,
    private val downloadStates: WorkStateHolder<DownloadState>,
    private val notificationManager: NotificationManagerCompat
) {
    private val _downloadEvents = Channel<UpdateEvent>(Channel.BUFFERED)

    init {
        scope.launch {
            downloadStates.observeStates()
                .collect { states ->
                    states.forEach { (key, state) ->
                        handleDownloadState(key, state)
                    }
                }
        }

        scope.launch {
            _downloadEvents.consumeAsFlow()
                .collect { event ->
                    updateNotification(event)
                }
        }
    }

    private suspend fun handleDownloadState(key: String, state: DownloadState) {
        MainApplication.db.getDownloadedDao().upsert(
            Downloaded(
                packageName = state.packageName,
                version = state.version,
                repositoryId = state.repoId,
                cacheFileName = state.cacheFileName,
                changed = System.currentTimeMillis(),
                state = state,
            )
        )

        when (state) {
            is DownloadState.Success -> scope.launch {
                MainApplication.db.getInstallTaskDao()
                    .put(state.toInstallTask())
                downloadStates.updateState(key, null)
            }

            is DownloadState.Error   -> {
                Log.e(
                    "DownloadState", "Download failed: ${state.packageName}",
                    Exception(state.validationError.toString())
                )
                if (state.stopReason != WorkInfo.STOP_REASON_NOT_STOPPED) Log.i(
                    this::class.java.name,
                    "stopReason: ${state.stopReason} for download task: ${state.packageName}"
                )
            }

            else                     -> {}
        }
        _downloadEvents.send(
            UpdateEvent(
                key = key,
                state = state
            )
        )
    }

    fun updateState(key: String, state: DownloadState) {
        downloadStates.updateState(key, state)
    }

    private fun updateNotification(event: UpdateEvent) {
        val appContext = MainApplication.context
        val builder = createNotificationBuilder(event.state)
        if (event.state is DownloadState.Success || event.state is DownloadState.Cancel) {
            MainApplication.wm.notificationManager.cancel(event.key.hashCode())
        } else if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) notificationManager.notify(event.key.hashCode(), builder.build())
    }

    private fun createNotificationBuilder(state: DownloadState): NotificationCompat.Builder {
        val appContext = MainApplication.context
        val notificationBuilder = appContext.downloadNotificationBuilder()

        val cancelIntent = Intent(appContext, ActionReceiver::class.java).apply {
            action = ActionReceiver.COMMAND_CANCEL_DOWNLOAD
            putExtra(ARG_PACKAGE_NAME, state.packageName)
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            appContext,
            state.packageName.hashCode(),
            cancelIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        return when (state) {
            is DownloadState.Pending,
            is DownloadState.Connecting  -> notificationBuilder
                .setContentTitle(
                    appContext.getString(
                        R.string.downloading_FORMAT,
                        "${state.name} (${state.version})"
                    )
                )
                .setContentText(appContext.getString(R.string.pending))
                .setProgress(1, 0, true)
                .addAction(
                    R.drawable.ic_cancel,
                    appContext.getString(R.string.cancel),
                    cancelPendingIntent
                )

            is DownloadState.Downloading -> notificationBuilder
                .setContentTitle(
                    appContext.getString(
                        R.string.downloading_FORMAT,
                        "${state.name} (${state.version})"
                    )
                )
                .setContentText("${state.read.formatSize()} / ${state.total?.formatSize()}")
                .setProgress(100, state.progress, false)
                .addAction(
                    R.drawable.ic_cancel,
                    appContext.getString(R.string.cancel),
                    cancelPendingIntent
                )

            is DownloadState.Cancel      -> notificationBuilder
                .setOngoing(false)
                .setContentTitle(
                    appContext.getString(
                        R.string.downloading_FORMAT,
                        "${state.name} (${state.version})"
                    )
                )
                .setContentText(appContext.getString(R.string.canceled))
                .setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)

            is DownloadState.Success     -> notificationBuilder
                .setOngoing(false)
                .setContentTitle(
                    appContext.getString(
                        R.string.downloaded_FORMAT,
                        state.name
                    )
                )
                .apply {
                    if (!Preferences[Preferences.Key.KeepInstallNotification]) {
                        setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
                    }
                }

            is DownloadState.Error       -> notificationBuilder
                .setOngoing(false)
                .updateWithError(appContext, state, state.validationError)
                .setTimeoutAfter(InstallerReceiver.INSTALLED_NOTIFICATION_TIMEOUT)
        }
    }

    companion object {
        private data class UpdateEvent(
            val key: String,
            val state: DownloadState,
        )
    }
}

/**
 * @return if this is a new state we haven't processed
 */
class DownloadsTracker {
    private val activeWorks =
        ConcurrentHashMap<String, Pair<WorkInfo.State, DownloadWorker.Progress>>()

    fun trackWork(workInfo: WorkInfo, data: Data): Boolean {
        val previousState = activeWorks[workInfo.id.toString()]?.first
        val previousProgress = activeWorks[workInfo.id.toString()]?.second
        val currentState = workInfo.state
        val currentProgress = DownloadWorker.getProgress(data)

        activeWorks[workInfo.id.toString()] = Pair(currentState, currentProgress)

        if (currentState.isFinished) {
            activeWorks.remove(workInfo.id.toString())
        }

        return previousState != currentState ||
                (currentState == WorkInfo.State.RUNNING && previousProgress != currentProgress)
    }
}
