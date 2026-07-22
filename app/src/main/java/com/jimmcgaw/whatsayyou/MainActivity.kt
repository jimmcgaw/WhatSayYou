package com.jimmcgaw.whatsayyou

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.jimmcgaw.whatsayyou.ui.navigation.WhatSayYouApp
import com.jimmcgaw.whatsayyou.ui.theme.WhatSayYouTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WhatSayYouTheme {
                WhatSayYouApp()
            }
        }
    }
}
