package org.indiv.dls.games.verboscruzados.ui

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

class Vibration(context: Context) {
    companion object {
        private const val VIBRATION_MSEC = 10L
    }

    private var vibrator: Vibrator? = null
    init {
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    fun vibrate() {
        vibrator?.let {
            if (it.hasVibrator()) {
                when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ->
                        vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ->
                        vibrator?.vibrate(VibrationEffect.createOneShot(VIBRATION_MSEC, VibrationEffect.DEFAULT_AMPLITUDE))
                    else -> vibrator?.vibrate(VIBRATION_MSEC)
                }
            }
        }
    }
}
