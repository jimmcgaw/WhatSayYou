package com.jimmcgaw.whatsayyou.data

import kotlinx.coroutines.flow.Flow

interface AudioRecordRepository {
    fun observeAllOrderedByLastAccessed(): Flow<List<AudioRecordEntity>>
}
