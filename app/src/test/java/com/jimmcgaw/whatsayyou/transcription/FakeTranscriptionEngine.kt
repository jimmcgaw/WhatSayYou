package com.jimmcgaw.whatsayyou.transcription

import java.io.File

class FakeTranscriptionEngine(private val result: TranscriptionResult) : TranscriptionEngine {
    override suspend fun transcribe(audioFile: File, language: String): TranscriptionResult = result
}
