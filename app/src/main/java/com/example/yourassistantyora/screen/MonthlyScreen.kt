package com.example.yourassistantyora

import androidx.compose.animation.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.components.TaskViewModeNavigation
import com.example.yourassistantyora.components.TaskFilterRow
import com.example.yourassistantyora.screen.CreateTaskScreen
import com.example.yourassistantyora.screen.Task
import com.example.yourassistantyora.utils.NavigationConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.plus

// Data class untuk Monthly Task
data class MonthlyTaskItem(
    val task: Task,
    val date: Int,
    val month: Int,
    val year: Int
)

// Data class untuk calendar date
data class CalendarDate(
    val day: Int,
    val isCurrentMonth: Boolean,
    val isSelected: Boolean = false,
    val isToday: Boolean = false,
    val taskCount: Int = 0
)

// ---------- HELPER FUNCTIONS ----------
fun generateCalendarDates(month: Int, year: Int, selectedDay: Int): List<CalendarDate> {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, 1)

    val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    calendar.add(Calendar.MONTH, -1)
    val daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

    val today = Calendar.getInstance()
    val todayDay = today.get(Calendar.DAY_OF_MONTH)
    val todayMonth = today.get(Calendar.MONTH)
    val todayYear = today.get(Calendar.YEAR)

    val dates = mutableListOf<CalendarDate>()

    // Previous month days
    for (i in firstDayOfWeek - 1 downTo 0) {
        dates.add(
            CalendarDate(
                day = daysInPrevMonth - i,
                isCurrentMonth = false
            )
        )
    }

    // Current month days
    for (day in 1..daysInMonth) {
        val isToday = day == todayDay && month == todayMonth && year == todayYear
        dates.add(
            CalendarDate(
                day = day,
                isCurrentMonth = true,
                isSelected = day == selectedDay,
                isToday = isToday
            )
        )
    }

    // Next month days to fill grid
    val remainingCells = 42 - dates.size
    for (day in 1..remainingCells) {
        dates.add(
            CalendarDate(
                day = day,
                isCurrentMonth = false
            )
        )
    }

    return dates
}

fun getSelectedDateString(day: Int, month: Int, year: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(year, month, day)
    val dayName = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)
    val monthName = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time)
    return "$dayName, $monthName $day"
}

// ---------- CALENDAR DATE CELL ----------
@Composable
fun CalendarDateCell(
    date: CalendarDate,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable(enabled = date.isCurrentMonth, onClick = onClick)
            .background(
                when {
                    date.isSelected -> Color(0xFF6C63FF)
                    date.isToday -> Color(0xFFE8E7FF)
                    else -> Color.Transparent
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.day.toString(),
            fontSize = 15.sp,
            fontWeight = when {
                date.isSelected -> FontWeight.Bold
                date.isToday -> FontWeight.SemiBold
                else -> FontWeight.Normal
            },
            color = when {
                !date.isCurrentMonth -> Color(0xFFCCCCCC)
                date.isSelected -> Color.White
                date.isToday -> Color(0xFF6C63FF)
                else -> Color(0xFF1F1F1F)
            },
            textAlign = TextAlign.Center
        )
    }
}

// Note: Menggunakan TaskCardDesignStyle yang sama dengan DailyScreen dan TaskScreen

