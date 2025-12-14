package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private data class WeekDayItem(
    val date: Date,
    val dayName: String,
    val dayNumber: String,
    val taskCount: Int,
    val isSelected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WeeklyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    // --------- STATE FROM VM ----------
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val allTasks by viewModel.listTasks.collectAsState(initial = emptyList())
    val tasksForSelectedDate by viewModel.dateFilteredTasks.collectAsState(initial = emptyList())

    // --------- LOCAL UI STATE ----------
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }
    val scope = rememberCoroutineScope()

    // biar cuma 1 card swipe kebuka
    var swipedTaskId by remember { mutableStateOf<String?>(null) }

    // snackbar undo complete
    var lastCompletedTask by remember { mutableStateOf<TaskModel?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    // snackbar undo delete (UI-only)
    var lastDeletedTask by remember { mutableStateOf<TaskModel?>(null) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }

    // dialogs
    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<TaskModel?>(null) }

    var deletingTask by remember { mutableStateOf<TaskModel?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.setViewMode("Weekly") }

    // --------- HELPERS (FIX isCompleted ISSUE) ----------
    fun isCompletedTask(t: TaskModel): Boolean = (t.Status == 2) // Done

    // split active vs completed (✅ FIXED)
    val (completedTasks, activeTasks) = remember(tasksForSelectedDate) {
        tasksForSelectedDate.partition { isCompletedTask(it) }
    }

    // generate week strip (start Sunday)
    val weekDays = remember(selectedDate, allTasks) {
        val cal = Calendar.getInstance().apply { time = selectedDate }
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        (0..6).map {
            val d = cal.time

            val count = allTasks.count { t ->
                val td = t.Deadline?.toDate() ?: return@count false
                isSameDay(td, d)
            }

            val item = WeekDayItem(
                date = d,
                dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(d).uppercase(),
                dayNumber = SimpleDateFormat("d", Locale.getDefault()).format(d),
                taskCount = count,
                isSelected = isSameDay(d, selectedDate)
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
            item
        }
    }

    // --------- ACTIONS ----------
    fun onComplete(task: TaskModel) {
        if (!isCompletedTask(task)) {
            viewModel.updateTaskStatus(task.id, true)
            lastCompletedTask = task
            showUndoSnackbar = true
            showDeleteSnackbar = false
            swipedTaskId = null

            scope.launch {
                delay(8000)
                showUndoSnackbar = false
                lastCompletedTask = null
            }
        }
    }

    fun undoCompletion() {
        lastCompletedTask?.let { viewModel.updateTaskStatus(it.id, false) }
        showUndoSnackbar = false
        lastCompletedTask = null
    }

    fun showRestoreConfirmation(task: TaskModel) {
        taskToRestore = task
        showRestoreDialog = true
        swipedTaskId = null
    }

    fun restoreTask() {
        taskToRestore?.let { viewModel.updateTaskStatus(it.id, false) }
        showRestoreDialog = false
        taskToRestore = null
    }

    fun deleteTaskConfirmed(task: TaskModel) {
        lastDeletedTask = task
        viewModel.deleteTask(task.id)

        showDeleteSnackbar = true
        showUndoSnackbar = false
        swipedTaskId = null

        scope.launch {
            delay(8000)
            showDeleteSnackbar = false
            lastDeletedTask = null
        }
    }

    fun undoDelete() {
        // UI-only undo (kalau mau “beneran restore”, kamu butuh soft delete di Firestore)
        showDeleteSnackbar = false
        lastDeletedTask = null
    }

    // --------- UI ----------
    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F7FA),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "My Tasks",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFF2D2D2D))
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* optional: search */ }) {
                            Icon(Icons.Outlined.Search, "Search", tint = Color(0xFF2D2D2D))
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        selectedTab = index
                        when (index) {
                            NavigationConstants.TAB_HOME -> navController.navigateSingleTop("home")
                            NavigationConstants.TAB_TASK -> navController.navigateSingleTop("task_list")
                            NavigationConstants.TAB_NOTE -> navController.navigateSingleTop("notes")
                            NavigationConstants.TAB_TEAM -> navController.navigateSingleTop("team")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("create_task") },
                    containerColor = Color(0xFF6A70D7),
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Add, "Create Task", modifier = Modifier.size(28.dp))
                }
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TaskViewModeNavigation(
                    selectedViewMode = "Weekly",
                    onViewModeChange = { viewModel.setViewMode(it) },
                    onNavigateToList = { navController.navigateSingleTop("task_list") },
                    onNavigateToDaily = { navController.navigateSingleTop("daily_tasks") },
                    onNavigateToWeekly = { /* already */ },
                    onNavigateToMonthly = { navController.navigateSingleTop("monthly_tasks") }
                )

                TaskFilterRow(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { viewModel.setStatusFilter(it) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    categories = listOf("All", "Work", "Study", "Project", "Meeting", "Travel", "Personal")
                )

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // Calendar strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp)
                ) {
                    items(weekDays, key = { it.date.time }) { day ->
                        WeekDayCard(
                            day = day,
                            onClick = { viewModel.setSelectedDate(day.date) }
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (activeTasks.isEmpty() && completedTasks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .padding(bottom = 80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("No tasks for this day.", color = Color.Gray)
                                }
                            }
                        }

                        items(activeTasks, key = { "active_${it.id}" }) { task ->
                            TaskCardDesignStyle(
                                task = task,
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onCheckboxClick = { checked ->
                                    if (checked) onComplete(task)
                                },
                                onDeleteIconClick = {
                                    deletingTask = task
                                    showDeleteConfirmDialog = true
                                },
                                swipedTaskId = swipedTaskId,
                                onSwipeChange = { id, isSwiped ->
                                    swipedTaskId = if (isSwiped) id else null
                                }
                            )
                        }

                        if (completedTasks.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Completed (${completedTasks.size})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                            items(completedTasks, key = { "done_${it.id}" }) { task ->
                                TaskCardDesignStyle(
                                    task = task,
                                    isCompleted = true,
                                    onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                    onCheckboxClick = { checked ->
                                        // kalau user uncheck -> restore confirm
                                        if (!checked) showRestoreConfirmation(task)
                                    },
                                    onDeleteIconClick = {
                                        deletingTask = task
                                        showDeleteConfirmDialog = true
                                    },
                                    swipedTaskId = swipedTaskId,
                                    onSwipeChange = { id, isSwiped ->
                                        swipedTaskId = if (isSwiped) id else null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ---------- SNACKBAR: UNDO COMPLETE ----------
        AnimatedVisibility(
            visible = showUndoSnackbar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            SnackbarCardSimple(
                icon = { Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50)) },
                text = "Task completed",
                actionText = "UNDO",
                onAction = { undoCompletion() }
            )
        }

        // ---------- SNACKBAR: UNDO DELETE ----------
        AnimatedVisibility(
            visible = showDeleteSnackbar,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
        ) {
            SnackbarCardSimple(
                icon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFF44336)) },
                text = "Task deleted",
                actionText = "UNDO",
                onAction = { undoDelete() }
            )
        }

        // ---------- DIALOG RESTORE ----------
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false; taskToRestore = null },
                title = { Text("Restore Task?") },
                text = { Text("Do you want to move this task back to active tasks?") },
                confirmButton = { TextButton(onClick = { restoreTask() }) { Text("Yes") } },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false; taskToRestore = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ---------- DIALOG DELETE CONFIRM ----------
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false; deletingTask = null },
                title = { Text("Hapus tugas?") },
                text = { Text("Apakah kamu yakin ingin menghapus tugas ini?") },
                confirmButton = {
                    TextButton(onClick = {
                        deletingTask?.let { deleteTaskConfirmed(it) }
                        showDeleteConfirmDialog = false
                        deletingTask = null
                    }) { Text("Hapus", color = Color(0xFFF44336)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false; deletingTask = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
private fun WeekDayCard(
    day: WeekDayItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (day.isSelected) Color(0xFF6A70D7) else Color(0xFFF5F7FA),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = day.dayName,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = if (day.isSelected) Color.White else Color(0xFF9E9E9E)
            )
            Text(
                text = day.dayNumber,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (day.isSelected) Color.White else Color(0xFF2D2D2D)
            )
            Text(
                text = "${day.taskCount} Task",
                fontSize = 9.sp,
                color = if (day.isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
private fun SnackbarCardSimple(
    icon: @Composable () -> Unit,
    text: String,
    actionText: String,
    onAction: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF323232)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                icon()
                Spacer(Modifier.width(12.dp))
                Text(text, color = Color.White, fontSize = 14.sp)
            }
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    actionText,
                    color = Color(0xFF6A70D7),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

internal fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
