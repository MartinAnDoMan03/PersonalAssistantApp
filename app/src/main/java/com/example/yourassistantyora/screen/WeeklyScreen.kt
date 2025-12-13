package com.example.yourassistantyora.screen

import androidx.compose.foundation.ExperimentalFoundationApi
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

// Data class untuk item di strip kalender mingguan
private data class CalendarDay(
    val date: Date,
    val dayName: String, // "SUN", "MON"
    val dayNumber: String, // "1", "2"
    val isSelected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WeeklyScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    val tasksForSelectedDate by viewModel.dateFilteredTasks.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }

    // ✅ 1. Tambahkan state untuk dialog
    var taskToConfirm by remember { mutableStateOf<Pair<TaskModel, Boolean>?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }


    LaunchedEffect(Unit) {
        viewModel.setViewMode("Weekly")
    }

    // ✅ PERBAIKAN: Generate 7 hari dalam satu minggu, dimulai dari hari Minggu.
    val weekDays = remember(selectedDate) {
        val calendar = Calendar.getInstance().apply { time = selectedDate }
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) // Set ke hari Minggu minggu ini
        (0..6).map { dayIndex ->
            val date = calendar.time
            val day = CalendarDay(
                date = date,
                dayName = SimpleDateFormat("EEE", Locale.getDefault()).format(date).uppercase(),
                dayNumber = SimpleDateFormat("d", Locale.getDefault()).format(date),
                isSelected = isSameDay(date, selectedDate)
            )
            calendar.add(Calendar.DAY_OF_YEAR, 1) // Maju ke hari berikutnya
            day
        }
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

    // ✅ PERBAIKAN: Lengkapi Scaffold
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
                    IconButton(onClick = { /*TODO*/ }) {
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TaskViewModeNavigation(
                selectedViewMode = "Weekly",
                onViewModeChange = { viewModel.setViewMode(it) },
                onNavigateToList = { navController.navigate("task_list") },
                onNavigateToDaily = { navController.navigate("daily_tasks") },
                onNavigateToWeekly = { /* Sudah di sini, tidak perlu aksi */ },
                onNavigateToMonthly = { navController.navigate("monthly_tasks") }
            )
            TaskFilterRow(
                selectedStatus = selectedStatus,
                onStatusSelected = { viewModel.setStatusFilter(it) },
                selectedCategory = selectedCategory,
                onCategorySelected = { viewModel.setCategoryFilter(it) },
                categories = listOf("All", "Work", "Study", "Project", "Meeting", "Travel")
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(weekDays, key = { it.date }) { day ->
                    DayChip(day = day, onDateSelected = { viewModel.setSelectedDate(it) })
                }
            }

            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (tasksForSelectedDate.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(bottom = 80.dp), contentAlignment = Alignment.Center
                            ) {
                                Text("No tasks for this day.", color = Color.Gray)
                            }
                        }
                    }

                    // ✅ 3. Ganti TaskCard menjadi SwipeableTaskCard
                    if(activeTasks.isNotEmpty()) {
                        items(activeTasks, key = { "active_${it.id}" }) { task ->
                            SwipeableTaskCard(
                                modifier = Modifier.animateItemPlacement(),
                                task = task,
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onSwipeToDelete = { taskToDelete = it },
                                onCheckboxClick = { isChecked -> taskToConfirm = Pair(task, isChecked) }
                            )
                        }
                    }

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

@Composable
private fun DayChip(day: CalendarDay, onDateSelected: (Date) -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (day.isSelected) Color(0xFF6A70D7) else Color(0xFFF0F0F0))
            .clickable { onDateSelected(day.date) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.dayName,
            color = if (day.isSelected) Color.White.copy(alpha = 0.8f) else Color.Gray,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = day.dayNumber,
            color = if (day.isSelected) Color.White else Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

internal fun isSameDay(date1: Date, date2: Date): Boolean {
    val cal1 = Calendar.getInstance().apply { time = date1 }
    val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
            cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
