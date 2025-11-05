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
                    },
                    onCreateTaskClick = {
                        // TODO: Handle create new task
                        // Contoh: buka CreateTaskActivity
                        // val intent = Intent(this, CreateTaskActivity::class.java)
                        // startActivity(intent)
                    },
                    // ✨ TAMBAHKAN 3 CALLBACK INI
                    onNavigateToHome = {
                        // Kembali ke HomeActivity
                        finish()
                    },
                    onNavigateToNotes = {
                        // TODO: Navigate ke NoteActivity
                        val intent = Intent(this, NoteActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToTeam = {
                        // TODO: Navigate ke TeamActivity (belum dibuat)
                        Toast.makeText(this, "Team feature coming soon", Toast.LENGTH_SHORT).show()
                        // val intent = Intent(this, TeamActivity::class.java)
                        // startActivity(intent)
                        // finish() // tutup TaskActivity
                    }
                )
            }
        }
    }
}