package com.jimmcgaw.whatsayyou.ui.list

import com.jimmcgaw.whatsayyou.data.AudioRecordEntity
import com.jimmcgaw.whatsayyou.data.TranscriptionStatus
import java.text.DateFormat
import java.util.Date

data class RecordingListItem(
    val id: Long,
    val displayTitle: String,
    val recordedAt: Long,
    val transcriptionStatus: TranscriptionStatus,
    val transcript: String?,
)

fun AudioRecordEntity.toListItem(): RecordingListItem = RecordingListItem(
    id = id,
    displayTitle = title ?: formatFallbackTitle(recordedAt),
    recordedAt = recordedAt,
    transcriptionStatus = transcriptionStatus,
    transcript = transcript,
)

private fun formatFallbackTitle(recordedAt: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(recordedAt))
