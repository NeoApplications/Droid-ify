package com.looker.droidify.installer

import android.content.Context
import com.looker.droidify.BuildConfig
import com.looker.droidify.content.Cache
import com.looker.droidify.content.Preferences
import com.looker.droidify.utility.extension.android.Android
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.regex.Pattern

class RootInstaller(context: Context) : BaseInstaller(context) {

    companion object {
        private val getCurrentUserState: String =
            if (Android.sdk(25)) Shell.su("am get-current-user").exec().out[0]
            else Shell.su("dumpsys activity | grep -E \"mUserLru\"")
                .exec().out[0].trim()
                .removePrefix("mUserLru: [").removeSuffix("]")

        private val String.quote
            get() = "\"${this.replace(Regex("""[\\$"`]""")) { c -> "\\${c.value}" }}\""

        private val getUtilBoxPath: String
            get() {
                listOf("toybox", "busybox").forEach {
                    val shellResult = Shell.su("which $it").exec()
                    if (shellResult.out.isNotEmpty()) {
                        val utilBoxPath = shellResult.out.joinToString("")
                        if (utilBoxPath.isNotEmpty()) return utilBoxPath.quote
                    }
                }
                return ""
            }

        val File.install
            get() = String.format(
                ROOT_INSTALL_PACKAGE,
                absolutePath,
                BuildConfig.APPLICATION_ID,
                getCurrentUserState,
                length()
            )

        val File.session_install_create
            get() = String.format(
                ROOT_INSTALL_PACKAGE_SESSION_CREATE,
                BuildConfig.APPLICATION_ID,
                getCurrentUserState,
                length()
            )

        fun File.sessionInstallWrite(session_id: Int) = String.format(
            ROOT_INSTALL_PACKAGE_SESSION_WRITE,
            absolutePath,
            length(),
            session_id,
            name
        )

        fun sessionInstallCommit(session_id: Int) = String.format(
            ROOT_INSTALL_PACKAGE_SESSION_COMMIT,
            session_id
        )

        val String.uninstall
            get() = String.format(
                ROOT_UNINSTALL_PACKAGE,
                getCurrentUserState,
                this
            )

        val File.deletePackage
            get() = String.format(
                DELETE_PACKAGE,
                getUtilBoxPath,
                absolutePath.quote
            )
    }

    override suspend fun install(cacheFileName: String) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        mRootInstaller(cacheFile)
    }

    override suspend fun install(packageName: String, cacheFileName: String) {
        val cacheFile = Cache.getReleaseFile(context, cacheFileName)
        mRootInstaller(cacheFile)
    }

    override suspend fun install(packageName: String, cacheFile: File) =
        mRootInstaller(cacheFile)

    override suspend fun uninstall(packageName: String) = mRootUninstaller(packageName)

    private suspend fun mRootInstaller(cacheFile: File) {
        withContext(Dispatchers.Default) {
            if (Preferences[Preferences.Key.RootSessionInstaller]) {
                Shell.su(cacheFile.session_install_create)
                    .submit {
                        val sessionIdPattern = Pattern.compile("(\\d+)")
                        val sessionIdMatcher = sessionIdPattern.matcher(it.out[0])
                        val found = sessionIdMatcher.find()

                        if (found) {
                            val sessionId = sessionIdMatcher.group(1).toInt()
                            Shell.su(cacheFile.sessionInstallWrite(sessionId))
                                .submit {
                                    Shell.su(sessionInstallCommit(sessionId)).exec()
                                    if (it.isSuccess) Shell.su(cacheFile.deletePackage).submit()
                                }
                        }
                    }

            } else {
                Shell.su(cacheFile.install)
                    .submit { if (it.isSuccess) Shell.su(cacheFile.deletePackage).submit() }
            }
        }
    }

    private suspend fun mRootUninstaller(packageName: String) {
        withContext(Dispatchers.Default) {
            Shell.su(packageName.uninstall).submit()
        }
    }
}