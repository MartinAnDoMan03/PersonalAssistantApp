package com.example.yourassistantyora

import android.content.Context
import android.media.MediaPlayer
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    userName: String = "Tom Holland",
    onNotificationClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    onTaskClick: (Task) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current
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

    // State untuk undo
    var lastCompletedTask by remember { mutableStateOf<Task?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }

    // Pisahkan task yang aktif dan selesai
    val activeTasks = tasks.filter { !it.isCompleted }
    val completedTasks = tasks.filter { it.isCompleted }

    // Hitung progress
    val totalTasks = tasks.size
    val progressPercentage = if (totalTasks > 0) (completedTasks.size * 100) / totalTasks else 0

    // Function untuk handle checkbox click
    fun onCheckboxClick(task: Task) {
        if (!task.isCompleted) {
            // Update task
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

            // Auto hide setelah 8 detik
            scope.launch {
                delay(8000)
                showUndoSnackbar = false
                lastCompletedTask = null
            }
        }
    }

    // Function untuk undo
    fun undoCompletion() {
        lastCompletedTask?.let { task ->
            tasks = tasks.map {
                if (it.id == task.id) {
                    it.copy(isCompleted = false)
                } else {
                    it
                }
            }
            showUndoSnackbar = false
            lastCompletedTask = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F7FA),
            bottomBar = {
                BottomNavigationBar(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Good Morning ",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "👋",
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = userName,
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
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
                                    onClick = onNotificationClick,
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
                            IconButton(onClick = onProfileClick, modifier = Modifier.size(36.dp)) {
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
                            TaskCard(
                                task = task,
                                onTaskClick = { onTaskClick(task) },
                                onCheckboxClick = { onCheckboxClick(task) }
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
                                TaskCard(
                                    task = task,
                                    onTaskClick = { onTaskClick(task) },
                                    onCheckboxClick = { },
                                    isCompleted = true
                                )
                            }
                        }
                    }
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

// ---------- TASK CARD ----------
@Composable
fun TaskCard(
    task: Task,
    onTaskClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    isCompleted: Boolean = false
) {
    val accentColors = if (task.category == "Personal") {
        listOf(Color(0xFF667EEA), Color(0xFF667EEA))
    } else {
        listOf(Color(0xFFF093FB), Color(0xFFF093FB))
    }

    val cardAlpha = if (isCompleted) 0.6f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = cardAlpha)
        ),
        onClick = onTaskClick
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
                            colors = if (isCompleted) {
                                listOf(Color(0xFF9E9E9E), Color(0xFF9E9E9E))
                            } else {
                                accentColors
                            }
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
                    color = if (isCompleted) Color(0xFF9E9E9E) else Color(0xFF2D2D2D),
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
                            tint = Color(0xFF9E9E9E),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(task.time, fontSize = 11.sp, color = Color(0xFF9E9E9E))
                    }

                    // Priority
                    Chip(
                        text = task.priority,
                        bg = Color(0xFFFFE5E5).copy(alpha = if (isCompleted) 0.5f else 1f),
                        fg = Color(0xFFE53935).copy(alpha = if (isCompleted) 0.5f else 1f)
                    )

                    // Category
                    if (task.category == "Team") {
                        Chip(
                            text = "Team",
                            bg = Color(0xFFFFF0F5).copy(alpha = if (isCompleted) 0.5f else 1f),
                            fg = Color(0xFFE91E63).copy(alpha = if (isCompleted) 0.5f else 1f),
                            leading = {
                                Icon(
                                    imageVector = Icons.Outlined.People,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63).copy(alpha = if (isCompleted) 0.5f else 1f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        )
                    } else {
                        Chip(
                            text = "Personal",
                            bg = Color(0xFFE3F2FD).copy(alpha = if (isCompleted) 0.5f else 1f),
                            fg = Color(0xFF1976D2).copy(alpha = if (isCompleted) 0.5f else 1f)
                        )
                    }

                    // Status
                    task.status?.let {
                        Chip(
                            text = it,
                            bg = Color(0xFFF3E5F5).copy(alpha = if (isCompleted) 0.5f else 1f),
                            fg = Color(0xFF9C27B0).copy(alpha = if (isCompleted) 0.5f else 1f)
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
                                color = Color(0xFF9E9E9E)
                            )
                            Text(
                                it,
                                fontSize = 11.sp,
                                color = if (isCompleted) Color(0xFF9E9E9E) else Color(0xFF6A70D7),
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
                                                }.copy(alpha = if (isCompleted) 0.5f else 1f)
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
                            }
                        }
                    }
                }
            }

            // Checkbox - Clickable
            Box(
                modifier = Modifier
                    .padding(top = 12.dp)
                    .size(22.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(enabled = !isCompleted) { onCheckboxClick() }
                    .then(
                        if (task.isCompleted) {
                            Modifier.background(Color(0xFF6A70D7))
                        } else {
                            Modifier.border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(6.dp))
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ---------- BOTTOM NAV ----------
@Composable
fun BottomNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp,
        modifier = Modifier.height(60.dp)
    ) {
        NavigationBarItem(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Home", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6A70D7),
                selectedTextColor = Color(0xFF6A70D7),
                unselectedIconColor = Color(0xFF9E9E9E),
                unselectedTextColor = Color(0xFF9E9E9E),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 1) Icons.Filled.Task else Icons.Outlined.Task,
                    contentDescription = "Task",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Task", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6A70D7),
                selectedTextColor = Color(0xFF6A70D7),
                unselectedIconColor = Color(0xFF9E9E9E),
                unselectedTextColor = Color(0xFF9E9E9E),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 2) Icons.Filled.Description else Icons.Outlined.Description,
                    contentDescription = "Note",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Note", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6A70D7),
                selectedTextColor = Color(0xFF6A70D7),
                unselectedIconColor = Color(0xFF9E9E9E),
                unselectedTextColor = Color(0xFF9E9E9E),
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = selectedTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    imageVector = if (selectedTab == 3) Icons.Filled.People else Icons.Outlined.People,
                    contentDescription = "Team",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = { Text("Team", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6A70D7),
                selectedTextColor = Color(0xFF6A70D7),
                unselectedIconColor = Color(0xFF9E9E9E),
                unselectedTextColor = Color(0xFF9E9E9E),
                indicatorColor = Color.Transparent
            )
        )
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun HomeScreenPreview() {
    YourAssistantYoraTheme {
        HomeScreen()
    }
}