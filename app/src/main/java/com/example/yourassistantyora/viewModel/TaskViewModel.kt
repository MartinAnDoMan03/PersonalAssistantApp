package com.example.yourassistantyora.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourassistantyora.models.TaskModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map // ✅ 1. IMPORT YANG HILANG DITAMBAHKAN
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.Date

// Enum untuk merepresentasikan sesi dalam sehari
enum class DailySession {
    Morning, Afternoon, Evening, Night
}

class TaskViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var tasksListener: ListenerRegistration? = null

    // --- STATE INTERNAL ---
    private val _allTasks = MutableStateFlow<List<TaskModel>>(emptyList())

    // --- STATE UNTUK KONTROL UI ---
    val selectedViewMode = MutableStateFlow("List")
    val selectedStatus = MutableStateFlow("All")
    val selectedCategory = MutableStateFlow("All")
    val selectedDate = MutableStateFlow(getStartOfToday().time) // Untuk Weekly & Monthly
    val searchQuery = MutableStateFlow("")
    // --- STATE YANG AKAN DIGUNAKAN OLEH UI ---
    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // 1. FLOW DASAR: Filter task berdasarkan Status dan Kategori
    private val baseFilteredTasks = combine(
        _allTasks,
        selectedStatus,
        selectedCategory,
        searchQuery // Sertakan searchQuery dalam combine
    ) { tasks, status, category, query ->
        tasks.filter { task ->
            val statusMatch = if (status == "All") true else task.statusText.equals(status, ignoreCase = true)
            val categoryMatch = if (category == "All") true else task.categoryText.equals(category, ignoreCase = true)
            // Tambahkan kondisi pencarian berdasarkan judul
            val queryMatch = if (query.isBlank()) true else task.Title.contains(query, ignoreCase = true)

            statusMatch && categoryMatch && queryMatch
        }
    }

    // 2. FLOW UNTUK TAMPILAN "LIST"
    val listTasks = baseFilteredTasks // Tampilan list langsung menggunakan filter dasar

    // 3. FLOW UNTUK TAMPILAN "DAILY"
    val dailyTasksGrouped = baseFilteredTasks.combine(MutableStateFlow(getStartOfToday())) { tasks, todayCal ->
        val startOfToday = todayCal.time
        val endOfToday = getEndOfDay(todayCal).time

        tasks.filter { it.Deadline.toDate() in startOfToday..endOfToday } // ✅ 2. PERBAIKI KAPITALISASI
            .groupBy { getSessionForTask(it) }
    }

    // 4. FLOW UNTUK TAMPILAN "WEEKLY" & "MONTHLY" (berdasarkan selectedDate)
    val dateFilteredTasks = baseFilteredTasks.combine(selectedDate) { tasks, date ->
        val startOfDay = getStartOfDay(date).time
        val endOfDay = getEndOfDay(getStartOfDay(date)).time

        tasks.filter { it.Deadline.toDate() in startOfDay..endOfDay } // ✅ 3. PERBAIKI KAPITALISASI
    }

    // 5. FLOW KHUSUS UNTUK TAMPILAN "DAILY"
    val tasksForToday = baseFilteredTasks.map { tasks ->
        val todayStart = getStartOfToday().time
        val todayEnd = getEndOfDay(getStartOfToday()).time
        tasks.filter { it.Deadline.toDate() in todayStart..todayEnd }
    }

    init {
        fetchTasks()
    }

    fun fetchTasks() {
        tasksListener?.remove()

        viewModelScope.launch {
            _isLoading.value = true
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _error.value = "User not logged in."
                _isLoading.value = false
                return@launch
            }

            try {
                val query = db.collection("tasks")
                    .whereEqualTo("UIDusers", currentUser.uid)
                    .orderBy("Deadline", Query.Direction.ASCENDING) // ✅ 4. PERBAIKI KAPITALISASI

                tasksListener = query.addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        _error.value = "Failed to listen for task updates: ${e.message}"
                        Log.e("TaskViewModel", "Listener error", e)
                        _isLoading.value = false
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val tasks = snapshot.documents.map { doc ->
                            doc.toObject(TaskModel::class.java)?.copy(id = doc.id)
                        }.filterNotNull()
                        _allTasks.value = tasks
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch tasks: ${e.message}"
                Log.e("TaskViewModel", "Fetch error", e)
                _isLoading.value = false
            }
        }
    }

    fun updateTaskStatus(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                val newStatus = if (isCompleted) 2 else 1
                db.collection("tasks").document(taskId).update("Status", newStatus).await() // ✅ 5. PERBAIKI KAPITALISASI
            } catch (e: Exception) {
                _error.value = "Failed to update task status: ${e.message}"
                Log.e("TaskViewModel", "Update status error", e)
            }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            try {
                db.collection("tasks").document(taskId).delete().await()
                // Tidak perlu mengubah state lokal karena listener Firestore akan melakukannya secara otomatis
            } catch (e: Exception) {
                _error.value = "Failed to delete task: ${e.message}"
                Log.e("TaskViewModel", "Delete task error", e)
            }
        }
    }

    // --- Fungsi Aksi untuk mengubah filter dari UI ---
    fun setViewMode(mode: String) { selectedViewMode.value = mode }
    fun setStatusFilter(status: String) { selectedStatus.value = status }
    fun setCategoryFilter(category: String) { selectedCategory.value = category }
    fun setSelectedDate(date: Date) { selectedDate.value = date }
    fun setSearchQuery(query: String) { searchQuery.value = query }

    override fun onCleared() {
        super.onCleared()
        tasksListener?.remove()
    }

    // --- HELPER FUNCTIONS ---
    fun getSessionForTask(task: TaskModel): DailySession { // ✅ 6. PERBAIKI VISIBILITAS
        val calendar = Calendar.getInstance().apply { time = task.Deadline.toDate() } // ✅ 7. PERBAIKI KAPITALISASI
        return when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 5..10 -> DailySession.Morning
            in 11..14 -> DailySession.Afternoon
            in 15..18 -> DailySession.Evening
            else -> DailySession.Night
        }
    }

    private fun getStartOfDay(date: Date): Calendar {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun getStartOfToday(): Calendar {
        return getStartOfDay(Date())
    }

    private fun getEndOfDay(calendar: Calendar): Calendar {
        return (calendar.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }
}
