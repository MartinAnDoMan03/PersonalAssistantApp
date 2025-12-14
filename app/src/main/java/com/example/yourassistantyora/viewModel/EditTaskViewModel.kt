package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourassistantyora.models.TaskModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

class EditTaskViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val selectedDate = mutableStateOf<Date?>(null)
    val selectedTime = mutableStateOf<Calendar?>(null)
    val selectedPriority = mutableStateOf("Medium")
    val selectedCategory = mutableStateOf("Work")
    val selectedReminder = mutableStateOf("Ingatkan 10 menit sebelumnya")
    val selectedStatus = mutableStateOf("To do")

    private val _originalTask = mutableStateOf<TaskModel?>(null)
    val originalTask = _originalTask

    val isLoading = mutableStateOf(true)
    val taskUpdated = mutableStateOf(false)
    val taskDeleted = mutableStateOf(false)
    val errorMessage = mutableStateOf<String?>(null)

    fun loadTask(taskId: String) {
        isLoading.value = true
        viewModelScope.launch {
            try {
                val doc = db.collection("tasks").document(taskId).get().await()
                val task = doc.toObject(TaskModel::class.java)?.apply { id = doc.id }

                if (task != null) {
                    _originalTask.value = task
                    title.value = task.Title
                    description.value = task.Description

                    val deadlineDate = task.Deadline?.toDate() ?: Date()
                    selectedDate.value = deadlineDate
                    selectedTime.value = Calendar.getInstance().apply { time = deadlineDate }

                    selectedPriority.value = task.priorityText
                    selectedCategory.value = task.categoryText
                    selectedStatus.value = task.statusText
                    selectedReminder.value = reminderNumberToText(task.Reminder)
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

    fun updateTask() {
        val taskId = _originalTask.value?.id ?: return
        if (title.value.isBlank()) {
            errorMessage.value = "Title cannot be empty."
            return
        }

        val date = selectedDate.value ?: Date()
        val time = selectedTime.value ?: Calendar.getInstance()

        isLoading.value = true
        viewModelScope.launch {
            try {
                val deadlineTimestamp = combineDateAndTime(date, time)

                val catCode = getCategoryNumber(selectedCategory.value)
                val statusCode = getStatusNumber(selectedStatus.value)

                val updatedData = hashMapOf<String, Any>(
                    "Title" to title.value,
                    "Description" to description.value,
                    "Deadline" to deadlineTimestamp,
                    "Priority" to getPriorityNumber(selectedPriority.value),

                    // legacy & multi category
                    "Category" to catCode,
                    "Categories" to listOf(catCode),

                    "Status" to statusCode,
                    "Reminder" to getReminderNumber(selectedReminder.value),

                    // optional (kalau kamu simpan, gapapa)
                    "isCompleted" to (statusCode == 2)
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

    fun resetEvents() {
        taskUpdated.value = false
        taskDeleted.value = false
        errorMessage.value = null
    }

    fun clearError() { errorMessage.value = null }

    private fun combineDateAndTime(date: Date, time: Calendar): Timestamp {
        val finalCalendar = Calendar.getInstance().apply {
            timeInMillis = date.time
            set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY))
            set(Calendar.MINUTE, time.get(Calendar.MINUTE))
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return Timestamp(finalCalendar.time)
    }

    private fun getPriorityNumber(p: String): Int =
        when (p) { "High" -> 2; "Medium" -> 1; "Low" -> 0; else -> 1 }

    private fun getCategoryNumber(c: String): Int =
        when (c) { "Work" -> 0; "Study" -> 1; "Travel" -> 2; "Meeting" -> 3; "Project" -> 4; "Personal" -> 5; else -> 0 }

    private fun getStatusNumber(s: String): Int =
        when (s) { "Waiting" -> 0; "To do" -> 1; "Done" -> 2; "Hold On" -> 3; "In Progress", "On Progress" -> 4; else -> 1 }

    private fun getReminderNumber(reminder: String): Int = when (reminder) {
        "Ingatkan 3 hari sebelumnya" -> 7
        "Ingatkan 2 hari sebelumnya" -> 6
        "Ingatkan 1 hari sebelumnya" -> 5
        "Ingatkan 30 menit sebelumnya" -> 4
        "Ingatkan 20 menit sebelumnya" -> 3
        "Ingatkan 10 menit sebelumnya" -> 2
        "Ingatkan pada waktunya" -> 1
        else -> 0
    }

    private fun reminderNumberToText(n: Int): String = when (n) {
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
