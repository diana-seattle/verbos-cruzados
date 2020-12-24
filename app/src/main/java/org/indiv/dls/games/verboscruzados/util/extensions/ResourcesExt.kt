package org.indiv.dls.games.verboscruzados.util.extensions

import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build

fun Resources.isInNightMode(): Boolean {
    return if (Build.VERSION.SDK_INT >= 30) {
        configuration.isNightModeActive
    } else {
        (configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }
}