package com.jimmcgaw.whatsayyou.ui.list

import com.jimmcgaw.whatsayyou.data.AudioRecordEntity
import java.text.DateFormat
import java.util.Date

data class RecordingListItem(
    val id: Long,
    val displayTitle: String,
    val recordedAt: Long,
)

fun AudioRecordEntity.toListItem(): RecordingListItem = RecordingListItem(
    id = id,
    displayTitle = title ?: formatFallbackTitle(recordedAt),
    recordedAt = recordedAt,
)

private fun formatFallbackTitle(recordedAt: Long): String =
    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(recordedAt))
