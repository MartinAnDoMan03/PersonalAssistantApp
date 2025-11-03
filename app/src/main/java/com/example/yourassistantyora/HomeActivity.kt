package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ambil username dari intent (opsional)
        val userName = intent.getStringExtra("USER_NAME") ?: "Tom Holland"
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: "tomholland@gmail.com"

        setContent {
            YourAssistantYoraTheme {
                HomeScreen(
                    userName = userName,
                    onNotificationClick = {
                        // TODO: Handle notification click
                    },
                    onProfileClick = {
                        // Navigate ke ProfileActivity
                        val intent = Intent(this, ProfileActivity::class.java)
                        intent.putExtra("USER_NAME", userName)
                        intent.putExtra("USER_EMAIL", userEmail)
                        // TODO: Hitung total tasks dan completed dari state
                        intent.putExtra("TOTAL_TASKS", 10)
                        intent.putExtra("COMPLETED_TASKS", 6)
                        startActivity(intent)
                    },
                    onTaskClick = { task ->
                        // TODO: Handle task click
                    },
                    onNavigateToTasks = {
                        // Navigate ke TaskActivity
                        val intent = Intent(this, TaskActivity::class.java)
                        startActivity(intent)
                    },
                    // ✨ TAMBAHKAN 2 CALLBACK INI
                    onNavigateToNotes = {
                        // TODO: Navigate ke NoteActivity (belum dibuat)
                        Toast.makeText(this, "Note feature coming soon", Toast.LENGTH_SHORT).show()
                        // val intent = Intent(this, NoteActivity::class.java)
                        // startActivity(intent)
                    },
                    onNavigateToTeam = {
                        // TODO: Navigate ke TeamActivity (belum dibuat)
                        Toast.makeText(this, "Team feature coming soon", Toast.LENGTH_SHORT).show()
                        // val intent = Intent(this, TeamActivity::class.java)
                        // startActivity(intent)
                    }
                )
            }
        }
    }
}