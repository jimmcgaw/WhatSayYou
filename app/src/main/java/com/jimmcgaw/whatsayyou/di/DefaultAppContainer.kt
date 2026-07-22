package com.jimmcgaw.whatsayyou.di

import android.content.Context
import androidx.room.Room
import androidx.work.WorkManager
import com.jimmcgaw.whatsayyou.audio.AudioCaptureEngine
import com.jimmcgaw.whatsayyou.audio.AudioRecordCaptureEngine
import com.jimmcgaw.whatsayyou.data.AppDatabase
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository
import com.jimmcgaw.whatsayyou.data.DefaultAudioRecordRepository
import com.jimmcgaw.whatsayyou.transcription.SpeechRecognizerTranscriptionEngine
import com.jimmcgaw.whatsayyou.transcription.TranscriptionEngine
import com.jimmcgaw.whatsayyou.work.TranscriptionScheduler
import com.jimmcgaw.whatsayyou.work.WorkManagerTranscriptionScheduler

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "whatsayyou.db").build()
    }

    override val audioRecordRepository: AudioRecordRepository by lazy {
        DefaultAudioRecordRepository(database.audioRecordDao())
    }

    override val audioCaptureEngine: AudioCaptureEngine by lazy {
        AudioRecordCaptureEngine(context.filesDir)
    }

    override val transcriptionEngine: TranscriptionEngine by lazy {
        SpeechRecognizerTranscriptionEngine(context)
    }

    override val transcriptionScheduler: TranscriptionScheduler by lazy {
        WorkManagerTranscriptionScheduler(WorkManager.getInstance(context))
    }
}
