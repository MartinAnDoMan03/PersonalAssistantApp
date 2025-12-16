package com.example.yourassistantyora.screen

import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.components.*
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.DailySession
import com.example.yourassistantyora.viewModel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    // ---------- STATE FROM VM ----------
    val isLoading by viewModel.isLoading.collectAsState(false)
    val error by viewModel.error.collectAsState(null)
    val selectedStatus by viewModel.selectedStatus.collectAsState("All")
    val selectedCategory by viewModel.selectedCategory.collectAsState("All")
    val tasksForToday by viewModel.tasksForToday.collectAsState(emptyList())

    // ---------- UI STATE ----------
    var isSearching by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }
    var swipedTaskId by remember { mutableStateOf<String?>(null) }

    // snackbar complete
    var lastCompletedTask by remember { mutableStateOf<TaskModel?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    // restore dialog
    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<TaskModel?>(null) }

    // delete dialog
    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }

    fun isCompleted(task: TaskModel) = task.Status == 2

    // ---------- COMPLETE / UNDO (SAMA TASKSCREEN) ----------
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

    // ---------- RESTORE (SAMA TASKSCREEN) ----------
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

    // ---------- FILTERING ----------
    val filteredTasks = remember(tasksForToday, searchQuery, selectedStatus, selectedCategory) {
        tasksForToday.filter { task ->
            val statusMatch = selectedStatus == "All" || task.statusText == selectedStatus
            val categoryMatch = selectedCategory == "All" || task.categoryNamesSafe.contains(selectedCategory)
            val queryMatch =
                searchQuery.isBlank() ||
                        task.Title.contains(searchQuery, true) ||
                        task.Description.contains(searchQuery, true)

            statusMatch && categoryMatch && queryMatch
        }
    }

    val completedTasks = filteredTasks.filter(::isCompleted)
    val activeTasks = filteredTasks.filterNot(::isCompleted)
    val groupedTasks = activeTasks.groupBy { getSessionForTaskModel(it) }

    LaunchedEffect(Unit) { viewModel.setViewMode("Daily") }

    // ================= UI =================
    Box(modifier = Modifier.fillMaxSize()) {

        Scaffold(
            containerColor = Color(0xFFF5F7FA),
            topBar = {
                TopAppBar(
                    title = {
                        AnimatedVisibility(!isSearching) {
                            Column {
                                Text("Today's Tasks", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                                Text(
                                    SimpleDateFormat(
                                        "EEEE, MMMM dd, yyyy",
                                        Locale.getDefault()
                                    ).format(Date()),
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        AnimatedVisibility(isSearching) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("Search tasks...") },
                                modifier = Modifier.fillMaxWidth(),
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
                            Icon(
                                if (isSearching) Icons.Default.Close else Icons.Outlined.Search,
                                null
                            )
                        }
                    }
                )
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
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("create_task") },
                    containerColor = Color(0xFF6A70D7)
                ) { Icon(Icons.Default.Add, null) }
            }
        ) { padding ->

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                TaskViewModeNavigation(
                    selectedViewMode = "Daily",
                    onViewModeChange = {
                        when (it) {
                            "List" -> navController.navigateSingleTop("task_list")
                            "Weekly" -> navController.navigateSingleTop("weekly_tasks")
                            "Monthly" -> navController.navigateSingleTop("monthly_tasks")
                        }
                    }
                )

                // ---------- FILTER (SAMA TASKSCREEN) ----------
                if (!isSearching) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        TaskFilterRow(
                            selectedStatus = selectedStatus,
                            onStatusSelected = { viewModel.setStatusFilter(it) },
                            selectedCategory = selectedCategory,
                            onCategorySelected = { viewModel.setCategoryFilter(it) },
                            categories = listOf(
                                "All",
                                "Work",
                                "Study",
                                "Project",
                                "Meeting",
                                "Travel",
                                "Personal"
                            )
                        )

                        Divider(color = Color(0xFFE0E0E0))
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@Column
                }

                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    DailySession.values().forEach { session ->
                        displaySessionTasks(
                            groupedTasks = groupedTasks,
                            session = session,
                            navController = navController,
                            swipedTaskId = swipedTaskId,
                            onSwipeChange = { id, swiped ->
                                swipedTaskId = if (swiped) id else null
                            },
                            onDeleteClick = { taskToDelete = it },
                            onCheckboxClick = { task, checked ->
                                if (checked) completeTask(task)
                            }
                        )
                    }

                    if (completedTasks.isNotEmpty()) {
                        item {
                            Text(
                                "Completed (${completedTasks.size})",
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Gray
                            )
                        }

                        items(completedTasks, key = { it.id }) { task ->
                            TaskCardDesignStyle(
                                task = task,
                                isCompleted = true,
                                onTaskClick = {
                                    navController.navigate("task_detail/${task.id}")
                                },
                                onCheckboxClick = { checked ->
                                    if (!checked) showRestoreConfirmation(task)
                                },
                                onDeleteIconClick = { taskToDelete = task },
                                swipedTaskId = swipedTaskId,
                                onSwipeChange = { id, swiped ->
                                    swipedTaskId = if (swiped) id else null
                                }
                            )
                        }
                    }
                }
            }
        }

        // ---------- SNACKBAR UNDO ----------
        AnimatedVisibility(
            visible = showUndoSnackbar,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp, start = 20.dp, end = 20.dp)
        ) {
            SnackbarCardSimple(
                icon = {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(20.dp)
                    )
                },
                text = "Task completed",
                actionText = "UNDO",
                onAction = { undoCompletion() }
            )
        }

        // ---------- RESTORE DIALOG ----------
        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = {
                    showRestoreDialog = false
                    taskToRestore = null
                },
                title = { Text("Restore Task?") },
                text = { Text("Do you want to move this task back to active tasks?") },
                confirmButton = {
                    TextButton(onClick = { restoreTask() }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showRestoreDialog = false
                        taskToRestore = null
                    }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // ---------- DELETE DIALOG ----------
        taskToDelete?.let { task ->
            AlertDialog(
                onDismissRequest = { taskToDelete = null },
                title = { Text("Hapus tugas?") },
                text = { Text("Apakah kamu yakin ingin menghapus tugas ini?") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.deleteTask(task.id)
                        taskToDelete = null
                    }) { Text("Hapus", color = Color(0xFFF44336)) }
                },
                dismissButton = {
                    TextButton(onClick = { taskToDelete = null }) { Text("Batal") }
                }
            )
        }
    }
}

