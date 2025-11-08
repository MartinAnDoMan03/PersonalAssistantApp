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

// Assuming you have a Task data class defined elsewhere
// data class Task(val id: String, /* other fields */)

class WeeklyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAssistantYoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeeklyScreen(
                        onBackClick = {
                            finish()
                        },
                        onTaskClick = { task: Task ->
                            // TODO: Navigate to Task Detail
                            // val intent = Intent(this@WeeklyActivity, TaskDetailActivity::class.java)
                            // intent.putExtra("TASK_ID", task.id)
                            // startActivity(intent)
                        },
                        onCreateTaskClick = {
                            // TODO: Navigate to Create Task
                            // val intent = Intent(this@WeeklyActivity, CreateTaskActivity::class.java)
                            // startActivity(intent)
                        },
                        onNavigateToHome = {
                            // TODO: Navigate to Home
                            // val intent = Intent(this@WeeklyActivity, MainActivity::class.java)
                            // intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            // startActivity(intent)
                            // finish()
                        },
                        onNavigateToNotes = {
                            // TODO: Navigate to Notes
                            // val intent = Intent(this@WeeklyActivity, NotesActivity::class.java)
                            // startActivity(intent)
                            // finish()
                        },
                        onNavigateToTeam = {
                            // TODO: Navigate to Team
                            // val intent = Intent(this@WeeklyActivity, TeamActivity::class.java)
                            // startActivity(intent)
                            // finish()
                        },
                        onNavigateToList = {
                            val intent = Intent(this, TaskActivity::class.java)
                            startActivity(intent)
                            finish()
                        },
                        onNavigateToDaily = {
                            // TODO: Navigate to Daily view
                            // val intent = Intent(this@WeeklyActivity, DailyActivity::class.java)
                            // startActivity(intent)
                            // finish()
                        },
                        onNavigateToMonthly = {
                            // TODO: Navigate to Monthly view
                            // val intent = Intent(this@WeeklyActivity, MonthlyActivity::class.java)
                            // startActivity(intent)
                            // finish()
                        }
                    )
                }
            }
        }
    }
}