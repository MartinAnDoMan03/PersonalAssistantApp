package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class TaskActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            YourAssistantYoraTheme {
                TaskScreen(
                    onBackClick = {
                        // Kembali ke halaman sebelumnya
                        finish()
                    },
                    onTaskClick = { task ->
                        // TODO: Handle task click - bisa navigasi ke detail task
                        // Contoh: buka TaskDetailActivity
                        // val intent = Intent(this, TaskDetailActivity::class.java)
                        // intent.putExtra("TASK_ID", task.id)
                        // startActivity(intent)
                        Toast.makeText(this, "Clicked: ${task.title}", Toast.LENGTH_SHORT).show()
                    },
                    onCreateTaskClick = {
                        // TODO: Handle create new task
                        // Contoh: buka CreateTaskActivity
                        // val intent = Intent(this, CreateTaskActivity::class.java)
                        // startActivity(intent)
                        Toast.makeText(this, "Create Task", Toast.LENGTH_SHORT).show()
                    },
                    onNavigateToHome = {
                        // Kembali ke HomeActivity
                        val intent = Intent(this, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToNotes = {
                        // Navigate ke NoteActivity
                        val intent = Intent(this, NoteActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToTeam = {
                        // TODO: Navigate ke TeamActivity (belum dibuat)
                        Toast.makeText(this, "Team feature coming soon", Toast.LENGTH_SHORT).show()
                        // val intent = Intent(this, TeamActivity::class.java)
                        // startActivity(intent)
                        // finish()
                    },
                    // ✨ TAMBAHKAN CALLBACK UNTUK NAVIGASI KE WEEKLY
                    onNavigateToWeekly = {
                        val intent = Intent(this, WeeklyActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToDaily = {
                        val intent = Intent(this, DailyActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToMonthly = {
                        val intent = Intent(this, MonthlyActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}