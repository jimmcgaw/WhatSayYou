package com.jimmcgaw.whatsayyou.work

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.jimmcgaw.whatsayyou.data.AppDatabase
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository
import com.jimmcgaw.whatsayyou.data.DefaultAudioRecordRepository
import com.jimmcgaw.whatsayyou.data.TranscriptionStatus
import com.jimmcgaw.whatsayyou.transcription.FakeTranscriptionEngine
import com.jimmcgaw.whatsayyou.transcription.TranscriptionEngine
import com.jimmcgaw.whatsayyou.transcription.TranscriptionResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TranscriptionWorkerTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AudioRecordRepository
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        repository = DefaultAudioRecordRepository(database.audioRecordDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun buildWorker(engine: TranscriptionEngine, recordId: Long): TranscriptionWorker =
        TestListenableWorkerBuilder<TranscriptionWorker>(context)
            .setInputData(workDataOf(TranscriptionWorker.KEY_RECORD_ID to recordId))
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters,
                ): ListenableWorker = TranscriptionWorker(appContext, workerParameters, repository, engine)
            })
            .build()

    @Test
    fun doWork_transcriptionSucceeds_marksCompletedWithTranscript() = runBlocking {
        val recordId = repository.addRecording("/fake/path.wav", recordedAt = 0, durationMs = 1_000)
        val worker = buildWorker(FakeTranscriptionEngine(TranscriptionResult.Success("hello world")), recordId)

        val result = worker.doWork()

        assertTrue(result is ListenableWorker.Result.Success)
        val updated = repository.getById(recordId)
        assertEquals(TranscriptionStatus.COMPLETED, updated?.transcriptionStatus)
        assertEquals("hello world", updated?.transcript)
    }

    @Test
    fun doWork_transcriptionFails_marksFailed() = runBlocking {
        val recordId = repository.addRecording("/fake/path.wav", recordedAt = 0, durationMs = 1_000)
        val worker = buildWorker(FakeTranscriptionEngine(TranscriptionResult.Failure("boom")), recordId)

        worker.doWork()

        val updated = repository.getById(recordId)
        assertEquals(TranscriptionStatus.FAILED, updated?.transcriptionStatus)
    }

    @Test
    fun doWork_noSpeechDetected_marksNoSpeechDetected() = runBlocking {
        val recordId = repository.addRecording("/fake/path.wav", recordedAt = 0, durationMs = 1_000)
        val worker = buildWorker(FakeTranscriptionEngine(TranscriptionResult.NoSpeechDetected), recordId)

        worker.doWork()

        val updated = repository.getById(recordId)
        assertEquals(TranscriptionStatus.NO_SPEECH_DETECTED, updated?.transcriptionStatus)
    }
}
