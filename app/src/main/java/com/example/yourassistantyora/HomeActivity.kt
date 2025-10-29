package com.example.yourassistantyora

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ambil username dari intent (opsional)
        val userName = intent.getStringExtra("USER_NAME") ?: "Tom Holland"

        setContent {
            YourAssistantYoraTheme {
                HomeScreen(
                    userName = userName,
                    onNotificationClick = {
                        // TODO: Handle notification click
                    },
                    onProfileClick = {
                        // TODO: Handle profile click
                    },
                    onTaskClick = { task ->
                        // TODO: Handle task click - bisa navigasi ke detail task
                    }
                )
            }
        }
    }
}