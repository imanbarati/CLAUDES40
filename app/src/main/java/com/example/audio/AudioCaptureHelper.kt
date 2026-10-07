package com.example.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * State representation for [AudioCaptureHelper] voice capture operations.
 */
sealed class AudioCaptureState {
    object Idle : AudioCaptureState()
    data class Capturing(
        val amplitude: Float,
        val dbLevel: Float,
        val isVoiceDetected: Boolean,
        val durationMs: Long
    ) : AudioCaptureState()
    data class Completed(
        val pcmData: ByteArray,
        val durationMs: Long
    ) : AudioCaptureState()
    data class Error(val message: String) : AudioCaptureState()
}

/**
 * AudioCaptureHelper
 *
 * Encapsulates audio stream processing, conversion, and hardware capture using Android's
 * low-level [AudioRecord] API.
 *
 * Features:
 * - 16kHz 16-bit Mono PCM audio capture (standard speech-to-text format).
 * - Real-time audio stream processing: RMS amplitude, dBFS decibel levels, and Voice Activity Detection (VAD).
 * - Format conversions: 16-bit PCM bytes <-> ShortArray, FloatArray (normalized -1.0 to 1.0), and WAV container encoding.
 * - Clean interface for starting, stopping, and observing voice capture in Jetpack Compose UI.
 */
class AudioCaptureHelper(private val context: Context) {

    companion object {
        private const val TAG = "AudioCaptureHelper"
        const val SAMPLE_RATE = 16000 // 16kHz standard for voice input
        const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val AUDIO_SOURCE = MediaRecorder.AudioSource.MIC

        // Voice activity detection threshold (normalized RMS >= 0.035 roughly equals voice)
        private const val VAD_AMPLITUDE_THRESHOLD = 0.035f

        /**
         * Converts raw 16-bit little-endian PCM byte array to signed ShortArray.
         */
        fun pcmToShortArray(pcmBytes: ByteArray): ShortArray {
            val shorts = ShortArray(pcmBytes.size / 2)
            ByteBuffer.wrap(pcmBytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts)
            return shorts
        }

        /**
         * Converts ShortArray to raw 16-bit little-endian PCM byte array.
         */
        fun shortArrayToPcm(shorts: ShortArray): ByteArray {
            val bytes = ByteArray(shorts.size * 2)
            ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(shorts)
            return bytes
        }

        /**
         * Converts 16-bit PCM byte array to normalized float array [-1.0f .. 1.0f].
         */
        fun pcmToFloatArray(pcmBytes: ByteArray): FloatArray {
            val shorts = pcmToShortArray(pcmBytes)
            val floats = FloatArray(shorts.size)
            for (i in shorts.indices) {
                floats[i] = (shorts[i] / 32768.0f).coerceIn(-1.0f, 1.0f)
            }
            return floats
        }

        /**
         * Wraps raw PCM audio in a valid 44-byte RIFF/WAV header.
         *
         * @param pcmData Raw PCM audio bytes.
         * @param sampleRate Sampling rate in Hz (default 16000).
         * @param channels Channel count (1 for mono, 2 for stereo).
         * @param bitsPerSample Bit depth (default 16).
         * @return Complete WAV file byte array ready for playback or export.
         */
        fun pcmToWav(
            pcmData: ByteArray,
            sampleRate: Int = SAMPLE_RATE,
            channels: Int = 1,
            bitsPerSample: Int = 16
        ): ByteArray {
            val totalAudioLen = pcmData.size.toLong()
            val totalDataLen = totalAudioLen + 36
            val byteRate = (sampleRate * channels * bitsPerSample / 8).toLong()

            val header = ByteArray(44)
            // RIFF chunk descriptor
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalDataLen and 0xff).toByte()
            header[5] = ((totalDataLen shr 8) and 0xff).toByte()
            header[6] = ((totalDataLen shr 16) and 0xff).toByte()
            header[7] = ((totalDataLen shr 24) and 0xff).toByte()
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()

            // fmt sub-chunk
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16 // 16 for PCM
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // AudioFormat: 1 = PCM
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = (channels * bitsPerSample / 8).toByte() // block align
            header[33] = 0
            header[34] = bitsPerSample.toByte()
            header[35] = 0

            // data sub-chunk
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalAudioLen and 0xff).toByte()
            header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

