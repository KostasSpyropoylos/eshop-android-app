package com.example.shopapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.compose.AppTheme
import com.example.shopapp.data.Product
import com.example.shopapp.data.Rating
import com.example.shopapp.data.Specifications
import com.example.shopapp.preferences.getThemePreference
import com.example.shopapp.ui.theme.AppThemeManager
import com.example.shopapp.viewmodels.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val themePreference = mutableStateOf<String?>(null) // Use null to indicate loading

        // Load the theme preference from DataStore
        lifecycleScope.launch {
            getThemePreference(applicationContext).collect { savedTheme ->
                themePreference.value = savedTheme
            }
        }

        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            AppTheme() {
                MainScreen(modifier = Modifier, authViewModel)
            }
        }
    }
}

