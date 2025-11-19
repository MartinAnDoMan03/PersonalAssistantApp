package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.components.TaskFilterRow
import com.example.yourassistantyora.components.TaskViewModeNavigation
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.NavigationConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun DailyScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Team meeting preparation", "10:00", "High", "Work", "Waiting"),
                Task(2, "Review design mockups", "10:45", "Medium", "Work", "To do"),
                Task(3, "Submit project report", "14:30", "Low", "Study", "In Progress"),
                Task(4, "Client presentation", "16:00", "High", "Work", "To do"),
                Task(5, "Code review session", "19:30", "Medium", "Work", "Hold On"),
                Task(6, "Evening workout", "20:00", "Low", "Personal", "To do")
            )
        )
    }

    var selectedViewMode by remember { mutableStateOf("Daily") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }

    var swipedTaskId by remember { mutableStateOf<Int?>(null) }
    var lastCompletedTask by remember { mutableStateOf<Task?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    var lastDeletedTask by remember { mutableStateOf<Task?>(null) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<Task?>(null) }

    var deletingTask by remember { mutableStateOf<Task?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    val filteredActiveTasks = activeTasks.filter { task ->
        val statusMatch = when (selectedStatus) {
            "All" -> true
            else -> task.status == selectedStatus
        }
        val categoryMatch = when (selectedCategory) {
            "All" -> true
            else -> task.category == selectedCategory
        }
        statusMatch && categoryMatch
    }

    val groupedTasks = groupTasksByTimePeriod(filteredActiveTasks)

    fun onCheckboxClick(task: Task) {
        if (!task.isCompleted) {
            tasks = tasks.map {
                if (it.id == task.id) it.copy(isCompleted = true) else it
            }
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
        lastCompletedTask?.let { t ->
            tasks = tasks.map { if (it.id == t.id) it.copy(isCompleted = false) else it }
        }
        showUndoSnackbar = false
        lastCompletedTask = null
    }

    fun deleteTaskConfirmed(task: Task) {
        lastDeletedTask = task
        tasks = tasks.filter { it.id != task.id }
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
        lastDeletedTask?.let { t ->
            tasks = (tasks + t).sortedBy { it.id }
        }
        showDeleteSnackbar = false
        lastDeletedTask = null
    }

    fun showRestoreConfirmation(task: Task) {
        taskToRestore = task
        showRestoreDialog = true
        swipedTaskId = null
    }

    fun restoreTask() {
        taskToRestore?.let { t ->
            tasks = tasks.map { if (it.id == t.id) it.copy(isCompleted = false) else it }
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
                        Column {
                            Text(
                                "Today's Tasks",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D2D2D)
                            )
                            Text(
                                getCurrentDate(),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF9E9E9E)
                            )
                        }
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
                TaskViewModeNavigation(
                    selectedViewMode = selectedViewMode,
                    onViewModeChange = { selectedViewMode = it },
                    onNavigateToDaily = { navController.navigateSingleTop("daily_tasks") },
                    onNavigateToWeekly = { navController.navigateSingleTop("weekly_tasks") },
                    onNavigateToMonthly = { navController.navigateSingleTop("monthly_tasks") }
                )

                TaskFilterRow(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { selectedStatus = it },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { selectedCategory = it },
                    categories = listOf("All", "Work", "Study", "Project")
                )

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (groupedTasks["Morning"]?.isNotEmpty() == true) {
                        item {
                            TimePeriodHeader(
                                title = "Morning",
                                timeRange = "05:00 - 10:59",
                                taskCount = groupedTasks["Morning"]?.size ?: 0
                            )
                        }
                        items(groupedTasks["Morning"] ?: emptyList(), key = { it.id }) { task ->
                            TaskCardDesignStyle(
                                task = task,
                                onTaskClick = {
                                    navController.navigate("task_detail/${task.id}")
                                },
                                onCheckboxClick = { onCheckboxClick(task) },
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

                    if (groupedTasks["Afternoon"]?.isNotEmpty() == true) {
                        item {
                            TimePeriodHeader(
                                title = "Afternoon",
                                timeRange = "11:00 - 15:00",
                                taskCount = groupedTasks["Afternoon"]?.size ?: 0
                            )
                        }
                        items(groupedTasks["Afternoon"] ?: emptyList(), key = { it.id }) { task ->
                            TaskCardDesignStyle(
                                task = task,
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onCheckboxClick = { onCheckboxClick(task) },
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

                    if (groupedTasks["Evening"]?.isNotEmpty() == true) {
                        item {
                            TimePeriodHeader(
                                title = "Evening",
                                timeRange = "15:00 - 18:00",
                                taskCount = groupedTasks["Evening"]?.size ?: 0
                            )
                        }
                        items(groupedTasks["Evening"] ?: emptyList(), key = { it.id }) { task ->
                            TaskCardDesignStyle(
                                task = task,
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onCheckboxClick = { onCheckboxClick(task) },
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

                    if (groupedTasks["Night"]?.isNotEmpty() == true) {
                        item {
                            TimePeriodHeader(
                                title = "Night",
                                timeRange = "19:00 - 21:00",
                                taskCount = groupedTasks["Night"]?.size ?: 0
                            )
                        }
                        items(groupedTasks["Night"] ?: emptyList(), key = { it.id }) { task ->
                            TaskCardDesignStyle(
                                task = task,
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onCheckboxClick = { onCheckboxClick(task) },
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
                        items(completedTasks, key = { it.id }) { task ->
                            TaskCardDesignStyle(
                                task = task,
                                onTaskClick = {
                                    navController.navigate("task_detail/${task.id}")
                                },
                                onCheckboxClick = { showRestoreConfirmation(task) },
                                onDeleteIconClick = {
                                    deletingTask = task
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
                    }) {
                        Text("Hapus", color = Color(0xFFF44336))
                    }
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

// ---------- HEADER PER WAKTU ----------
@Composable
fun TimePeriodHeader(
    title: String,
    timeRange: String,
    taskCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )
            Text(
                text = timeRange,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF9E9E9E)
            )
        }
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF6A70D7).copy(alpha = 0.1f)
        ) {
            Text(
                text = "$taskCount ${if (taskCount == 1) "task" else "tasks"}",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6A70D7)
            )
        }
    }
}

// ---------- GROUPING WAKTU ----------
fun groupTasksByTimePeriod(tasks: List<Task>): Map<String, List<Task>> {
    return tasks.groupBy { task ->
        val hour = task.time.split(":")[0].toIntOrNull() ?: 0
        when (hour) {
            in 5..10 -> "Morning"
            in 11..14 -> "Afternoon"
            in 15..18 -> "Evening"
            in 19..21 -> "Night"
            else -> "Other"
        }
    }.filterKeys { it != "Other" }
}

// ---------- TANGGAL HARI INI ----------
fun getCurrentDate(): String {
    val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
    return sdf.format(Date())
}
