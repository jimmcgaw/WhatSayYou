package com.jimmcgaw.whatsayyou.ui.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jimmcgaw.whatsayyou.data.AudioRecordEntity
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository
import com.jimmcgaw.whatsayyou.di.WhatSayYouApplication
import com.jimmcgaw.whatsayyou.playback.AudioPlayer
import com.jimmcgaw.whatsayyou.ui.common.resolveDisplayTitle
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val EMPTY_TITLE_ERROR = "Title cannot be empty"

class ViewViewModel(
    private val recordId: Long,
    private val repository: AudioRecordRepository,
    private val audioPlayer: AudioPlayer,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ViewUiState())
    val uiState: StateFlow<ViewUiState> = _uiState.asStateFlow()

    private val _events = Channel<ViewEvent>(Channel.BUFFERED)
    val events: Flow<ViewEvent> = _events.receiveAsFlow()

    private var loadedRecord: AudioRecordEntity? = null

    init {
        viewModelScope.launch {
            val record = repository.getById(recordId) ?: return@launch
            loadedRecord = record
            _uiState.update {
                it.copy(
                    titleInput = resolveDisplayTitle(record.title, record.recordedAt),
                    transcript = record.transcript,
                    transcriptionStatus = record.transcriptionStatus,
                    durationMs = record.durationMs,
                )
            }

            val touched = record.copy(lastAccessedAt = System.currentTimeMillis())
            loadedRecord = touched
            repository.update(touched)

            audioPlayer.prepare(record.audioFilePath)
        }

        audioPlayer.playbackState
            .onEach { state ->
                _uiState.update {
                    it.copy(
                        isPlaying = state.isPlaying,
                        positionMs = state.positionMs,
                        durationMs = if (state.durationMs > 0) state.durationMs else it.durationMs,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    fun onPlayPauseClick() {
        if (_uiState.value.isPlaying) audioPlayer.pause() else audioPlayer.play()
    }

    fun onSeek(positionMs: Long) {
        audioPlayer.seekTo(positionMs)
    }

    fun onTitleChanged(newTitle: String) {
        _uiState.update { it.copy(titleInput = newTitle) }

        if (newTitle.isBlank()) {
            _uiState.update { it.copy(titleError = EMPTY_TITLE_ERROR) }
            return
        }
        _uiState.update { it.copy(titleError = null) }

        val updated = (loadedRecord ?: return).copy(title = newTitle)
        loadedRecord = updated
        viewModelScope.launch { repository.update(updated) }
    }

    fun onDeleteClick() {
        _uiState.update { it.copy(showDeleteConfirmation = true) }
    }

    fun onDeleteDismiss() {
        _uiState.update { it.copy(showDeleteConfirmation = false) }
    }

    fun onDeleteConfirm() {
        viewModelScope.launch {
            repository.deleteRecording(recordId)
            _events.send(ViewEvent.NavigateBack)
        }
    }

    override fun onCleared() {
        audioPlayer.release()
    }

    companion object {
        fun factory(recordId: Long): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WhatSayYouApplication
                ViewViewModel(
                    recordId,
                    application.container.audioRecordRepository,
                    application.container.audioPlayerFactory(),
                )
            }
        }
    }
}
