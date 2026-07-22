package com.jimmcgaw.whatsayyou.audio

import java.io.File

interface AudioCaptureEngine {
    /** Suspends until [stopRecording] is called, then returns the finished WAV file. */
    suspend fun startRecording(): File

    fun stopRecording()
}
