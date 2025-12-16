package com.example.yourassistantyora.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.components.TaskCardDesignStyle
import com.example.yourassistantyora.components.TaskFilterRow
import com.example.yourassistantyora.components.TaskViewModeNavigation
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.isSameDay
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalFoundationApi::class,
    ExperimentalAnimationApi::class
)
@Composable
fun MonthlyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    // ===== STATE =====
    val tasksForSelectedDate by viewModel.dateFilteredTasks.collectAsState(emptyList())
    val isLoading by viewModel.isLoading.collectAsState(false)
    val selectedDate by viewModel.selectedDate.collectAsState(Date())
    val selectedStatus by viewModel.selectedStatus.collectAsState("All")
    val selectedCategory by viewModel.selectedCategory.collectAsState("All")

    var currentMonthDate by remember { mutableStateOf(Date()) }
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }
    var swipedTaskId by remember { mutableStateOf<String?>(null) }

    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // snackbar undo
    var lastCompletedTask by remember { mutableStateOf<TaskModel?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    // dialogs
    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<TaskModel?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }

    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { viewModel.setViewMode("Monthly") }

    fun isCompleted(task: TaskModel) = task.Status == 2

    // ===== COMPLETE / UNDO =====
    fun completeTask(task: TaskModel) {
        if (!isCompleted(task)) {
            viewModel.updateTaskStatus(task.id, true)
            lastCompletedTask = task
            showUndoSnackbar = true
            swipedTaskId = null

            scope.launch {
                delay(8000)
                showUndoSnackbar = false
                lastCompletedTask = null
            }
        }
    }

    fun undoCompletion() {
        lastCompletedTask?.let {
            viewModel.updateTaskStatus(it.id, false)
        }
        showUndoSnackbar = false
        lastCompletedTask = null
    }

    // ===== RESTORE =====
    fun showRestoreConfirmation(task: TaskModel) {
        taskToRestore = task
        showRestoreDialog = true
        swipedTaskId = null
    }

    fun restoreTask() {
        taskToRestore?.let {
            viewModel.updateTaskStatus(it.id, false)
        }
        showRestoreDialog = false
        taskToRestore = null
    }

    // ===== FILTER =====
    val filteredTasks = remember(
        tasksForSelectedDate,
        searchQuery,
        selectedStatus,
        selectedCategory
    ) {
        tasksForSelectedDate
            .filter { task -> !task.id.startsWith("team_") }
            .filter { task ->
                val statusMatch = if (!isSearching) selectedStatus == "All" || task.statusText == selectedStatus else true
                val categoryMatch = if (!isSearching) selectedCategory == "All" || task.categoryNamesSafe.contains(selectedCategory) else true
                val queryMatch = searchQuery.isBlank() || task.Title.contains(searchQuery, true) || task.Description.contains(searchQuery, true)

                statusMatch && categoryMatch && queryMatch
            }
    }

    val completedTasks = filteredTasks.filter(::isCompleted)
    val activeTasks = filteredTasks.filterNot(::isCompleted)

    val calendarCells = remember(currentMonthDate, selectedDate) {
        generateMonthlyCalendarCells(currentMonthDate, selectedDate)
    }

    // ================= UI =================
    Box(Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F7FA),
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(!isSearching) {
                            Text("My Tasks", fontWeight = FontWeight.Bold)
                        }
                        AnimatedVisibility(isSearching) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search tasks...") },
                                modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedContainerColor = Color(0xFFF0F0F0),
                                    unfocusedContainerColor = Color(0xFFF0F0F0)
                                )
                            )
                        }
                    },
                    navigationIcon = {
                        if (!isSearching) {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            if (isSearching) searchQuery = ""
                            isSearching = !isSearching
                        }) {
                            Icon(if (isSearching) Icons.Default.Close else Icons.Outlined.Search, null)
                        }
                    }
                )
            },
            // ✅ TOMBOL CREATE (FAB) DISAMAKAN DENGAN WEEKLY
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("create_task") },
                    containerColor = Color(0xFF6A70D7),
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Add, "Create Task", modifier = Modifier.size(28.dp))
                }
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = {
                        selectedTab = it
                        when (it) {
                            NavigationConstants.TAB_HOME -> navController.navigateSingleTop("home")
                            NavigationConstants.TAB_TASK -> navController.navigateSingleTop("task_list")
                            NavigationConstants.TAB_NOTE -> navController.navigateSingleTop("notes")
                            NavigationConstants.TAB_TEAM -> navController.navigateSingleTop("team")
                        }
                    }
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = modifier.fillMaxSize().padding(padding)
            ) {
                item {
                    TaskViewModeNavigation(
                        selectedViewMode = "Monthly",
                        onViewModeChange = {
                            when (it) {
                                "List" -> navController.navigateSingleTop("task_list")
                                "Daily" -> navController.navigateSingleTop("daily_tasks")
                                "Weekly" -> navController.navigateSingleTop("weekly_tasks")
                                "Monthly" -> {}
                            }
                        }
                    )
                }

                if (!isSearching) {
                    item {
                        Column(Modifier.background(Color.White)) {
                            TaskFilterRow(
                                selectedStatus = selectedStatus,
                                onStatusSelected = { viewModel.setStatusFilter(it) },
                                selectedCategory = selectedCategory,
                                onCategorySelected = { viewModel.setCategoryFilter(it) },
                                categories = listOf("All", "Work", "Study", "Project", "Meeting", "Travel", "Personal")
                            )
                            HorizontalDivider(thickness = 0.5.dp, color = Color.LightGray)
                        }
                    }
                }

                item {
                    MonthlyCalendar(
                        currentMonthDate = currentMonthDate,
                        selectedDate = selectedDate,
                        cells = calendarCells,
                        onPrevMonth = {
                            currentMonthDate = Calendar.getInstance().apply {
                                time = currentMonthDate
                                add(Calendar.MONTH, -1)
                            }.time
                        },
                        onNextMonth = {
                            currentMonthDate = Calendar.getInstance().apply {
                                time = currentMonthDate
                                add(Calendar.MONTH, 1)
                            }.time
                        },
                        onDateSelected = { viewModel.setSelectedDate(it) }
                    )
                }

                items(activeTasks, key = { it.id }) { task ->
                    TaskCardDesignStyle(
                        task = task,
                        isCompleted = false,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        onTaskClick = { navController.navigate("task_detail/${task.id}") },
                        onCheckboxClick = { checked -> if (checked) completeTask(task) },
                        onDeleteIconClick = { taskToDelete = task },
                        swipedTaskId = swipedTaskId,
                        onSwipeChange = { id, swiped -> swipedTaskId = if (swiped) id else null }
                    )
                }

                if (completedTasks.isNotEmpty()) {
                    item {
                        Text(
                            "Completed (${completedTasks.size})",
                            modifier = Modifier.padding(16.dp),
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                    items(completedTasks, key = { "c_${it.id}" }) { task ->
                        TaskCardDesignStyle(
                            task = task,
                            isCompleted = true,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            onTaskClick = { navController.navigate("task_detail/${task.id}") },
                            onCheckboxClick = { checked -> if (!checked) showRestoreConfirmation(task) },
                            onDeleteIconClick = { taskToDelete = task },
                            swipedTaskId = swipedTaskId,
                            onSwipeChange = { id, swiped -> swipedTaskId = if (swiped) id else null }
                        )
                    }
                }
                item { Spacer(Modifier.height(100.dp)) } // Spacer lebih tinggi agar FAB tidak menutupi item terakhir
            }
        }

        // SNACKBAR UNDO
        AnimatedVisibility(
            visible = showUndoSnackbar,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 90.dp, start = 20.dp, end = 20.dp)
        ) {
            SnackbarCardSimple(
                icon = { Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) },
                text = "Task completed",
                actionText = "UNDO",
                onAction = { undoCompletion() }
            )
        }

        // DIALOGS
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("Restore Task?") },
                text = { Text("Do you want to move this task back to active tasks?") },
                confirmButton = { TextButton(onClick = { restoreTask() }) { Text("Yes") } },
                dismissButton = { TextButton(onClick = { showRestoreDialog = false }) { Text("Cancel") } }
            )
        }

        taskToDelete?.let { task ->
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text("Hapus tugas?") },
                text = { Text("Apakah kamu yakin ingin menghapus tugas ini?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteTask(task.id)
                        taskToDelete = null
                    }) { Text("Hapus", color = Color.Red) }
                },
                dismissButton = { TextButton(onClick = { taskToDelete = null }) { Text("Batal") } }
            )
        }
    }
}

