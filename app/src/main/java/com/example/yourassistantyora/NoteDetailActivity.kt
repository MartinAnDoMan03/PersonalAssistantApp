package com.example.yourassistantyora

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class NoteDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ambil data note dari intent (jika edit existing note)
        val noteId = intent.getIntExtra("NOTE_ID", -1)
        val noteTitle = intent.getStringExtra("NOTE_TITLE")
        val noteContent = intent.getStringExtra("NOTE_CONTENT")
        val noteCategory = intent.getStringExtra("NOTE_CATEGORY")
        val noteTime = intent.getStringExtra("NOTE_TIME")

        // Create note object jika ada data
        val note = if (noteId != -1 && noteTitle != null) {
            Note(
                id = noteId,
                title = noteTitle,
                content = noteContent ?: "",
                category = noteCategory ?: "Work",
                time = noteTime ?: "Just now"
            )
        } else null

        setContent {
            YourAssistantYoraTheme {
                NoteDetailScreen(
                    note = note,
                    onBackClick = {
                        finish()
                    },
                    onSaveClick = { title, content, category ->
                        // TODO: Save note to database/storage
                        Toast.makeText(
                            this,
                            "Note saved: $title",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Return result to NoteActivity
                        setResult(RESULT_OK)
                        finish()
                    },
                    onDeleteClick = {
                        // TODO: Delete note from database
                        Toast.makeText(
                            this,
                            "Note deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                )
            }
        }
    }
}