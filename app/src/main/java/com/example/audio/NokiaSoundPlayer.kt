package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NokiaSoundPlayer(private val context: Context) {

    private var toneGenerator: ToneGenerator? = null
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 65)
        } catch (e: Exception) {
            // Audio policy or stream unavailable, fallback safely
            toneGenerator = null
        }
    }

    fun playKeyClick(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (vibrateEnabled) {
            triggerVibration(12)
        }
        if (!soundEnabled) return

        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 20)
        } catch (_: Exception) {}
    }

    fun playSoftKeyBeep(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (vibrateEnabled) {
            triggerVibration(18)
        }
        if (!soundEnabled) return

        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 35)
        } catch (_: Exception) {}
    }

    fun playSendTone(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (vibrateEnabled) {
            triggerVibration(25)
        }
        if (!soundEnabled) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_A, 40)
                delay(60)
                toneGenerator?.startTone(ToneGenerator.TONE_DTMF_D, 60)
            } catch (_: Exception) {}
        }
    }

    fun playMessageReceivedSms(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (vibrateEnabled) {
            triggerVibration(45)
        }
        if (!soundEnabled) return

        // Famous Nokia SMS tone: Morse code for S-M-S (... -- ...)
        // S: dot dot dot
        // M: dash dash
        // S: dot dot dot
        CoroutineScope(Dispatchers.Default).launch {
            try {
                // S
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                delay(70)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                delay(70)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                delay(140)

                // M
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 90)
                delay(130)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 90)
                delay(140)

                // S
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                delay(70)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
                delay(70)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 40)
            } catch (_: Exception) {}
        }
    }

    fun playErrorTone(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 120)
        } catch (_: Exception) {}
    }

    private fun triggerVibration(millis: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(millis, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(millis)
            }
        } catch (_: Exception) {}
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {}
    }
}
