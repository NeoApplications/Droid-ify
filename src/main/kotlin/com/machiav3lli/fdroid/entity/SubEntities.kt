package com.machiav3lli.fdroid.entity

import android.content.Context
import android.content.pm.PermissionGroupInfo
import android.content.pm.PermissionInfo
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.vector.ImageVector
import com.machiav3lli.backup.ui.compose.icons.phosphor.Plus
import com.machiav3lli.fdroid.FILTER_CATEGORY_ALL
import com.machiav3lli.fdroid.R
import com.machiav3lli.fdroid.content.Preferences
import com.machiav3lli.fdroid.database.entity.Release
import com.machiav3lli.fdroid.ui.compose.icons.Icon
import com.machiav3lli.fdroid.ui.compose.icons.Phosphor
import com.machiav3lli.fdroid.ui.compose.icons.icon.IcDonateFlattr
import com.machiav3lli.fdroid.ui.compose.icons.icon.IcDonateLiberapay
import com.machiav3lli.fdroid.ui.compose.icons.icon.IcDonateLitecoin
import com.machiav3lli.fdroid.ui.compose.icons.icon.IcDonateOpencollective
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ArrowSquareOut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Asterisk
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Barbell
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.BookBookmark
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Books
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Brain
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Calendar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Chat
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CircleWavyWarning
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CirclesFour
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Clock
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Code
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Command
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Compass
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CurrencyBTC
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.CurrencyDollarSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Download
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GameController
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Globe
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.GlobeSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Graph
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.HeartStraight
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.HeartStraightFill
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Key
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Leaf
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Microphone
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Nut
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.PaintBrush
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.PenNib
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Phone
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Pizza
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.PlayCircle
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Robot
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.RssSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShareNetwork
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.ShieldStar
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.SlidersHorizontal
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Swatches
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.TrashSimple
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.Wrench
import com.machiav3lli.fdroid.ui.compose.icons.phosphor.X
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Author(val name: String = "", val email: String = "", val web: String = "") {
    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Author>(json)
    }
}

@Serializable
sealed class Donate {
    @Serializable
    data class Regular(val url: String) : Donate()

    @Serializable
    data class Bitcoin(val address: String) : Donate()

    @Serializable
    data class Litecoin(val address: String) : Donate()

    @Serializable
    data class Flattr(val id: String) : Donate()

    @Serializable
    data class Liberapay(val id: String) : Donate()

    @Serializable
    data class OpenCollective(val id: String) : Donate()

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Donate>(json)
    }
}

@Serializable
class Screenshot(val locale: String, val type: Type, val path: String) {
    enum class Type(val jsonName: String) {
        PHONE("phone"),
        SMALL_TABLET("smallTablet"),
        LARGE_TABLET("largeTablet")
    }

    val identifier: String
        get() = "$locale.${type.name}.$path"

    fun toJSON() = Json.encodeToString(this)

    companion object {
        fun fromJson(json: String) = Json.decodeFromString<Screenshot>(json)
    }
}

enum class AntiFeature(val key: String, @StringRes val titleResId: Int) {
    ADS("Ads", R.string.has_advertising),
    DEBUGGABLE("ApplicationDebuggable", R.string.compiled_for_debugging),
    DISABLED_ALGORITHM("DisabledAlgorithm", R.string.signed_using_unsafe_algorithm),
    KNOWN_VULN("KnownVuln", R.string.has_security_vulnerabilities),
    NO_SOURCE_SINCE("NoSourceSince", R.string.source_code_no_longer_available),
    NON_FREE_ADD("NonFreeAdd", R.string.promotes_non_free_software),
    NON_FREE_ASSETS("NonFreeAssets", R.string.contains_non_free_media),
    NON_FREE_DEP("NonFreeDep", R.string.has_non_free_dependencies),
    NON_FREE_NET("NonFreeNet", R.string.promotes_non_free_network_services),
    TRACKING("Tracking", R.string.tracks_or_reports_your_activity),
    NON_FREE_UPSTREAM("UpstreamNonFree", R.string.upstream_source_code_is_not_free),
    NSFW("NSFW", R.string.not_safe_for_work)
}

