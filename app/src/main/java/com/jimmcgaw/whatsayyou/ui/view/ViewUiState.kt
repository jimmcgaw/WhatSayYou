package com.jimmcgaw.whatsayyou.ui.view

import com.jimmcgaw.whatsayyou.data.TranscriptionStatus

data class ViewUiState(
    val titleInput: String = "",
    val titleError: String? = null,
    val transcript: String? = null,
    val transcriptionStatus: TranscriptionStatus = TranscriptionStatus.PENDING,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val showDeleteConfirmation: Boolean = false,
)

sealed class ViewEvent {
    data object NavigateBack : ViewEvent()
}
