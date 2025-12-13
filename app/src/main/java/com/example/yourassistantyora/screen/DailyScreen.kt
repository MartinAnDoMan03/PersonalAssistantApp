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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
import com.example.yourassistantyora.components.TaskCard
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
    viewModel: TaskViewModel = viewModel() // 1. Inject ViewModel
) {
// 2. Kumpulkan state dari ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

// ✅ PERBAIKAN: Ambil HANYA tugas untuk hari ini, lalu pisahkan.
    val tasksForToday by viewModel.tasksForToday.collectAsState(initial = emptyList())
    val (completedTasks, activeTasks) = tasksForToday.partition { it.isCompleted }

// Kelompokkan tugas aktif berdasarkan sesi (Pagi, Siang, dll.)
    val groupedTasks = activeTasks.groupBy { viewModel.getSessionForTask(it) }

// State lokal untuk UI
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }

    // ✅ 1. TAMBAHKAN STATE UNTUK DIALOG
    var taskToConfirm by remember { mutableStateOf<Pair<TaskModel, Boolean>?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }


    // Set view mode ke "Daily" saat masuk ke screen ini
    LaunchedEffect(Unit) {
        viewModel.setViewMode("Daily")
    }

    // ✅ 2. TAMBAHKAN SEMUA DIALOG KONFIRMASI (SAMA SEPERTI DI TASKSCREEN)
    taskToConfirm?.let { (task, isCompleting) ->
        AlertDialog(
            onDismissRequest = { taskToConfirm = null },
            title = { Text(if (isCompleting) "Complete Task" else "Restore Task") },
            text = { Text("Are you sure?") },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateTaskStatus(task.id, isCompleting)
                    taskToConfirm = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A70D7))) { Text("Confirm") }
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
                Button(onClick = {
                    viewModel.deleteTask(task.id)
                    taskToDelete = null
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("Delete") }
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
                    // Navigasi
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
            ) {
                Icon(Icons.Default.Add, "Create Task", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 3. Hubungkan komponen filter ke ViewModel
            TaskViewModeNavigation(
                selectedViewMode = "Daily",
                onViewModeChange = {
                    // Jika user klik mode lain, navigasikan
                    when(it) {
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
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tampilkan pesan jika tidak ada task sama sekali untuk hari ini
                    if (groupedTasks.isEmpty() && completedTasks.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(bottom = 80.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No tasks for today!",
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    // ✅ 3. UBAH displaySessionTasks AGAR MENGGUNAKAN SwipeableTaskCard
                    displaySessionTasks(
                        groupedTasks = groupedTasks,
                        session = DailySession.Morning,
                        timeRange = "05:00 - 10:59",
                        navController = navController,onSwipeToDelete = { taskToDelete = it },
                        // ✅ TERUSKAN LOGIKA UNTUK MENAMPILKAN DIALOG
                        onCheckboxClick = { task, isChecked -> taskToConfirm = Pair(task, isChecked) }
                    )
                    displaySessionTasks(
                        groupedTasks = groupedTasks,
                        session = DailySession.Afternoon,
                        timeRange = "11:00 - 14:59",
                        navController = navController,
                        onSwipeToDelete = { taskToDelete = it },
                        onCheckboxClick = { task, isChecked -> taskToConfirm = Pair(task, isChecked) }
                    )
                    displaySessionTasks(
                        groupedTasks = groupedTasks,
                        session = DailySession.Evening,
                        timeRange = "15:00 - 18:59",
                        navController = navController,
                        onSwipeToDelete = { taskToDelete = it },
                        onCheckboxClick = { task, isChecked -> taskToConfirm = Pair(task, isChecked) }
                    )
                    displaySessionTasks(
                        groupedTasks = groupedTasks,
                        session = DailySession.Night,
                        timeRange = "19:00 - 23:59",
                        navController = navController,
                        onSwipeToDelete = { taskToDelete = it },
                        onCheckboxClick = { task, isChecked -> taskToConfirm = Pair(task, isChecked) }
                    )
                    // Section Completed
                    if (completedTasks.isNotEmpty()) {
                        item(key = "completed_header_daily") {
                            Text("Completed (${completedTasks.size})", modifier = Modifier.padding(top = 16.dp, bottom = 8.dp), fontWeight = FontWeight.SemiBold, color = Color.Gray)
                        }
                        items(completedTasks, key = { "completed_${it.id}" }) { task ->
                            SwipeableTaskCard(
                                task = task,
                                onSwipeToDelete = { taskToDelete = it },
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onCheckboxClick = { isChecked ->
                                    taskToConfirm = Pair(task, isChecked)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4. Helper Composable untuk menampilkan header dan list task per sesi
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.displaySessionTasks(
    groupedTasks: Map<DailySession, List<TaskModel>>,
    session: DailySession,
    timeRange: String,
    viewModel: TaskViewModel
) {
    val tasksForSession = groupedTasks[session]
    if (!tasksForSession.isNullOrEmpty()) {
        item(key = session.name) {
            TimePeriodHeader(
                title = session.name,
                timeRange = timeRange,
                taskCount = tasksForSession.size
            )
        }
        items(tasksForSession, key = { it.id }) { task ->
            TaskCard(
                modifier = Modifier.animateItemPlacement(),
                task = task,
                onTaskClick = { /* Navigasi ke detail */ },
                onCheckboxClick = { isChecked ->
                    viewModel.updateTaskStatus(task.id, isChecked)
                }
            )
        }
    }
}

// Header untuk setiap sesi (Morning, Afternoon, dll.)
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
                .background(
                    Color(0xFFE8E7FF),
                    RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ✅ 4. MODIFIKASI HELPER UNTUK MENGGUNAKAN SwipeableTaskCard
@OptIn(ExperimentalFoundationApi::class)
private fun LazyListScope.displaySessionTasks(
    groupedTasks: Map<DailySession, List<TaskModel>>,
    session: DailySession,
    timeRange: String,
    navController: NavController,
    onSwipeToDelete: (TaskModel) -> Unit,
    // ✅ TAMBAHKAN PARAMETER BARU INI
    onCheckboxClick: (task: TaskModel, isChecked: Boolean) -> Unit
) {
    val tasksForSession = groupedTasks[session]
    if (!tasksForSession.isNullOrEmpty()) {
        item(key = session.name) {
            TimePeriodHeader(title = session.name, timeRange = timeRange, taskCount = tasksForSession.size)
        }
        items(tasksForSession, key = { it.id }) { task ->
            SwipeableTaskCard(
                modifier = Modifier.animateItemPlacement(), // Jangan lupa tambahkan ini untuk animasi
                task = task,
                onSwipeToDelete = onSwipeToDelete,
                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                // ✅ GUNAKAN PARAMETER onCheckboxClick
                onCheckboxClick = { isChecked -> onCheckboxClick(task, isChecked) }
            )
        }
    }
}