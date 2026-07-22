package com.jimmcgaw.whatsayyou.transcription

interface LiveTranscriptionEngine {
    /** Starts listening now; suspends until [stopListening] is called, then returns the result. */
    suspend fun startListening(language: String): TranscriptionResult

    fun stopListening()
}
