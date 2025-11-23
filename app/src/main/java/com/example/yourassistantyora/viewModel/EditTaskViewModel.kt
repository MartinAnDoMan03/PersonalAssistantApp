package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourassistantyora.models.TaskModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class EditTaskViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- UI State (mirip CreateTaskViewModel) ---
    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val selectedDate = mutableStateOf<Date?>(null)
    val selectedTime = mutableStateOf<Calendar?>(null)
    val selectedPriority = mutableStateOf("Medium")
    val selectedCategory = mutableStateOf("Work")
    val selectedReminder = mutableStateOf("Ingatkan 10 menit sebelumnya")
    val selectedStatus = mutableStateOf("To do")

    // --- State untuk memegang data asli ---
    private val _originalTask = mutableStateOf<TaskModel?>(null)
    val originalTask = _originalTask

    // --- Interaction State ---
    val isLoading = mutableStateOf(true) // Default true saat memuat data
    val taskUpdated = mutableStateOf(false)
    val taskDeleted = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    /**
     * Fungsi utama untuk memuat data task berdasarkan ID dari Firestore.
     */
    fun loadTask(taskId: String) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val document = db.collection("tasks").document(taskId).get().await()
                val task = document.toObject(TaskModel::class.java)?.copy(id = document.id)
                if (task != null) {
                    // Isi semua state UI dengan data dari task yang dimuat
                    _originalTask.value = task
                    title.value = task.Title
                    description.value = task.Description
                    val deadlineDate = task.Deadline.toDate()
                    selectedDate.value = deadlineDate
                    selectedTime.value = Calendar.getInstance().apply { time = deadlineDate }
                    selectedPriority.value = task.priorityText
                    selectedCategory.value = task.categoryText
                    selectedStatus.value = task.statusText
                    selectedReminder.value = getReminderText(task.Reminder)
                    // Anda bisa menambahkan logika untuk reminder jika perlu
                } else {
                    errorMessage.value = "Task not found."
                }
            } catch (e: Exception) {
                errorMessage.value = "Failed to load task: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Menyimpan perubahan ke Firestore.
     */
    fun updateTask() {
        val taskId = _originalTask.value?.id ?: return
        if (title.value.isBlank()) {
            errorMessage.value = "Title cannot be empty."
            return
        }
        // Validasi lain jika perlu...

        isLoading.value = true
        viewModelScope.launch {
            try {
                val deadlineTimestamp = combineDateAndTime(selectedDate.value!!, selectedTime.value!!)

                val updatedData = hashMapOf<String, Any>(
                    "Title" to title.value,
                    "Description" to description.value,
                    "Deadline" to deadlineTimestamp,
                    "Priority" to getPriorityNumber(selectedPriority.value),
                    "Category" to getCategoryNumber(selectedCategory.value),
                    "Status" to getStatusNumber(selectedStatus.value),
                    "Reminder" to getReminderNumber(selectedReminder.value)
                )

                db.collection("tasks").document(taskId).update(updatedData).await()
                taskUpdated.value = true
            } catch (e: Exception) {
                errorMessage.value = "Failed to update task: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    /**
     * Menghapus task dari Firestore.
     */
    fun deleteTask() {
        val taskId = _originalTask.value?.id ?: return
        isLoading.value = true
        viewModelScope.launch {
            try {
                db.collection("tasks").document(taskId).delete().await()
                taskDeleted.value = true
            } catch (e: Exception) {
                errorMessage.value = "Failed to delete task: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }


    fun clearError() {
        errorMessage.value = null
    }

    // --- Helper Functions (sama seperti di CreateTaskViewModel) ---
    private fun combineDateAndTime(date: Date, time: Calendar): Timestamp {
        val finalCalendar = Calendar.getInstance().apply {
            timeInMillis = date.time
            set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, time.get(Calendar.MINUTE))
        }
        return Timestamp(finalCalendar.time)
    }

    private fun getPriorityNumber(p: String): Int = when (p) {"High"->2;"Medium"->1;"Low"->0;else->1}
    private fun getCategoryNumber(c: String): Int = when (c) {"Project"->4;"Meeting"->3;"Travel"->2;"Study"->1;"Work"->0;else->0}
    private fun getStatusNumber(s: String): Int = when (s) {"On Progress"->4;"Hold On"->3;"Done"->2;"To do"->1;"Waiting"->0;else->1}
    // ✅ 3. TAMBAHKAN DUA FUNGSI HELPER BARU INI
    private fun getReminderNumber(reminder: String): Int = when (reminder) {
        "Ingatkan 3 hari sebelumnya" -> 7
        "Ingatkan 2 hari sebelumnya" -> 6
        "Ingatkan 1 hari sebelumnya" -> 5
        "Ingatkan 30 menit sebelumnya" -> 4
        "Ingatkan 20 menit sebelumnya" -> 3
        "Ingatkan 10 menit sebelumnya" -> 2
        "Ingatkan pada waktunya" -> 1
        "Tidak ada pengingat" -> 0
        else -> 0
    }

    private fun getReminderText(reminder: Int): String = when (reminder) {
        7 -> "Ingatkan 3 hari sebelumnya"
        6 -> "Ingatkan 2 hari sebelumnya"
        5 -> "Ingatkan 1 hari sebelumnya"
        4 -> "Ingatkan 30 menit sebelumnya"
        3 -> "Ingatkan 20 menit sebelumnya"
        2 -> "Ingatkan 10 menit sebelumnya"
        1 -> "Ingatkan pada waktunya"
        0 -> "Tidak ada pengingat"
        else -> "Tidak ada pengingat"
    }
}

