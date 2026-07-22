package com.jimmcgaw.whatsayyou.transcription

sealed class TranscriptionResult {
    data class Success(val transcript: String) : TranscriptionResult()
    data object NoSpeechDetected : TranscriptionResult()
    data class Failure(val reason: String) : TranscriptionResult()
}
