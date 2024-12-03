package com.example.shopapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AppThemeManager {
    var isDarkThemeEnabled by mutableStateOf(true)
}