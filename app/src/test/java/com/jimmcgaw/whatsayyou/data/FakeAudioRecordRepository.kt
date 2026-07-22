package com.jimmcgaw.whatsayyou.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAudioRecordRepository(
    initialRecords: List<AudioRecordEntity> = emptyList(),
) : AudioRecordRepository {
    private val records = MutableStateFlow(initialRecords)

    override fun observeAllOrderedByLastAccessed(): Flow<List<AudioRecordEntity>> = records
}
