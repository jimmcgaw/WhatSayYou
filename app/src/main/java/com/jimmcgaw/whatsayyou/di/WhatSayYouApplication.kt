package com.jimmcgaw.whatsayyou.di

import android.app.Application

class WhatSayYouApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
