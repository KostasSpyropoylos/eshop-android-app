package com.example.shopapp

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.ui.Modifier
import com.example.compose.AppTheme
import com.example.shopapp.ui.theme.AppThemeManager
import com.example.shopapp.viewmodels.AuthViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val db = FirebaseFirestore.getInstance()


        enableEdgeToEdge()
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            AppTheme(darkTheme = AppThemeManager.isDarkThemeEnabled) {
                MainScreen(modifier = Modifier, authViewModel)
            }
        }
    }
}

