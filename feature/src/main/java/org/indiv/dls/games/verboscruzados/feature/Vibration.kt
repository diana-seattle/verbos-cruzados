package org.indiv.dls.games.verboscruzados.feature

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class Vibration(context: Context) {
    val VIBRATION_MSEC = 10L
    val VIBRATION_MSEC_LEGACY = 5L

    private var vibrator: Vibrator? = null
    init {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun vibrate() {
        vibrator?.let {
            if (it.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(VIBRATION_MSEC, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator?.vibrate(VIBRATION_MSEC_LEGACY)
                }
            }
        }
    }
}
