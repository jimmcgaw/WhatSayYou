package com.jimmcgaw.whatsayyou.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jimmcgaw.whatsayyou.ui.home.HomeScreen
import com.jimmcgaw.whatsayyou.ui.list.ListScreen
import com.jimmcgaw.whatsayyou.ui.settings.SettingsScreen
import com.jimmcgaw.whatsayyou.ui.view.ViewScreen

@Composable
fun WhatSayYouApp() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
                Tab.all.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Tab.Home.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(Tab.Home.route) { HomeScreen() }
            composable(Tab.List.route) {
                ListScreen(onRecordingClick = { id -> navController.navigate("view/$id") })
            }
            composable(Tab.Settings.route) { SettingsScreen() }
            composable(
                route = "view/{recordId}",
                arguments = listOf(navArgument("recordId") { type = NavType.LongType }),
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: return@composable
                ViewScreen(recordId = recordId, onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}
