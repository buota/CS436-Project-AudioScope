package com.calpoly.audioscopeproject.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat.*
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AudioProcessor(private val context: Context) {

    private val sampleRate = 44100
    private val bufferSize = 1024
    private var isRecording = false
    private var audioRecord: AudioRecord? = null

    suspend fun startContinuousRecording(onRawDataCaptured: suspend (ShortArray) -> Unit) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("AudioDebug", "Microphone permission not granted")
            throw SecurityException("Microphone permission not granted")
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            CHANNEL_IN_MONO,
            ENCODING_PCM_16BIT,
            bufferSize * 2
        )

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("AudioDebug", "AudioRecord failed to initialize")
            return
        }

        val audioBuffer = ShortArray(bufferSize)
        isRecording = true
        audioRecord?.startRecording()
        Log.d("AudioDebug", "Audio recording started")

        try {
            while (isRecording) {
                val readSize = audioRecord?.read(audioBuffer, 0, bufferSize) ?: 0
                if (readSize > 0) {
                    val validData = audioBuffer.copyOf(readSize) // Keep only valid samples
                    withContext(Dispatchers.Main) {
                        onRawDataCaptured(validData)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("AudioDebug", "Error during recording: ${e.message}")
        } finally {
            stopRecording()
        }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        Log.d("AudioDebug", "Recording stopped")
    }
}
