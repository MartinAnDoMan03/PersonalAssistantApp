package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Search
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
import androidx.navigation.NavController
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.components.TaskFilterRow
import com.example.yourassistantyora.components.TaskViewModeNavigation
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.NavigationConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ---------- DATA CLASS TAMBAHAN UNTUK WEEKLY ----------
data class WeeklyTaskItem(
    val task: Task,
    val dayOfMonth: Int,
    val dayName: String
)

data class CalendarDay(
    val dayName: String,
    val dayNumber: Int,
    val taskCount: Int
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun WeeklyScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var selectedViewMode by remember { mutableStateOf("Weekly") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }

    // State untuk selected day
    var selectedDayNumber by remember { mutableStateOf(3) } // default TUE 3

    // Calendar strip dummy
    val weekDays = remember {
        listOf(
            CalendarDay("SUN", 1, 2),
            CalendarDay("MON", 2, 1),
            CalendarDay("TUE", 3, 4),
            CalendarDay("WED", 4, 2),
            CalendarDay("THU", 5, 2),
            CalendarDay("FRI", 6, 2),
            CalendarDay("SAT", 7, 2)
        )
    }

    // Weekly tasks dummy
    var weeklyTasks by remember {
        mutableStateOf(
            listOf(
                WeeklyTaskItem(
                    Task(
                        1,
                        "Team meeting preparation",
                        "06:00",
                        "High",
                        "Work",
                        "Waiting"
                    ), 3, "TUE"
                ),
                WeeklyTaskItem(
                    Task(
                        2,
                        "Review design mockups",
                        "08:00",
                        "Medium",
                        "Work",
                        "To do"
                    ), 3, "TUE"
                ),
                WeeklyTaskItem(
                    Task(
                        3,
                        "Submit project report",
                        "14:30",
                        "Low",
                        "Study",
                        "In Progress"
                    ), 3, "TUE"
                ),
                WeeklyTaskItem(
                    Task(
                        4,
                        "Morning workout routine",
                        "06:00",
                        "High",
                        "Personal",
                        "Hold On"
                    ), 3, "TUE"
                ),
                WeeklyTaskItem(
                    Task(
                        5,
                        "Client presentation",
                        "15:00",
                        "High",
                        "Work",
                        "To do"
                    ), 4, "WED"
                ),
                WeeklyTaskItem(
                    Task(
                        6,
                        "Code review session",
                        "11:00",
                        "Medium",
                        "Work",
                        "Hold On"
                    ), 5, "THU"
                ),
                WeeklyTaskItem(
                    Task(
                        7,
                        "Team lunch",
                        "12:30",
                        "Low",
                        "Work",
                        "To do"
                    ), 5, "THU"
                )
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

    val activeTasks = weeklyTasks.filter { !it.task.isCompleted }
    val completedTasks = weeklyTasks.filter { it.task.isCompleted }

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

    fun onCheckboxClick(weeklyTask: WeeklyTaskItem) {
        if (!weeklyTask.task.isCompleted) {
            weeklyTasks = weeklyTasks.map {
                if (it.task.id == weeklyTask.task.id) {
                    it.copy(task = it.task.copy(isCompleted = true))
                } else it
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
                } else it
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
                } else it
            }
        }
        showRestoreDialog = false
        taskToRestore = null
    }

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
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF2D2D2D)
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* TODO: search */ }) {
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
                        selectedTab = index
                        when (index) {
                            NavigationConstants.TAB_HOME -> {
                                navController.navigateSingleTop("home")
                            }
                            NavigationConstants.TAB_TASK -> {
                                navController.navigateSingleTop("task_list")
                            }
                            NavigationConstants.TAB_NOTE -> {
                                navController.navigateSingleTop("notes")
                            }
                            NavigationConstants.TAB_TEAM -> {
                                navController.navigateSingleTop("team")
                            }
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
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Task",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // View mode (List / Daily / Weekly / Monthly)
                TaskViewModeNavigation(
                    selectedViewMode = selectedViewMode,
                    onViewModeChange = { selectedViewMode = it },
                    onNavigateToDaily = { navController.navigateSingleTop("daily_tasks") },
                    onNavigateToWeekly = { navController.navigateSingleTop("weekly_tasks") },
                    onNavigateToMonthly = { navController.navigateSingleTop("monthly_tasks") },
                    onNavigateToList = { navController.navigateSingleTop("task_list") }
                )

                TaskFilterRow(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = it },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    categories = listOf("All", "Work", "Study", "Project")
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
                    items(weekDays) { day ->
                        WeekDayCard(
                            day = day,
                            isSelected = day.dayNumber == selectedDayNumber,
                            onClick = { selectedDayNumber = day.dayNumber }
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // List task untuk hari terpilih
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
                                onTaskClick = {
                                    navController.navigate("task_detail/${weeklyTask.task.id}")
                                },
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

                    // Completed tasks untuk hari terpilih
                    val completedForDay =
                        completedTasks.filter { it.dayOfMonth == selectedDayNumber }

                    if (completedForDay.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Completed (${completedForDay.size})",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                        items(completedForDay, key = { it.task.id }) { weeklyTask ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                TaskCardWithStrip(
                                    task = weeklyTask.task,
                                    onTaskClick = {
                                        navController.navigate("task_detail/${weeklyTask.task.id}")
                                    },
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

        // ---------- SNACKBAR: UNDO COMPLETE ----------
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

        // ---------- SNACKBAR: UNDO DELETE ----------
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

        // ---------- DIALOG RESTORE ----------
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false; taskToRestore = null },
                title = { Text("Restore Task?") },
                text = { Text("Do you want to move this task back to active tasks?") },
                confirmButton = {
                    TextButton(onClick = { restoreTask() }) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false; taskToRestore = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ---------- DIALOG DELETE ----------
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

// ---------- WEEKDAY CARD ----------
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

// ---------- CARD TASK DENGAN STRIP KIRI ----------
@Composable
private fun TaskCardWithStrip(
    task: Task,
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
                        scope.launch {
                            deleteOffset.snapTo(0f)
                            onSwipeChange(task.id, false)
                        }
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
        // Background merah untuk delete
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

        // Card task
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
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .fillMaxHeight()
                        .background(stripColor)
                )

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
                                imageVector = Icons.Outlined.AccessTime,
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
