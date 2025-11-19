package com.example.yourassistantyora.screen

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.utils.NavigationConstants
import androidx.navigation.NavController
import com.example.yourassistantyora.navigateSingleTop
import androidx.navigation.compose.rememberNavController

// ---------- DATA ----------
data class Task(
    val id: Int,
    val title: String,
    val time: String,
    val priority: String,
    val category: String,
    val status: String? = null,
    val teamName: String? = null,
    val teamMembers: Int = 0,
    var isCompleted: Boolean = false
)

// ---------- SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
navController: NavController,
userName: String = "Tom Holland",
modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_HOME) }
    val scope = rememberCoroutineScope()

    // State untuk tasks yang bisa diubah
    var tasks by remember {
        mutableStateOf(
            listOf(
                Task(1, "Morning workout routine", "06:00 AM", "High", "Personal", "Meeting"),
                Task(2, "Review design mockups", "10:00 AM", "High", "Team", "Waiting", "Mobile Dev Team", 3),
                Task(3, "Prepare presentation slides", "02:00 PM", "Medium", "Personal"),
                Task(4, "Team sync meeting", "04:00 PM", "High", "Team", "Meeting", "Mobile Dev Team", 3)
            )
        )
    }

    // STATE BARU: Melacak Task ID yang sedang di-slide (untuk Single Slide)
    var swipedTaskId by remember { mutableStateOf<Int?>(null) }

    // State untuk undo completion
    var lastCompletedTask by remember { mutableStateOf<Task?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    // State untuk undo deletion
    var lastDeletedTask by remember { mutableStateOf<Task?>(null) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }

    // State untuk dialog restore (memindahkan completed ke active)
    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<Task?>(null) }

    // State untuk delete confirmation
    var deletingTask by remember { mutableStateOf<Task?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Pisahkan task yang aktif dan selesai
    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    // Hitung progress
    val totalTasks = tasks.size
    val progressPercentage = if (totalTasks > 0) (completedTasks.size * 100) / totalTasks else 0

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

            // Simpan untuk undo
            lastCompletedTask = task
            showUndoSnackbar = true
            showDeleteSnackbar = false

            // Tutup slide jika ada
            swipedTaskId = null

            // Auto hide setelah 8 detik
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

    // Function untuk delete task (dipanggil setelah konfirmasi)
    fun deleteTaskConfirmed(task: Task) {
        lastDeletedTask = task
        tasks = tasks.filter { it.id != task.id }
        showDeleteSnackbar = true
        showUndoSnackbar = false

        // Tutup slide jika ada
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
            // masukkan kembali ke list (sederhana: append lalu sort berdasarkan id supaya deterministik)
            tasks = (tasks + t).sortedBy { it.id }
        }
        showDeleteSnackbar = false
        lastDeletedTask = null
    }

    // show restore dialog
    fun showRestoreConfirmation(task: Task) {
        taskToRestore = task
        showRestoreDialog = true
        swipedTaskId = null // Tutup slide sebelum menampilkan dialog
    }

    // restore task (pindahkan completed -> active)
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
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        when (index) {
                            NavigationConstants.TAB_HOME -> {
                                selectedTab = index
                                // tetap di home
                            }
                            NavigationConstants.TAB_TASK -> {
                                selectedTab = index
                                navController.navigateSingleTop("task_list")
                            }
                            NavigationConstants.TAB_NOTE -> {
                                selectedTab = index
                                navController.navigateSingleTop("notes")
                            }
                            NavigationConstants.TAB_TEAM -> {
                                selectedTab = index
                                navController.navigateSingleTop("team")
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // ---------- HEADER ----------
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    val inspection = LocalInspectionMode.current
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column (modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Good Morning ",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Image(
                                    painter = painterResource(id = R.drawable.day_icon),
                                    contentDescription = "Day Icon",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = userName,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Saturday, October 23, 2025",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                IconButton(
                                    onClick = {
                                        // TODO: nanti bikin route "notifications"
                                        // navController.navigateSingleTop("notifications")
                                    },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Notifications,
                                        contentDescription = "Notifications",
                                        tint = Color.White
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-2).dp, y = 2.dp)
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD54F))
                                )
                            }
                            IconButton(
                                onClick = { navController.navigateSingleTop("profile") },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFB74D)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("T", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }

                        }
                    }
                }

                // ---------- CONTENT ----------
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Progress Card
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Today's Progress",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2D2D2D)
                                    )
                                    Text(
                                        "$progressPercentage %",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6A70D7)
                                    )
                                }
                                Spacer(Modifier.height(12.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFEDEBFF))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progressPercentage / 100f)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
                                                )
                                            )
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "${completedTasks.size} of $totalTasks tasks completed",
                                    fontSize = 11.sp,
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                    }

                    // Header Active Tasks
                    item {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Today's Tasks",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D2D2D)
                            )
                            TextButton(onClick = { }, contentPadding = PaddingValues(0.dp)) {
                                Text(
                                    "See All",
                                    color = Color(0xFF6A70D7),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Active Task list
                    items(activeTasks, key = { it.id }) { task ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            TaskCardWithTrailingDelete(
                                task = task,
                                onTaskClick = {
                                    navController.navigate("task_detail/${task.id}")
                                },
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

                    // Header Completed Tasks
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

                        // Completed Task list
                        items(completedTasks, key = { it.id }) { task ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                TaskCardWithTrailingDelete(
                                    task = task,
                                    onTaskClick = {
                                        navController.navigate("task_detail/${task.id}")
                                    },
                                    onCheckboxClick = { showRestoreConfirmation(task) }, // akan memunculkan dialog restore
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

        // Restore Confirmation Dialog (AlertDialog)
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

// ---------- CHIP ----------
@Composable
private fun Chip(
    text: String,
    bg: Color,
    fg: Color,
    leading: (@Composable () -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (leading != null) {
            leading()
            Spacer(Modifier.width(4.dp))
        }
        Text(text, fontSize = 10.sp, color = fg, fontWeight = FontWeight.Medium)
    }
}

// ---------- TASK CARD (Dasar) ----------
// Versi yang lebih terang untuk completed (kurangi kegelapan)
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    val accentColors = if (task.category == "Personal") {
        listOf(Color(0xFF667EEA), Color(0xFF667EEA))
    } else {
        listOf(Color(0xFFF093FB), Color(0xFFF093FB))
    }

    // New: warna latar untuk completed dibuat SANGAT LIGHT agar tidak gelap
    val backgroundColor = if (isCompleted) {
        // Warna mendekati background utama, sedikit lebih terang supaya terlihat "soft card"
        Color(0xFFF7F7F9)
    } else Color.White

    // Border lebih ringan untuk completed agar terlihat plate
    val borderColor = if (isCompleted) Color(0xFFDDDDDD) else Color.Transparent

    // Strip kiri dibuat lebih soft untuk completed
    val stripColor = if (isCompleted) Color(0xFFBDBDBD) else accentColors[0]

    // Konten dibuat pudar (alpha) tapi bukan terlalu gelap
    val contentAlpha = if (isCompleted) 0.6f else 1f
    val titleColor = Color(0xFF2D2D2D).copy(alpha = contentAlpha)
    val secondaryTextColor = Color(0xFF9E9E9E).copy(alpha = contentAlpha)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (isCompleted) 0.dp else 2.dp, RoundedCornerShape(14.dp))
            .then(if (isCompleted) Modifier.border(1.dp, borderColor, RoundedCornerShape(14.dp)) else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onTaskClick,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 0.dp else 3.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Strip Kiri
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 14.dp, bottomStart = 14.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(stripColor, stripColor)
                        )
                    )
            )

            Spacer(Modifier.width(10.dp))

            // Konten
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

                Spacer(Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(task.time, fontSize = 11.sp, color = secondaryTextColor)
                    }

                    // Priority
                    Chip(
                        text = task.priority,
                        bg = Color(0xFFFFE5E5).copy(alpha = if (isCompleted) 0.22f else 1f),
                        fg = Color(0xFFE53935).copy(alpha = if (isCompleted) 0.7f else 1f)
                    )

                    // Category
                    if (task.category == "Team") {
                        Chip(
                            text = "Team",
                            bg = Color(0xFFFFF0F5).copy(alpha = if (isCompleted) 0.22f else 1f),
                            fg = Color(0xFFE91E63).copy(alpha = if (isCompleted) 0.7f else 1f),
                            leading = {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63).copy(alpha = if (isCompleted) 0.7f else 1f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                    } else {
                        Chip(
                            text = "Personal",
                            bg = Color(0xFFE3F2FD).copy(alpha = if (isCompleted) 0.22f else 1f),
                            fg = Color(0xFF1976D2).copy(alpha = if (isCompleted) 0.7f else 1f)
                        )
                    }

                    // Status
                    task.status?.let {
                        Chip(
                            text = it,
                            bg = Color(0xFFF3E5F5).copy(alpha = if (isCompleted) 0.22f else 1f),
                            fg = Color(0xFF9C27B0).copy(alpha = if (isCompleted) 0.7f else 1f)
                        )
                    }
                }

                // Team info
                task.teamName?.let {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Team: ",
                                fontSize = 11.sp,
                                color = secondaryTextColor
                            )
                            Text(
                                it,
                                fontSize = 11.sp,
                                color = if (isCompleted) secondaryTextColor else Color(0xFF6A70D7),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        if (task.teamMembers > 0) {
                            Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                                repeat(minOf(task.teamMembers, 3)) { idx ->
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (idx) {
                                                    0 -> Color(0xFFE91E63)
                                                    1 -> Color(0xFF9C27B0)
                                                    else -> Color(0xFF673AB7)
                                                }.copy(alpha = if (isCompleted) 0.55f else 1f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            ('A' + idx).toString(),
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (task.teamMembers > 3) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF9E9E9E).copy(alpha = if (isCompleted) 0.55f else 1f))
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "+${task.teamMembers - 3}",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.width(10.dp))

            // Checkbox (ubah warna centang menjadi ungu ketika done)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 14.dp, bottom = 14.dp, end = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onCheckboxClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF7353AD), // ungu untuk "done"
                        uncheckedColor = Color(0xFF9E9E9E),
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    }
}

// ---------- TASK CARD WITH SWIPE (Modifikasi) ----------
@Composable
fun TaskCardWithTrailingDelete(
    task: Task,
    onTaskClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    onDeleteIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false,
    // PARAMETER BARU: State dari luar dan callback
    swipedTaskId: Int? = null,
    onSwipeChange: (Int, Boolean) -> Unit = { _, _ -> }
) {
    // Lebar "Hapus" icon
    val deleteWidth = 80.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    // Ganti remember { mutableStateOf(0f) } dengan remember { Animatable(0f) }
    val deleteOffset = remember { Animatable(0f) }

    // Tentukan warna berdasarkan kategori task
    val accentColors = if (task.category == "Personal") {
        listOf(Color(0xFF667EEA), Color(0xFF667EEA))
    } else {
        listOf(Color(0xFFF093FB), Color(0xFFF093FB))
    }

    // EFEK BARU: Mengamati swipedTaskId dari luar.
    LaunchedEffect(swipedTaskId) {
        val deleteWidthPx = with(density) { deleteWidth.toPx() }
        val isCardCurrentlyOpen = deleteOffset.value < 0f

        if (swipedTaskId != null && swipedTaskId != task.id) {
            if (isCardCurrentlyOpen) {
                scope.launch {
                    deleteOffset.animateTo(0f, animationSpec = tween(300))
                }
            }
        } else if (swipedTaskId == null && isCardCurrentlyOpen) {
            scope.launch {
                deleteOffset.animateTo(0f, animationSpec = tween(300))
            }
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
                            val target = if (deleteOffset.value < -deleteWidthPx / 2) {
                                -deleteWidthPx
                            } else {
                                0f
                            }
                            deleteOffset.animateTo(target, animationSpec = tween(300))
                            onSwipeChange(task.id, target != 0f)
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            val target = if (deleteOffset.value < -deleteWidthPx / 2) {
                                -deleteWidthPx
                            } else {
                                0f
                            }
                            deleteOffset.animateTo(target, animationSpec = tween(300))
                            onSwipeChange(task.id, target != 0f)
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = deleteOffset.value + dragAmount
                        val clampedOffset = newOffset.coerceIn(-deleteWidthPx, 0f)
                        scope.launch {
                            deleteOffset.snapTo(clampedOffset)
                        }
                    }
                )
            }
    ) {
        // Latar belakang (lebih subtle, jangan gelap saat completed)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = if (isCompleted) {
                            // very light gradient for completed background behind card
                            listOf(Color(0xFFF8F8F8), Color(0xFFF5F5F5))
                        } else {
                            accentColors
                        }
                    )
                ),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .width(deleteWidth)
                    .fillMaxHeight()
                    .clickable(onClick = onDeleteIconClick)
                    .background(Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Hapus",
                        tint = if (isCompleted) Color(0xFF555555).copy(alpha = 0.7f) else Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Hapus",
                        color = if (isCompleted) Color(0xFF555555).copy(alpha = 0.7f) else Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Konten Card Task (DI ATAS)
        TaskCard(
            task = task,
            onTaskClick = onTaskClick,
            onCheckboxClick = onCheckboxClick,
            isCompleted = isCompleted,
            modifier = Modifier.offset { IntOffset(deleteOffset.value.roundToInt(), 0) }
        )
    }
}


// ---------- PREVIEW ----------
@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    YourAssistantYoraTheme {
        val navController = rememberNavController()
        HomeScreen(
            navController = navController,
            modifier = Modifier.fillMaxSize(),
            userName = "Tom Holland"
        )
    }
}
