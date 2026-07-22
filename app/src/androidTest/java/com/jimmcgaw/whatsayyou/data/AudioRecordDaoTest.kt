package com.jimmcgaw.whatsayyou.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioRecordDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: AudioRecordDao

    @Before
    fun createDatabase() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.audioRecordDao()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun record(recordedAt: Long, lastAccessedAt: Long) = AudioRecordEntity(
        audioFilePath = "/data/recordings/$recordedAt.wav",
        transcript = null,
        transcriptionStatus = TranscriptionStatus.PENDING,
        recordedAt = recordedAt,
        lastAccessedAt = lastAccessedAt,
        durationMs = 1_000,
        transcriptionEngine = null,
        language = null,
        title = null,
    )

    @Test
    fun insertAndGetById_returnsInsertedRecord() = runTest {
        val id = dao.insert(record(recordedAt = 100, lastAccessedAt = 100))

        val loaded = dao.getById(id)

        assert(loaded != null)
        assert(loaded!!.audioFilePath == "/data/recordings/100.wav")
    }

    @Test
    fun observeAllOrderedByLastAccessed_returnsNewestFirst() = runTest {
        dao.insert(record(recordedAt = 1, lastAccessedAt = 100))
        dao.insert(record(recordedAt = 2, lastAccessedAt = 300))
        dao.insert(record(recordedAt = 3, lastAccessedAt = 200))

        val records = dao.observeAllOrderedByLastAccessed().first()

        assert(records.map { it.lastAccessedAt } == listOf(300L, 200L, 100L))
    }
}