            val wavOut = ByteArray(44 + pcmData.size)
            System.arraycopy(header, 0, wavOut, 0, 44)
            System.arraycopy(pcmData, 0, wavOut, 44, pcmData.size)
            return wavOut
        }
    }

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private val audioBufferStream = ByteArrayOutputStream()
    private var recordingStartTime = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _dbLevel = MutableStateFlow(-60f)
    val dbLevel: StateFlow<Float> = _dbLevel.asStateFlow()

    private val _isVoiceDetected = MutableStateFlow(false)
    val isVoiceDetected: StateFlow<Boolean> = _isVoiceDetected.asStateFlow()

    private val _captureState = MutableStateFlow<AudioCaptureState>(AudioCaptureState.Idle)
    val captureState: StateFlow<AudioCaptureState> = _captureState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Checks if the application holds [Manifest.permission.RECORD_AUDIO].
     */
    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Clean interface for starting audio capture.
     *
     * @param onAudioChunk Optional callback receiving captured PCM audio chunk on IO thread.
     * @return True if capture successfully started, false otherwise.
     */
    @Synchronized
    fun start(onAudioChunk: ((ByteArray, Int) -> Unit)? = null): Boolean {
        return startRecording(onAudioChunk)
    }

    /**
     * Backward-compatible start recording method.
     */
    @Synchronized
    fun startRecording(onBufferCaptured: ((ByteArray, Int) -> Unit)? = null): Boolean {
        if (_isRecording.value) {
            Log.w(TAG, "AudioRecord is already running.")
            return true
        }

        if (!hasPermission()) {
            val err = "RECORD_AUDIO permission not granted"
            _errorMessage.value = err
            _captureState.value = AudioCaptureState.Error(err)
            Log.e(TAG, err)
            return false
        }

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        if (minBufferSize <= 0) {
            val err = "Failed to calculate valid min buffer size ($minBufferSize)"
            _errorMessage.value = err
            _captureState.value = AudioCaptureState.Error(err)
            Log.e(TAG, err)
            return false
        }

        val bufferSize = (minBufferSize * 2).coerceAtLeast(1024)

        return try {
            val record = AudioRecord(
                AUDIO_SOURCE,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )

            if (record.state != AudioRecord.STATE_INITIALIZED) {
                val err = "AudioRecord failed initialization"
                _errorMessage.value = err
                _captureState.value = AudioCaptureState.Error(err)
                record.release()
                return false
            }

            audioBufferStream.reset()
            recordingStartTime = System.currentTimeMillis()

            record.startRecording()
            audioRecord = record
            _isRecording.value = true
            _errorMessage.value = null
            _captureState.value = AudioCaptureState.Capturing(0f, -60f, false, 0L)

            recordingJob = scope.launch {
                val buffer = ByteArray(bufferSize)
                while (isActive && _isRecording.value) {
                    val bytesRead = record.read(buffer, 0, buffer.size)
                    if (bytesRead > 0) {
                        // Store to internal stream
                        audioBufferStream.write(buffer, 0, bytesRead)

                        // Calculate RMS amplitude (0.0 to 1.0)
                        val rms = calculateRms(buffer, bytesRead)
                        val normalized = (rms / 32767f).coerceIn(0f, 1f)
                        _audioAmplitude.value = normalized

                        // Calculate decibels (dBFS)
                        val db = if (rms > 0) 20 * log10(rms / 32767.0).toFloat().coerceIn(-60f, 0f) else -60f
                        _dbLevel.value = db

                        // Voice activity detection
                        val voiceActive = normalized >= VAD_AMPLITUDE_THRESHOLD
                        _isVoiceDetected.value = voiceActive

                        val durationMs = System.currentTimeMillis() - recordingStartTime
                        _captureState.value = AudioCaptureState.Capturing(
                            amplitude = normalized,
                            dbLevel = db,
                            isVoiceDetected = voiceActive,
                            durationMs = durationMs
                        )

                        onBufferCaptured?.invoke(buffer.copyOf(bytesRead), bytesRead)
                    } else if (bytesRead < 0) {
                        Log.w(TAG, "AudioRecord read error code: $bytesRead")
                        break
                    }
                }
            }
            true
        } catch (e: SecurityException) {
            val err = "SecurityException: Missing RECORD_AUDIO permission"
            _errorMessage.value = err
            _captureState.value = AudioCaptureState.Error(err)
            Log.e(TAG, err, e)
            false
        } catch (e: Exception) {
            val err = "Error initializing AudioRecord: ${e.message}"
            _errorMessage.value = err
            _captureState.value = AudioCaptureState.Error(err)
            Log.e(TAG, err, e)
            false
        }
    }

    /**
     * Clean interface for stopping audio capture.
     *
     * @return Captured PCM byte array, or null if nothing was recorded.
     */
    @Synchronized
    fun stop(): ByteArray? {
        val data = if (audioBufferStream.size() > 0) audioBufferStream.toByteArray() else null
        val durationMs = if (recordingStartTime > 0) System.currentTimeMillis() - recordingStartTime else 0L

        stopRecording()

        if (data != null && data.isNotEmpty()) {
            _captureState.value = AudioCaptureState.Completed(data, durationMs)
        } else {
            _captureState.value = AudioCaptureState.Idle
        }
        return data
    }

    /**
     * Backward-compatible stop recording method.
     */
    @Synchronized
    fun stopRecording() {
        _isRecording.value = false
        _audioAmplitude.value = 0f
        _dbLevel.value = -60f
        _isVoiceDetected.value = false

        recordingJob?.cancel()
        recordingJob = null

        try {
            audioRecord?.let { record ->
                if (record.recordingState == AudioRecord.RECORDSTATE_RECORDING) {
                    record.stop()
                }
                record.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Exception stopping AudioRecord", e)
        } finally {
            audioRecord = null
        }
    }

    /**
     * Returns true if audio recording is currently active.
     */
    fun isCapturing(): Boolean = _isRecording.value

    /**
     * Retrieves all recorded audio in raw 16-bit PCM format.
     */
    fun getCapturedPcm(): ByteArray = audioBufferStream.toByteArray()

    /**
     * Retrieves all recorded audio packaged as a playable WAV file.
     */
    fun getCapturedWav(): ByteArray = pcmToWav(audioBufferStream.toByteArray(), SAMPLE_RATE, 1, 16)

    /**
     * Calculates the Root-Mean-Square (RMS) amplitude of 16-bit PCM samples.
     */
    private fun calculateRms(buffer: ByteArray, bytesRead: Int): Float {
        var sum = 0.0
        val shortCount = bytesRead / 2
        if (shortCount <= 0) return 0f

        for (i in 0 until bytesRead step 2) {
            if (i + 1 < bytesRead) {
                val sample = ((buffer[i + 1].toInt() shl 8) or (buffer[i].toInt() and 0xFF)).toShort()
                sum += sample * sample
            }
        }
        return sqrt(sum / shortCount).toFloat()
    }

    /**
     * Releases hardware resources and cancels internal coroutine scopes.
     */
    fun release() {
        stopRecording()
        scope.cancel()
    }
}
