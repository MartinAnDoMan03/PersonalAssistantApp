package com.example.yourassistantyora.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.components.TaskCardDesignStyle
import com.example.yourassistantyora.components.TaskFilterRow
import com.example.yourassistantyora.components.TaskViewModeNavigation
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.DailySession
import com.example.yourassistantyora.viewModel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun DailyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    val isLoading: Boolean by viewModel.isLoading.collectAsState(initial = false)
    val error: String? by viewModel.error.collectAsState(initial = null)
    val selectedStatus: String by viewModel.selectedStatus.collectAsState(initial = "All")
    val selectedCategory: String by viewModel.selectedCategory.collectAsState(initial = "All")

    // ✅ pastikan ini memang List<TaskModel> dari VM
    val tasksForToday: List<TaskModel> by viewModel.tasksForToday.collectAsState(initial = emptyList())

    // ✅ Completed rule untuk TaskModel: Status == 2 (Done)
    fun isCompletedTask(t: TaskModel) = t.Status == 2

    val completedTasks = tasksForToday.filter(::isCompletedTask)
    val activeTasks = tasksForToday.filterNot(::isCompletedTask)

    // ✅ grouping session pakai Deadline time
    val groupedTasks: Map<DailySession, List<TaskModel>> =
        activeTasks.groupBy { getSessionForTaskModel(it) }

    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }

    var taskToConfirm by remember { mutableStateOf<Pair<TaskModel, Boolean>?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }

    var swipedTaskId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { viewModel.setViewMode("Daily") }

    // ---------- DIALOG COMPLETE / RESTORE ----------
    taskToConfirm?.let { (task, isCompleting) ->
        AlertDialog(
            onDismissRequest = { taskToConfirm = null },
            title = { Text(if (isCompleting) "Complete Task" else "Restore Task") },
            text = { Text("Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateTaskStatus(task.id, isCompleting)
                        taskToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A70D7))
                ) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { taskToConfirm = null }) { Text("Cancel") }
            }
        )
    }

    // ---------- DIALOG DELETE ----------
    taskToDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            title = { Text("Delete Task") },
            text = { Text("Are you sure you want to permanently delete '${task.Title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteTask(task.id)
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Today's Tasks", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault()).format(Date()),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
                containerColor = Color(0xFF6A70D7)
            ) { Icon(Icons.Default.Add, "Create Task", tint = Color.White) }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
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

            TaskFilterRow(
                selectedStatus = selectedStatus,
                onStatusSelected = { viewModel.setStatusFilter(it) },
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setCategoryFilter(it) },
                categories = listOf("All", "Work", "Study", "Project", "Meeting", "Travel")
            )

            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            if (isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (!error.isNullOrBlank()) {
                Text(error ?: "", color = Color.Red, modifier = Modifier.padding(16.dp))
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (groupedTasks.isEmpty() && completedTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillParentMaxSize()
                                .padding(bottom = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tasks for today!", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }

                displaySessionTasks(
                    groupedTasks = groupedTasks,
                    session = DailySession.Morning,
                    timeRange = "05:00 - 10:59",
                    navController = navController,
                    swipedTaskId = swipedTaskId,
                    onSwipeChange = { id, isSwiped -> swipedTaskId = if (isSwiped) id else null },
                    onDeleteClick = { taskToDelete = it },
                    onCheckboxClick = { task, isChecked -> taskToConfirm = Pair(task, isChecked) }
                )

                displaySessionTasks(
                    groupedTasks = groupedTasks,
                    session = DailySession.Afternoon,
                    timeRange = "11:00 - 14:59",
                    navController = navController,
                    swipedTaskId = swipedTaskId,
                    onSwipeChange = { id, isSwiped -> swipedTaskId = if (isSwiped) id else null },
                    onDeleteClick = { taskToDelete = it },
                    onCheckboxClick = { task, isChecked -> taskToConfirm = Pair(task, isChecked) }
                )

                displaySessionTasks(
                    groupedTasks = groupedTasks,
                    session = DailySession.Evening,
                    timeRange = "15:00 - 18:59",
                    navController = navController,
                    swipedTaskId = swipedTaskId,
                    onSwipeChange = { id, isSwiped -> swipedTaskId = if (isSwiped) id else null },
                    onDeleteClick = { taskToDelete = it },
                    onCheckboxClick = { task, isChecked -> taskToConfirm = Pair(task, isChecked) }
                )

                displaySessionTasks(
                    groupedTasks = groupedTasks,
                    session = DailySession.Night,
                    timeRange = "19:00 - 23:59",
                    navController = navController,
                    swipedTaskId = swipedTaskId,
                    onSwipeChange = { id, isSwiped -> swipedTaskId = if (isSwiped) id else null },
                    onDeleteClick = { taskToDelete = it },
                    onCheckboxClick = { task, isChecked -> taskToConfirm = Pair(task, isChecked) }
                )

                if (completedTasks.isNotEmpty()) {
                    item {
                        Text(
                            "Completed (${completedTasks.size})",
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Gray
                        )
                    }

                    items(completedTasks, key = { "completed_${it.id}" }) { task ->
                        TaskCardDesignStyle(
                            task = task,
                            onTaskClick = { navController.navigate("task_detail/${task.id}") },
                            onCheckboxClick = { checked -> taskToConfirm = Pair(task, checked) },
                            onDeleteIconClick = { taskToDelete = task },
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

@Composable
fun TimePeriodHeader(title: String, timeRange: String, taskCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(timeRange, fontSize = 12.sp, color = Color.Gray)
        }
        Text(
            "$taskCount tasks",
            fontSize = 12.sp,
            color = Color(0xFF6A70D7),
            modifier = Modifier
                .background(Color(0xFFE8E7FF), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.displaySessionTasks(
    groupedTasks: Map<DailySession, List<TaskModel>>,
    session: DailySession,
    timeRange: String,
    navController: NavController,
    swipedTaskId: String?,
    onSwipeChange: (String, Boolean) -> Unit,
    onDeleteClick: (TaskModel) -> Unit,
    onCheckboxClick: (task: TaskModel, isChecked: Boolean) -> Unit
) {
    val tasksForSession = groupedTasks[session].orEmpty()
    if (tasksForSession.isEmpty()) return

    item(key = "header_${session.name}") {
        TimePeriodHeader(title = session.name, timeRange = timeRange, taskCount = tasksForSession.size)
    }

    items(tasksForSession, key = { it.id }) { task ->
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

/**
 * DailySession berdasarkan jam di Deadline (Firestore Timestamp -> Date).
 * Kalau Deadline null, taruh ke Night biar ga crash.
 */
private fun getSessionForTaskModel(task: TaskModel): DailySession {
    val date = task.Deadline?.toDate() ?: return DailySession.Night
    val cal = Calendar.getInstance().apply { time = date }
    val hour = cal.get(Calendar.HOUR_OF_DAY)

    return when (hour) {
        in 5..10 -> DailySession.Morning
        in 11..14 -> DailySession.Afternoon
        in 15..18 -> DailySession.Evening
        else -> DailySession.Night
    }
}
