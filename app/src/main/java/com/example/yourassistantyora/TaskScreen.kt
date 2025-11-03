package com.example.yourassistantyora

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import androidx.compose.ui.zIndex
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.utils.NavigationConstants

// ---------- TASK SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun TaskScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},   // ✨ BARU
    onNavigateToNotes: () -> Unit = {},  // ✨ BARU
    onNavigateToTeam: () -> Unit = {}    // ✨ BARU
) {
    var selectedViewMode by remember { mutableStateOf("List") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    val selectedTab = NavigationConstants.TAB_TASK

    // State untuk tasks yang bisa diubah
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Team meeting preparation", "10:00 AM", "High", "Work", "Waiting"),
                Task(2, "Review design mockups", "10:00 AM", "Medium", "Work", "To do"),
                Task(3, "Submit project report", "03:00 AM", "Low", "Study", "In Progress"),
                Task(4, "Morning workout routine", "06:00 AM", "High", "Work", "Hold On")
            )
        )
    }

    // State untuk melacak task yang di-slide
    var swipedTaskId by remember { mutableStateOf<Int?>(null) }

    // State untuk undo completion
    var lastCompletedTask by remember { mutableStateOf<Task?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    // State untuk undo deletion
    var lastDeletedTask by remember { mutableStateOf<Task?>(null) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }

    // State untuk dialog restore
    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<Task?>(null) }

    // State untuk delete confirmation
    var deletingTask by remember { mutableStateOf<Task?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Pisahkan task yang aktif dan selesai
    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    // Filter tasks berdasarkan status dan category yang dipilih
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

    // Function untuk handle checkbox click (complete task)
    fun onCheckboxClick(task: Task) {
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

    // Function untuk undo completion
    fun undoCompletion() {
        lastCompletedTask?.let { t ->
            tasks = tasks.map { if (it.id == t.id) it.copy(isCompleted = false) else it }
        }
        showUndoSnackbar = false
        lastCompletedTask = null
    }

    // Function untuk delete task
    fun deleteTaskConfirmed(task: Task) {
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

    // Function untuk undo deletion
    fun undoDelete() {
        lastDeletedTask?.let { t ->
            tasks = (tasks + t).sortedBy { it.id }
        }
        showDeleteSnackbar = false
        lastDeletedTask = null
    }

    // Function untuk restore
    fun showRestoreConfirmation(task: Task) {
        taskToRestore = task
        showRestoreDialog = true
        swipedTaskId = null
    }

    // Function restore task
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
            bottomBar = {  // ← TAMBAHKAN INI
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
                    onClick = onCreateTaskClick,
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
                // View Mode Tabs (List, Daily, Weekly, Monthly)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ViewModeTab(
                        icon = Icons.Outlined.List,
                        text = "List",
                        isSelected = selectedViewMode == "List",
                        onClick = { selectedViewMode = "List" }
                    )
                    ViewModeTab(
                        icon = Icons.Outlined.DateRange,
                        text = "Daily",
                        isSelected = selectedViewMode == "Daily",
                        onClick = { selectedViewMode = "Daily" }
                    )
                    ViewModeTab(
                        icon = Icons.Outlined.CalendarMonth,
                        text = "Weekly",
                        isSelected = selectedViewMode == "Weekly",
                        onClick = { selectedViewMode = "Weekly" }
                    )
                    ViewModeTab(
                        icon = Icons.Outlined.CalendarToday,
                        text = "Monthly",
                        isSelected = selectedViewMode == "Monthly",
                        onClick = { selectedViewMode = "Monthly" }
                    )
                }

                // Filter Row (Status Dropdown + Category Chips)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Status Dropdown
                    StatusDropdownNew(
                        selectedStatus = selectedStatus,
                        onStatusSelected = { selectedStatus = it }
                    )

                    // Category chips - scrollable horizontal
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChipCompact(
                            text = "All",
                            isSelected = selectedCategory == "All",
                            onClick = { selectedCategory = "All" }
                        )
                        FilterChipCompact(
                            text = "Work",
                            isSelected = selectedCategory == "Work",
                            onClick = { selectedCategory = if (selectedCategory == "Work") "All" else "Work" }
                        )
                        FilterChipCompact(
                            text = "Study",
                            isSelected = selectedCategory == "Study",
                            onClick = { selectedCategory = if (selectedCategory == "Study") "All" else "Study" }
                        )
                        FilterChipCompact(
                            text = "Project",
                            isSelected = selectedCategory == "Project",
                            onClick = { selectedCategory = if (selectedCategory == "Project") "All" else "Project" }
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // Task List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Active Tasks
                    items(filteredActiveTasks, key = { it.id }) { task ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            TaskCardDesignStyle(
                                task = task,
                                onTaskClick = { onTaskClick(task) },
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

                    // Completed Tasks Section
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
                                    onTaskClick = { onTaskClick(task) },
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

        // Undo Snackbar (completion)
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

        // Restore Confirmation Dialog
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

        // Delete Confirmation Dialog
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

// ---------- VIEW MODE TAB ----------
@Composable
fun ViewModeTab(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF6A70D7) else Color(0xFFF5F7FA),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (isSelected) Color.White else Color(0xFF666666),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                color = if (isSelected) Color.White else Color(0xFF666666),
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

// ---------- STATUS DROPDOWN NEW (DENGAN CHIPS DI DALAM DROPDOWN) ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusDropdownNew(
    selectedStatus: String,
    onStatusSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val statusOptions = listOf(
        StatusOption("All", null),
        StatusOption("Waiting", Color(0xFFE1BEE7)),
        StatusOption("To do", Color(0xFFBBDEFB)),
        StatusOption("Hold On", Color(0xFFFFF9C4)),
        StatusOption("In Progress", Color(0xFFB2DFDB)),
        StatusOption("Done", Color(0xFFC8E6C9))
    )

    Box(modifier = modifier) {
        // Dropdown Button
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .clickable { expanded = !expanded },
            color = Color(0xFF6A70D7),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = if (selectedStatus == "All") "Status" else selectedStatus,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.ArrowDropUp else Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Dropdown Menu dengan chips
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(220.dp)
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                statusOptions.forEach { option ->
                    val isSelected = selectedStatus == option.name

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onStatusSelected(option.name)
                                expanded = false
                            },
                        color = option.color ?: Color(0xFFF5F7FA),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = option.name,
                                fontSize = 13.sp,
                                color = if (option.color != null) {
                                    when(option.name) {
                                        "Waiting" -> Color(0xFF7B1FA2)
                                        "To do" -> Color(0xFF1976D2)
                                        "Hold On" -> Color(0xFFF57F17)
                                        "In Progress" -> Color(0xFF00796B)
                                        "Done" -> Color(0xFF388E3C)
                                        else -> Color(0xFF666666)
                                    }
                                } else Color(0xFF666666),
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Selected",
                                    tint = Color(0xFF6A70D7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class StatusOption(
    val name: String,
    val color: Color?
)

// ---------- FILTER CHIP COMPACT ----------
@Composable
fun FilterChipCompact(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFF6A70D7) else Color(0xFFF5F7FA),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color(0xFF666666),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ---------- TASK CARD DESIGN STYLE ----------
@Composable
fun TaskCardDesignStyle(
    task: Task,
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

    // WARNA PRIORITY
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
        // BACKGROUND SWIPE (penuh warna priority)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(stripColor)
        ) {
            // Tombol Hapus (di kanan)
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

        // CARD PUTIH (menimpa sempurna)
        // --- GANTI DENGAN INI (mengganti "CARD PUTIH (menimpa sempurna)" Surface block) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(deleteOffset.value.roundToInt(), 0) }
                .zIndex(1f) // pastikan card di atas background swipe
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
                // STRIP KIRI
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
// ---------- BADGE CHIP ----------
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

// ---------- PREVIEW ----------
@Preview(showBackground = true)
@Composable
fun TaskScreenPreview() {
    YourAssistantYoraTheme {
        TaskScreen(
            modifier = Modifier.fillMaxSize()
        )
    }
}