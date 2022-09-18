package com.machiav3lli.fdroid.content

import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.utility.extension.android.Android

val BooleanPrefsMeta = mapOf(
    Preferences.Key.ShowScreenshots to Pair(
        R.string.show_screenshots,
        R.string.show_screenshots_description
    ),
    Preferences.Key.InstallAfterSync to Pair(
        R.string.install_after_sync,
        R.string.install_after_sync_summary
    ),
    Preferences.Key.UpdateNotify to Pair(
        R.string.notify_about_updates,
        R.string.notify_about_updates_summary
    ),
    Preferences.Key.UpdateUnstable to Pair(
        R.string.unstable_updates,
        R.string.unstable_updates_summary
    ),
    Preferences.Key.IncompatibleVersions to Pair(
        R.string.incompatible_versions,
        R.string.incompatible_versions_summary
    ),
    Preferences.Key.RootPermission to Pair(
        R.string.root_permission,
        R.string.root_permission_description
    ),
    Preferences.Key.RootSessionInstaller to Pair(
        R.string.root_session_installer,
        R.string.root_session_installer_description
    ),
)

val NonBooleanPrefsMeta = mapOf(
    Preferences.Key.Language to R.string.prefs_language_title,
    Preferences.Key.Theme to R.string.theme,
    Preferences.Key.DefaultTab to R.string.default_tab,
    Preferences.Key.UpdatedApps to R.string.prefs_updated_apps,
    Preferences.Key.NewApps to R.string.prefs_new_apps,
    Preferences.Key.AutoSync to R.string.sync_repositories_automatically,
    Preferences.Key.AutoSyncInterval to R.string.auto_sync_interval,
    Preferences.Key.ReleasesCacheRetention to R.string.releases_cache_retention,
    Preferences.Key.ImagesCacheRetention to R.string.images_cache_retention,
    Preferences.Key.ProxyType to R.string.proxy_type,
    Preferences.Key.ProxyHost to R.string.proxy_host,
    Preferences.Key.ProxyPort to R.string.proxy_port,
)

val PrefsEntries = mapOf(
    Preferences.Key.Theme to mutableMapOf(
        Preferences.Theme.Light to R.string.light,
        Preferences.Theme.Dark to R.string.dark,
        Preferences.Theme.Black to R.string.amoled
    ).apply {
        if (Android.sdk(29)) {
            put(Preferences.Theme.System, R.string.system)
            put(Preferences.Theme.SystemBlack, R.string.system_black)
        }
        if (Android.sdk(31))
            put(Preferences.Theme.Dynamic, R.string.dynamic)
    },
    Preferences.Key.DefaultTab to mapOf(
        Preferences.DefaultTab.Explore to R.string.explore,
        Preferences.DefaultTab.Latest to R.string.latest,
        Preferences.DefaultTab.Installed to R.string.installed,
    ),
    Preferences.Key.AutoSync to mapOf(
        Preferences.AutoSync.Wifi to R.string.only_on_wifi,
        Preferences.AutoSync.WifiBattery to R.string.only_on_wifi_and_battery,
        Preferences.AutoSync.Always to R.string.always,
        Preferences.AutoSync.Never to R.string.never,
    ),
    Preferences.Key.ProxyType to mapOf(
        Preferences.ProxyType.Direct to R.string.no_proxy,
        Preferences.ProxyType.Http to R.string.http_proxy,
        Preferences.ProxyType.Socks to R.string.socks_proxy,
    ),
)

val IntPrefsRanges = mapOf(
    Preferences.Key.UpdatedApps to 1..200,
    Preferences.Key.NewApps to 1..50,
    Preferences.Key.AutoSyncInterval to null,
    Preferences.Key.ReleasesCacheRetention to 0..365,
    Preferences.Key.ImagesCacheRetention to 0..365,
    Preferences.Key.ProxyPort to 1..65535,
)

val PrefsDependencies = mapOf(
    Preferences.Key.RootSessionInstaller to Preferences.Key.RootPermission,
    Preferences.Key.ProxyHost to Preferences.Key.ProxyType,
    Preferences.Key.ProxyPort to Preferences.Key.ProxyType,
)
