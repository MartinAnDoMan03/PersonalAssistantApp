package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class TeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAssistantYoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TeamScreen(
                            onNavigateToHome = {
                                // Kembali ke HomeActivity
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                startActivity(intent)
                                finish()
                            },
                        onNavigateToTasks = {
                            startActivity(Intent(this, TaskActivity::class.java))
                        },
                        onNavigateToNotes = {
                            startActivity(Intent(this, NoteActivity::class.java))
                        },
                        onNavigateToTeam = {
                            // Already in Team screen
                        },
                        onCreateTeam = {
                            startActivity(Intent(this, CreateTeamActivity::class.java))
                        },
                        onJoinTeam = {
                            // TODO: Implement join team functionality
                        }
                    )
                }
            }
        }
    }
}