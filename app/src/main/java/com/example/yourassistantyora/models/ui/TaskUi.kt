package com.example.yourassistantyora.models.ui

data class TaskUi(
    val id: String,
    val title: String,
    val time: String,              // "HH:mm"
    val priority: String,          // "Low" | "Medium" | "High"
    val categories: List<String>,  // MULTI
    val status: String,            // "Waiting" | "To do" | "In Progress" | "Hold On" | "Done"
    val description: String = "",
    val isCompleted: Boolean = false
) {
    val category: String get() = categories.firstOrNull() ?: "Work" // legacy
}
