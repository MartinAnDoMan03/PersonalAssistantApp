package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar
import java.util.Date

class CreateTaskViewModel : ViewModel() {

    // -------- UI state --------
    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val location = mutableStateOf("")

    val selectedPriority = mutableStateOf("Medium") // Low / Medium / High
    val selectedStatus = mutableStateOf("To do")     // Waiting / To do / Done / Hold On / In Progress
    val selectedReminder = mutableStateOf("Tidak ada pengingat")

    // ✅ date & time (biar CreateTaskScreen kamu jalan)
    val selectedDate = mutableStateOf<Date?>(null)
    val selectedTime = mutableStateOf<Calendar?>(null)

    // ✅ multi category (LIST STRING) — TANPA mutableStateListOf
    val selectedCategories = mutableStateOf<List<String>>(emptyList())

    // -------- system state --------
    val isLoading = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)
    val created = mutableStateOf(false)

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    fun toggleCategory(cat: String) {
        val cur = selectedCategories.value
        selectedCategories.value =
            if (cur.contains(cat)) cur - cat else cur + cat
    }

    fun resetCreated() {
        created.value = false
    }

    fun createTask() {
        errorMessage.value = null

        val uid = auth.currentUser?.uid
        if (uid.isNullOrBlank()) {
            errorMessage.value = "User not logged in."
            return
        }

        val t = title.value.trim()
        if (t.isBlank()) {
            errorMessage.value = "Title cannot be empty."
            return
        }

        // ✅ gabung date + time -> Timestamp (kalau belum pilih, boleh null)
        val deadlineTs: Timestamp? = buildDeadlineTimestamp(
            selectedDate.value,
            selectedTime.value
        )

        val priorityInt = when (selectedPriority.value) {
            "High" -> 2
            "Medium" -> 1
            else -> 0
        }

        val statusInt = when (selectedStatus.value) {
            "Waiting" -> 0
            "To do" -> 1
            "Done" -> 2
            "Hold On" -> 3
            "In Progress" -> 4
            else -> 1
        }

        val reminderInt = reminderToInt(selectedReminder.value)

        // legacy single category: isi dari pilihan pertama kalau dikenal, kalau custom -> -1
        val firstCat = selectedCategories.value.firstOrNull()
        val legacyCategoryInt = if (firstCat == null) 0 else categoryNameToIdOrMinus1(firstCat)

        val data = hashMapOf(
            "Title" to t,
            "Description" to description.value.trim(),
            "Deadline" to deadlineTs, // Timestamp? (Firestore bisa simpan null)

            "Priority" to priorityInt,

            // legacy (optional)
            "Category" to legacyCategoryInt,

            // ✅ ini yang dipakai untuk tampilkan semua category yang dipilih
            "CategoryNames" to selectedCategories.value,

            "Status" to statusInt,
            "Reminder" to reminderInt,
            "Location" to location.value.trim(),

            "userId" to uid,
            "createdAt" to Timestamp.now()
        )

        isLoading.value = true

        // ✅ pakai .document() biar id bisa kita simpan juga
        val docRef = db.collection("tasks").document()
        data["id"] = docRef.id

        docRef.set(data)
            .addOnSuccessListener {
                isLoading.value = false
                created.value = true
            }
            .addOnFailureListener { e ->
                isLoading.value = false
                errorMessage.value = e.message ?: "Failed to create task."
            }
    }

    private fun buildDeadlineTimestamp(date: Date?, time: Calendar?): Timestamp? {
        if (date == null || time == null) return null

        val cal = Calendar.getInstance().apply {
            timeInMillis = date.time
            set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, time.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(cal.time)
    }

    private fun reminderToInt(s: String): Int = when (s) {
        "Ingatkan pada waktunya" -> 1
        "Ingatkan 10 menit sebelumnya" -> 2
        "Ingatkan 20 menit sebelumnya" -> 3
        "Ingatkan 30 menit sebelumnya" -> 4
        "Ingatkan 1 hari sebelumnya" -> 5
        "Ingatkan 2 hari sebelumnya" -> 6
        "Ingatkan 3 hari sebelumnya" -> 7
        else -> 0 // "Tidak ada pengingat"
    }

    private fun categoryNameToIdOrMinus1(name: String): Int = when (name) {
        "Work" -> 0
        "Study" -> 1
        "Travel" -> 2
        "Meeting" -> 3
        "Project" -> 4
        "Personal" -> 5
        else -> -1 // custom category
    }
}
