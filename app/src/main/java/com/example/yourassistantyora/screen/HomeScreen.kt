package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.yourassistantyora.R
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.utils.NavigationConstants
import com.example.yourassistantyora.viewModel.TaskViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale


// ---------- DATA ----------
data class Task(
    val id: String,
    val title: String,
    val time: String,
    val priority: String,
    val category: String,
    val status: String? = null,
    val teamName: String? = null,
    val teamMembers: Int = 0,
    var isCompleted: Boolean = false,
    val teamId: String? = null,
    val teamMemberNames: List<String> = emptyList()
)

// ---------- SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TaskViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(NavigationConstants.TAB_HOME) }
    val scope = rememberCoroutineScope()
    var userName by remember { mutableStateOf("User") }
    var userPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Ambil data dari ViewModel
    val tasksForToday by viewModel.tasksForToday.collectAsState(emptyList())
    val isLoading by viewModel.isLoading.collectAsState(false)

    LaunchedEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            android.util.Log.d("HomeScreenDebug", "Current User UID: ${currentUser.uid}")
            val docRef = db.collection("users").document(currentUser.uid)

            docRef.addSnapshotListener { snapshot, e ->
                if (e != null) {
                    android.util.Log.e("HomeScreenDebug", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val fetchedName = snapshot.getString("username")
                    val fetchedPhoto = snapshot.getString("photoUrl")
                    android.util.Log.d("HomeScreenDebug", "Fetched 'username': '$fetchedName'")
                    android.util.Log.d("HomeScreenDebug", "Fetched 'photoUrl' length: ${fetchedPhoto?.length ?: 0}")

                    userName = fetchedName ?: "User"
                    userPhotoUrl = fetchedPhoto

                    android.util.Log.d("HomeScreenDebug", "Profile updated - Name: $userName")
                } else {
                    android.util.Log.d("HomeScreenDebug", "Snapshot is null or doesn't exist")
                }
            }
        }
    }

    // STATE untuk UI interactions
    var swipedTaskId by remember { mutableStateOf<String?>(null) }
    var lastCompletedTask by remember { mutableStateOf<TaskModel?>(null) }
    var showUndoSnackbar by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var taskToRestore by remember { mutableStateOf<TaskModel?>(null) }
    var deletingTask by remember { mutableStateOf<TaskModel?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Helper function untuk check completion
    fun isCompleted(task: TaskModel) = task.Status == 2

    // Pisahkan task yang aktif dan selesai dari ViewModel
    val activeTasks = tasksForToday.filter { !isCompleted(it) }
    val completedTasks = tasksForToday.filter { isCompleted(it) }

    // Hitung progress
    val totalTasks = tasksForToday.size
    val progressPercentage = if (totalTasks > 0) (completedTasks.size * 100) / totalTasks else 0

    // Function untuk handle checkbox click (complete task)
    fun onCheckboxClick(task: TaskModel) {
        if (!isCompleted(task)) {
            viewModel.updateTaskStatus(task.id, true) // Update ke Firestore via ViewModel
            lastCompletedTask = task
            showUndoSnackbar = true
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
        lastCompletedTask?.let {
            viewModel.updateTaskStatus(it.id, false)
        }
        showUndoSnackbar = false
        lastCompletedTask = null
    }

    // Function untuk delete task
    fun deleteTaskConfirmed(task: TaskModel) {
        viewModel.deleteTask(task.id)
        swipedTaskId = null
    }

    // show restore dialog
    fun showRestoreConfirmation(task: TaskModel) {
        taskToRestore = task
        showRestoreDialog = true
        swipedTaskId = null
    }

    // restore task (pindahkan completed -> active)
    fun restoreTask() {
        taskToRestore?.let {
            viewModel.updateTaskStatus(it.id, false)
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
                    val currentHour = remember {
                        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
                    }

                    val (greetingText, greetingIconRes) = remember(currentHour) {
                        when (currentHour) {
                            in 0..11 -> Pair("Good Morning", R.drawable.day_icon)
                            in 12..17 -> Pair("Good Afternoon", R.drawable.afternoon)
                            else -> Pair("Good Night", R.drawable.night)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$greetingText ",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Image(
                                    painter = painterResource(id = greetingIconRes),
                                    contentDescription = "Greeting Icon",
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
                            val currentDate = remember {
                                val dateFormat = java.text.SimpleDateFormat(
                                    "EEEE, MMMM dd, yyyy",
                                    java.util.Locale.ENGLISH
                                )
                                dateFormat.format(java.util.Date())
                            }
                            Text(
                                text = currentDate,
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
                                    onClick = { },
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
                                    if (!userPhotoUrl.isNullOrEmpty()) {
                                        AsyncImage(
                                            model = userPhotoUrl,
                                            contentDescription = "Profile Picture",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = if (userName.isNotEmpty()) userName.take(1)
                                                .uppercase() else "U",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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
                            TextButton(
                                onClick = { navController.navigateSingleTop("task_list") },
                                contentPadding = PaddingValues(0.dp)
                            ) {
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
                            TaskCardFromModel(
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
                                TaskCardFromModel(
                                    task = task,
                                    onTaskClick = {
                                        navController.navigate("task_detail/${task.id}")
                                    },
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
                    TextButton(onClick = {
                        showRestoreDialog = false; taskToRestore = null
                    }) { Text("Cancel") }
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
                        showDeleteConfirmDialog = false; deletingTask = null
                    }) { Text("Batal") }
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

// ---------- TASK CARD FROM TASKMODEL ----------
@Composable
fun TaskCardFromModel(
    task: TaskModel,
    onTaskClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    onDeleteIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false,
    swipedTaskId: String? = null,
    onSwipeChange: (String, Boolean) -> Unit = { _, _ -> }
) {
    val deleteWidth = 80.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val deleteOffset = remember { Animatable(0f) }

    // Priority-based strip color
    val stripColor = when (task.Priority) {
        2 -> Color(0xFFD32F2F) // High
        0 -> Color(0xFF1976D2) // Low
        else -> Color(0xFFEF6C00) // Medium
    }

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
        // Background layer untuk delete
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(stripColor),
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

        // Card Task di atas
        TaskCardModel(
            task = task,
            onTaskClick = onTaskClick,
            onCheckboxClick = onCheckboxClick,
            isCompleted = isCompleted,
            modifier = Modifier
                .offset { IntOffset(deleteOffset.value.roundToInt(), 0) }
                .zIndex(1f)
        )
    }
}

// ---------- TASK CARD MODEL ----------
@Composable
fun TaskCardModel(
    task: TaskModel,
    onTaskClick: () -> Unit,
    onCheckboxClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = false
) {
    // Priority-based strip color
    val baseStripColor = when (task.Priority) {
        2 -> Color(0xFFD32F2F) // High
        0 -> Color(0xFF1976D2) // Low
        else -> Color(0xFFEF6C00) // Medium
    }

    val backgroundColor = Color.White
    val stripColor = baseStripColor

    // Opacity untuk konten completed
    val contentAlpha = if (isCompleted) 0.5f else 1f
    val titleColor = Color(0xFF2D2D2D).copy(alpha = contentAlpha)
    val secondaryTextColor = Color(0xFF9E9E9E).copy(alpha = contentAlpha)

    // Format time
    val timeString = task.Deadline?.toDate()?.let { date ->
        java.text.SimpleDateFormat("hh:mm a", java.util.Locale.ENGLISH).format(date)
    } ?: "--:--"

    // Priority text
    val priorityText = when (task.Priority) {
        2 -> "High"
        0 -> "Low"
        else -> "Medium"
    }

    // Status text
    val statusText = when (task.Status) {
        0 -> "Waiting"
        1 -> "To do"
        2 -> "Done"
        3 -> "Hold On"
        4 -> "In Progress"
        else -> null
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onTaskClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(stripColor)
            )

            Spacer(Modifier.width(12.dp))

            // Konten
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 14.dp)
            ) {
                Text(
                    text = task.Title,
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
                    // Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            tint = secondaryTextColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(Modifier.width(3.dp))
                        Text(timeString, fontSize = 11.sp, color = secondaryTextColor)
                    }

                    // Priority
                    Chip(
                        text = priorityText,
                        bg = when (task.Priority) {
                            2 -> Color(0xFFFFEBEE)
                            0 -> Color(0xFFE3F2FD)
                            else -> Color(0xFFFFF3E0)
                        }.copy(alpha = if (isCompleted) 0.3f else 1f),
                        fg = when (task.Priority) {
                            2 -> Color(0xFFD32F2F)
                            0 -> Color(0xFF1976D2)
                            else -> Color(0xFFEF6C00)
                        }.copy(alpha = if (isCompleted) 0.6f else 1f)
                    )

                    // Category
                    val categoryName = task.categoryNamesSafe.firstOrNull() ?: "Personal"
                    Chip(
                        text = categoryName,
                        bg = Color(0xFFE8EAF6).copy(alpha = if (isCompleted) 0.3f else 1f),
                        fg = Color(0xFF3949AB).copy(alpha = if (isCompleted) 0.6f else 1f)
                    )

                    // Status
                    statusText?.let {
                        Chip(
                            text = it,
                            bg = Color(0xFFF3E5F5).copy(alpha = if (isCompleted) 0.3f else 1f),
                            fg = Color(0xFF9C27B0).copy(alpha = if (isCompleted) 0.6f else 1f)
                        )
                    }
                }
            }

            Spacer(Modifier.width(8.dp))

            // Checkbox
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(top = 12.dp, bottom = 14.dp, end = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { onCheckboxClick() },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF6A70D7),
                        uncheckedColor = Color(0xFFB0B0B0),
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
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
        )
    }
}