/* ================= HELPERS ================= */

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.displaySessionTasks(
    groupedTasks: Map<DailySession, List<TaskModel>>,
    session: DailySession,
    navController: NavController,
    swipedTaskId: String?,
    onSwipeChange: (String, Boolean) -> Unit,
    onDeleteClick: (TaskModel) -> Unit,
    onCheckboxClick: (TaskModel, Boolean) -> Unit
) {
    val tasks = groupedTasks[session].orEmpty()
    if (tasks.isEmpty()) return

    item {
        Text(session.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }

    items(tasks, key = { it.id }) { task ->
        TaskCardDesignStyle(
            modifier = Modifier.animateItemPlacement(),
            task = task,
            onTaskClick = { navController.navigate("task_detail/${task.id}") },
            onCheckboxClick = { checked -> onCheckboxClick(task, checked) },
            onDeleteIconClick = { onDeleteClick(task) },
            isCompleted = false,
            swipedTaskId = swipedTaskId,
            onSwipeChange = onSwipeChange
        )
    }
}

private fun getSessionForTaskModel(task: TaskModel): DailySession {
    val date = task.Deadline?.toDate() ?: return DailySession.Night
    val hour = Calendar.getInstance().apply { time = date }
        .get(Calendar.HOUR_OF_DAY)

    return when (hour) {
        in 5..10 -> DailySession.Morning
        in 11..14 -> DailySession.Afternoon
        in 15..18 -> DailySession.Evening
        else -> DailySession.Night
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                icon()
                Spacer(Modifier.width(12.dp))
                Text(text, color = Color.White, fontSize = 14.sp)
            }
            TextButton(onClick = onAction) {
                Text(
                    actionText,
                    color = Color(0xFF6A70D7),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}