package com.jimmcgaw.whatsayyou.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAudioRecordRepository(
    initialRecords: List<AudioRecordEntity> = emptyList(),
) : AudioRecordRepository {
    private val records = MutableStateFlow(initialRecords)
    private var nextId = (initialRecords.maxOfOrNull { it.id } ?: 0) + 1

    val insertedRecords: List<AudioRecordEntity> get() = records.value

    override fun observeAllOrderedByLastAccessed(): Flow<List<AudioRecordEntity>> = records

    override suspend fun addRecording(audioFilePath: String, recordedAt: Long, durationMs: Long): Long {
        val entity = AudioRecordEntity(
            id = nextId++,
            audioFilePath = audioFilePath,
            transcript = null,
            transcriptionStatus = TranscriptionStatus.PENDING,
            recordedAt = recordedAt,
            lastAccessedAt = recordedAt,
            durationMs = durationMs,
            transcriptionEngine = null,
            language = null,
            title = null,
        )
        records.value = records.value + entity
        return entity.id
    }

    override suspend fun getById(id: Long): AudioRecordEntity? = records.value.find { it.id == id }

    override suspend fun update(record: AudioRecordEntity) {
        records.value = records.value.map { if (it.id == record.id) record else it }
    }
}
