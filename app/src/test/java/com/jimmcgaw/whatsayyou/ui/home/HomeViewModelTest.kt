package com.jimmcgaw.whatsayyou.ui.home

import com.jimmcgaw.whatsayyou.audio.FakeAudioCaptureEngine
import com.jimmcgaw.whatsayyou.data.FakeAudioRecordRepository
import com.jimmcgaw.whatsayyou.data.TranscriptionStatus
import com.jimmcgaw.whatsayyou.work.FakeTranscriptionScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @Before
    fun setMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun onRecordClick_start_setsRecordingStateAndCallsEngine() {
        val engine = FakeAudioCaptureEngine()
        val repository = FakeAudioRecordRepository()
        val viewModel = HomeViewModel(engine, repository, FakeTranscriptionScheduler())

        viewModel.onRecordClick()

        assertTrue(viewModel.uiState.value.isRecording)
        assertTrue(engine.startRecordingCalled)
    }

    @Test
    fun onRecordClick_startThenStop_insertsPendingRecordingAndEnqueuesTranscription() = runTest {
        val engine = FakeAudioCaptureEngine()
        val repository = FakeAudioRecordRepository()
        val scheduler = FakeTranscriptionScheduler()
        val viewModel = HomeViewModel(engine, repository, scheduler)

        viewModel.onRecordClick() // start
        viewModel.onRecordClick() // stop
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isRecording)
        assertEquals(1, repository.insertedRecords.size)
        val insertedRecord = repository.insertedRecords.single()
        assertEquals(TranscriptionStatus.PENDING, insertedRecord.transcriptionStatus)
        assertEquals(listOf(insertedRecord.id), scheduler.enqueuedRecordIds)
    }
}
