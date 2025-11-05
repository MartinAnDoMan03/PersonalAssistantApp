package com.example.yourassistantyora

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

// ---------- NOTE DETAIL SCREEN ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    note: Note? = null, // null = create new note
    onBackClick: () -> Unit = {},
    onSaveClick: (String, String, String) -> Unit = { _, _, _ -> },
    onDeleteClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf(note?.title ?: "") }
    var content by remember { mutableStateOf(note?.content ?: "") }
    // Ubah ke list untuk multi-select category
    var selectedCategories by remember {
        mutableStateOf(
            if (note?.category != null) {
                listOf(note.category)
            } else {
                listOf("Work")
            }
        )
    }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(note == null) } // Auto edit mode untuk note baru
    var hasChanges by remember { mutableStateOf(false) }

    // Kategori awal untuk warna header (tetap pakai kategori pertama)
    val primaryCategory = selectedCategories.firstOrNull() ?: "Work"

    // Deteksi perubahan
    LaunchedEffect(title, content, selectedCategories) {
        if (note != null) {
            val originalCategories = listOf(note.category)
            hasChanges = title != note.title ||
                    content != note.content ||
                    selectedCategories != originalCategories
        } else {
            hasChanges = title.isNotEmpty() || content.isNotEmpty()
        }
    }

    // Gradient berdasarkan kategori pertama - diperkuat
    val headerGradient = when (primaryCategory) {
        "Work" -> listOf(Color(0xFF5B6FE8), Color(0xFF8E44AD))
        "Study" -> listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))
        "Project" -> listOf(Color(0xFFBF3FBB), Color(0xFFD32E8B))
        "Idea" -> listOf(Color(0xFFFA9E5F), Color(0xFFFF8F00))
        else -> listOf(Color(0xFF5B6FE8), Color(0xFF8E44AD))
    }

    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            // Custom Top Bar dengan Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = headerGradient
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                ) {
                    // Top Actions
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }


                    }

                    // Title
                    Text(
                        text = if (isEditMode) (note?.title ?: "New Note") else title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )

                    // Date & Time Row
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
            // Content Area (Scrollable)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Content Display / Input berdasarkan mode
                if (isEditMode) {
                    // Title Section
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

                    // Notes Section
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

                    // Category Section
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

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryButton(
                                text = "Work",
                                isSelected = selectedCategories.contains("Work"),
                                onClick = {
                                    selectedCategories = if (selectedCategories.contains("Work")) {
                                        if (selectedCategories.size > 1) {
                                            selectedCategories - "Work"
                                        } else {
                                            selectedCategories // Tidak bisa unselect jika hanya 1
                                        }
                                    } else {
                                        selectedCategories + "Work"
                                    }
                                },
                                color = Color(0xFF667EEA)
                            )
                            CategoryButton(
                                text = "Study",
                                isSelected = selectedCategories.contains("Study"),
                                onClick = {
                                    selectedCategories = if (selectedCategories.contains("Study")) {
                                        if (selectedCategories.size > 1) {
                                            selectedCategories - "Study"
                                        } else {
                                            selectedCategories
                                        }
                                    } else {
                                        selectedCategories + "Study"
                                    }
                                },
                                color = Color(0xFF64B5F6)
                            )
                            CategoryButton(
                                text = "Project",
                                isSelected = selectedCategories.contains("Project"),
                                onClick = {
                                    selectedCategories = if (selectedCategories.contains("Project")) {
                                        if (selectedCategories.size > 1) {
                                            selectedCategories - "Project"
                                        } else {
                                            selectedCategories
                                        }
                                    } else {
                                        selectedCategories + "Project"
                                    }
                                },
                                color = Color(0xFFEF5350)
                            )
                            CategoryButton(
                                text = "Idea",
                                isSelected = selectedCategories.contains("Idea"),
                                onClick = {
                                    selectedCategories = if (selectedCategories.contains("Idea")) {
                                        if (selectedCategories.size > 1) {
                                            selectedCategories - "Idea"
                                        } else {
                                            selectedCategories
                                        }
                                    } else {
                                        selectedCategories + "Idea"
                                    }
                                },
                                color = Color(0xFFFFB74D)
                            )
                        }
                    }
                } else {
                    // Read-only Mode - Display content
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
                            lineHeight = 20.sp
                        )
                    }

                    // Selected Categories Display (Read-only)
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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            selectedCategories.forEach { category ->
                                val categoryColor = when (category) {
                                    "Work" -> Color(0xFF667EEA)
                                    "Study" -> Color(0xFF64B5F6)
                                    "Project" -> Color(0xFFEF5350)
                                    "Idea" -> Color(0xFFFFB74D)
                                    else -> Color(0xFF667EEA)
                                }
                                CategoryChip(text = category, color = categoryColor)
                            }
                        }
                    }

                    // Reminder Section (Optional - if note has reminder)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Reminder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF757575)
                        )
                        Text(
                            "30 minutes before (09:30 AM)",
                            fontSize = 14.sp,
                            color = Color(0xFF424242)
                        )
                    }
                }
            }

            // Bottom Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isEditMode) {
                    // Save Button (Edit Mode)
                    Button(
                        onClick = {
                            if (title.isNotEmpty() && content.isNotEmpty()) {
                                // Gabungkan categories dengan separator (misalnya koma)
                                val categoriesString = selectedCategories.joinToString(", ")
                                onSaveClick(title, content, categoriesString)
                                if (note != null) {
                                    isEditMode = false
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
                    // Edit & Delete Buttons (View Mode)
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
                            border = ButtonDefaults.outlinedButtonBorder.copy(
                                width = 1.5.dp,
                                brush = Brush.linearGradient(headerGradient)
                            ),
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
                                containerColor = Color(0xFFE57373)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Delete,
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

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Note?") },
                text = { Text("Are you sure you want to delete this note? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            onDeleteClick()
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

// ---------- CATEGORY CHIP (for read-only display) ----------
@Composable
fun CategoryChip(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(8.dp)),
        color = color,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------- CATEGORY BUTTON ----------
@Composable
fun CategoryButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp)),
        color = if (isSelected) color else color.copy(alpha = 0.15f),
        onClick = onClick,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Color.White else color)
            )
            Text(
                text = text,
                color = if (isSelected) Color.White else color,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

// ---------- KEY POINT ITEM ----------
@Composable
fun KeyPointItem(text: String) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(Color(0xFF9E9E9E))
        )
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF616161),
            lineHeight = 18.sp
        )
    }
}

// ---------- PREVIEW ----------
@Preview(showBackground = true)
@Composable
fun NoteDetailScreenPreview() {
    YourAssistantYoraTheme {
        NoteDetailScreen(
            note = Note(
                1,
                "Project Requirements",
                "Discuss with team about new mobile app requirements. Need to finalize UI/UX designs and prepare for client presentation next week.",
                "Project",
                "10:30 AM"
            )
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NoteDetailScreenNewPreview() {
    YourAssistantYoraTheme {
        NoteDetailScreen()
    }
}