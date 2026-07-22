package com.jimmcgaw.whatsayyou.di

import com.jimmcgaw.whatsayyou.data.AppDatabase

interface AppContainer {
    val database: AppDatabase
}
