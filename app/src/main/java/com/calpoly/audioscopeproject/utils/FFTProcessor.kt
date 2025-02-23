package com.calpoly.audioscopeproject.utils

import kotlin.math.*

object FFTProcessor {

    fun fft(real: DoubleArray, imag: DoubleArray) {
        val n = real.size
        if (n <= 1) return

        // Bit-reversal permutation
        val bitReversedIndices = IntArray(n)
        var bits = 0
        while (1 shl bits < n) bits++
        for (i in 0 until n) {
            var reversed = 0
            var temp = i
            for (j in 0 until bits) {
                reversed = (reversed shl 1) or (temp and 1)
                temp = temp shr 1
            }
            bitReversedIndices[i] = reversed
        }
        for (i in 0 until n) {
            if (i < bitReversedIndices[i]) {
                real[i] = real[bitReversedIndices[i]].also { real[bitReversedIndices[i]] = real[i] }
                imag[i] = imag[bitReversedIndices[i]].also { imag[bitReversedIndices[i]] = imag[i] }
            }
        }

        // Iterative FFT Cooley-Tukey
        var halfSize = 1
        while (halfSize < n) {
            val phaseShiftStepReal = kotlin.math.cos(Math.PI / halfSize)
            val phaseShiftStepImag = -kotlin.math.sin(Math.PI / halfSize)
            var currentPhaseShiftReal = 1.0
            var currentPhaseShiftImag = 0.0

            for (fftStep in 0 until halfSize) {
                for (i in fftStep until n step 2 * halfSize) {
                    val tempReal = currentPhaseShiftReal * real[i + halfSize] - currentPhaseShiftImag * imag[i + halfSize]
                    val tempImag = currentPhaseShiftReal * imag[i + halfSize] + currentPhaseShiftImag * real[i + halfSize]

                    real[i + halfSize] = real[i] - tempReal
                    imag[i + halfSize] = imag[i] - tempImag

                    real[i] += tempReal
                    imag[i] += tempImag
                }
                val tempReal = currentPhaseShiftReal * phaseShiftStepReal - currentPhaseShiftImag * phaseShiftStepImag
                currentPhaseShiftImag = currentPhaseShiftReal * phaseShiftStepImag + currentPhaseShiftImag * phaseShiftStepReal
                currentPhaseShiftReal = tempReal
            }
            halfSize *= 2
        }
    }

    // Computes the magnitude of the FFT output
    fun computeMagnitude(real: DoubleArray, imag: DoubleArray): DoubleArray {
        return DoubleArray(real.size) { sqrt(real[it] * real[it] + imag[it] * imag[it]) }
    }

}
