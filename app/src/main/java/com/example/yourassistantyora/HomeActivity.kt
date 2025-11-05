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

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // No logged-in user, kick back to login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }

        val userName = currentUser.displayName ?: intent.getStringExtra("USER_NAME") ?: "Tom Holland"
        val userEmail = currentUser.email ?: intent.getStringExtra("USER_EMAIL") ?: "tomholland@gmail.com"
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
                        // TODO: Navigate ke NoteActivity
                        val intent = Intent(this, NoteActivity::class.java)
                        startActivity(intent)

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