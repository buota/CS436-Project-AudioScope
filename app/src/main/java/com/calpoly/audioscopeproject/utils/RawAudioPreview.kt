package com.calpoly.audioscopeproject.utils.RawAudioPreview

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.calpoly.audioscopeproject.utils.AudioProcessor
import kotlinx.coroutines.*
import com.calpoly.audioscopeproject.utils.FFTProcessor


@Composable
fun RawAudioPreview(context: Context) {
    var dominantFrequency by remember { mutableStateOf(0.0) }
    var isRecording by remember { mutableStateOf(false) }
    val audioProcessor = remember { AudioProcessor(context) }
    val coroutineScope = rememberCoroutineScope()
    val sampleRate = 44100

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Button(
            onClick = {
                if (!isRecording) {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            (context as android.app.Activity),
                            arrayOf(Manifest.permission.RECORD_AUDIO),
                            0
                        )
                        Toast.makeText(context, "Grant microphone permission", Toast.LENGTH_SHORT).show()
                    } else {
                        isRecording = true
                        Log.d("AudioDebug", "Starting recording...")

                        coroutineScope.launch(Dispatchers.IO) {
                            audioProcessor.startContinuousRecording { audioData ->
                                val real = DoubleArray(audioData.size) { audioData[it].toDouble() }
                                val imag = DoubleArray(audioData.size) { 0.0 }

                                // Apply FFT
                                FFTProcessor.fft(real, imag)
                                val magnitudes = FFTProcessor.computeMagnitude(real, imag)

                                // Find the dominant frequency
                                val maxIndex = magnitudes.indices.maxByOrNull { magnitudes[it] } ?: 0
                                val frequency = (maxIndex * sampleRate) / audioData.size

                                Log.d("AudioDebug", "Dominant Frequency: $frequency Hz")

                                withContext(Dispatchers.Main) {
                                    dominantFrequency = frequency.toDouble()
                                }
                            }
                        }
                    }
                } else {
                    Log.d("AudioDebug", "Stopping recording...")
                    audioProcessor.stopRecording()
                    isRecording = false
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        ) {
            Text(if (isRecording) "Stop Recording" else "Start Recording")
        }

        Text(text = "Dominant Frequency: ${dominantFrequency} Hz", modifier = Modifier.padding(top = 16.dp))

        // Updates UI every 10ms while recording
        LaunchedEffect(isRecording) {
            while (isRecording) {
                delay(10) // Refresh every 10ms
            }
        }
    }
}

@Preview
@Composable
fun PreviewRawAudio() {
    RawAudioPreview(context = LocalContext.current)
}

