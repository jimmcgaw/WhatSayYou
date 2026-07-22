package com.jimmcgaw.whatsayyou.data

import kotlinx.coroutines.flow.Flow

class DefaultAudioRecordRepository(private val dao: AudioRecordDao) : AudioRecordRepository {
    override fun observeAllOrderedByLastAccessed(): Flow<List<AudioRecordEntity>> =
        dao.observeAllOrderedByLastAccessed()
}
