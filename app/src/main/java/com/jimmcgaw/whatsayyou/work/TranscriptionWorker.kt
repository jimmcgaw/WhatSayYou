package com.jimmcgaw.whatsayyou.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerParameters
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository
import com.jimmcgaw.whatsayyou.data.TranscriptionStatus
import com.jimmcgaw.whatsayyou.transcription.TranscriptionEngine
import com.jimmcgaw.whatsayyou.transcription.TranscriptionResult
import java.io.File

class TranscriptionWorker(
    context: Context,
    params: WorkerParameters,
    private val repository: AudioRecordRepository,
    private val transcriptionEngine: TranscriptionEngine,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val recordId = inputData.getLong(KEY_RECORD_ID, -1L)
        if (recordId == -1L) return Result.failure()

        val record = repository.getById(recordId) ?: return Result.failure()

        val inProgress = record.copy(transcriptionStatus = TranscriptionStatus.IN_PROGRESS)
        repository.update(inProgress)

        val language = record.language ?: DEFAULT_LANGUAGE
        val transcriptionResult = transcriptionEngine.transcribe(File(record.audioFilePath), language)

        val updated = when (transcriptionResult) {
            is TranscriptionResult.Success -> inProgress.copy(
                transcript = transcriptionResult.transcript,
                transcriptionStatus = TranscriptionStatus.COMPLETED,
                transcriptionEngine = ENGINE_NAME,
                language = language,
            )
            TranscriptionResult.NoSpeechDetected -> inProgress.copy(
                transcriptionStatus = TranscriptionStatus.NO_SPEECH_DETECTED,
                transcriptionEngine = ENGINE_NAME,
                language = language,
            )
            is TranscriptionResult.Failure -> inProgress.copy(transcriptionStatus = TranscriptionStatus.FAILED)
        }
        repository.update(updated)

        return Result.success()
    }

    companion object {
        const val KEY_RECORD_ID = "record_id"
        private const val DEFAULT_LANGUAGE = "en-US"
        private const val ENGINE_NAME = "SpeechRecognizer"
    }
}
