package com.example.yourassistantyora.screen

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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.utils.NavigationConstants
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import com.example.yourassistantyora.navigateSingleTop
import androidx.navigation.compose.rememberNavController

// ---------- DATA ----------
data class Note(
    val id: Int,
    val title: String,
    val content: String,
    val category: String,
    val time: String
)

// ---------- NOTE SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NoteScreen(
    navController: androidx.navigation.NavController,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    val selectedTab = NavigationConstants.TAB_NOTE

    // State untuk notes yang bisa diubah
    var notes by remember {
        mutableStateOf(
            listOf(
                Note(
                    1,
                    "Project Requirements",
                    "Discuss with team about new mobile app requirements. Need to finalize UI/UX designs and prepare for client presentation next...",
                    "Project",
                    "11:30 AM"
                ),
                Note(
                    2,
                    "Design Ideas",
                    "Consider using gradient backgrounds for the dashboard. Maybe add micro-interactions on task c...",
                    "Project",
                    "09:15 AM"
                ),
                Note(
                    3,
                    "Study Notes - React Hooks",
                    "useState: manages state in functional components. useEffect: handles side effects. useCallback: memoizes functions.",
                    "Study",
                    "06:00 AM"
                ),
                Note(
                    4,
                    "Meeting Notes",
                    "Discussed project timeline and deliverables with the team. Need to follow up on design mockups.",
                    "Work",
                    "02:30 PM"
                ),
                Note(
                    5,
                    "Book Summary",
                    "Atomic Habits by James Clear - Small habits make a big difference over time.",
                    "Idea",
                    "Yesterday"
                )
            )
        )
    }

    // State untuk melacak note yang di-slide
    var swipedNoteId by remember { mutableStateOf<Int?>(null) }

    // State untuk undo deletion
    var lastDeletedNote by remember { mutableStateOf<Note?>(null) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }

    // State untuk delete confirmation
    var deletingNote by remember { mutableStateOf<Note?>(null) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    // Filter notes berdasarkan category yang dipilih
    val filteredNotes = notes.filter { note ->
        when (selectedCategory) {
            "All" -> true
            else -> note.category == selectedCategory
        }
    }

    // Function untuk delete note
    fun deleteNoteConfirmed(note: Note) {
        lastDeletedNote = note
        notes = notes.filter { it.id != note.id }
        showDeleteSnackbar = true
        swipedNoteId = null

        scope.launch {
            delay(8000)
            showDeleteSnackbar = false
            lastDeletedNote = null
        }
    }

    // Function untuk undo deletion
    fun undoDelete() {
        lastDeletedNote?.let { n ->
            notes = (notes + n).sortedBy { it.id }
        }
        showDeleteSnackbar = false
        lastDeletedNote = null
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F7FA),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "My Notes",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2D2D2D)
                        )
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
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        when (index) {
                            NavigationConstants.TAB_HOME -> navController.navigateSingleTop("home")
                            NavigationConstants.TAB_TASK -> navController.navigateSingleTop("task_list")
                            NavigationConstants.TAB_NOTE -> { /* sudah di notes */ }
                            NavigationConstants.TAB_TEAM -> navController.navigateSingleTop("team")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navController.navigate("create_note") },
                    containerColor = Color(0xFF6A70D7),
                    contentColor = Color.White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Note",
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
                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryChip(
                        text = "All (${notes.size})",
                        isSelected = selectedCategory == "All",
                        onClick = { selectedCategory = "All" }
                    )
                    CategoryChip(
                        text = "Work",
                        isSelected = selectedCategory == "Work",
                        onClick = { selectedCategory = "Work" }
                    )
                    CategoryChip(
                        text = "Study",
                        isSelected = selectedCategory == "Study",
                        onClick = { selectedCategory = "Study" }
                    )
                    CategoryChip(
                        text = "Project",
                        isSelected = selectedCategory == "Project",
                        onClick = { selectedCategory = "Project" }
                    )
                    CategoryChip(
                        text = "Idea",
                        isSelected = selectedCategory == "Idea",
                        onClick = { selectedCategory = "Idea" }
                    )
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // Notes List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotes, key = { it.id }) { note ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            NoteCardWithSwipe(
                                note = note,
                                onNoteClick = {
                                    navController.navigate("note_detail/${note.id}")
                                },
                                onDeleteIconClick = {
                                    deletingNote = note
                                    showDeleteConfirmDialog = true
                                },
                                swipedNoteId = swipedNoteId,
                                onSwipeChange = { id, isSwiped ->
                                    if (isSwiped) {
                                        swipedNoteId = id
                                    } else if (swipedNoteId == id) {
                                        swipedNoteId = null
                                    }
                                }
                            )
                        }
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
                            "Note deleted",
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

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteConfirmDialog = false; deletingNote = null },
                title = { Text("Hapus catatan?") },
                text = { Text("Apakah kamu yakin ingin menghapus catatan ini?") },
                confirmButton = {
                    TextButton(onClick = {
                        deletingNote?.let { deleteNoteConfirmed(it) }
                        showDeleteConfirmDialog = false
                        deletingNote = null
                    }) {
                        Text("Hapus", color = Color(0xFFF44336))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteConfirmDialog = false; deletingNote = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

// ---------- CATEGORY CHIP ----------
@Composable
fun CategoryChip(
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

// ---------- NOTE CARD WITH SWIPE ----------
@Composable
fun NoteCardWithSwipe(
    note: Note,
    onNoteClick: () -> Unit,
    onDeleteIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    swipedNoteId: Int? = null,
    onSwipeChange: (Int, Boolean) -> Unit = { _, _ -> }
) {
    val deleteWidth = 80.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val deleteOffset = remember { Animatable(0f) }

    // Strip color berdasarkan category
    val stripColor = when (note.category) {
        "Work" -> Color(0xFF667EEA)
        "Study" -> Color(0xFF64B5F6)
        "Project" -> Color(0xFFEF5350)
        "Idea" -> Color(0xFFFFB74D)
        else -> Color(0xFF9E9E9E)
    }

    LaunchedEffect(swipedNoteId) {
        val deleteWidthPx = with(density) { deleteWidth.toPx() }
        val isOpen = deleteOffset.value < 0f
        if (swipedNoteId != null && swipedNoteId != note.id && isOpen) {
            deleteOffset.animateTo(0f, tween(300))
        } else if (swipedNoteId == null && isOpen) {
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
                            onSwipeChange(note.id, target != 0f)
                        }
                    },
                    onDragCancel = {
                        scope.launch { deleteOffset.snapTo(0f); onSwipeChange(note.id, false) }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (deleteOffset.value + dragAmount).coerceIn(-deleteWidthPx, 0f)
                        scope.launch { deleteOffset.snapTo(newOffset) }
                    }
                )
            }
    ) {
        // Background Swipe (warna strip)
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(stripColor)
        ) {
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

        // Card Putih
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(deleteOffset.value.roundToInt(), 0) }
                .zIndex(1f)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onNoteClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Strip Kiri
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(stripColor)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(12.dp)
                ) {
                    // Title
                    Text(
                        text = note.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D2D2D),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(6.dp))

                    // Content
                    Text(
                        text = note.content,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    Spacer(Modifier.height(10.dp))

                    // Footer (Category + Time)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Category Badge
                        Surface(
                            color = when (note.category) {
                                "Work" -> Color(0xFFE3F2FD)
                                "Study" -> Color(0xFFE8F5E9)
                                "Project" -> Color(0xFFFFEBEE)
                                "Idea" -> Color(0xFFFFF3E0)
                                else -> Color(0xFFF5F5F5)
                            },
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = note.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                color = when (note.category) {
                                    "Work" -> Color(0xFF1976D2)
                                    "Study" -> Color(0xFF388E3C)
                                    "Project" -> Color(0xFFD32F2F)
                                    "Idea" -> Color(0xFFEF6C00)
                                    else -> Color(0xFF616161)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Time
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text(
                                text = note.time,
                                fontSize = 10.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------- PREVIEW ----------
@Preview(showBackground = true)
@Composable
fun NoteScreenPreview() {
    YourAssistantYoraTheme {
        val navController = rememberNavController()
        NoteScreen(
            navController = navController,
            modifier = Modifier.fillMaxSize()
        )
    }
}