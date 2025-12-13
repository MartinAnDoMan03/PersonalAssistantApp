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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.yourassistantyora.components.TaskCard
import com.example.yourassistantyora.components.TaskFilterRow
import com.example.yourassistantyora.components.TaskViewModeNavigation
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.TaskViewModel
import com.example.yourassistantyora.models.TaskModel
import java.text.SimpleDateFormat
import java.util.*

private data class CalendarDate(
    val date: Date,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MonthlyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    val tasksForSelectedDate by viewModel.dateFilteredTasks.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()

    var currentMonthDate by remember { mutableStateOf(Date()) }
    val selectedDate by viewModel.selectedDate.collectAsState()

    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }

    // ✅ 1. Tambahkan state untuk dialog
    var taskToConfirm by remember { mutableStateOf<Pair<TaskModel, Boolean>?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }


    LaunchedEffect(Unit) {
        viewModel.setViewMode("Monthly")
    }

    val calendarDates = remember(currentMonthDate, selectedDate) {
        generateCalendarDatesForMonth(currentMonthDate, selectedDate)
    }

    val (completedTasks, activeTasks) = tasksForSelectedDate.partition { it.isCompleted }


    // ✅ 2. Tambahkan semua dialog konfirmasi
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
                    IconButton(onClick = { /*SEARCH ACTION*/ }) {
                        Icon(Icons.Outlined.Search, "Search")
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
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, "Create Task")
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {

            // --- NAV MODE SELECTOR ---
            TaskViewModeNavigation(
                selectedViewMode = "Monthly",
                onViewModeChange = { viewModel.setViewMode(it) },
                onNavigateToList = { navController.navigate("task_list") },
                onNavigateToDaily = { navController.navigate("daily_tasks") },
                onNavigateToWeekly = { navController.navigate("weekly_tasks") },
                onNavigateToMonthly = { /* already here */ }
            )

//            // --- FILTER ROW ---
//            TaskFilterRow(
//                selectedStatus = viewModel.selectedStatus.collectAsState().value,
//                onStatusSelected = { viewModel.setStatusFilter(it) },
//                selectedCategory = viewModel.selectedCategory.collectAsState().value,
//                onCategorySelected = { viewModel.setCategoryFilter(it) },
//                categories = listOf("All", "Work", "Study", "Project", "Meeting", "Travel")
//            )

            // --- CALENDAR HEADER ---
            CalendarHeader(
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

            // --- CALENDAR GRID ---
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .background(Color.White)
                    .padding(8.dp)
                    .heightIn(max = 280.dp)
            ) {
                items(listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")) { day ->
                    Text(
                        day,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(4.dp),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }

                items(calendarDates) { date ->
                    CalendarCell(
                        date = date,
                        onDateSelected = { viewModel.setSelectedDate(it) }
                    )
                }
            }

            Divider()

            // --- SMALL HEADER FOR SELECTED DATE ---
            SelectedDateHeader(
                selectedDate = selectedDate,
                onTodayClick = { viewModel.setSelectedDate(Date()) }
            )

            // --- TASK LIST ---
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                if (isLoading) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                } else if (tasksForSelectedDate.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 80.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No tasks for this day.", color = Color.Gray)
                        }
                    }
                } else {
                    // ✅ 3. Ganti TaskCard menjadi SwipeableTaskCard
                    items(activeTasks, key = { it.id }) { task ->
                        SwipeableTaskCard(
                            modifier = Modifier.animateItemPlacement(),
                            task = task,
                            onTaskClick = { navController.navigate("task_detail/${task.id}") },
                            onSwipeToDelete = { taskToDelete = it },
                            onCheckboxClick = { isChecked -> taskToConfirm = Pair(task, isChecked) }
                        )
                    }

                    // Completed tasks
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
                            TaskCard(
                                modifier = Modifier.animateItemPlacement(),
                                task = task,
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onCheckboxClick = { viewModel.updateTaskStatus(task.id, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// ------------------------------------------------------------
//  Helper Components
// ------------------------------------------------------------

@Composable
private fun CalendarHeader(date: Date, onPrevMonth: () -> Unit, onNextMonth: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(date),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Row {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, "Previous Month")
            }
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, "Next Month")
            }
        }
    }
}

@Composable
private fun SelectedDateHeader(selectedDate: Date, onTodayClick: () -> Unit) {
    // ✅ Cek apakah tanggal yang dipilih adalah hari ini
    val isToday = remember(selectedDate) {
        isDaySame(selectedDate, Date())
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = SimpleDateFormat("EEEE, MMM dd", Locale.getDefault()).format(selectedDate),
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        if (isToday) {
            Button(
                onClick = onTodayClick,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E7FF)),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Today", color = Color(0xFF6A70D7), fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun CalendarCell(date: CalendarDate, onDateSelected: (Date) -> Unit) {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    date.isSelected -> Color(0xFF6A70D7)
                    else -> Color.Transparent
                }
            )
            .clickable(enabled = date.isCurrentMonth) { onDateSelected(date.date) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            fontSize = 13.sp,
            color = when {
                !date.isCurrentMonth -> Color.LightGray
                date.isSelected -> Color.White
                else -> Color.Black
            }
        )
    }
}

// ✅ Fungsi helper untuk membandingkan dua tanggal (tanpa waktu)
private fun isDaySame(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

private fun generateCalendarDatesForMonth(monthDate: Date, selectedDate: Date): List<CalendarDate> {
    val cal = Calendar.getInstance().apply { time = monthDate }
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    cal.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

    val currentMonth = Calendar.getInstance().apply { time = monthDate }.get(Calendar.MONTH)

    return (0..41).map {
        val date = cal.time
        val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
        val isCurrentMonthDay = cal.get(Calendar.MONTH) == currentMonth
        val isSelected = isDaySame(date, selectedDate)

        cal.add(Calendar.DAY_OF_MONTH, 1)

        CalendarDate(
            date = date,
            dayOfMonth = dayOfMonth,
            isCurrentMonth = isCurrentMonthDay,
            isSelected = isSelected
        )
    }
}