package com.example.yourassistantyora

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.components.TaskViewModeNavigation
import com.example.yourassistantyora.components.TaskFilterRow
import com.example.yourassistantyora.screen.CreateTaskScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

// ---------- TASK SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onTaskClick: (com.example.yourassistantyora.screen.Task) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToTeam: () -> Unit = {},
    onNavigateToDaily: () -> Unit = {},
    onNavigateToWeekly: () -> Unit = {},
    onNavigateToMonthly: () -> Unit = {}
) {
    // ✨ STATE UNTUK DETAIL SCREEN
    var showDetailScreen by remember { mutableStateOf(false) }
    var selectedTaskForDetail by remember { mutableStateOf<com.example.yourassistantyora.screen.Task?>(null) }

    // ✨ STATE UNTUK CREATE TASK SCREEN
    var showCreateTaskScreen by remember { mutableStateOf(false) }

    var tasks by remember {
        mutableStateOf(
            listOf(
                _root_ide_package_.com.example.yourassistantyora.screen.Task(
                    1,
                    "Team meeting preparation",
                    "10:00 AM",
                    "High",
                    "Work",
                    "Waiting"
                ),
                _root_ide_package_.com.example.yourassistantyora.screen.Task(
                    2,
                    "Review design mockups",
                    "10:00 AM",
                    "Medium",
                    "Work",
                    "To do"
                ),
                _root_ide_package_.com.example.yourassistantyora.screen.Task(
                    3,
                    "Submit project report",
                    "03:00 AM",
                    "Low",
                    "Study",
                    "In Progress"
                ),
                _root_ide_package_.com.example.yourassistantyora.screen.Task(
                    4,
                    "Morning workout routine",
                    "06:00 AM",
                    "High",
                    "Work",
                    "Hold On"
                )
            )
        )
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
                    // Tambahkan task baru ke list
                    tasks = tasks + newTask
                    showCreateTaskScreen = false
                }
            )
        }
        else -> {
            // EXISTING CONTENT
            var selectedViewMode by remember { mutableStateOf("List") }
            var selectedStatus by remember { mutableStateOf("All") }
            var selectedCategory by remember { mutableStateOf("All") }
            val scope = rememberCoroutineScope()
            val selectedTab = NavigationConstants.TAB_TASK



            var swipedTaskId by remember { mutableStateOf<Int?>(null) }
            var lastCompletedTask by remember { mutableStateOf<com.example.yourassistantyora.screen.Task?>(null) }
            var showUndoSnackbar by remember { mutableStateOf(false) }
            var lastDeletedTask by remember { mutableStateOf<com.example.yourassistantyora.screen.Task?>(null) }
            var showDeleteSnackbar by remember { mutableStateOf(false) }
            var showRestoreDialog by remember { mutableStateOf(false) }
            var taskToRestore by remember { mutableStateOf<com.example.yourassistantyora.screen.Task?>(null) }
            var deletingTask by remember { mutableStateOf<com.example.yourassistantyora.screen.Task?>(null) }
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

            fun onCheckboxClick(task: com.example.yourassistantyora.screen.Task) {
                if (!task.isCompleted) {
                    tasks = tasks.map {
                        if (it.id == task.id) {
                            it.copy(isCompleted = true)
                        } else {
                            it
                        }
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

            fun deleteTaskConfirmed(task: com.example.yourassistantyora.screen.Task) {
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

            fun showRestoreConfirmation(task: com.example.yourassistantyora.screen.Task) {
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
                                    NavigationConstants.TAB_TASK -> { /* sudah di Task */ }
                                    NavigationConstants.TAB_NOTE -> onNavigateToNotes()
                                    NavigationConstants.TAB_TEAM -> onNavigateToTeam()
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showCreateTaskScreen = true }, // ✅ UBAH INI
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
                            onNavigateToDaily = onNavigateToDaily,
                            onNavigateToWeekly = onNavigateToWeekly,
                            onNavigateToMonthly = onNavigateToMonthly
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
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredActiveTasks, key = { it.id }) { task ->
                                AnimatedVisibility(
                                    visible = true,
                                    enter = fadeIn() + expandVertically(),
                                    exit = fadeOut() + shrinkVertically()
                                ) {
                                    TaskCardDesignStyle(
                                        task = task,
                                        onTaskClick = {
                                            selectedTaskForDetail = task
                                            showDetailScreen = true
                                        },
                                        onCheckboxClick = { onCheckboxClick(task) },
                                        onDeleteIconClick = {
                                            deletingTask = task
                                            showDeleteConfirmDialog = true
                                        },
                                        swipedTaskId = swipedTaskId,
                                        onSwipeChange = { id, isSwiped ->
                                            if (isSwiped) {
                                                swipedTaskId = id
                                            } else if (swipedTaskId == id) {
                                                swipedTaskId = null
                                            }
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
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = fadeIn() + expandVertically(),
                                        exit = fadeOut() + shrinkVertically()
                                    ) {
                                        TaskCardDesignStyle(
                                            task = task,
                                            onTaskClick = {
                                                selectedTaskForDetail = task
                                                showDetailScreen = true
                                            },
                                            onCheckboxClick = { showRestoreConfirmation(task) },
                                            onDeleteIconClick = {
                                                deletingTask = task
                                                showDeleteConfirmDialog = true
                                            },
                                            isCompleted = true,
                                            swipedTaskId = swipedTaskId,
                                            onSwipeChange = { id, isSwiped ->
                                                if (isSwiped) {
                                                    swipedTaskId = id
                                                } else if (swipedTaskId == id) {
                                                    swipedTaskId = null
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Undo Snackbar
                AnimatedVisibility(
                    visible = showUndoSnackbar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 20.dp, end = 20.dp)
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
                                Text(
                                    "Task completed",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
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
                        .padding(bottom = 80.dp, start = 20.dp, end = 20.dp)
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
                                Text(
                                    "Task deleted",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
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

                if (showRestoreDialog) {
                    AlertDialog(
                        onDismissRequest = { showRestoreDialog = false; taskToRestore = null },
                        title = { Text("Restore Task?") },
                        text = { Text("Do you want to move this task back to active tasks?") },
                        confirmButton = {
                            TextButton(onClick = { restoreTask() }) { Text("Yes") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showRestoreDialog = false; taskToRestore = null }) { Text("Cancel") }
                        }
                    )
                }

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
                            TextButton(onClick = { showDeleteConfirmDialog = false; deletingTask = null }) { Text("Batal") }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun TaskCardDesignStyle(
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
                            val target = if (deleteOffset.value < -deleteWidthPx / 2) -deleteWidthPx else 0f
                            deleteOffset.animateTo(target, tween(300))
                            onSwipeChange(task.id, target != 0f)
                        }
                    },
                    onDragCancel = {
                        scope.launch { deleteOffset.snapTo(0f); onSwipeChange(task.id, false) }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (deleteOffset.value + dragAmount).coerceIn(-deleteWidthPx, 0f)
                        scope.launch { deleteOffset.snapTo(newOffset) }
                    }
                )
            }
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(stripColor)
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

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(deleteOffset.value.roundToInt(), 0) }
                .zIndex(1f)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onTaskClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(stripColor)
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp)
                ) {
                    Text(
                        text = task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = secondaryTextColor,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(task.time, fontSize = 11.sp, color = secondaryTextColor)
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

                Spacer(Modifier.width(8.dp))

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
                        .padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
fun BadgeChip(
    text: String,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = backgroundColor,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = textColor,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}