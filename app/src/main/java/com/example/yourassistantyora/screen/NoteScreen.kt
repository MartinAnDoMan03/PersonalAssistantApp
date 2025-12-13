package com.example.yourassistantyora.screen

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.yourassistantyora.components.BottomNavigationBar
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.utils.NavigationConstants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

// ========================================
// 1. DATA MODEL
// ========================================
data class Note(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val categories: List<String> = listOf(),
    val time: String,
    val userId: String,
    val rawTimestamp: Long = 0
)

// ========================================
// 2. VIEW MODEL
// ========================================
class NoteViewModel : ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    init {
        fetchNotes()
    }

    private fun fetchNotes() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("notes")
                .whereEqualTo("user_id", currentUser.uid)
                .addSnapshotListener { snapshot, e ->
                    if (e != null || snapshot == null) return@addSnapshotListener

                    val notesList = snapshot.documents.map { doc ->
                        val categoriesList = doc.get("categories") as? List<String>
                        val categoryStr = categoriesList?.firstOrNull() ?: "General"

                        val timestamp = doc.getTimestamp("created_at")
                        val date = timestamp?.toDate()
                        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                        val timeStr = if (date != null) sdf.format(date) else ""

                        Note(
                            id = doc.id,
                            title = doc.getString("title") ?: "",
                            content = doc.getString("note") ?: "",
                            category = categoryStr,
                            categories = categoriesList ?: listOf("General"),
                            time = timeStr,
                            userId = doc.getString("user_id") ?: "",
                            rawTimestamp = timestamp?.seconds ?: 0
                        )
                    }
                    _notes.value = notesList.sortedByDescending { it.rawTimestamp }
                }
        } else {
            _notes.value = emptyList()
        }
    }

    fun deleteNote(noteId: String) {
        db.collection("notes").document(noteId).delete()
    }

    fun restoreNote(note: Note) {
        val currentUser = auth.currentUser ?: return
        val noteData = hashMapOf(
            "title" to note.title,
            "note" to note.content,
            "categories" to note.categories,
            "user_id" to currentUser.uid,
            "created_at" to com.google.firebase.Timestamp.now()
        )
        db.collection("notes").add(noteData)
    }

    fun updateNote(noteId: String, title: String, content: String, categories: List<String>) {
        db.collection("notes").document(noteId).update(
            mapOf(
                "title" to title,
                "note" to content,
                "categories" to categories
            )
        )
    }
}

// ========================================
// 3. UTILITY FUNCTIONS
// ========================================
fun generateColorForCategory(category: String): Color {
    val hashCode = category.hashCode()
    val colors = listOf(
        Color(0xFF667EEA), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
        Color(0xFF5C6BC0), Color(0xFF42A5F5), Color(0xFF29B6F6), Color(0xFF26C6DA),
        Color(0xFF26A69A), Color(0xFF66BB6A), Color(0xFF9CCC65), Color(0xFFD4E157),
        Color(0xFFFFEE58), Color(0xFFFFCA28), Color(0xFFFF7043), Color(0xFF8D6E63)
    )
    val index = hashCode.absoluteValue % colors.size
    return colors[index]
}

// ========================================
// 4. REUSABLE COMPONENTS
// ========================================

// Filter Category Chip (untuk top filter bar)
@Composable
fun NoteCategoryChip(
    text: String,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        color = if (isSelected) Color(0xFF6A70D7) else Color(0xFFF5F7FA),
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color(0xFF666666),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}

