package com.jimmcgaw.whatsayyou.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository
import com.jimmcgaw.whatsayyou.di.WhatSayYouApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ListViewModel(repository: AudioRecordRepository) : ViewModel() {
    val uiState: StateFlow<List<RecordingListItem>> =
        repository.observeAllOrderedByLastAccessed()
            .map { records -> records.map { it.toListItem() } }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as WhatSayYouApplication
                ListViewModel(application.container.audioRecordRepository)
            }
        }
    }
}
