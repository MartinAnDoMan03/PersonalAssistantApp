package com.example.yourassistantyora

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.components.TaskViewModeNavigation
import com.example.yourassistantyora.components.TaskFilterRow
import com.example.yourassistantyora.screen.BadgeChip
import com.example.yourassistantyora.screen.CreateTaskScreen
import com.example.yourassistantyora.screen.TaskDetailScreen
import com.example.yourassistantyora.utils.NavigationConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.collections.plus
import kotlin.math.roundToInt

// Data class untuk Weekly Task dengan day
data class WeeklyTaskItem(
    val task: com.example.yourassistantyora.screen.Task,
    val dayOfMonth: Int,
    val dayName: String
)

// Data class untuk calendar day
data class CalendarDay(
    val dayName: String,
    val dayNumber: Int,
    val taskCount: Int,
    val isSelected: Boolean = false
)

// ---------- WEEKLY SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WeeklyScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onTaskClick: (com.example.yourassistantyora.screen.Task) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToTeam: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToDaily: () -> Unit = {},
    onNavigateToMonthly: () -> Unit = {}
) {
    // ✨ STATE UNTUK DETAIL SCREEN - PALING ATAS
    var showDetailScreen by remember { mutableStateOf(false) }
    var selectedTaskForDetail by remember { mutableStateOf<com.example.yourassistantyora.screen.Task?>(null) }

    var showCreateTaskScreen by remember { mutableStateOf(false) }

    fun handleTaskClick(task: com.example.yourassistantyora.screen.Task) {
        selectedTaskForDetail = task
        showDetailScreen = true
    }

    var selectedViewMode by remember { mutableStateOf("Weekly") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    val selectedTab = NavigationConstants.TAB_TASK

    // State untuk selected day
    var selectedDayNumber by remember { mutableStateOf(3) } // Default TUE 3

    // Generate week days
    val weekDays = remember {
        listOf(
            CalendarDay("SUN", 1, 2),
            CalendarDay("MON", 2, 1),
            CalendarDay("TUE", 3, 4, isSelected = true),
            CalendarDay("WED", 4, 2),
            CalendarDay("THU", 5, 2),
            CalendarDay("FRI", 6, 2),
            CalendarDay("SAT", 7, 2)
        )
    }

    // State untuk weekly tasks dengan hari
    var weeklyTasks by remember {
        mutableStateOf(
            listOf(
                WeeklyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        1,
                        "Team meeting preparation",
                        "06:00 AM",
                        "High",
                        "Work",
                        "Waiting"
                    ), 3, "TUE"
                ),
                WeeklyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        2,
                        "Review design mockups",
                        "06:00 AM",
                        "Medium",
                        "Work",
                        "To do"
                    ), 3, "TUE"
                ),
                WeeklyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        3,
                        "Submit project report",
                        "06:00 AM",
                        "Low",
                        "Study",
                        "In Progress"
                    ), 3, "TUE"
                ),
                WeeklyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        4,
                        "Morning workout routine",
                        "06:00 AM",
                        "High",
                        "Work",
                        "Hold On"
                    ), 3, "TUE"
                ),
                WeeklyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        5,
                        "Client presentation",
                        "15:00",
                        "High",
                        "Work",
                        "To do"
                    ),
                    4,
                    "WED"
                ),
                WeeklyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        6,
                        "Code review session",
                        "11:00",
                        "Medium",
                        "Work",
                        "Hold On"
                    ),
                    5,
                    "THU"
                ),
                WeeklyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        7,
                        "Team lunch",
                        "12:30",
                        "Low",
                        "Work",
                        "To do"
                    ), 5, "THU")
            )
        )
    }

    var swipedTaskId by remember { mutableStateOf<Int?>(null) }
    var lastCompletedTask by remember { mutableStateOf<WeeklyTaskItem?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }
    var lastDeletedTask by remember { mutableStateOf<WeeklyTaskItem?>(null) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<WeeklyTaskItem?>(null) }
    var deletingTask by remember { mutableStateOf<WeeklyTaskItem?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Pisahkan task aktif dan selesai
    val activeTasks = weeklyTasks.filter { !it.task.isCompleted }
    val completedTasks = weeklyTasks.filter { it.task.isCompleted }

    // Filter tasks untuk selected day
    val filteredActiveTasks = activeTasks.filter { weeklyTask ->
        val task = weeklyTask.task
        val dayMatch = weeklyTask.dayOfMonth == selectedDayNumber
        val statusMatch = when (selectedStatus) {
            "All" -> true
            else -> task.status == selectedStatus
        }
        val categoryMatch = when (selectedCategory) {
            "All" -> true
            else -> task.category == selectedCategory
        }
        dayMatch && statusMatch && categoryMatch
    }

    // Functions
    fun onCheckboxClick(weeklyTask: WeeklyTaskItem) {
        if (!weeklyTask.task.isCompleted) {
            weeklyTasks = weeklyTasks.map {
                if (it.task.id == weeklyTask.task.id) {
                    it.copy(task = it.task.copy(isCompleted = true))
                } else {
                    it
                }
            }
            lastCompletedTask = weeklyTask
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
        lastCompletedTask?.let { wt ->
            weeklyTasks = weeklyTasks.map {
                if (it.task.id == wt.task.id) {
                    it.copy(task = it.task.copy(isCompleted = false))
                } else {
                    it
                }
            }
        }
        showUndoSnackbar = false
        lastCompletedTask = null
    }

    fun deleteTaskConfirmed(weeklyTask: WeeklyTaskItem) {
        lastDeletedTask = weeklyTask
        weeklyTasks = weeklyTasks.filter { it.task.id != weeklyTask.task.id }
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
        lastDeletedTask?.let { wt ->
            weeklyTasks = (weeklyTasks + wt).sortedBy { it.task.id }
        }
        showDeleteSnackbar = false
        lastDeletedTask = null
    }

    fun showRestoreConfirmation(weeklyTask: WeeklyTaskItem) {
        taskToRestore = weeklyTask
        showRestoreDialog = true
        swipedTaskId = null
    }

    fun restoreTask() {
        taskToRestore?.let { wt ->
            weeklyTasks = weeklyTasks.map {
                if (it.task.id == wt.task.id) {
                    it.copy(task = it.task.copy(isCompleted = false))
                } else {
                    it
                }
            }
        }
        showRestoreDialog = false
        taskToRestore = null
    }

    // ✨ CONDITIONAL RENDERING
    when {
        showDetailScreen && selectedTaskForDetail != null -> {
            TaskDetailScreen(
                task = selectedTaskForDetail!!,
                onBackClick = {
                    showDetailScreen = false
                    selectedTaskForDetail = null
                },
                onEditClick = {
                    // TODO: Handle edit
                },
                onDeleteClick = {
                    showDetailScreen = false
                    selectedTaskForDetail = null
                },
                onSaveChanges = {
                    showDetailScreen = false
                    selectedTaskForDetail = null
                }
            )
        }

        showCreateTaskScreen -> {
            CreateTaskScreen(
                onBackClick = { showCreateTaskScreen = false },
                onSaveClick = { newTask ->
                    // Tambahkan task baru ke list weeklyTasks (misalnya ke hari yang dipilih)
                    weeklyTasks = weeklyTasks + WeeklyTaskItem(
                        task = newTask,
                        dayOfMonth = selectedDayNumber,
                        dayName = weekDays.firstOrNull { it.dayNumber == selectedDayNumber }?.dayName
                            ?: ""
                    )
                    showCreateTaskScreen = false
                }
            )
        }

        else -> {

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
                                IconButton(onClick = onBackClick) {
                                    Icon(
                                        imageVector = Icons.Filled.ArrowBack,
                                        contentDescription = "Back",
                                        tint = Color(0xFF2D2D2D)
                                    )
                                }
                            },
                            actions = {
                                IconButton(onClick = { /* Search action */ }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF2D2D2D)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White
                            )
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(
                            selectedTab = selectedTab,
                            onTabSelected = { index ->
                                when (index) {
                                    NavigationConstants.TAB_HOME -> onNavigateToHome()
                                    NavigationConstants.TAB_TASK -> { /* sudah di Task */
                                    }

                                    NavigationConstants.TAB_NOTE -> onNavigateToNotes()
                                    NavigationConstants.TAB_TEAM -> onNavigateToTeam()
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showCreateTaskScreen = true },
                            containerColor = Color(0xFF6A70D7),
                            contentColor = Color.White,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Create Task",
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                ) { paddingValues ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // View Mode Navigation
                        TaskViewModeNavigation(
                            selectedViewMode = selectedViewMode,
                            onViewModeChange = { selectedViewMode = it },
                            onNavigateToList = onNavigateToList,
                            onNavigateToDaily = onNavigateToDaily,
                            onNavigateToMonthly = onNavigateToMonthly
                        )

                        // Filter Row
                        TaskFilterRow(
                            selectedStatus = selectedStatus,
                            onStatusSelected = { selectedStatus = it },
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it },
                            categories = listOf("All", "Work", "Study", "Project")
                        )

                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                        // Week Calendar Strip
                        LazyRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            items(weekDays) { day ->
                                WeekDayCard(
                                    day = day,
                                    isSelected = day.dayNumber == selectedDayNumber,
                                    onClick = { selectedDayNumber = day.dayNumber }
                                )
                            }
                        }

                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                        // Task List for selected day
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredActiveTasks, key = { it.task.id }) { weeklyTask ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    TaskCardWithStrip(
                                        task = weeklyTask.task,
                                        onTaskClick = { handleTaskClick(weeklyTask.task) },
                                        onCheckboxClick = { onCheckboxClick(weeklyTask) },
                                        onDeleteIconClick = {
                                            deletingTask = weeklyTask
                                            showDeleteConfirmDialog = true
                                        },
                                        swipedTaskId = swipedTaskId,
                                        onSwipeChange = { id, isSwiped ->
                                            swipedTaskId = if (isSwiped) id else null
                                        }
                                    )
                                }
                            }

                            // Completed Tasks
                            if (completedTasks.filter { it.dayOfMonth == selectedDayNumber }
                                    .isNotEmpty()) {
                                item {
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        "Completed (${completedTasks.filter { it.dayOfMonth == selectedDayNumber }.size})",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                                items(
                                    completedTasks.filter { it.dayOfMonth == selectedDayNumber },
                                    key = { it.task.id }
                                ) { weeklyTask ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        TaskCardWithStrip(
                                            task = weeklyTask.task,
                                            onTaskClick = { onTaskClick(weeklyTask.task) },
                                            onCheckboxClick = { showRestoreConfirmation(weeklyTask) },
                                            onDeleteIconClick = {
                                                deletingTask = weeklyTask
                                                showDeleteConfirmDialog = true
                                            },
                                            isCompleted = true,
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

                // Undo Snackbar (completion)
                AnimatedVisibility(
                    visible = showUndoSnackbar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Task completed", color = Color.White, fontSize = 14.sp)
                            }
                            TextButton(
                                onClick = { undoCompletion() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "UNDO",
                                    color = Color(0xFF6A70D7),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Undo Delete Snackbar
                AnimatedVisibility(
                    visible = showDeleteSnackbar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = null,
                                    tint = Color(0xFFF44336),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Text("Task deleted", color = Color.White, fontSize = 14.sp)
                            }
                            TextButton(
                                onClick = { undoDelete() },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "UNDO",
                                    color = Color(0xFF6A70D7),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Restore Dialog
                if (showRestoreDialog) {
                    AlertDialog(
                        onDismissRequest = { showRestoreDialog = false; taskToRestore = null },
                        title = { Text("Restore Task?") },
                        text = { Text("Do you want to move this task back to active tasks?") },
                        confirmButton = {
                            TextButton(onClick = { restoreTask() }) { Text("Yes") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showRestoreDialog = false; taskToRestore = null
                            }) {
                                Text("Cancel")
                            }
                        }
                    )
                }

                // Delete Confirmation Dialog
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
                            }) {
                                Text("Hapus", color = Color(0xFFF44336))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDeleteConfirmDialog = false
                                deletingTask = null
                            }) { Text("Batal") }
                        }
                    )
                }
            }
        }
    }
}
    @Composable
    private fun WeekDayCard(
        day: CalendarDay,
        isSelected: Boolean,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        Surface(
            modifier = modifier
                .width(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onClick),
            color = if (isSelected) Color(0xFF6A70D7) else Color(0xFFF5F7FA),
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
                    color = if (isSelected) Color.White else Color(0xFF9E9E9E)
                )
                Text(
                    text = day.dayNumber.toString(),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else Color(0xFF2D2D2D)
                )
                Text(
                    text = "${day.taskCount} Task",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Normal,
                    color = if (isSelected) Color.White.copy(alpha = 0.8f) else Color(0xFF9E9E9E)
                )
            }
        }
    }

    @Composable
    private fun TaskCardWithStrip(
        task: com.example.yourassistantyora.screen.Task,
        onTaskClick: () -> Unit,
        onCheckboxClick: () -> Unit,
        onDeleteIconClick: () -> Unit,
        modifier: Modifier = Modifier,
        isCompleted: Boolean = false,
        swipedTaskId: Int? = null,
        onSwipeChange: (Int, Boolean) -> Unit = { _, _ -> }
    ) {
        val deleteWidth = 80.dp
        val density = LocalDensity.current
        val scope = rememberCoroutineScope()
        val deleteOffset = remember { Animatable(0f) }

        // Strip color based on priority
        val stripColor = when (task.priority) {
            "High" -> Color(0xFFEF5350)
            "Medium" -> Color(0xFFFFB74D)
            "Low" -> Color(0xFF64B5F6)
            else -> Color(0xFF64B5F6)
        }.copy(alpha = if (isCompleted) 0.4f else 0.9f)

        val titleColor = Color(0xFF2D2D2D).copy(alpha = if (isCompleted) 0.5f else 1f)
        val secondaryTextColor = Color(0xFF9E9E9E).copy(alpha = if (isCompleted) 0.5f else 1f)

        LaunchedEffect(swipedTaskId) {
            val deleteWidthPx = with(density) { deleteWidth.toPx() }
            val isOpen = deleteOffset.value < 0f
            if (swipedTaskId != null && swipedTaskId != task.id && isOpen) {
                deleteOffset.animateTo(0f, tween(300))
            } else if (swipedTaskId == null && isOpen) {
                deleteOffset.animateTo(0f, tween(300))
            }
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    val deleteWidthPx = with(density) { deleteWidth.toPx() }
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            scope.launch {
                                val target =
                                    if (deleteOffset.value < -deleteWidthPx / 2) -deleteWidthPx else 0f
                                deleteOffset.animateTo(target, tween(300))
                                onSwipeChange(task.id, target != 0f)
                            }
                        },
                        onDragCancel = {
                            scope.launch { deleteOffset.snapTo(0f); onSwipeChange(task.id, false) }
                        },
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset =
                                (deleteOffset.value + dragAmount).coerceIn(-deleteWidthPx, 0f)
                            scope.launch { deleteOffset.snapTo(newOffset) }
                        }
                    )
                }
        ) {
            // Background swipe (tombol hapus)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF44336))
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(deleteWidth)
                        .fillMaxHeight()
                        .clickable(onClick = onDeleteIconClick),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Hapus",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Hapus",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Card dengan strip kiri
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(deleteOffset.value.roundToInt(), 0) }
                    .zIndex(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onTaskClick),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Strip kiri
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .fillMaxHeight()
                            .background(stripColor)
                    )

                    // Checkbox
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { onCheckboxClick() },
                        colors = CheckboxDefaults.colors(
                            checkedColor = Color(0xFF6A70D7),
                            uncheckedColor = Color(0xFFB0B0B0),
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier
                            .size(24.dp)
                            .padding(start = 12.dp)
                    )

                    Spacer(Modifier.width(12.dp))

                    // Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 16.dp, horizontal = 8.dp)
                    ) {
                        Text(
                            text = task.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = titleColor,
                            maxLines = 2
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.AccessTime,
                                    contentDescription = null,
                                    tint = secondaryTextColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(task.time, fontSize = 12.sp, color = secondaryTextColor)
                            }

                            BadgeChip(
                                text = task.priority,
                                backgroundColor = when (task.priority) {
                                    "High" -> Color(0xFFFFEBEE)
                                    "Medium" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE3F2FD)
                                }.copy(alpha = if (isCompleted) 0.3f else 1f),
                                textColor = when (task.priority) {
                                    "High" -> Color(0xFFD32F2F)
                                    "Medium" -> Color(0xFFEF6C00)
                                    else -> Color(0xFF1976D2)
                                }.copy(alpha = if (isCompleted) 0.6f else 1f)
                            )

                            BadgeChip(
                                text = task.category,
                                backgroundColor = Color(0xFFE8EAF6).copy(alpha = if (isCompleted) 0.3f else 1f),
                                textColor = Color(0xFF3949AB).copy(alpha = if (isCompleted) 0.6f else 1f)
                            )

                            task.status?.let { status ->
                                BadgeChip(
                                    text = status,
                                    backgroundColor = when (status) {
                                        "Waiting" -> Color(0xFFF3E5F5)
                                        "To do" -> Color(0xFFE3F2FD)
                                        "Hold On" -> Color(0xFFFFF3E0)
                                        "In Progress" -> Color(0xFFE0F2F1)
                                        "Done" -> Color(0xFFE8F5E8)
                                        else -> Color(0xFFF5F5F5)
                                    }.copy(alpha = if (isCompleted) 0.3f else 1f),
                                    textColor = when (status) {
                                        "Waiting" -> Color(0xFF6A1B9A)
                                        "To do" -> Color(0xFF1976D2)
                                        "Hold On" -> Color(0xFFEF6C00)
                                        "In Progress" -> Color(0xFF00695C)
                                        "Done" -> Color(0xFF2E7D32)
                                        else -> Color(0xFF616161)
                                    }.copy(alpha = if (isCompleted) 0.6f else 1f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(12.dp))
                }
            }
        }
    }



// BadgeChip sudah didefinisikan di TaskScreen.kt