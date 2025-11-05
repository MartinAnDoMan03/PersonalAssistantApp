package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme


class NoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAssistantYoraTheme {
                NoteScreen(
                    onNoteClick = { note ->
                        // Navigate ke NoteDetailActivity untuk edit
                        val intent = Intent(this, NoteDetailActivity::class.java)
                        intent.putExtra("NOTE_ID", note.id)
                        intent.putExtra("NOTE_TITLE", note.title)
                        intent.putExtra("NOTE_CONTENT", note.content)
                        intent.putExtra("NOTE_CATEGORY", note.category)
                        intent.putExtra("NOTE_TIME", note.time)
                        startActivity(intent)
                    },
                    onCreateNoteClick = {
                        // TODO: Handle create new note
                        // Contoh: buka CreateNoteActivity
                        // val intent = Intent(this, CreateNoteActivity::class.java)
                        // startActivity(intent)
                        Toast.makeText(
                            this,
                            "Create note feature coming soon",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onNavigateToHome = {
                        // Kembali ke HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToTasks = {
                        // Navigate ke TaskActivity
                        val intent = Intent(this, TaskActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onNavigateToTeam = {
                        // TODO: Navigate ke TeamActivity (belum dibuat)
                        Toast.makeText(
                            this,
                            "Team feature coming soon",
                            Toast.LENGTH_SHORT
                        ).show()
                        // val intent = Intent(this, TeamActivity::class.java)
                        // startActivity(intent)
                        // finish()
                    }
                )
            }
        }
    }
}