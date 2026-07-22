package com.jimmcgaw.whatsayyou.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Tab(val route: String, val label: String, val icon: ImageVector) {
    data object Home : Tab("home", "Home", Icons.Filled.Home)
    data object List : Tab("list", "Recordings", Icons.AutoMirrored.Filled.List)
    data object Settings : Tab("settings", "Settings", Icons.Filled.Settings)

    companion object {
        val all = listOf(Home, List, Settings)
    }
}
