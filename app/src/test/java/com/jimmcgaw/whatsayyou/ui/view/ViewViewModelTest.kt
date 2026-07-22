package com.jimmcgaw.whatsayyou.ui.view

import com.jimmcgaw.whatsayyou.data.AudioRecordEntity
import com.jimmcgaw.whatsayyou.data.FakeAudioRecordRepository
import com.jimmcgaw.whatsayyou.data.TranscriptionStatus
import com.jimmcgaw.whatsayyou.playback.FakeAudioPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ViewViewModelTest {

    @Before
    fun setMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    private fun record(title: String? = "My recording") = AudioRecordEntity(
        id = 1,
        audioFilePath = "/data/recordings/1.wav",
        transcript = "hello world",
        transcriptionStatus = TranscriptionStatus.COMPLETED,
        recordedAt = 1_000,
        lastAccessedAt = 1_000,
        durationMs = 5_000,
        transcriptionEngine = "SpeechRecognizer",
        language = "en-US",
        title = title,
    )

    @Test
    fun init_seedsTitleAndTranscriptFromRepository() = runTest {
        val repository = FakeAudioRecordRepository(initialRecords = listOf(record()))
        val viewModel = ViewViewModel(1, repository, FakeAudioPlayer())

        assertEquals("My recording", viewModel.uiState.value.titleInput)
        assertEquals("hello world", viewModel.uiState.value.transcript)
    }

    @Test
    fun onTitleChanged_blank_setsErrorAndDoesNotPersist() = runTest {
        val repository = FakeAudioRecordRepository(initialRecords = listOf(record()))
        val viewModel = ViewViewModel(1, repository, FakeAudioPlayer())

        viewModel.onTitleChanged("")

        assertEquals("Title cannot be empty", viewModel.uiState.value.titleError)
        assertEquals("My recording", repository.getById(1)?.title)
    }

    @Test
    fun onTitleChanged_nonBlank_clearsErrorAndPersists() = runTest {
        val repository = FakeAudioRecordRepository(initialRecords = listOf(record()))
        val viewModel = ViewViewModel(1, repository, FakeAudioPlayer())

        viewModel.onTitleChanged("New title")

        assertNull(viewModel.uiState.value.titleError)
        assertEquals("New title", repository.getById(1)?.title)
    }

    @Test
    fun onDeleteConfirm_deletesRecordingAndEmitsNavigateBack() = runTest {
        val repository = FakeAudioRecordRepository(initialRecords = listOf(record()))
        val viewModel = ViewViewModel(1, repository, FakeAudioPlayer())

        viewModel.onDeleteConfirm()

        assertNull(repository.getById(1))
        assertTrue(viewModel.events.first() is ViewEvent.NavigateBack)
    }
}
