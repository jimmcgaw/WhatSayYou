package com.jimmcgaw.whatsayyou.di

import android.app.Application
import androidx.work.Configuration
import com.jimmcgaw.whatsayyou.work.TranscriptionWorkerFactory

class WhatSayYouApplication : Application(), Configuration.Provider {

    // WorkManager's androidx.startup initializer runs in a ContentProvider, which executes
    // before Application.onCreate() — so this must be lazy rather than assigned in onCreate(),
    // or workManagerConfiguration below would see it uninitialized.
    val container: AppContainer by lazy { DefaultAppContainer(this) }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(TranscriptionWorkerFactory(container))
            .build()
}
