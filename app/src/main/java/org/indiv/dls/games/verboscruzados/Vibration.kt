package org.indiv.dls.games.verboscruzados

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class Vibration(context: Context) {
    companion object {
        private const val VIBRATION_MSEC = 25L
        private const val VIBRATION_MSEC_LEGACY = 10L
    }

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
