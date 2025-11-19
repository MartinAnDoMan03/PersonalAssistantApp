package com.example.yourassistantyora

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.viewModel.AuthViewModel
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        // Dibaca sekali di sini, sebelum setContent
        val startDestinationFromSplash = intent.getStringExtra("START_DESTINATION")
        val userNameFromSplash = intent.getStringExtra("USER_NAME")

        setContent {
            YourAssistantYoraTheme {
                val authViewModel: AuthViewModel = viewModel()
                val authState by authViewModel.currentUser
                val isLoading by authViewModel.isLoading

                // Kalau Splash sudah kirim username dari Firestore, sync ke AuthViewModel
                LaunchedEffect(userNameFromSplash) {
                    if (!userNameFromSplash.isNullOrBlank()) {
                        authViewModel.updateUserName(userNameFromSplash)
                    }
                }

                if (startDestinationFromSplash == null) {
                    // Case: MainActivity diluncurkan langsung (tanpa Splash) → pakai logic lama
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
                    // Case: Datang dari SplashActivity → percaya pada START_DESTINATION dari Splash
                    AppNavigation(
                        startDestination = startDestinationFromSplash,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
