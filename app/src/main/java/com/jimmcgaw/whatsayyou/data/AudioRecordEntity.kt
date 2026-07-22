package com.jimmcgaw.whatsayyou.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "audio_records",
    indices = [Index(value = ["lastAccessedAt"])],
)
data class AudioRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val audioFilePath: String,
    val transcript: String?,
    val transcriptionStatus: TranscriptionStatus,
    val recordedAt: Long,
    val lastAccessedAt: Long,
    val durationMs: Long,
    val transcriptionEngine: String?,
    val language: String?,
    val title: String?,
)
