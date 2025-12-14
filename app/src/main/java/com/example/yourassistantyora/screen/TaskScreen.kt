package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.yourassistantyora.models.categoryNamesSafe
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.shape.RoundedCornerShape

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TaskScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    val tasks by viewModel.listTasks.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }
    var swipedTaskId by remember { mutableStateOf<String?>(null) }

    var lastCompletedTask by remember { mutableStateOf<TaskModel?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    var lastDeletedTask by remember { mutableStateOf<TaskModel?>(null) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }

    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<TaskModel?>(null) }

    var deletingTask by remember { mutableStateOf<TaskModel?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    val filteredActiveTasks = activeTasks.filter { task ->
        val statusMatch = (selectedStatus == "All") || (task.statusText == selectedStatus)
        val categoryMatch = (selectedCategory == "All") || task.categoryNamesSafe.contains(selectedCategory)
        statusMatch && categoryMatch
    }

    fun completeTask(task: TaskModel) {
        if (!task.isCompleted) {
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
        showDeleteSnackbar = false
        lastDeletedTask = null
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
                        IconButton(onClick = { /* TODO Search */ }) {
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
                ) { Icon(Icons.Filled.Add, "Create Task", modifier = Modifier.size(28.dp)) }
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                TaskViewModeNavigation(
                    selectedViewMode = "List",
                    onViewModeChange = { },
                    onNavigateToDaily = { navController.navigateSingleTop("daily_tasks") },
                    onNavigateToWeekly = { navController.navigateSingleTop("weekly_tasks") },
                    onNavigateToMonthly = { navController.navigateSingleTop("monthly_tasks") }
                )

                TaskFilterRow(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { viewModel.setStatusFilter(it) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    categories = listOf("All", "Work", "Study", "Travel", "Meeting", "Project", "Personal")
                )

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
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
                                    onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                    onCheckboxClick = { checked -> if (checked) completeTask(task) },
                                    onDeleteIconClick = {
                                        deletingTask = task
                                        showDeleteConfirmDialog = true
                                    },
                                    isCompleted = false,
                                    swipedTaskId = swipedTaskId,
                                    onSwipeChange = { id, isSwiped -> swipedTaskId = if (isSwiped) id else null }
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
                                        onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                        onCheckboxClick = { checked -> if (!checked) showRestoreConfirmation(task) },
                                        onDeleteIconClick = {
                                            deletingTask = task
                                            showDeleteConfirmDialog = true
                                        },
                                        isCompleted = true,
                                        swipedTaskId = swipedTaskId,
                                        onSwipeChange = { id, isSwiped -> swipedTaskId = if (isSwiped) id else null }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // SNACKBAR complete
        AnimatedVisibility(
            visible = showUndoSnackbar,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 20.dp, end = 20.dp)
        ) {
            SnackbarCard(
                icon = { Icon(Icons.Filled.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp)) },
                text = "Task completed",
                actionText = "UNDO",
                onAction = { undoCompletion() }
            )
        }

        // SNACKBAR delete
        AnimatedVisibility(
            visible = showDeleteSnackbar,
            enter = androidx.compose.animation.slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = androidx.compose.animation.slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 20.dp, end = 20.dp)
        ) {
            SnackbarCard(
                icon = { Icon(Icons.Filled.Delete, null, tint = Color(0xFFF44336), modifier = Modifier.size(20.dp)) },
                text = "Task deleted",
                actionText = "UNDO",
                onAction = { undoDelete() }
            )
        }

        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false; taskToRestore = null },
                title = { Text("Restore Task?") },
                text = { Text("Do you want to move this task back to active tasks?") },
                confirmButton = { TextButton(onClick = { restoreTask() }) { Text("Yes") } },
                dismissButton = { TextButton(onClick = { showRestoreDialog = false; taskToRestore = null }) { Text("Cancel") } }
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
                    }) { Text("Hapus", color = Color(0xFFF44336)) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false; deletingTask = null }) { Text("Batal") }
                }
            )
        }
    }
}

@Composable
private fun SnackbarCard(
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
            TextButton(onClick = onAction, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)) {
                Text(actionText, color = Color(0xFF6A70D7), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}