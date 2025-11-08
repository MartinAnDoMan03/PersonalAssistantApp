package com.example.yourassistantyora

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class CreateNoteActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAssistantYoraTheme {
                CreateNoteScreen(
                    onBackClick = {
                        // Kembali ke NoteActivity
                        finish()
                    },
                    onSaveClick = { title, content, categories ->
                        // TODO: Simpan note ke database atau shared preferences
                        // Untuk sementara kita tampilkan toast
                        val categoriesString = categories.joinToString(", ")

                        Toast.makeText(
                            this,
                            "Note saved!\nTitle: $title\nCategories: $categoriesString",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Kembali ke NoteActivity setelah save
                        finish()

                        // TODO: Nanti implementasi save ke Room Database
                        /*
                        val note = Note(
                            id = 0, // Auto-generate
                            title = title,
                            content = content,
                            category = categoriesString,
                            time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                        )
                        viewModel.insertNote(note)
                        */
                    }
                )
            }
        }
    }
}