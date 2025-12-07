package com.example.yourassistantyora.components

import androidx.compose.foundation.background
//...
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Attachment
import androidx.compose.runtime.Composable
import com.example.yourassistantyora.models.TeamTask // Buat data class ini
import coil.compose.AsyncImage

@Composable
fun TeamTaskCard(task: TeamTask, onTaskClick: () -> Unit) {
    // Implementasi UI Card sesuai gambar Anda
    // ...
    // Gunakan AsyncImage dari Coil untuk menampilkan avatar member
    // AsyncImage(model = member.avatarUrl, contentDescription = "Member avatar")
}