// === SNACKBAR COMPONENT ===
@Composable
private fun SnackbarCardSimple(icon: @Composable () -> Unit, text: String, actionText: String, onAction: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF323232)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            icon()
            Spacer(Modifier.width(12.dp))
            Text(text, color = Color.White, fontSize = 14.sp, modifier = Modifier.weight(1f))
            TextButton(onClick = onAction) { Text(actionText, color = Color(0xFF6A70D7), fontWeight = FontWeight.Bold) }
        }
    }
}

// === CALENDAR HELPERS ===
private data class MonthlyCalendarCell(val date: Date, val dayOfMonth: Int, val isCurrentMonth: Boolean, val isSelected: Boolean, val isToday: Boolean)

@Composable
private fun MonthlyCalendar(currentMonthDate: Date, selectedDate: Date, cells: List<MonthlyCalendarCell>, onPrevMonth: () -> Unit, onNextMonth: () -> Unit, onDateSelected: (Date) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
        val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonthDate)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(monthLabel, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Row {
                IconButton(onClick = onPrevMonth) { Icon(Icons.Default.ChevronLeft, null) }
                IconButton(onClick = onNextMonth) { Icon(Icons.Default.ChevronRight, null) }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(260.dp), userScrollEnabled = false) {
            items(cells, key = { it.date.time }) { cell ->
                Box(
                    modifier = Modifier.aspectRatio(1f).clip(CircleShape).clickable(enabled = cell.isCurrentMonth) { onDateSelected(cell.date) }
                        .background(when { cell.isSelected -> Color(0xFF6A70D7); cell.isToday -> Color(0xFFE8E7FF); else -> Color.Transparent }),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cell.dayOfMonth.toString(), color = when { !cell.isCurrentMonth -> Color.LightGray; cell.isSelected -> Color.White; cell.isToday -> Color(0xFF6A70D7); else -> Color.Black }, fontWeight = if (cell.isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

private fun generateMonthlyCalendarCells(monthDate: Date, selectedDate: Date): List<MonthlyCalendarCell> {
    val cal = Calendar.getInstance().apply { time = monthDate; set(Calendar.DAY_OF_MONTH, 1) }
    val firstDayOffset = cal.get(Calendar.DAY_OF_WEEK) - 1
    cal.add(Calendar.DAY_OF_MONTH, -firstDayOffset)
    val currentMonth = Calendar.getInstance().apply { time = monthDate }.get(Calendar.MONTH)
    val today = Date()
    return (0 until 42).map {
        val date = cal.time
        val cell = MonthlyCalendarCell(date, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH) == currentMonth, isSameDay(date, selectedDate), isSameDay(date, today))
        cal.add(Calendar.DAY_OF_MONTH, 1)
        cell
    }
}