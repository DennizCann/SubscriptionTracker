package com.denizcan.subscriptiontracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "Ana Sayfa",
        icon = Icons.Default.Home
    )
    
    object Calendar : BottomNavItem(
        route = Screen.Calendar.route,
        title = "Takvim",
        icon = Icons.Default.DateRange
    )
    
    object Analytics : BottomNavItem(
        route = Screen.Analytics.route,
        title = "Analiz",
        icon = Icons.Default.List
    )
    
    object Profile : BottomNavItem(
        route = Screen.Profile.route,
        title = "Profil",
        icon = Icons.Default.Person
    )
} 