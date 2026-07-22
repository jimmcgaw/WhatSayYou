package com.jimmcgaw.whatsayyou.di

import com.jimmcgaw.whatsayyou.audio.AudioCaptureEngine
import com.jimmcgaw.whatsayyou.data.AppDatabase
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository
import com.jimmcgaw.whatsayyou.playback.AudioPlayer
import com.jimmcgaw.whatsayyou.transcription.TranscriptionEngine
import com.jimmcgaw.whatsayyou.work.TranscriptionScheduler

interface AppContainer {
    val database: AppDatabase
    val audioRecordRepository: AudioRecordRepository
    val audioCaptureEngine: AudioCaptureEngine
    val transcriptionEngine: TranscriptionEngine
    val transcriptionScheduler: TranscriptionScheduler
    val audioPlayerFactory: () -> AudioPlayer
}
