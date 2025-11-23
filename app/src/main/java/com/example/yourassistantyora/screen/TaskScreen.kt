package com.example.yourassistantyora.screen

import android.widget.Toast
// import androidx.compose.animation.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.components.*
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.TaskViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut



// Tambahkan @OptIn di level file untuk mencakup semuanya
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    // --- Kumpulkan State dari ViewModel ---
    val tasks by viewModel.listTasks.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    val selectedViewMode by viewModel.selectedViewMode.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    // --- State Lokal untuk UI ---
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }
    val context = LocalContext.current

    // --- Side Effects ---
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // --- Logika Turunan ---
    val (completedTasks, activeTasks) = tasks.partition { it.isCompleted }

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
                        IconButton(onClick = { /* TODO: Implement Search */ }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF2D2D2D)
                            )
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
                    onViewModeChange = { viewModel.setViewMode(it) },
                    onNavigateToDaily = {  navController.navigate("daily_tasks")  },
                    onNavigateToWeekly = { navController.navigate("weekly_tasks") },
                    onNavigateToMonthly = {  navController.navigate("monthly_tasks") }
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
                } else                 if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Daftar Task Aktif
                        items(items = activeTasks, key = { it.id }) { task ->
                            // ✅ modifier `animateItemPlacement` diterapkan di sini, di dalam item scope
                            TaskCard(
                                modifier = Modifier.animateItemPlacement(),
                                task = task,
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onCheckboxClick = { isChecked ->
                                    viewModel.updateTaskStatus(task.id, isChecked)
                                }
                            )
                        }

                        // Bagian Task Selesai
                        if (completedTasks.isNotEmpty()) {
                            item(key = "completed_header") {
                                Text(
                                    text = "Completed (${completedTasks.size})",
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                            items(items = completedTasks, key = { "completed_${it.id}" }) { task ->
                                // ✅ modifier `animateItemPlacement` juga diterapkan di sini
                                TaskCard(
                                    modifier = Modifier.animateItemPlacement(),
                                    task = task,
                                    onTaskClick = {  navController.navigate("task_detail/${task.id}") },
                                    onCheckboxClick = { isChecked ->
                                        viewModel.updateTaskStatus(task.id, isChecked)
                                    }
                                )
                            }
                        }

                        // Pesan jika tidak ada task sama sekali
                        if (tasks.isEmpty()) {
                            item(key = "empty_state") {
                                Box(
                                    modifier = Modifier
                                        .fillParentMaxSize()
                                        .padding(bottom = 80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No tasks found.\nTap the '+' button to add a new task.",
                                        color = Color.Gray,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
