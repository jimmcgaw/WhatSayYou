package com.jimmcgaw.whatsayyou.transcription

import kotlinx.coroutines.CompletableDeferred

class FakeLiveTranscriptionEngine(
    private val result: TranscriptionResult = TranscriptionResult.NoSpeechDetected,
) : LiveTranscriptionEngine {
    private val stopSignal = CompletableDeferred<Unit>()

    var startListeningCalled = false
        private set

    override suspend fun startListening(language: String): TranscriptionResult {
        startListeningCalled = true
        stopSignal.await()
        return result
    }

    override fun stopListening() {
        stopSignal.complete(Unit)
    }
}