// Category Badge (untuk card note)
@Composable
fun CategoryBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Warna background dan text disesuaikan dengan category - SAMA DENGAN CREATE
    val (bgColor, textColor) = when (text.lowercase()) {
        "work" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        "study" -> Color(0xFFE8F5E9) to Color(0xFF388E3C)
        "project" -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        "idea" -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
        "travel" -> Color(0xFFE0F2F1) to Color(0xFF00897B)
        "meeting" -> Color(0xFFF3E5F5) to Color(0xFF8E24AA)
        else -> color.copy(alpha = 0.15f) to color
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = bgColor
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

// Category Button (untuk edit mode)
@Composable
fun NoteCategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    val containerColor = if (isSelected) color else Color.White
    val contentColor = if (isSelected) Color.White else color

    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = BorderStroke(1.5.dp, color),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// Action Chip (untuk Add New)
@Composable
fun ActionChip(
    text: String,
    onClick: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text, fontSize = 12.sp) },
        leadingIcon = { Icon(icon, contentDescription = null, Modifier.size(16.dp)) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = Color(0xFFE0E0E0),
            labelColor = Color(0xFF424242),
            leadingIconContentColor = Color(0xFF424242)
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

// ========================================
// 5. NOTE CARD WITH SWIPE
// ========================================
@Composable
fun NoteCardWithSwipe(
    note: Note,
    onNoteClick: () -> Unit,
    onDeleteSwipe: () -> Unit,
    swipedNoteId: String?,
    onSwipeChange: (String, Boolean) -> Unit
) {
    val deleteWidth = 80.dp
    val density = androidx.compose.ui.platform.LocalDensity.current
    val scope = rememberCoroutineScope()
    val deleteOffset = remember { Animatable(0f) }

    // Strip color berdasarkan category pertama - SAMA DENGAN CREATE SCREEN
    val stripColor = when (note.category.lowercase()) {
        "work" -> Color(0xFF667EEA)
        "study" -> Color(0xFF64B5F6)
        "project" -> Color(0xFFEF5350)
        "idea" -> Color(0xFFFFB74D)
        "travel" -> Color(0xFF4DB6AC)
        "meeting" -> Color(0xFF9575CD)
        else -> generateColorForCategory(note.category)
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
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                val deleteWidthPx = with(density) { deleteWidth.toPx() }
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            val target = if (deleteOffset.value < -deleteWidthPx / 2) -deleteWidthPx else 0f
                            deleteOffset.animateTo(target, tween(300))
                            if (target != 0f) {
                                onSwipeChange(note.id, true)
                            } else {
                                onSwipeChange(note.id, false)
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            deleteOffset.snapTo(0f)
                            onSwipeChange(note.id, false)
                        }
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
                    .clickable(onClick = onDeleteSwipe),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Delete",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Card Putih dengan Strip
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

                    // Footer (Categories + Time)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Categories - Tampilkan maksimal 2, sisanya +X
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ambil maksimal 2 category pertama
                            note.categories.take(2).forEach { category ->
                                val categoryColor = when (category.lowercase()) {
                                    "work" -> Color(0xFF667EEA)
                                    "study" -> Color(0xFF64B5F6)
                                    "project" -> Color(0xFFEF5350)
                                    "idea" -> Color(0xFFFFB74D)
                                    "travel" -> Color(0xFF4DB6AC)
                                    "meeting" -> Color(0xFF9575CD)
                                    else -> generateColorForCategory(category)
                                }

                                CategoryBadge(
                                    text = category,
                                    color = categoryColor
                                )
                            }

                            // Tampilkan +X jika ada lebih dari 2 category
                            if (note.categories.size > 2) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFEEEEEE)
                                ) {
                                    Text(
                                        text = "+${note.categories.size - 2}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF757575),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        // Time
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(12.dp)
                            )
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

// ========================================
// 6. NOTE SCREEN (LIST)
// ========================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun NoteScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel = viewModel()
) {
    val notes by viewModel.notes.collectAsState()
    var selectedCategory by remember { mutableStateOf("All") }
    val scope = rememberCoroutineScope()
    val selectedTab = NavigationConstants.TAB_NOTE

    var swipedNoteId by remember { mutableStateOf<String?>(null) }
    var lastDeletedNote by remember { mutableStateOf<Note?>(null) }
    var showDeleteSnackbar by remember { mutableStateOf(false) }

    // State untuk dialog konfirmasi delete dari swipe
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteToDelete by remember { mutableStateOf<Note?>(null) }

    val allCategories = remember(notes) {
        notes.flatMap { it.categories }.distinct().sorted()
    }

    val filteredNotes = notes.filter { note ->
        when (selectedCategory) {
            "All" -> true
            else -> note.categories.any { it.equals(selectedCategory, ignoreCase = true) }
        }
    }

    // Fungsi untuk menampilkan dialog konfirmasi
    fun confirmDeleteNote(note: Note) {
        noteToDelete = note
        showDeleteDialog = true
    }

    // Fungsi untuk benar-benar menghapus setelah konfirmasi
    fun deleteNoteConfirmed() {
        noteToDelete?.let { note ->
            lastDeletedNote = note
            viewModel.deleteNote(note.id)
            showDeleteSnackbar = true

            scope.launch {
                delay(4000)
                showDeleteSnackbar = false
                lastDeletedNote = null
            }
        }
        showDeleteDialog = false
        noteToDelete = null
    }

    fun undoDelete() {
        lastDeletedNote?.let { n ->
            viewModel.restoreNote(n)
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    selectedTab = selectedTab,
                    onTabSelected = { index ->
                        when (index) {
                            NavigationConstants.TAB_HOME -> navController.navigateSingleTop("home")
                            NavigationConstants.TAB_TASK -> navController.navigateSingleTop("task_list")
                            NavigationConstants.TAB_NOTE -> { /* current */ }
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
                        Icons.Filled.Add,
                        contentDescription = "Create",
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
                // Category Filter
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteCategoryChip(
                        text = "All (${notes.size})",
                        isSelected = selectedCategory == "All",
                        onClick = { selectedCategory = "All" }
                    )

                    allCategories.forEach { cat ->
                        NoteCategoryChip(
                            text = cat.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.ROOT)
                                else it.toString()
                            },
                            isSelected = selectedCategory.equals(cat, ignoreCase = true),
                            onClick = { selectedCategory = cat }
                        )
                    }
                }

                Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

                // Notes List
                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Outlined.Description,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(64.dp)
                            )
                            Text(
                                "No notes found",
                                color = Color.Gray,
                                fontSize = 16.sp
                            )
                        }
                    }
                } else {
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
                                    onDeleteSwipe = { confirmDeleteNote(note) },
                                    swipedNoteId = swipedNoteId,
                                    onSwipeChange = { id, isSwiped ->
                                        if (isSwiped) swipedNoteId = id
                                        else if (swipedNoteId == id) swipedNoteId = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // Dialog Konfirmasi Delete dari Swipe - DESIGN LAMA
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {
                    showDeleteDialog = false
                    noteToDelete = null
                },
                title = {
                    Text(
                        "Hapus catatan?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Text(
                        "Apakah kamu yakin ingin menghapus catatan ini?",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = { deleteNoteConfirmed() }
                    ) {
                        Text(
                            "Hapus",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            noteToDelete = null
                        }
                    ) {
                        Text(
                            "Batal",
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }

        // Undo Snackbar
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
                            Icons.Filled.Delete,
                            null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text("Note deleted", color = Color.White, fontSize = 14.sp)
                    }
                    TextButton(onClick = { undoDelete() }) {
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

// ========================================
// 7. NOTE DETAIL SCREEN
// ========================================
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NoteDetailScreen(
    noteId: String?,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: NoteViewModel = viewModel()
) {
    val notes by viewModel.notes.collectAsState()
    val note = remember(noteId, notes) {
        noteId?.let { id -> notes.find { it.id == id } }
    }

    var title by remember(note) { mutableStateOf(note?.title ?: "") }
    var content by remember(note) { mutableStateOf(note?.content ?: "") }
    var selectedCategories by remember(note) {
        mutableStateOf(note?.categories ?: listOf("Work"))
    }

    val predefinedCategories = listOf(
        "Work" to Color(0xFF667EEA),
        "Study" to Color(0xFF64B5F6),
        "Project" to Color(0xFFEF5350),
        "Idea" to Color(0xFFFFB74D),
        "Travel" to Color(0xFF4DB6AC),
        "Meeting" to Color(0xFF9575CD)
    )

    var customCategories by remember(note) {
        mutableStateOf<List<Pair<String, Color>>>(
            note?.categories
                ?.filter { cat ->
                    !predefinedCategories.any { it.first.equals(cat, ignoreCase = true) }
                }
                ?.map { it to generateColorForCategory(it) }
                ?: listOf()
        )
    }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val allAvailableCategories = remember(predefinedCategories, customCategories) {
        predefinedCategories.plus(customCategories)
    }

    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }

    val primaryCategory = selectedCategories.firstOrNull() ?: "Work"

    LaunchedEffect(title, content, selectedCategories) {
        if (note != null) {
            val originalCategories = note.categories
            hasChanges = title != note.title ||
                    content != note.content ||
                    selectedCategories.sorted() != originalCategories.sorted()
        } else {
            hasChanges = title.isNotEmpty() || content.isNotEmpty()
        }
    }

    val headerGradient = when (primaryCategory.lowercase()) {
        "work" -> listOf(Color(0xFF5B6FE8), Color(0xFF8E44AD))
        "study" -> listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
        "project" -> listOf(Color(0xFFBF3FBB), Color(0xFFD32E8B))
        "idea" -> listOf(Color(0xFFFA9E5F), Color(0xFFFF8F00))
        "travel" -> listOf(Color(0xFF26C6DA), Color(0xFF00ACC1))
        "meeting" -> listOf(Color(0xFF9575CD), Color(0xFF7E57C2))
        else -> listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = Brush.linearGradient(colors = headerGradient))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }

                    Text(
                        text = if (isEditMode) (note?.title ?: "New Note") else title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                "Saturday, October 23, 2025",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.AccessTime,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                note?.time ?: "10:30 AM",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isEditMode) {
                    // EDIT MODE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Title",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            placeholder = { Text("Enter note title...") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = headerGradient[0],
                                cursorColor = headerGradient[0],
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Notes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                        OutlinedTextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = { Text("Write your note here...") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = headerGradient[0],
                                cursorColor = headerGradient[0],
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                unfocusedContainerColor = Color.White,
                                focusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            maxLines = Int.MAX_VALUE
                        )
                    }

                    // ✅ CATEGORY SECTION - SUDAH DIUBAH JADI CHIP STYLE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Category",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF9E9E9E)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allAvailableCategories.forEach { (catName, catColor) ->
                                // ✅ CHIP STYLE SEPERTI CREATE SCREEN
                                Surface(
                                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                                    color = if (selectedCategories.contains(catName)) catColor else catColor.copy(alpha = 0.15f),
                                    onClick = {
                                        selectedCategories =
                                            if (selectedCategories.contains(catName)) {
                                                if (selectedCategories.size > 1) {
                                                    selectedCategories - catName
                                                } else {
                                                    selectedCategories
                                                }
                                            } else {
                                                selectedCategories + catName
                                            }
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text(
                                        text = catName,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = if (selectedCategories.contains(catName)) Color.White else catColor,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedCategories.contains(catName)) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }

                            // ✅ ADD NEW BUTTON - SAMA SEPERTI CREATE SCREEN
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showAddCategoryDialog = true },
                                color = Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add Category",
                                        tint = Color(0xFF6A70D7),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "Add New",
                                        color = Color(0xFF6A70D7),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // VIEW MODE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Catatan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                        Text(
                            text = content,
                            fontSize = 14.sp,
                            color = Color(0xFF424242),
                            lineHeight = 20.sp,
                            textAlign = TextAlign.Justify
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Categories",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            selectedCategories.forEach { category ->
                                CategoryBadge(
                                    text = category,
                                    color = generateColorForCategory(category)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Action Buttons
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditMode) {
                    // ✅ BUTTON SAVE DENGAN GRADIENT SELALU TERLIHAT
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && content.isNotEmpty()) {
                                note?.let {
                                    viewModel.updateNote(it.id, title, content, selectedCategories)
                                    navController.popBackStack()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        enabled = title.isNotEmpty() && content.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            "Save Changes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { isEditMode = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = headerGradient[0]
                            ),
                            border = BorderStroke(1.5.dp, headerGradient[0]),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Edit", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }

                        Button(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF44336)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Delete", fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }

        // ✅ ADD CATEGORY DIALOG - SAMA SEPERTI CREATE SCREEN
        // Blur Background Overlay
        AnimatedVisibility(
            visible = showAddCategoryDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable(enabled = false) { }
            )
        }

        // Dialog Content
        if (showAddCategoryDialog) {
            Dialog(
                onDismissRequest = {
                    showAddCategoryDialog = false
                    newCategoryName = ""
                },
                properties = androidx.compose.ui.window.DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                AnimatedVisibility(
                    visible = showAddCategoryDialog,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            // Title
                            Text(
                                "New Category",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )

                            // Input Field
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    "Category Name",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color(0xFF9E9E9E)
                                )

                                TextField(
                                    value = newCategoryName,
                                    onValueChange = { newCategoryName = it },
                                    placeholder = {
                                        Text(
                                            "Holiday",
                                            color = Color(0xFFBDBDBD),
                                            fontSize = 15.sp
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = Color(0xFF6A70D7)
                                    ),
                                    textStyle = LocalTextStyle.current.copy(
                                        fontSize = 15.sp,
                                        color = Color(0xFF2D2D2D)
                                    ),
                                    singleLine = true
                                )

                                HorizontalDivider(
                                    color = Color(0xFFE0E0E0),
                                    thickness = 0.5.dp
                                )
                            }

                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Cancel Button
                                OutlinedButton(
                                    onClick = {
                                        showAddCategoryDialog = false
                                        newCategoryName = ""
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFFEF5350)
                                    ),
                                    border = ButtonDefaults.outlinedButtonBorder.copy(
                                        width = 1.5.dp,
                                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF5350))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        "Cancel",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFEF5350)
                                    )
                                }

                                // Create Button dengan Gradient - SELALU TERLIHAT
                                Button(
                                    onClick = {
                                        if (newCategoryName.isNotEmpty()) {
                                            // Generate random color for new category
                                            val randomColor = listOf(
                                                Color(0xFFE91E63),
                                                Color(0xFF9C27B0),
                                                Color(0xFF3F51B5),
                                                Color(0xFF00BCD4),
                                                Color(0xFF4CAF50),
                                                Color(0xFFFF9800),
                                                Color(0xFFFF5722)
                                            ).random()

                                            customCategories = customCategories + (newCategoryName to randomColor)
                                            selectedCategories = selectedCategories + newCategoryName
                                            showAddCategoryDialog = false
                                            newCategoryName = ""
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
                                            ),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    enabled = newCategoryName.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color.Transparent,
                                        disabledContainerColor = Color.Transparent
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(
                                        "Create",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        "Hapus catatan?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                text = {
                    Text(
                        "Apakah kamu yakin ingin menghapus catatan ini?",
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            note?.let { viewModel.deleteNote(it.id) }
                            navController.popBackStack()
                            showDeleteDialog = false
                        }
                    ) {
                        Text(
                            "Hapus",
                            color = Color(0xFFF44336),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(
                            "Batal",
                            color = Color(0xFF757575),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            )
        }
    }
}

// ========================================
// 8. PREVIEW
// ========================================
@Preview(showBackground = true)
@Composable
fun NoteScreenPreview() {
    YourAssistantYoraTheme {
        val navController = rememberNavController()
        NoteScreen(navController = navController)
    }
}