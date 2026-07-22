package com.jimmcgaw.whatsayyou.di

import com.jimmcgaw.whatsayyou.audio.AudioCaptureEngine
import com.jimmcgaw.whatsayyou.data.AppDatabase
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository

interface AppContainer {
    val database: AppDatabase
    val audioRecordRepository: AudioRecordRepository
    val audioCaptureEngine: AudioCaptureEngine
}
