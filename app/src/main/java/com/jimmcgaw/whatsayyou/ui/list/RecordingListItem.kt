package com.jimmcgaw.whatsayyou.ui.list

import com.jimmcgaw.whatsayyou.data.AudioRecordEntity
import com.jimmcgaw.whatsayyou.ui.common.resolveDisplayTitle

data class RecordingListItem(
    val id: Long,
    val displayTitle: String,
    val recordedAt: Long,
)

fun AudioRecordEntity.toListItem(): RecordingListItem = RecordingListItem(
    id = id,
    displayTitle = resolveDisplayTitle(title, recordedAt),
    recordedAt = recordedAt,
)
