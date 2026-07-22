package com.jimmcgaw.whatsayyou.data

import kotlinx.coroutines.flow.Flow

interface AudioRecordRepository {
    fun observeAllOrderedByLastAccessed(): Flow<List<AudioRecordEntity>>

    suspend fun addRecording(audioFilePath: String, recordedAt: Long, durationMs: Long): Long
}
