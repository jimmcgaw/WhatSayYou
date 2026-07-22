package com.jimmcgaw.whatsayyou.work

interface TranscriptionScheduler {
    fun enqueueTranscription(recordId: Long)
}
