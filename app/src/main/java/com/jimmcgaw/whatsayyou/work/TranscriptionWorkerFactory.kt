package com.jimmcgaw.whatsayyou.work

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.jimmcgaw.whatsayyou.di.AppContainer

class TranscriptionWorkerFactory(private val container: AppContainer) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {
        TranscriptionWorker::class.java.name -> TranscriptionWorker(
            appContext,
            workerParameters,
            container.audioRecordRepository,
            container.transcriptionEngine,
        )
        else -> null
    }
}
