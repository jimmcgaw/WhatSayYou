package com.jimmcgaw.whatsayyou.di

import android.content.Context
import androidx.room.Room
import com.jimmcgaw.whatsayyou.data.AppDatabase

class DefaultAppContainer(private val context: Context) : AppContainer {
    override val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, "whatsayyou.db").build()
    }
}
