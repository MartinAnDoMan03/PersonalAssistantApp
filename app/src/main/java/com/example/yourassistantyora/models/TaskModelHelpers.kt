package com.example.yourassistantyora.models

import androidx.compose.ui.graphics.Color

private fun catNameFromCode(code: Int): String = when (code) {
    0 -> "Work"
    1 -> "Study"
    2 -> "Travel"
    3 -> "Meeting"
    4 -> "Project"
    5 -> "Personal"
    else -> "Other"
}

/** ✅ dipakai buat FlowRow chips */
val TaskModel.categoryNamesSafe: List<String>
    get() = when {
        CategoryNames.isNotEmpty() -> CategoryNames.filter { it.isNotBlank() }.distinct()
        Categories.isNotEmpty() -> Categories.distinct().map(::catNameFromCode)
        else -> listOf(catNameFromCode(Category))
    }

/** helper lama biar tetap kompatibel */
fun TaskModel.timeText(): String = deadlineTimeFormatted
fun TaskModel.priorityLabel(): String = priorityText
fun TaskModel.statusLabel(): String = statusText
fun TaskModel.categoryLabel(): String = categoryText

fun TaskModel.priorityStripColor(isCompleted: Boolean): Color {
    val base = when (Priority) {
        2 -> Color(0xFFEF5350)
        1 -> Color(0xFFFFB74D)
        else -> Color(0xFF64B5F6)
    }
    return base.copy(alpha = if (isCompleted) 0.4f else 0.9f)
}

fun TaskModel.statusColors(isCompleted: Boolean): Pair<Color, Color> {
    val (bg, fg) = when (Status) {
        0 -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
        1 -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        2 -> Color(0xFFE8F5E8) to Color(0xFF2E7D32)
        3 -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        4 -> Color(0xFFE0F2F1) to Color(0xFF00695C)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    val aBg = if (isCompleted) 0.3f else 1f
    val aFg = if (isCompleted) 0.6f else 1f
    return bg.copy(alpha = aBg) to fg.copy(alpha = aFg)
}

val TaskModel.reminderText: String
    get() = when (Reminder) {
        7 -> "Ingatkan 3 hari sebelumnya"
        6 -> "Ingatkan 2 hari sebelumnya"
        5 -> "Ingatkan 1 hari sebelumnya"
        4 -> "Ingatkan 30 menit sebelumnya"
        3 -> "Ingatkan 20 menit sebelumnya"
        2 -> "Ingatkan 10 menit sebelumnya"
        1 -> "Ingatkan pada waktunya"
        else -> "Tidak ada pengingat"
    }
