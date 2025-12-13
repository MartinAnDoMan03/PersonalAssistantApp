package com.example.yourassistantyora.screen

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.filled.Close
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.components.*
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.TaskViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TaskScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    // --- State dari ViewModel (tetap sama) ---
    val tasks by viewModel.listTasks.collectAsState(initial = emptyList())
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val selectedViewMode by viewModel.selectedViewMode.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    var isSearchActive by remember { mutableStateOf(false) }

    // --- State Lokal untuk UI (tetap sama) ---
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_TASK) }
    val context = LocalContext.current

    // ✅ State untuk mengelola dialog konfirmasi (sudah benar)
    var taskToConfirm by remember { mutableStateOf<Pair<TaskModel, Boolean>?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskModel?>(null) }

    // --- Side Effects (sudah benar) ---
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    // --- Logika Turunan (sudah benar) ---
    val (completedTasks, activeTasks) = tasks.partition { it.isCompleted }

    // ✅ --- Dialog Konfirmasi untuk Selesai/Restore Task (sudah benar) ---
    taskToConfirm?.let { (task, isCompleting) ->
        AlertDialog(
            onDismissRequest = { taskToConfirm = null },
            title = { Text(if (isCompleting) "Complete Task" else "Restore Task") },
            text = { Text("Are you sure you want to ${if (isCompleting) "mark this task as completed" else "restore this task"}?") },
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

    // ✅ --- Dialog Konfirmasi untuk Hapus Task (sudah benar) ---
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F7FA),
            topBar = {
                // ✅ TOP APP BAR DENGAN LOGIKA PENCARIAN
                TopAppBar(
                    title = {
                        // Tampilkan search bar jika isSearchActive true, jika tidak tampilkan judul
                        AnimatedVisibility(
                            visible = !isSearchActive,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            Text("My Tasks", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2D2D))
                        }
                        AnimatedVisibility(
                            visible = isSearchActive,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Search by title...", fontSize = 12.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                                modifier = Modifier
                                    .fillMaxWidth()
//                                    .height(50.dp)
                                    .padding(end = 16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    cursorColor = Color(0xFF6A70D7),
                                    focusedContainerColor = Color(0xFFF0F0F0),
                                    unfocusedContainerColor = Color(0xFFF0F0F0)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }
                    },
                    navigationIcon = {
                        // Jika sedang mencari, ikon back akan menutup search bar, jika tidak akan kembali ke halaman sebelumnya
                        IconButton(onClick = {
                            if (isSearchActive) {
                                isSearchActive = false
                                viewModel.setSearchQuery("") // Reset query saat search ditutup
                            } else {
                                navController.popBackStack()
                            }
                        }) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFF2D2D2D))
                        }
                    },
                    actions = {
                        // Tombol untuk mengaktifkan/menonaktifkan mode pencarian
                        IconButton(onClick = {
                            if (isSearchActive) {
                                viewModel.setSearchQuery("")
                            }
                            isSearchActive = !isSearchActive
                        }) {
                            Icon(
                                imageVector = if (isSearchActive) Icons.Filled.Close else Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = Color(0xFF2D2D2D)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = { // ✅ TAMBAHKAN BOTTOM BAR
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
                // FAB sudah benar
                FloatingActionButton(
                    onClick = { navController.navigate("create_task") },
                    containerColor = Color(0xFF6A70D7),
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.Add, "Create Task", modifier = Modifier.size(28.dp))
                }
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Filter UI sudah benar
                TaskViewModeNavigation(
                    selectedViewMode = selectedViewMode,
                    onViewModeChange = { viewModel.setViewMode(it) },
                    onNavigateToDaily = { navController.navigate("daily_tasks") },
                    onNavigateToWeekly = { navController.navigate("weekly_tasks") },
                    onNavigateToMonthly = { navController.navigate("monthly_tasks") }
                )
                TaskFilterRow(
                    selectedStatus = selectedStatus,
                    onStatusSelected = { viewModel.setStatusFilter(it) },
                    selectedCategory = selectedCategory,
                    onCategorySelected = { viewModel.setCategoryFilter(it) },
                    categories = listOf("All", "Work", "Study", "Project", "Meeting", "Travel")
                )
                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // ✅ 5. HAPUS BLOK `if (isLoading)` YANG DUPLIKAT
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    // ✅ 3. PERBAIKI STRUKTUR `LazyColumn`
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Render Task Aktif
                        items(items = activeTasks, key = { it.id }) { task ->
                            SwipeableTaskCard(
                                task = task,
                                onSwipeToDelete = { taskToDelete = it },
                                onTaskClick = { navController.navigate("task_detail/${task.id}") },
                                onCheckboxClick = { isChecked ->
                                    taskToConfirm = Pair(task, isChecked)
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

// ✅ 2. PINDAHKAN `SwipeableTaskCard` KE LUAR `TaskScreen`
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
public fun SwipeableTaskCard(
    task: TaskModel,
    onSwipeToDelete: (TaskModel) -> Unit,
    onTaskClick: () -> Unit,
    onCheckboxClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onSwipeToDelete(task)
            }
            false // ⬅️ jangan auto remove
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        backgroundContent = {
            val isSwiped = dismissState.targetValue == SwipeToDismissBoxValue.EndToStart

            val bgColor by animateColorAsState(
                targetValue = if (isSwiped)
                    Color.Red.copy(alpha = 0.85f)
                else
                    Color.LightGray.copy(alpha = 0.4f),
                label = "bg"
            )

            val scale by animateFloatAsState(
                targetValue = if (isSwiped) 1.15f else 0.9f,
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(bgColor, RoundedCornerShape(16.dp))
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.scale(scale)
                )
            }
        }
    ) {
        TaskCard(
            task = task,
            onTaskClick = onTaskClick,
            onCheckboxClick = onCheckboxClick
        )
    }
}



