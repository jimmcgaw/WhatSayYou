package com.jimmcgaw.whatsayyou.transcription

import java.io.File

interface TranscriptionEngine {
    suspend fun transcribe(audioFile: File, language: String): TranscriptionResult
}
