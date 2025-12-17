package com.example.yourassistantyora

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yourassistantyora.models.UniversalNotificationListener
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.viewModel.AuthViewModel
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        val startDestinationFromSplash = intent.getStringExtra("START_DESTINATION")
        val userNameFromSplash = intent.getStringExtra("USER_NAME")

        setContent {
            YourAssistantYoraTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.currentUser
                val isLoading by authViewModel.isLoading

                // Jalankan listener notifikasi otomatis setelah login
                LaunchedEffect(authState) {
                    if (authState != null) {
                        // User sudah login → mulai mendengarkan Firestore
                        UniversalNotificationListener.startListening(this@MainActivity)
                    } else {
                        // User logout → hentikan listener
                        UniversalNotificationListener.stopListening()
                    }
                }

                // Sinkronisasi nama dari Splash ke ViewModel
                LaunchedEffect(userNameFromSplash) {
                    if (!userNameFromSplash.isNullOrBlank()) {
                        authViewModel.updateUserProfile(userNameFromSplash, null)
                    }
                }

                if (startDestinationFromSplash == null) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        val startDestination = if (authState != null) "home" else "login"
                        AppNavigation(
                            startDestination = startDestination,
                            authViewModel = authViewModel
                        )
                    }
                } else {
                    AppNavigation(
                        startDestination = startDestinationFromSplash,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Pastikan listener berhenti agar tidak memory leak
        UniversalNotificationListener.stopListening()
    }
}
