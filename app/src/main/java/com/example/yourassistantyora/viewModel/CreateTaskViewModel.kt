package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateTaskViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // --- UI State ---
    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val selectedDate = mutableStateOf<Date?>(null) // State untuk tanggal
    val selectedTime = mutableStateOf<Calendar?>(null) // State untuk waktu
    val selectedPriority = mutableStateOf("Medium")
    val selectedCategory = mutableStateOf("Work")
    val selectedReminder = mutableStateOf("Ingatkan 10 menit sebelumnya") // Dulu "Tidak ada peringat"
    val selectedStatus = mutableStateOf("To do")

    // --- Interaction State ---
    val loading = mutableStateOf(false)
    val taskSaved = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun clearError() {
        errorMessage.value = null
    }

    fun createTask() {
        // --- Validasi ---
        if (title.value.isBlank()) {
            errorMessage.value = "Title cannot be empty."
            return
        }
        if (selectedDate.value == null || selectedTime.value == null) {
            errorMessage.value = "Please select a valid date and time."
            return
        }

        loading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    throw IllegalStateException("User is not logged in.")
                }

                // --- Konversi & Penggabungan Data ---
                val deadlineTimestamp = combineDateAndTime(selectedDate.value!!, selectedTime.value!!)

                val taskData = hashMapOf(
                    "Title" to title.value,
                    "Description" to description.value,
                    "Deadline" to deadlineTimestamp,
                    "Priority" to getPriorityNumber(selectedPriority.value),
                    "Category" to getCategoryNumber(selectedCategory.value),
                    "Reminder" to getReminderNumber(selectedReminder.value),
                    "Status" to getStatusNumber(selectedStatus.value),
                    "UIDusers" to currentUser.uid
                )

                // --- Simpan ke Firestore ---
                db.collection("tasks").add(taskData).await()

                taskSaved.value = true // Tandai bahwa penyimpanan berhasil

            } catch (e: Exception) {
                errorMessage.value = e.message ?: "An unknown error occurred."
            } finally {
                loading.value = false
            }
        }
    }

    // --- Fungsi Konversi (Helper) ---

    private fun combineDateAndTime(date: Date, time: Calendar): Timestamp {
        val finalCalendar = Calendar.getInstance()
        finalCalendar.time = date
        finalCalendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
        finalCalendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE))
        finalCalendar.set(Calendar.SECOND, 0)
        finalCalendar.set(Calendar.MILLISECOND, 0)
        return Timestamp(finalCalendar.time)
    }

    private fun getPriorityNumber(priority: String): Int = when (priority) {
        "High" -> 2
        "Medium" -> 1
        "Low" -> 0
        else -> 1
    }

    private fun getCategoryNumber(category: String): Int = when (category) {
        "Project" -> 4
        "Meeting" -> 3
        "Travel" -> 2
        "Study" -> 1
        "Work" -> 0
        else -> 0
    }

    private fun getReminderNumber(reminder: String): Int = when (reminder) {
        "Ingatkan 3 hari sebelumnya" -> 7
        "Ingatkan 2 hari sebelumnya" -> 6
        "Ingatkan 1 hari sebelumnya" -> 5
        "Ingatkan 30 menit sebelumnya" -> 4
        "Ingatkan 20 menit sebelumnya" -> 3
        "Ingatkan 10 menit sebelumnya" -> 2
        "Ingatkan pada waktunya", "Ingat ketepat waktu" -> 1 // Handle typo
        "Tidak ada pengingat", "Tidak ada peringat" -> 0 // Handle typo
        else -> 2
    }

    private fun getStatusNumber(status: String): Int = when (status) {
        "On Progress", "In Progress" -> 4
        "Hold On" -> 3
        "Done" -> 2
        "To do" -> 1
        "Waiting" -> 0
        else -> 1
    }
}
