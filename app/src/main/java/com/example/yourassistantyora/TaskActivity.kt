package com.example.yourassistantyora

import android.os.Bundle
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
                    }
                )
            }
        }
    }
}