package com.example.shopapp.preferences

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val THEME_KEY = stringPreferencesKey("theme")

// Extension property to access the DataStore
val Context.dataStore by preferencesDataStore(name = "settings")

// Function to save the theme preference
suspend fun saveThemePreference(context: Context, theme: String) {
    context.dataStore.edit { preferences ->
        preferences[THEME_KEY] = theme
    }
}

// Function to read the theme preference
suspend fun getThemePreference(context: Context): Flow<String> {
    return context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: "light" // Default to light theme if not set
    }
}