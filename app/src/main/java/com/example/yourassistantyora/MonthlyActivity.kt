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

class MonthlyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAssistantYoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MonthlyScreen(
                        modifier = Modifier.fillMaxSize(),
                        onBackClick = {
                            finish()
                        },
                        onTaskClick = { task ->  // ← Hapus explicit type
                            // TODO: Navigate to Task Detail
                            // val intent = Intent(this@MonthlyActivity, TaskDetailActivity::class.java)
                            // intent.putExtra("TASK_ID", task.id)
                            // startActivity(intent)
                        },
                        onCreateTaskClick = {
                            // TODO: Navigate to Create Task
                            // val intent = Intent(this@MonthlyActivity, CreateTaskActivity::class.java)
                            // startActivity(intent)
                        },
                        onNavigateToHome = {
                            val intent = Intent(this@MonthlyActivity, HomeActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            startActivity(intent)
                            finish()
                        },
                        onNavigateToNotes = {
                            val intent = Intent(this@MonthlyActivity, NoteActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onNavigateToTeam = {
                            // TODO: Navigate to Team
                            // val intent = Intent(this@MonthlyActivity, TeamActivity::class.java)
                            // startActivity(intent)
                            // finish()
                        },
                        onNavigateToList = {
                            val intent = Intent(this@MonthlyActivity, TaskActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onNavigateToDaily = {
                            val intent = Intent(this@MonthlyActivity, DailyActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onNavigateToWeekly = {
                            val intent = Intent(this@MonthlyActivity, WeeklyActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    )
                }
            }
        }
    }
}