package com.jimmcgaw.whatsayyou.ui.list

import com.jimmcgaw.whatsayyou.data.AudioRecordEntity
import com.jimmcgaw.whatsayyou.data.FakeAudioRecordRepository
import com.jimmcgaw.whatsayyou.data.TranscriptionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ListViewModelTest {

    @Before
    fun setMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    private fun record(id: Long, lastAccessedAt: Long, title: String?) = AudioRecordEntity(
        id = id,
        audioFilePath = "/data/recordings/$id.wav",
        transcript = null,
        transcriptionStatus = TranscriptionStatus.PENDING,
        recordedAt = lastAccessedAt,
        lastAccessedAt = lastAccessedAt,
        durationMs = 1_000,
        transcriptionEngine = null,
        language = null,
        title = title,
    )

    @Test
    fun uiState_preservesRepositoryOrder() = runTest {
        val repository = FakeAudioRecordRepository(
            initialRecords = listOf(
                record(id = 2, lastAccessedAt = 300, title = "Second"),
                record(id = 3, lastAccessedAt = 200, title = "Third"),
                record(id = 1, lastAccessedAt = 100, title = "First"),
            ),
        )
        val viewModel = ListViewModel(repository)

        val items = viewModel.uiState.first { it.isNotEmpty() }

        assertEquals(listOf("Second", "Third", "First"), items.map { it.displayTitle })
    }

    @Test
    fun uiState_nullTitle_fallsBackToFormattedTimestamp() = runTest {
        val entity = record(id = 1, lastAccessedAt = 0, title = null)
        val repository = FakeAudioRecordRepository(initialRecords = listOf(entity))
        val viewModel = ListViewModel(repository)

        val item = viewModel.uiState.first { it.isNotEmpty() }.single()

        assertEquals(entity.toListItem().displayTitle, item.displayTitle)
    }
}
