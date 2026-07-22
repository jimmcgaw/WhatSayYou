package com.jimmcgaw.whatsayyou.work

class FakeTranscriptionScheduler : TranscriptionScheduler {
    val enqueuedRecordIds = mutableListOf<Long>()

    override fun enqueueTranscription(recordId: Long) {
        enqueuedRecordIds.add(recordId)
    }
}