fun String.toAntiFeature(): AntiFeature? = AntiFeature.values().find { it.key == this }

sealed interface ComponentState {
    val icon: ImageVector
    val textId: Int
}

sealed class ActionState(
    @StringRes override val textId: Int,
    override val icon: ImageVector = Phosphor.Download,
) : ComponentState {

    data object Install : ActionState(R.string.install, Phosphor.Download)
    data object Update : ActionState(R.string.update, Phosphor.Download)
    data object Uninstall : ActionState(R.string.uninstall, Phosphor.TrashSimple)
    data object Launch : ActionState(R.string.launch, Phosphor.ArrowSquareOut)
    data object Details : ActionState(R.string.details, Phosphor.SlidersHorizontal)
    data object Share : ActionState(R.string.share, Phosphor.ShareNetwork)
    class Cancel(@StringRes stateId: Int) : ActionState(stateId, Phosphor.X)
    data object NoAction : ActionState(R.string.no_action_possible, Phosphor.X)
    data object Bookmark : ActionState(R.string.favorite_add, Phosphor.HeartStraight)
    data object Bookmarked : ActionState(R.string.favorite_remove, Phosphor.HeartStraightFill)
}

open class LinkType(
    val icon: ImageVector,
    val title: String,
    val link: Uri? = null,
)

class DonateType(donate: Donate, context: Context) : LinkType(
    icon = when (donate) {
        is Donate.Regular        -> Phosphor.CurrencyDollarSimple
        is Donate.Bitcoin        -> Phosphor.CurrencyBTC
        is Donate.Litecoin       -> Icon.IcDonateLitecoin
        is Donate.Flattr         -> Icon.IcDonateFlattr
        is Donate.Liberapay      -> Icon.IcDonateLiberapay
        is Donate.OpenCollective -> Icon.IcDonateOpencollective
    },
    title = when (donate) {
        is Donate.Regular        -> context.getString(R.string.website)
        is Donate.Bitcoin        -> "Bitcoin"
        is Donate.Litecoin       -> "Litecoin"
        is Donate.Flattr         -> "Flattr"
        is Donate.Liberapay      -> "Liberapay"
        is Donate.OpenCollective -> "Open Collective"
    },
    link = when (donate) {
        is Donate.Regular        -> Uri.parse(donate.url)
        is Donate.Bitcoin        -> Uri.parse("bitcoin:${donate.address}")
        is Donate.Litecoin       -> Uri.parse("litecoin:${donate.address}")
        is Donate.Flattr         -> Uri.parse("https://flattr.com/thing/${donate.id}")
        is Donate.Liberapay      -> Uri.parse("https://liberapay.com/~${donate.id}")
        is Donate.OpenCollective -> Uri.parse("https://opencollective.com/${donate.id}")
    }
)

sealed class Request {
    internal abstract val id: Int
    internal abstract val installed: Boolean
    internal abstract val updates: Boolean
    internal abstract val filteredOutRepos: Set<String>
    internal abstract val category: String
    internal abstract val filteredAntiFeatures: Set<String>
    internal abstract val filteredLicenses: Set<String>
    internal abstract val section: Section
    internal abstract val order: Order
    internal abstract val ascending: Boolean
    internal abstract val updateCategory: UpdateCategory
    internal open val numberOfItems: Int = 0

    data class ProductsAll(override val section: Section) : Request() {
        override val id: Int
            get() = 1
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.ALL
        override val order: Order
            get() = Preferences[Preferences.Key.SortOrderExplore].order
        override val filteredOutRepos: Set<String>
            get() = Preferences[Preferences.Key.ReposFilterExplore]
        override val category: String
            get() = Preferences[Preferences.Key.CategoriesFilterExplore]
        override val filteredAntiFeatures: Set<String>
            get() = Preferences[Preferences.Key.AntifeaturesFilterExplore]
        override val filteredLicenses: Set<String>
            get() = Preferences[Preferences.Key.LicensesFilterExplore]
        override val ascending: Boolean
            get() = Preferences[Preferences.Key.SortOrderAscendingExplore]
    }

