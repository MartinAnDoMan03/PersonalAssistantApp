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
        Color(0xFFEF5350), Color(0xFFEC407A), Color(0xFFAB47BC), Color(0xFF7E57C2),
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
    onClick: (() -> Unit)? = null,
    color: Color = Color(0xFF6A70D7)
) {
    val backgroundColor = if (isSelected) color else Color.White
    val textColor = if (isSelected) Color.White else color
    val borderColor = if (isSelected) color else color.copy(alpha = 0.3f)

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.5.dp, borderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 2.dp else 0.dp
        ),
        modifier = Modifier.clickable(enabled = onClick != null) { onClick?.invoke() }
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
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
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
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
    val density = androidx.compose.ui.platform.LocalDensity.current
    val itemHeight = 120.dp
    val swipeLimit = with(density) { -150.dp.toPx() }

    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        onSwipeChange(note.id, true)
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        val newOffset = (offsetX.value + dragAmount).coerceIn(swipeLimit, 0f)
                        scope.launch {
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragEnd = {
                        if (offsetX.value < swipeLimit * 0.75f) {
                            onDeleteSwipe()
                        } else {
                            scope.launch {
                                offsetX.animateTo(0f, animationSpec = tween(300))
                                onSwipeChange(note.id, false)
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f, animationSpec = tween(300))
                            onSwipeChange(note.id, false)
                        }
                    }
                )
            }
    ) {
        // Background Delete
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF44336), RoundedCornerShape(12.dp))
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    "Delete",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Card Content
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .clickable(onClick = onNoteClick),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Title
                Text(
                    text = note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color(0xFF2D2D2D)
                )

                // Content
                Text(
                    text = note.content,
                    fontSize = 13.sp,
                    color = Color(0xFF757575),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )

                // Categories + Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Categories (Max 3)
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        note.categories.take(3).forEach { category ->
                            CategoryBadge(
                                text = category,
                                color = generateColorForCategory(category)
                            )
                        }

                        // +X indicator
                        if (note.categories.size > 3) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0E0E0)
                            ) {
                                Text(
                                    text = "+${note.categories.size - 3}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF616161),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Time
                    Text(
                        text = note.time,
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E),
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }

    LaunchedEffect(swipedNoteId) {
        if (swipedNoteId != null && swipedNoteId != note.id) {
            if (offsetX.value != 0f) {
                offsetX.animateTo(0f, animationSpec = tween(300))
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

    val allCategories = remember(notes) {
        notes.flatMap { it.categories }.distinct().sorted()
    }

    val filteredNotes = notes.filter { note ->
        when (selectedCategory) {
            "All" -> true
            else -> note.categories.any { it.equals(selectedCategory, ignoreCase = true) }
        }
    }

    fun deleteNoteFromSwipe(note: Note) {
        lastDeletedNote = note
        viewModel.deleteNote(note.id)
        showDeleteSnackbar = true

        scope.launch {
            delay(4000)
            showDeleteSnackbar = false
            lastDeletedNote = null
        }
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
                        text = "All",
                        isSelected = selectedCategory == "All",
                        onClick = { selectedCategory = "All" },
                        color = Color(0xFF6A70D7)
                    )

                    allCategories.forEach { cat ->
                        NoteCategoryChip(
                            text = cat.replaceFirstChar {
                                if (it.isLowerCase()) it.titlecase(Locale.ROOT)
                                else it.toString()
                            },
                            isSelected = selectedCategory.equals(cat, ignoreCase = true),
                            onClick = { selectedCategory = cat },
                            color = generateColorForCategory(cat)
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
                                    onDeleteSwipe = { deleteNoteFromSwipe(note) },
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
        "Travel" to Color(0xFF4DB6AC),
        "Meeting" to Color(0xFF9575CD),
        "Project" to Color(0xFFEF5350)
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
        else -> listOf(Color(0xFF5B6FE8), Color(0xFF8E44AD))
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

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allAvailableCategories.forEach { (catName, catColor) ->
                                NoteCategoryButton(
                                    text = catName,
                                    isSelected = selectedCategories.contains(catName),
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
                                    color = catColor
                                )
                            }

                            ActionChip(
                                text = "Add New",
                                onClick = { showAddCategoryDialog = true },
                                icon = Icons.Filled.Add
                            )
                        }
                    }
                } else {
                    // VIEW MODE
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Description",
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
                            .height(50.dp),
                        enabled = hasChanges && title.isNotEmpty() && content.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = headerGradient[0],
                            disabledContainerColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Save Changes",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
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

        // Add Category Dialog
        if (showAddCategoryDialog) {
            Dialog(onDismissRequest = {
                showAddCategoryDialog = false
                newCategoryName = ""
            }) {
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
                        Text(
                            "Add New Category",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Category Name",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF9E9E9E)
                            )

                            OutlinedTextField(
                                value = newCategoryName,
                                onValueChange = { newCategoryName = it },
                                placeholder = { Text("e.g., Holiday") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = headerGradient[0],
                                    cursorColor = headerGradient[0]
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    showAddCategoryDialog = false
                                    newCategoryName = ""
                                }
                            ) {
                                Text("Cancel", color = Color(0xFF757575))
                            }

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        customCategories = customCategories + (newCategoryName to generateColorForCategory(newCategoryName))
                                        selectedCategories = selectedCategories + newCategoryName
                                        newCategoryName = ""
                                        showAddCategoryDialog = false
                                    }
                                },
                                enabled = newCategoryName.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = headerGradient[0]
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Add")
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
                title = { Text("Delete Note?") },
                text = { Text("Are you sure you want to permanently delete this note?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            note?.let { viewModel.deleteNote(it.id) }
                            navController.popBackStack()
                            showDeleteDialog = false
                        }
                    ) {
                        Text("Delete", color = Color(0xFFF44336), fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
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