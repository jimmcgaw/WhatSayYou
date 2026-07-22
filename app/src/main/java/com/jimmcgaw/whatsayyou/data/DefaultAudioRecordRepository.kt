package com.jimmcgaw.whatsayyou.data

import kotlinx.coroutines.flow.Flow

class DefaultAudioRecordRepository(private val dao: AudioRecordDao) : AudioRecordRepository {
    override fun observeAllOrderedByLastAccessed(): Flow<List<AudioRecordEntity>> =
        dao.observeAllOrderedByLastAccessed()

    override suspend fun addRecording(audioFilePath: String, recordedAt: Long, durationMs: Long): Long =
        dao.insert(
            AudioRecordEntity(
                audioFilePath = audioFilePath,
                transcript = null,
                transcriptionStatus = TranscriptionStatus.PENDING,
                recordedAt = recordedAt,
                lastAccessedAt = recordedAt,
                durationMs = durationMs,
                transcriptionEngine = null,
                language = null,
                title = null,
            ),
        )

    override suspend fun getById(id: Long): AudioRecordEntity? = dao.getById(id)

    override suspend fun update(record: AudioRecordEntity) = dao.update(record)
}