    data class ProductsSearch(override val section: Section) : Request() {
        override val id: Int
            get() = 1
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.ALL
        override val order: Order
            get() = Preferences[Preferences.Key.SortOrderSearch].order
        override val filteredOutRepos: Set<String>
            get() = Preferences[Preferences.Key.ReposFilterSearch]
        override val category: String
            get() = Preferences[Preferences.Key.CategoriesFilterSearch]
        override val filteredAntiFeatures: Set<String>
            get() = Preferences[Preferences.Key.AntifeaturesFilterSearch]
        override val filteredLicenses: Set<String>
            get() = Preferences[Preferences.Key.LicensesFilterSearch]
        override val ascending: Boolean
            get() = Preferences[Preferences.Key.SortOrderAscendingSearch]
    }

    data class ProductsInstalled(override val section: Section) : Request() {
        override val id: Int
            get() = 2
        override val installed: Boolean
            get() = true
        override val updates: Boolean
            get() = false
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.ALL
        override val order: Order
            get() = Preferences[Preferences.Key.SortOrderInstalled].order
        override val filteredOutRepos: Set<String>
            get() = Preferences[Preferences.Key.ReposFilterInstalled]
        override val category: String
            get() = Preferences[Preferences.Key.CategoriesFilterInstalled]
        override val filteredAntiFeatures: Set<String>
            get() = Preferences[Preferences.Key.AntifeaturesFilterInstalled]
        override val filteredLicenses: Set<String>
            get() = Preferences[Preferences.Key.LicensesFilterInstalled]
        override val ascending: Boolean
            get() = Preferences[Preferences.Key.SortOrderAscendingInstalled]
    }

    data class ProductsUpdates(override val section: Section) : Request() {
        override val id: Int
            get() = 3
        override val installed: Boolean
            get() = true
        override val updates: Boolean
            get() = true
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.ALL
        override val filteredOutRepos: Set<String>
            get() = emptySet()
        override val category: String
            get() = FILTER_CATEGORY_ALL
        override val filteredAntiFeatures: Set<String>
            get() = emptySet()
        override val filteredLicenses: Set<String>
            get() = emptySet()
        override val order: Order
            get() = Order.NAME
        override val ascending: Boolean
            get() = true
    }

    data class ProductsUpdated(override val section: Section) : Request() {
        override val id: Int
            get() = 4
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.UPDATED
        override val filteredOutRepos: Set<String>
            get() = Preferences[Preferences.Key.ReposFilterLatest]
        override val category: String
            get() = Preferences[Preferences.Key.CategoriesFilterLatest]
        override val filteredAntiFeatures: Set<String>
            get() = Preferences[Preferences.Key.AntifeaturesFilterLatest]
        override val filteredLicenses: Set<String>
            get() = Preferences[Preferences.Key.LicensesFilterLatest]
        override val order: Order
            get() = Preferences[Preferences.Key.SortOrderLatest].order
        override val ascending: Boolean
            get() = Preferences[Preferences.Key.SortOrderAscendingLatest]
        override val numberOfItems: Int
            get() = Preferences[Preferences.Key.UpdatedApps]
    }

    data class ProductsNew(override val section: Section) : Request() {
        override val id: Int
            get() = 5
        override val installed: Boolean
            get() = false
        override val updates: Boolean
            get() = false
        override val updateCategory: UpdateCategory
            get() = UpdateCategory.NEW
        override val filteredOutRepos: Set<String>
            get() = emptySet()
        override val category: String
            get() = FILTER_CATEGORY_ALL
        override val filteredAntiFeatures: Set<String>
            get() = emptySet()
        override val filteredLicenses: Set<String>
            get() = emptySet()
        override val order: Order
            get() = Order.DATE_ADDED
        override val ascending: Boolean
            get() = false
        override val numberOfItems: Int
            get() = Preferences[Preferences.Key.NewApps]
    }
}

