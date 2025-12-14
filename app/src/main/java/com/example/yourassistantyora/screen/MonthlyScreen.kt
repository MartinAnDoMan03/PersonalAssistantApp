package com.example.yourassistantyora.screen

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
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
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

private data class MonthlyCalendarCell(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean,
    val isToday: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    val tasksForSelectedDate: List<TaskModel> by viewModel.dateFilteredTasks
        .collectAsState(initial = emptyList())

    val isLoading: Boolean by viewModel.isLoading.collectAsState(initial = false)

    val selectedDate: Date by viewModel.selectedDate.collectAsState(initial = Date())
    val selectedStatus: String by viewModel.selectedStatus.collectAsState(initial = "All")
    val selectedCategory: String by viewModel.selectedCategory.collectAsState(initial = "All")

    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }

    var taskToConfirm by remember { mutableStateOf<Pair<TaskModel, Boolean>?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }

    var swipedTaskId by remember { mutableStateOf<String?>(null) }
    var currentMonthDate by remember { mutableStateOf(Date()) }

    LaunchedEffect(Unit) { viewModel.setViewMode("Monthly") }

    val calendarCells: List<MonthlyCalendarCell> = remember(currentMonthDate, selectedDate) {
        generateMonthlyCalendarCells(currentMonthDate, selectedDate)
    }

    // ✅ jangan bergantung ke utils isCompleted biar ga bingung:
    fun isCompletedTask(t: TaskModel): Boolean = t.Status == 2

    val (completedTasks, activeTasks) = remember(tasksForSelectedDate) {
        tasksForSelectedDate.partition { isCompletedTask(it) }
    }

    // Dialog complete/restore
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

    // Dialog delete
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
                title = { Text("My Tasks", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* TODO */ }) { Icon(Icons.Outlined.Search, "Search") }
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
                contentColor = Color.White
            ) { Icon(Icons.Default.Add, "Create Task") }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                TaskViewModeNavigation(
                    selectedViewMode = "Monthly",
                    onViewModeChange = { viewModel.setViewMode(it) },
                    onNavigateToList = { navController.navigateSingleTop("task_list") },
                    onNavigateToDaily = { navController.navigateSingleTop("daily_tasks") },
                    onNavigateToWeekly = { navController.navigateSingleTop("weekly_tasks") },
                    onNavigateToMonthly = { /* already here */ }
                )
            }

            item {
                TaskFilterRow(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { viewModel.setStatusFilter(it) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    categories = listOf("All", "Work", "Study", "Project", "Meeting", "Travel")
                )
            }

            item { Divider(color = Color(0xFFE0E0E0), thickness = 1.dp) }

            // Calendar block
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    MonthHeaderMonthly(
                        date = currentMonthDate,
                        onPrevMonth = {
                            val cal = Calendar.getInstance().apply { time = currentMonthDate }
                            cal.add(Calendar.MONTH, -1)
                            currentMonthDate = cal.time
                        },
                        onNextMonth = {
                            val cal = Calendar.getInstance().apply { time = currentMonthDate }
                            cal.add(Calendar.MONTH, 1)
                            currentMonthDate = cal.time
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa").forEach { d ->
                            Text(
                                d,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF757575)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier.height(280.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        userScrollEnabled = false
                    ) {
                        items(
                            items = calendarCells,
                            key = { cell -> cell.date.time }
                        ) { cell ->
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(CircleShape)
                                    .clickable(enabled = cell.isCurrentMonth) {
                                        viewModel.setSelectedDate(cell.date)
                                    }
                                    .background(
                                        when {
                                            cell.isSelected -> Color(0xFF6A70D7)
                                            cell.isToday -> Color(0xFFE8E7FF)
                                            else -> Color.Transparent
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cell.dayOfMonth.toString(),
                                    fontSize = 15.sp,
                                    fontWeight = when {
                                        cell.isSelected -> FontWeight.Bold
                                        cell.isToday -> FontWeight.SemiBold
                                        else -> FontWeight.Normal
                                    },
                                    color = when {
                                        !cell.isCurrentMonth -> Color(0xFFCCCCCC)
                                        cell.isSelected -> Color.White
                                        cell.isToday -> Color(0xFF6A70D7)
                                        else -> Color(0xFF1F1F1F)
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }

            // Selected date header
            item {
                val today = Date()
                val isToday = isSameCalendarDayMonthly(selectedDate, today)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(selectedDate),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F1F1F)
                    )
                    if (isToday) {
                        Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF6A70D7)) {
                            Text(
                                "Today",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    } else {
                        TextButton(onClick = { viewModel.setSelectedDate(Date()) }) {
                            Text("Go to Today", color = Color(0xFF6A70D7))
                        }
                    }
                }
            }

            // Tasks section
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }
            } else {
                if (activeTasks.isEmpty() && completedTasks.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tasks for this day.", fontSize = 14.sp, color = Color(0xFF9E9E9E))
                        }
                    }
                }

                items(activeTasks, key = { "active_${it.id}" }) { task ->
                    TaskCardDesignStyle(
                        task = task,
                        onTaskClick = { navController.navigate("task_detail/${task.id}") },
                        onCheckboxClick = { checked -> taskToConfirm = Pair(task, checked) },
                        onDeleteIconClick = { taskToDelete = task },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        isCompleted = false,
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
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                    items(completedTasks, key = { "completed_${it.id}" }) { task ->
                        TaskCardDesignStyle(
                            task = task,
                            onTaskClick = { navController.navigate("task_detail/${task.id}") },
                            onCheckboxClick = { checked -> taskToConfirm = Pair(task, checked) },
                            onDeleteIconClick = { taskToDelete = task },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            isCompleted = true,
                            swipedTaskId = swipedTaskId,
                            onSwipeChange = { id, isSwiped ->
                                swipedTaskId = if (isSwiped) id else null
                            }
                        )
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun MonthHeaderMonthly(
    date: Date,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val label = remember(date) {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date)
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F1F1F))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = onPrevMonth, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronLeft, "Previous Month", tint = Color(0xFF1F1F1F))
            }
            IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ChevronRight, "Next Month", tint = Color(0xFF1F1F1F))
            }
        }
    }
}

// ✅ Rename helper supaya ga bentrok sama fungsi isSameDay lain di project
private fun isSameCalendarDayMonthly(d1: Date, d2: Date): Boolean {
    val c1 = Calendar.getInstance().apply { time = d1 }
    val c2 = Calendar.getInstance().apply { time = d2 }
    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
            c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)
}

private fun generateMonthlyCalendarCells(monthDate: Date, selectedDate: Date): List<MonthlyCalendarCell> {
    val cal = Calendar.getInstance().apply { time = monthDate }
    cal.set(Calendar.DAY_OF_MONTH, 1)

    val firstDayOffset = cal.get(Calendar.DAY_OF_WEEK) - 1 // Sun=0..Sat=6
    cal.add(Calendar.DAY_OF_MONTH, -firstDayOffset)

    val currentMonth = Calendar.getInstance().apply { time = monthDate }.get(Calendar.MONTH)
    val today = Date()

    return (0 until 42).map {
        val date = cal.time
        val day = cal.get(Calendar.DAY_OF_MONTH)
        val isCurrent = cal.get(Calendar.MONTH) == currentMonth
        val isSel = isSameCalendarDayMonthly(date, selectedDate)
        val isTod = isSameCalendarDayMonthly(date, today)

        cal.add(Calendar.DAY_OF_MONTH, 1)

        MonthlyCalendarCell(
            date = date,
            dayOfMonth = day,
            isCurrentMonth = isCurrent,
            isSelected = isSel,
            isToday = isTod
        )
    }
}
