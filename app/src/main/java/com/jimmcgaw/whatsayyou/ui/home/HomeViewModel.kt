package com.jimmcgaw.whatsayyou.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jimmcgaw.whatsayyou.audio.AudioCaptureEngine
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository
import com.jimmcgaw.whatsayyou.di.WhatSayYouApplication
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class HomeUiState(
    val isRecording: Boolean = false,
    val elapsedMs: Long = 0L,
)

class HomeViewModel(
    private val audioCaptureEngine: AudioCaptureEngine,
    private val repository: AudioRecordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var elapsedTimeJob: Job? = null
    private var recordingStartedAt: Long = 0L

    fun onRecordClick() {
        if (_uiState.value.isRecording) {
            stopRecording()
        } else {
            startRecording()
        }
    }

    private fun startRecording() {
        recordingStartedAt = System.currentTimeMillis()
        _uiState.value = HomeUiState(isRecording = true, elapsedMs = 0L)

        viewModelScope.launch {
            val file = audioCaptureEngine.startRecording()
            val durationMs = System.currentTimeMillis() - recordingStartedAt
            repository.addRecording(
                audioFilePath = file.absolutePath,
                recordedAt = recordingStartedAt,
                durationMs = durationMs,
            )
            _uiState.value = HomeUiState(isRecording = false, elapsedMs = 0L)
        }

        elapsedTimeJob = viewModelScope.launch {
            while (isActive) {
                delay(1_000)
                _uiState.update { it.copy(elapsedMs = System.currentTimeMillis() - recordingStartedAt) }
            }
        }
    }

    private fun stopRecording() {
        audioCaptureEngine.stopRecording()
        elapsedTimeJob?.cancel()
        elapsedTimeJob = null
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WhatSayYouApplication
                HomeViewModel(application.container.audioCaptureEngine, application.container.audioRecordRepository)
            }
        }
    }
}