// ---------- MONTHLY SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun MonthlyScreen(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onTaskClick: (com.example.yourassistantyora.screen.Task) -> Unit = {},
    onCreateTaskClick: () -> Unit = {},
    onNavigateToHome: () -> Unit = {},
    onNavigateToNotes: () -> Unit = {},
    onNavigateToTeam: () -> Unit = {},
    onNavigateToList: () -> Unit = {},
    onNavigateToDaily: () -> Unit = {},
    onNavigateToWeekly: () -> Unit = {}
) {
    // ✨ STATE UNTUK DETAIL SCREEN
    var showDetailScreen by remember { mutableStateOf(false) }
    var selectedTaskForDetail by remember { mutableStateOf<com.example.yourassistantyora.screen.Task?>(null) }

    var selectedViewMode by remember { mutableStateOf("Monthly") }
    var selectedStatus by remember { mutableStateOf("All") }
    var selectedCategory by remember { mutableStateOf("All") }
    val selectedTab = NavigationConstants.TAB_TASK
    val scope = rememberCoroutineScope()

    // Current calendar state
    val calendar = Calendar.getInstance()
    var currentMonth by remember { mutableStateOf(calendar.get(Calendar.MONTH)) }
    var currentYear by remember { mutableStateOf(calendar.get(Calendar.YEAR)) }
    var selectedDate by remember { mutableStateOf(24) }

    // State untuk monthly tasks
    var monthlyTasks by remember {
        mutableStateOf(
            listOf(
                MonthlyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        1,
                        "Review design mockups",
                        "06:00 AM",
                        "Medium",
                        "Work",
                        "To do"
                    ), 24, 9, 2025
                ),
                MonthlyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        2,
                        "Submit project report",
                        "06:00 AM",
                        "Low",
                        "Study",
                        "In Progress"
                    ), 24, 9, 2025
                ),
                MonthlyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        3,
                        "Team meeting preparation",
                        "10:00 AM",
                        "High",
                        "Work",
                        "Waiting"
                    ), 25, 9, 2025
                ),
                MonthlyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        4,
                        "Client presentation",
                        "15:00",
                        "High",
                        "Work",
                        "To do"
                    ), 26, 9, 2025
                ),
                MonthlyTaskItem(
                    _root_ide_package_.com.example.yourassistantyora.screen.Task(
                        5,
                        "Code review session",
                        "11:00",
                        "Medium",
                        "Work",
                        "Hold On"
                    ), 27, 9, 2025
                )
            )
        )
    }
    var showCreateTaskScreen by remember { mutableStateOf(false) }
    // ✨ CONDITIONAL RENDERING
    when {
        showDetailScreen && selectedTaskForDetail != null -> {
            TaskDetailScreen(
                task = selectedTaskForDetail!!,
                onBackClick = {
                    showDetailScreen = false
                    selectedTaskForDetail = null
                },
                onEditClick = {
                    // TODO: Handle edit
                },
                onDeleteClick = {
                    selectedTaskForDetail?.let { task ->
                        monthlyTasks = monthlyTasks.filter { it.task.id != task.id }
                    }
                    showDetailScreen = false
                    selectedTaskForDetail = null
                },
                onSaveChanges = {
                    showDetailScreen = false
                    selectedTaskForDetail = null
                }
            )
        }

        showCreateTaskScreen -> {
            CreateTaskScreen(
                onBackClick = { showCreateTaskScreen = false },
                onSaveClick = { newTask ->
                    val newMonthlyTask = MonthlyTaskItem(
                        task = newTask,
                        date = selectedDate,
                        month = currentMonth,
                        year = currentYear
                    )
                    monthlyTasks = monthlyTasks + newMonthlyTask
                    showCreateTaskScreen = false
                }
            )
        }

        else -> {
            fun handleTaskClick(task: com.example.yourassistantyora.screen.Task) {
                selectedTaskForDetail = task
                showDetailScreen = true
            }



            // Interaction states
            var swipedTaskId by remember { mutableStateOf<Int?>(null) }
            var lastCompletedTask by remember { mutableStateOf<MonthlyTaskItem?>(null) }
            var showUndoSnackbar by remember { mutableStateOf(false) }
            var lastDeletedTask by remember { mutableStateOf<MonthlyTaskItem?>(null) }
            var showDeleteSnackbar by remember { mutableStateOf(false) }
            var showRestoreDialog by remember { mutableStateOf(false) }
            var taskToRestore by remember { mutableStateOf<MonthlyTaskItem?>(null) }
            var deletingTask by remember { mutableStateOf<MonthlyTaskItem?>(null) }
            var showDeleteConfirmDialog by remember { mutableStateOf(false) }

            // Generate calendar dates
            val calendarDates = generateCalendarDates(currentMonth, currentYear, selectedDate)

            // Get month name
            val monthName = SimpleDateFormat("MMMM", Locale.getDefault()).apply {
                calendar.set(Calendar.MONTH, currentMonth)
            }.format(calendar.time)

            // Pisahkan task aktif dan selesai
            val activeTasks = monthlyTasks.filter { !it.task.isCompleted }
            val completedTasks = monthlyTasks.filter { it.task.isCompleted }

            // Filter tasks untuk selected date
            val filteredActiveTasks = activeTasks.filter { monthlyTask ->
                val task = monthlyTask.task
                val dateMatch = monthlyTask.date == selectedDate &&
                        monthlyTask.month == currentMonth &&
                        monthlyTask.year == currentYear
                val statusMatch = when (selectedStatus) {
                    "All" -> true
                    else -> task.status == selectedStatus
                }
                val categoryMatch = when (selectedCategory) {
                    "All" -> true
                    else -> task.category == selectedCategory
                }
                dateMatch && statusMatch && categoryMatch
            }

            val filteredCompletedTasks = completedTasks.filter { monthlyTask ->
                monthlyTask.date == selectedDate &&
                        monthlyTask.month == currentMonth &&
                        monthlyTask.year == currentYear
            }

            // Check if selected date is today
            val today = Calendar.getInstance()
            val isToday = selectedDate == today.get(Calendar.DAY_OF_MONTH) &&
                    currentMonth == today.get(Calendar.MONTH) &&
                    currentYear == today.get(Calendar.YEAR)

            // Functions
            fun onCheckboxClick(monthlyTask: MonthlyTaskItem) {
                if (!monthlyTask.task.isCompleted) {
                    monthlyTasks = monthlyTasks.map {
                        if (it.task.id == monthlyTask.task.id) {
                            it.copy(task = it.task.copy(isCompleted = true))
                        } else {
                            it
                        }
                    }
                    lastCompletedTask = monthlyTask
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
                lastCompletedTask?.let { t ->
                    monthlyTasks = monthlyTasks.map {
                        if (it.task.id == t.task.id) it.copy(task = it.task.copy(isCompleted = false)) else it
                    }
                }
                showUndoSnackbar = false
                lastCompletedTask = null
            }

            fun deleteTaskConfirmed(monthlyTask: MonthlyTaskItem) {
                lastDeletedTask = monthlyTask
                monthlyTasks = monthlyTasks.filter { it.task.id != monthlyTask.task.id }
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
                lastDeletedTask?.let { t ->
                    monthlyTasks = (monthlyTasks + t).sortedBy { it.task.id }
                }
                showDeleteSnackbar = false
                lastDeletedTask = null
            }

            fun showRestoreConfirmation(monthlyTask: MonthlyTaskItem) {
                taskToRestore = monthlyTask
                showRestoreDialog = true
                swipedTaskId = null
            }

            fun restoreTask() {
                taskToRestore?.let { t ->
                    monthlyTasks = monthlyTasks.map {
                        if (it.task.id == t.task.id) it.copy(task = it.task.copy(isCompleted = false)) else it
                    }
                }
                showRestoreDialog = false
                taskToRestore = null
            }

            Box(modifier = Modifier.fillMaxSize()) {
                Scaffold(
                    containerColor = Color(0xFFFAFAFA),
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    "My Tasks",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F1F1F)
                                )
                            },
                            actions = {
                                IconButton(onClick = { /* Search action */ }) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "Search",
                                        tint = Color(0xFF1F1F1F)
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = Color.White
                            )
                        )
                    },
                    bottomBar = {
                        BottomNavigationBar(
                            selectedTab = selectedTab,
                            onTabSelected = { index ->
                                when (index) {
                                    NavigationConstants.TAB_HOME -> onNavigateToHome()
                                    NavigationConstants.TAB_TASK -> { /* sudah di Task */
                                    }

                                    NavigationConstants.TAB_NOTE -> onNavigateToNotes()
                                    NavigationConstants.TAB_TEAM -> onNavigateToTeam()
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showCreateTaskScreen = true },
                            containerColor = Color(0xFF6C63FF),
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
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {
                        // View Mode Navigation
                        item {
                            TaskViewModeNavigation(
                                selectedViewMode = selectedViewMode,
                                onViewModeChange = { selectedViewMode = it },
                                onNavigateToList = onNavigateToList,
                                onNavigateToDaily = onNavigateToDaily,
                                onNavigateToWeekly = onNavigateToWeekly
                            )
                        }

                        // Filter Row
                        item {
                            TaskFilterRow(
                                selectedStatus = selectedStatus,
                                onStatusSelected = { selectedStatus = it },
                                selectedCategory = selectedCategory,
                                onCategorySelected = { selectedCategory = it },
                                categories = listOf("All", "Work", "Study", "Project")
                            )
                        }

                        // Calendar Section
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) {
                                // Month header with navigation
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$monthName     $currentYear",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F1F1F)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = {
                                                if (currentMonth == 0) {
                                                    currentMonth = 11
                                                    currentYear--
                                                } else {
                                                    currentMonth--
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.ChevronLeft,
                                                contentDescription = "Previous Month",
                                                tint = Color(0xFF1F1F1F),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                if (currentMonth == 11) {
                                                    currentMonth = 0
                                                    currentYear++
                                                } else {
                                                    currentMonth++
                                                }
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                Icons.Filled.ChevronRight,
                                                contentDescription = "Next Month",
                                                tint = Color(0xFF1F1F1F),
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(Modifier.height(20.dp))

                                // Day names header
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    listOf(
                                        "Su",
                                        "Mo",
                                        "Tu",
                                        "We",
                                        "Th",
                                        "Fr",
                                        "Sa"
                                    ).forEach { day ->
                                        Text(
                                            text = day,
                                            modifier = Modifier.weight(1f),
                                            textAlign = TextAlign.Center,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF757575)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(12.dp))

                                // Calendar grid
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(7),
                                    modifier = Modifier.height(280.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    userScrollEnabled = false
                                ) {
                                    items(
                                        items = calendarDates,
                                        key = { calDate -> "${calDate.day}_${calDate.isCurrentMonth}" }
                                    ) { calDate ->
                                        CalendarDateCell(
                                            date = calDate,
                                            onClick = {
                                                if (calDate.isCurrentMonth) {
                                                    selectedDate = calDate.day
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Selected date header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White)
                                    .padding(horizontal = 16.dp, vertical = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = getSelectedDateString(
                                        selectedDate,
                                        currentMonth,
                                        currentYear
                                    ),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1F1F1F)
                                )
                                if (isToday) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF6C63FF)
                                    ) {
                                        Text(
                                            text = "Today",
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 6.dp
                                            ),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        // Active Task List
                        items(
                            items = filteredActiveTasks,
                            key = { monthlyTask -> monthlyTask.task.id }
                        ) { monthlyTask ->
                            TaskCardDesignStyle(
                                task = monthlyTask.task,
                                onTaskClick = { handleTaskClick(monthlyTask.task) },
                                onCheckboxClick = { onCheckboxClick(monthlyTask) },
                                onDeleteIconClick = {
                                    deletingTask = monthlyTask
                                    showDeleteConfirmDialog = true
                                },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                swipedTaskId = swipedTaskId,
                                onSwipeChange = { id, isSwiped ->
                                    swipedTaskId = if (isSwiped) id else null
                                }
                            )
                        }

                        // Completed Tasks
                        if (filteredCompletedTasks.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Completed (${filteredCompletedTasks.size})",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF9E9E9E),
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 12.dp
                                    )
                                )
                            }
                            items(
                                items = filteredCompletedTasks,
                                key = { monthlyTask -> monthlyTask.task.id }
                            ) { monthlyTask ->
                                TaskCardDesignStyle(
                                    task = monthlyTask.task,
                                    onTaskClick = { handleTaskClick(monthlyTask.task) },
                                    onCheckboxClick = { showRestoreConfirmation(monthlyTask) },
                                    onDeleteIconClick = {
                                        deletingTask = monthlyTask
                                        showDeleteConfirmDialog = true
                                    },
                                    isCompleted = true,
                                    modifier = Modifier.padding(
                                        horizontal = 16.dp,
                                        vertical = 6.dp
                                    ),
                                    swipedTaskId = swipedTaskId,
                                    onSwipeChange = { id, isSwiped ->
                                        swipedTaskId = if (isSwiped) id else null
                                    }
                                )
                            }
                        }

                        // Empty state
                        if (filteredActiveTasks.isEmpty() && filteredCompletedTasks.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "No tasks for this day",
                                        fontSize = 14.sp,
                                        color = Color(0xFF9E9E9E)
                                    )
                                }
                            }
                        }

                        // Bottom spacing
                        item {
                            Spacer(Modifier.height(80.dp))
                        }
                    }
                }

                // Undo Snackbar
                AnimatedVisibility(
                    visible = showUndoSnackbar,
                    enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
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
                                Text("Task completed", color = Color.White, fontSize = 14.sp)
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
                        .padding(bottom = 80.dp, start = 16.dp, end = 16.dp)
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
                                Text("Task deleted", color = Color.White, fontSize = 14.sp)
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

                // Restore Dialog
                if (showRestoreDialog) {
                    AlertDialog(
                        onDismissRequest = { showRestoreDialog = false; taskToRestore = null },
                        title = { Text("Restore Task?") },
                        text = { Text("Do you want to move this task back to active tasks?") },
                        confirmButton = {
                            TextButton(onClick = { restoreTask() }) { Text("Yes") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showRestoreDialog = false; taskToRestore = null
                            }) {
                                Text("Cancel")
                            }
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
                            TextButton(onClick = {
                                showDeleteConfirmDialog = false
                                deletingTask = null
                            }) { Text("Batal") }
                        }
                    )
                }
            }
        }
    }
}