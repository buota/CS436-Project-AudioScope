package com.calpoly.audioscopeproject.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat.*
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat

class AudioRecorder(private val context: Context) {
    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val SAMPLE_RATE = 44100
    private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
        SAMPLE_RATE, CHANNEL_IN_MONO, ENCODING_PCM_16BIT
    )

    fun startRecording() {
        if (isRecording) return
        isRecording = true

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Handle missing permissions (request from activity)
            return
        }

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_IN_MONO,
            ENCODING_PCM_16BIT,
            BUFFER_SIZE
        ).apply { startRecording() }
    }

    fun stopRecording() {
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
    }
}
