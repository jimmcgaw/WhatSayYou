package com.jimmcgaw.whatsayyou.audio

import java.io.File
import kotlinx.coroutines.CompletableDeferred

class FakeAudioCaptureEngine(private val resultFile: File = File("fake_recording.wav")) : AudioCaptureEngine {
    private val stopSignal = CompletableDeferred<Unit>()

    var startRecordingCalled = false
        private set

    override suspend fun startRecording(): File {
        startRecordingCalled = true
        stopSignal.await()
        return resultFile
    }

    override fun stopRecording() {
        stopSignal.complete(Unit)
    }
}
