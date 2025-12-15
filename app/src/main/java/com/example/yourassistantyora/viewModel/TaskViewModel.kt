package com.example.yourassistantyora.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.repository.FirebaseTaskRepository
import com.example.yourassistantyora.repository.TaskRepository
import com.example.yourassistantyora.utils.DateUtils.isSameDay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

enum class DailySession { Morning, Afternoon, Evening, Night }

class TaskViewModel(
    private val repo: TaskRepository = FirebaseTaskRepository()
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _selectedViewMode = MutableStateFlow("List")
    val selectedViewMode: StateFlow<String> = _selectedViewMode.asStateFlow()

    private val _selectedStatus = MutableStateFlow("All")
    val selectedStatus: StateFlow<String> = _selectedStatus.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()

    private val rawTasks: StateFlow<List<TaskModel>> =
        repo.observeMyTasks()
            .onStart { _isLoading.value = true }
            .catch { e ->
                _error.value = e.message
                emit(emptyList())
            }
            .onEach { _isLoading.value = false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private fun statusCodeFromLabel(label: String): Int? = when (label) {
        "Waiting" -> 0
        "To do" -> 1
        "Done" -> 2
        "Hold On" -> 3
        "In Progress", "On Progress" -> 4
        else -> null
    }

    private fun categoryCodeFromLabel(label: String): Int? = when (label) {
        "Work" -> 0
        "Study" -> 1
        "Travel" -> 2
        "Meeting" -> 3
        "Project" -> 4
        "Personal" -> 5
        else -> null
    }

    val listTasks: StateFlow<List<TaskModel>> = combine(
        rawTasks, selectedStatus, selectedCategory, searchQuery
    ) { tasks, statusLabel, categoryLabel, q ->

        val statusCode = statusCodeFromLabel(statusLabel)
        val categoryCode = categoryCodeFromLabel(categoryLabel)

        tasks.filter { t ->
            val okStatus = statusCode == null || t.Status == statusCode
            val okCategory =
                categoryCode == null ||
                        t.Categories.contains(categoryCode) ||
                        t.Category == categoryCode

            val okSearch = q.isBlank() || t.Title.contains(q, ignoreCase = true)
            okStatus && okCategory && okSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dateFilteredTasks: StateFlow<List<TaskModel>> =
        combine(listTasks, selectedDate) { tasks, date ->
            tasks.filter { t ->
                val d = t.Deadline?.toDate() ?: return@filter false
                isSameDay(d, date)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tasksForToday: StateFlow<List<TaskModel>> =
        listTasks.map { tasks ->
            val today = Date()
            tasks.filter { t ->
                val d = t.Deadline?.toDate() ?: return@filter false
                isSameDay(d, today)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setViewMode(mode: String) { _selectedViewMode.value = mode }
    fun setStatusFilter(v: String) { _selectedStatus.value = v }
    fun setCategoryFilter(v: String) { _selectedCategory.value = v }
    fun setSearchQuery(v: String) { _searchQuery.value = v }
    fun setSelectedDate(date: Date) { _selectedDate.value = date }

    fun updateTaskStatus(taskId: String, isCompleting: Boolean) {
        viewModelScope.launch {
            runCatching {
                repo.updateStatus(taskId, if (isCompleting) 2 else 1)
            }.onFailure { _error.value = it.message }
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            runCatching { repo.deleteTask(taskId) }
                .onFailure { _error.value = it.message }
        }
    }

    fun getSessionForTask(task: TaskModel): DailySession {
        val date = task.Deadline?.toDate()
        val cal = Calendar.getInstance()
        if (date != null) cal.time = date
        val hour = cal.get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 5..10 -> DailySession.Morning
            in 11..14 -> DailySession.Afternoon
            in 15..18 -> DailySession.Evening
            else -> DailySession.Night
        }
    }
}