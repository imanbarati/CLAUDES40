package com.example.audio

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
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
            toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 50)
        } catch (_: Exception) {
            toneGenerator = null
        }
    }

    fun playKeyClick(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (vibrateEnabled) {
            triggerVibration(10)
        }
        if (!soundEnabled) return

        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 15)
        } catch (_: Exception) {}
    }

    fun playSoftKeyBeep(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (vibrateEnabled) {
            triggerVibration(12)
        }
        if (!soundEnabled) return

        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 20)
        } catch (_: Exception) {}
    }

    fun playSendTone(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (vibrateEnabled) {
            triggerVibration(18)
        }
        if (!soundEnabled) return

        CoroutineScope(Dispatchers.Default).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
            } catch (_: Exception) {}
        }
    }

    fun playMessageReceivedSms(soundEnabled: Boolean = true, vibrateEnabled: Boolean = true) {
        if (vibrateEnabled) {
            triggerVibration(25)
        }
        if (!soundEnabled) return

        // Gentle two-tone notification chime for Samsung Galaxy One UI
        CoroutineScope(Dispatchers.Default).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 40)
                delay(80)
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 50)
            } catch (_: Exception) {}
        }
    }

    fun playErrorTone(soundEnabled: Boolean = true) {
        if (!soundEnabled) return
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 80)
        } catch (_: Exception) {}
    }

    fun vibrateAgentToggle(vibrateEnabled: Boolean = true) {
        if (!vibrateEnabled) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(18)
            }
        } catch (_: Exception) {}
    }

    fun vibrateSpeechToggle(isStarting: Boolean, vibrateEnabled: Boolean = true) {
        if (!vibrateEnabled) return
        try {
            if (isStarting) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val timings = longArrayOf(0, 20, 40, 25)
                    val amplitudes = intArrayOf(0, 180, 0, 220)
                    vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(30)
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(12, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(12)
                }
            }
        } catch (_: Exception) {}
    }

    private fun triggerVibration(millis: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
