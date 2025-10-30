package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ambil data dari intent
        val userName = intent.getStringExtra("USER_NAME") ?: "Tom Holland"
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "tomholland@gmail.com"
        val totalTasks = intent.getIntExtra("TOTAL_TASKS", 2)
        val completedTasks = intent.getIntExtra("COMPLETED_TASKS", 2)

        setContent {
            YourAssistantYoraTheme {
                ProfileScreen(
                    userName = userName,
                    userEmail = userEmail,
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    onBackClick = {
                        finish() // Kembali ke HomeActivity
                    },
                    onEditProfile = {
                        // Tidak perlu lagi, sudah handle di dalam ProfileScreen
                    },
                    onCameraClick = {
                        // TODO: Buka kamera untuk ambil foto
                        android.widget.Toast.makeText(this, "Open Camera", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onGalleryClick = {
                        // TODO: Buka galeri untuk pilih foto
                        android.widget.Toast.makeText(this, "Open Gallery", android.widget.Toast.LENGTH_SHORT).show()
                    },
                    onLogout = {
                        // Logout: Clear session dan kembali ke Login
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}