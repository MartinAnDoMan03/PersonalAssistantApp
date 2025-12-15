package com.example.yourassistantyora.models

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class TaskModel(
    var id: String = "",

    val Title: String = "",
    val Description: String = "",

    val Deadline: Timestamp? = null,

    val Priority: Int = 1,          // 0 low, 1 medium, 2 high

    // legacy single category code
    val Category: Int = 0,

    // legacy multi category codes
    val Categories: List<Int> = emptyList(),

    // ✅ NEW multi category labels (mis. ["Work","Meeting"])
    val CategoryNames: List<String> = emptyList(),

    val Status: Int = 1,            // 0 waiting, 1 todo, 2 done, 3 hold, 4 progress
    val Reminder: Int = 0,
    val Location: String = "",

    val userId: String = "",
    val createdAt: Timestamp? = null
) {
    val isCompleted: Boolean
        get() = Status == 2

    val priorityText: String
        get() = when (Priority) {
            2 -> "High"
            1 -> "Medium"
            else -> "Low"
        }

    val statusText: String
        get() = when (Status) {
            0 -> "Waiting"
            1 -> "To do"
            2 -> "Done"
            3 -> "Hold On"
            4 -> "In Progress"
            else -> "To do"
        }

    // fallback mapping kalau CategoryNames kosong
    private fun codeToName(code: Int): String = when (code) {
        0 -> "Work"
        1 -> "Study"
        2 -> "Travel"
        3 -> "Meeting"
        4 -> "Project"
        5 -> "Personal"
        else -> "Other"
    }

    val categoryNamesSafe: List<String>
        get() = when {
            CategoryNames.isNotEmpty() -> CategoryNames
            Categories.isNotEmpty() -> Categories.map { codeToName(it) }
            else -> listOf(codeToName(Category))
        }

    /**
     * ✅ Ini yang kamu cari:
     * Kalau kategori lebih dari 1 -> "Work +1"
     */
    val categoriesText: String
        get() {
            val names = categoryNamesSafe.filter { it.isNotBlank() }
            if (names.isEmpty()) return "Uncategorized"
            val first = names.first()
            val extra = names.size - 1
            return if (extra > 0) "$first +$extra" else first
        }

    val categoryText: String
        get() = categoryNamesSafe.firstOrNull() ?: "Uncategorized"

    val deadlineDateFormatted: String
        get() {
            val d = Deadline?.toDate() ?: return "-"
            return SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(d)
        }

    val deadlineTimeFormatted: String
        get() {
            val d = Deadline?.toDate() ?: return "-"
            return SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(d)
        }




    val reminderText: String
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
}
