package com.machiav3lli.fdroid

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.installer.AppInstaller
import com.machiav3lli.fdroid.pages.AppSheet
import com.machiav3lli.fdroid.ui.compose.theme.AppTheme
import com.machiav3lli.fdroid.ui.navigation.AppNavHost
import com.machiav3lli.fdroid.ui.navigation.NavItem
import com.machiav3lli.fdroid.utility.extension.text.nullIfEmpty
import com.machiav3lli.fdroid.utility.extension.text.pathCropped
import com.machiav3lli.fdroid.utility.isDarkTheme
import com.machiav3lli.fdroid.utility.setCustomTheme
import com.machiav3lli.fdroid.viewmodels.AppSheetVM
import com.machiav3lli.fdroid.viewmodels.ExploreVM
import com.machiav3lli.fdroid.viewmodels.InstalledVM
import com.machiav3lli.fdroid.viewmodels.LatestVM
import com.machiav3lli.fdroid.viewmodels.PrefsVM
import com.machiav3lli.fdroid.viewmodels.SearchVM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class NeoActivity : AppCompatActivity() {
    companion object {
        const val ACTION_UPDATES = "${BuildConfig.APPLICATION_ID}.intent.action.UPDATES"
        const val ACTION_INSTALL = "${BuildConfig.APPLICATION_ID}.intent.action.INSTALL"
        const val EXTRA_UPDATES = "${BuildConfig.APPLICATION_ID}.intent.extra.UPDATES"
        const val EXTRA_CACHE_FILE_NAME =
            "${BuildConfig.APPLICATION_ID}.intent.extra.CACHE_FILE_NAME"
    }

    sealed class SpecialIntent {
        data object Updates : SpecialIntent()
        class Install(val packageName: String?, val cacheFileName: String?) : SpecialIntent()
        class AddRepo(val address: String?, val fingerprint: String?) : SpecialIntent()
    }

    private lateinit var navController: NavHostController
    private val cScope: CoroutineScope = CoroutineScope(Dispatchers.Default)

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private lateinit var appSheetPackage: MutableState<String>

    val db
        get() = (application as MainApplication).db

    private var currentTheme by Delegates.notNull<Int>()

    val exploreViewModel: ExploreVM by viewModels {
        ExploreVM.Factory(db)
    }
    val latestViewModel: LatestVM by viewModels {
        LatestVM.Factory(db)
    }
    val installedViewModel: InstalledVM by viewModels {
        InstalledVM.Factory(db)
    }
    val searchViewModel: SearchVM by viewModels {
        SearchVM.Factory(db)
    }
    val prefsViewModel: PrefsVM by viewModels {
        PrefsVM.Factory(db)
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        (application as MainApplication).mActivity = this
        currentTheme = Preferences[Preferences.Key.Theme].resId
        setCustomTheme()
        enableEdgeToEdge()
        MainApplication.mainActivity = this
        super.onCreate(savedInstanceState)

        setContent {
            DisposableEffect(Preferences[Preferences.Key.Theme]) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDarkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { isDarkTheme },
                )
                onDispose {}
            }

            AppTheme(
                darkTheme = when (Preferences[Preferences.Key.Theme]) {
                    is Preferences.Theme.System -> isSystemInDarkTheme()
                    is Preferences.Theme.SystemBlack -> isSystemInDarkTheme()
                    else -> isDarkTheme
                }
            ) {
                val mScope = rememberCoroutineScope()
                navController = rememberNavController()

                appSheetPackage = remember { mutableStateOf("") }
                val appSheetState = rememberModalBottomSheetState(true)
                val appSheetVM = remember(appSheetPackage.value) {
                    AppSheetVM(
                        MainApplication.db,
                        appSheetPackage.value,
                    )
                }

                Scaffold(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.onBackground,
                ) { paddingValues ->
                    LaunchedEffect(key1 = navController) {
                        if (savedInstanceState == null && (intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY) == 0) {
                            handleIntent(intent)
                        }
                    }

                    AppNavHost(
                        modifier = Modifier.imePadding(),
                        navController = navController,
                    )

                    if (appSheetPackage.value.isNotEmpty()) {
                        ModalBottomSheet(
                            sheetState = appSheetState,
                            containerColor = MaterialTheme.colorScheme.background,
                            scrimColor = Color.Transparent,
                            dragHandle = null,
                            onDismissRequest = {
                                mScope.launch { appSheetState.hide() }
                                appSheetPackage.value = ""
                            },
                        ) {

                            AppSheet(
                                appSheetVM,
                                appSheetPackage.value,
                            ) {
                                mScope.launch { appSheetState.hide() }
                                appSheetPackage.value = ""
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (currentTheme != Preferences[Preferences.Key.Theme].resId)
            recreate()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private val Intent.packageName: String?
        get() {
            val uri = data
            return when {
                uri?.scheme == "package" || uri?.scheme == "fdroid.app" -> {
                    uri.schemeSpecificPart?.nullIfEmpty()
                }

                uri?.scheme == "market" && uri.host == "details"        -> {
                    uri.getQueryParameter("id")?.nullIfEmpty()
                }

                uri != null && uri.scheme in setOf("http", "https")     -> {
                    val host = uri.host.orEmpty()
                    if (host == "f-droid.org" || host.endsWith(".f-droid.org")) {
                        uri.lastPathSegment?.nullIfEmpty()
                    } else if (host == "apt.izzysoft.de") {
                        uri.lastPathSegment?.nullIfEmpty()
                    } else {
                        null
                    }
                }

                else                                                    -> {
                    null
                }
            }
        }

    private fun handleSpecialIntent(specialIntent: SpecialIntent) {
        when (specialIntent) {
            is SpecialIntent.Updates -> {
                // TODO directly update the apps??
                navController.navigate("${NavItem.Main.destination}?page=2")
            }

            is SpecialIntent.AddRepo -> {
                prefsViewModel.setIntent(
                    specialIntent.address,
                    specialIntent.fingerprint,
                )
                navController.navigate("${NavItem.Prefs.destination}?page=2")
            }

            is SpecialIntent.Install -> {
                val packageName = specialIntent.packageName
                if (!packageName.isNullOrEmpty()) {
                    lifecycleScope.launch {
                        specialIntent.cacheFileName?.let {
                            AppInstaller.getInstance(this@NeoActivity)
                                ?.defaultInstaller?.install(packageName, it)
                        }
                    }
                }
                Unit
            }
        }::class
    }

    private fun handleIntent(intent: Intent?) {
        val data = intent?.data
        val host = data?.host

        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                if (data != null && !intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)) {
                    intent.putExtra(EXTRA_INTENT_HANDLED, true)
                    val (addressText, fingerprintText) = try {
                        val uri = data.buildUpon()
                            .scheme("https")
                            .path(data.path?.pathCropped)
                            .query(null).fragment(null).build().toString()
                        val fingerprintText =
                            data.getQueryParameter("fingerprint")?.uppercase()?.nullIfEmpty()
                                ?: data.getQueryParameter("FINGERPRINT")?.uppercase()?.nullIfEmpty()
                        Pair(uri, fingerprintText)
                    } catch (e: Exception) {
                        Pair(null, null)
                    }
                    handleSpecialIntent(
                        SpecialIntent.AddRepo(
                            addressText,
                            fingerprintText
                        )
                    )
                } else if (host == "search") {
                    val query = data.getQueryParameter("q")
                    navController.navigate("${NavItem.Main.destination}?page=3")
                    cScope.launch { _searchQuery.emit(query ?: "") }
                } else {
                    val packageName = intent.packageName
                    if (!packageName.isNullOrEmpty()) {
                        navigateProduct(packageName)
                    }
                }
            }

            ACTION_UPDATES     -> { // TODO Handle EXTRA_UPDATES
                if (!intent.getBooleanExtra(EXTRA_INTENT_HANDLED, false)) {
                    intent.putExtra(EXTRA_INTENT_HANDLED, true)
                    handleSpecialIntent(SpecialIntent.Updates)
                }
            }

            ACTION_INSTALL     -> handleSpecialIntent(
                SpecialIntent.Install(
                    intent.packageName,
                    intent.getStringExtra(EXTRA_CACHE_FILE_NAME)
                )
            )
        }
    }


    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val scan = result.data?.getStringExtra("SCAN_RESULT")
                scan?.replace("fdroidrepo", "http")
                intent.data = Uri.parse(scan)
                intent.action = Intent.ACTION_VIEW
                handleIntent(intent)
            }
        }

    fun openScanner() {
        intent.putExtra(EXTRA_INTENT_HANDLED, false)
        resultLauncher.launch(Intent(INTENT_ACTION_BINARY_EYE))
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ContextWrapperX.wrap(newBase))
    }

    internal fun navigateProduct(packageName: String) {
        appSheetPackage.value = packageName
    }

    fun setSearchQuery(value: String) {
        cScope.launch { _searchQuery.emit(value) }
    }
}