sealed class DialogKey {
    data class Link(val uri: Uri) : DialogKey()
    data class ReleaseIncompatible(
        val incompatibilities: List<Release.Incompatibility>,
        val platforms: List<String>,
        val minSdkVersion: Int,
        val maxSdkVersion: Int,
    ) : DialogKey()

    data class ReleaseIssue(val resId: Int) : DialogKey()
    data class Launch(
        val packageName: String,
        val launcherActivities: List<Pair<String, String>>,
    ) : DialogKey()
}

data class Permission(
    val nameId: Int,
    val icon: ImageVector,
    val descriptionId: Int,
    val warningTextId: Int = -1,
    val ignorePref: Preferences.Key<Boolean>? = null,
) {
    companion object {
        val BatteryOptimization = Permission(
            R.string.ignore_battery_optimization_title,
            Phosphor.Leaf,
            R.string.ignore_battery_optimization_message,
            R.string.warning_disable_battery_optimization,
            Preferences.Key.IgnoreDisableBatteryOptimization,
        )
        val PostNotifications = Permission(
            R.string.post_notifications_permission_title,
            Phosphor.CircleWavyWarning,
            R.string.post_notifications_permission_message,
            R.string.warning_show_notification,
            Preferences.Key.IgnoreShowNotifications,
        )
    }
}

class PermissionsType(
    val group: PermissionGroupInfo?,
    val permissions: List<PermissionInfo>,
)

val String.appCategoryIcon: ImageVector
    get() = when (this.lowercase()) {
        FILTER_CATEGORY_ALL.lowercase() -> Phosphor.CirclesFour
        "audio"                         -> Phosphor.Microphone
        "audiovideo"                    -> Phosphor.PlayCircle
        "automation"                    -> Phosphor.Robot
        "connectivity"                  -> Phosphor.Graph
        "communication"                 -> Phosphor.Chat
        "calendar"                      -> Phosphor.Calendar
        "development"                   -> Phosphor.Code
        "education"                     -> Phosphor.Brain
        "fedilab"                       -> Phosphor.ShieldStar
        "fdroid"                        -> Phosphor.Asterisk
        "feed"                          -> Phosphor.RssSimple
        "food"                          -> Phosphor.Pizza
        "game"                          -> Phosphor.GameController
        "games"                         -> Phosphor.GameController
        "graphics"                      -> Phosphor.PaintBrush
        "guardian project"              -> Phosphor.ShieldStar
        "internet"                      -> Phosphor.Globe
        "kde"                           -> Phosphor.Code
        "kidsgame"                      -> Phosphor.GameController
        "math"                          -> Phosphor.Plus
        "messaging"                     -> Phosphor.Chat
        "money"                         -> Phosphor.CurrencyDollarSimple
        "multimedia"                    -> Phosphor.PlayCircle
        "navigation"                    -> Phosphor.Compass
        "network"                       -> Phosphor.GlobeSimple
        "office"                        -> Phosphor.Books
        "offline"                       -> Phosphor.Leaf
        "osmand"                        -> Phosphor.Compass
        "phone & sms"                   -> Phosphor.Phone
        "productivity"                  -> Phosphor.Compass
        "qt"                            -> Phosphor.Books
        "reading"                       -> Phosphor.BookBookmark
        "recorder"                      -> Phosphor.Microphone
        "religion"                      -> Phosphor.Command
        "science"                       -> Phosphor.Brain
        "science & education"           -> Phosphor.Brain
        "security"                      -> Phosphor.Key
        "sports & health"               -> Phosphor.Barbell
        "system"                        -> Phosphor.Nut
        "theming"                       -> Phosphor.Swatches
        "time"                          -> Phosphor.Clock
        "utility"                       -> Phosphor.Wrench
        "writing"                       -> Phosphor.PenNib
        "xposed"                        -> Phosphor.ShieldStar
        else                            -> Phosphor.Asterisk
    }
