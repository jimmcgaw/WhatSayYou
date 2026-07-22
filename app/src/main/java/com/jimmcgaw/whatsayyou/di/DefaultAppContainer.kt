package com.jimmcgaw.whatsayyou.di

import android.content.Context
import androidx.room.Room
import com.jimmcgaw.whatsayyou.data.AppDatabase
import com.jimmcgaw.whatsayyou.data.AudioRecordRepository
import com.jimmcgaw.whatsayyou.data.DefaultAudioRecordRepository

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "whatsayyou.db").build()
    }

    override val audioRecordRepository: AudioRecordRepository by lazy {
        DefaultAudioRecordRepository(database.audioRecordDao())
    }
}
