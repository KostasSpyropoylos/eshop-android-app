package com.example.shopapp.data

import androidx.compose.ui.graphics.vector.ImageVector

data class NavItem(
    val label: String,
    val route: String,
    val icon: ImageVector,
    val badgeCount: Int? = null
)
