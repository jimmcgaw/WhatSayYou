package com.jimmcgaw.whatsayyou.work

import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

class WorkManagerTranscriptionScheduler(private val workManager: WorkManager) : TranscriptionScheduler {
    override fun enqueueTranscription(recordId: Long) {
        val request = OneTimeWorkRequestBuilder<TranscriptionWorker>()
            .setInputData(workDataOf(TranscriptionWorker.KEY_RECORD_ID to recordId))
            .build()
        workManager.enqueue(request)
    }
}
