/*
 *  This file is part of Omega Launcher
 *  Copyright (c) 2021   Saul Henriquez
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.saggitt.omega.icons

import android.content.Context
import android.content.pm.LauncherActivityInfo
import android.content.pm.ShortcutInfo
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.annotation.RequiresApi
import com.android.launcher3.R
import com.android.launcher3.Utilities
import com.android.launcher3.model.data.ItemInfo
import com.google.android.apps.nexuslauncher.DynamicIconProvider
import com.saggitt.omega.iconpack.AdaptiveIconCompat
import com.saggitt.omega.iconpack.IconPackManager

class CustomIconProvider(context: Context) : DynamicIconProvider(context) {
    private val iconPackManager by lazy { IconPackManager.getInstance(context) }

    fun getIcon(shortcutInfo: ShortcutInfo, iconDpi: Int): Drawable? {
        return iconPackManager.getIcon(shortcutInfo, iconDpi).assertNotAdaptiveIconDrawable(shortcutInfo)
    }

    fun getDynamicIcon(launcherActivityInfo: LauncherActivityInfo?, iconDpi: Int): Drawable {
        return super.getIcon(launcherActivityInfo, iconDpi).assertNotAdaptiveIconDrawable(launcherActivityInfo)
    }

    private fun <T> T.assertNotAdaptiveIconDrawable(info: Any?): T {
        if (Utilities.ATLEAST_OREO && this is AdaptiveIconDrawable) {
            error("unwrapped AdaptiveIconDrawable for ${
                if (info is LauncherActivityInfo) info.applicationInfo else info
            }")
        }
        return this
    }

    companion object {

        @JvmStatic
        fun getAdaptiveIconDrawableWrapper(context: Context): AdaptiveIconCompat {
            return AdaptiveIconCompat.wrap(context.getDrawable(
                    R.drawable.adaptive_icon_drawable_wrapper)!!.mutate()) as AdaptiveIconCompat
        }
    }
}