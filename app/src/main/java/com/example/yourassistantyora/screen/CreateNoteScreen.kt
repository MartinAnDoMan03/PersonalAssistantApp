package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateNoteScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: (String, String, List<String>) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    val firestore = remember { FirebaseFirestore.getInstance() }
    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user" }

    // Predefined categories dengan warna yang SAMA dengan NoteScreen
    val predefinedCategories = listOf(
        "Work" to Color(0xFF667EEA),
        "Study" to Color(0xFF64B5F6),
        "Project" to Color(0xFFEF5350),
        "Idea" to Color(0xFFFFB74D),
        "Travel" to Color(0xFF4DB6AC),
        "Meeting" to Color(0xFF9575CD)
    )

    // Custom categories yang ditambahkan user
    var customCategories by remember { mutableStateOf(listOf<Pair<String, Color>>()) }

    // Gabungkan semua categories
    val allCategories = predefinedCategories + customCategories

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF8F9FA),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "New Note",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF8F9FA)
                    ),
                    actions = {
                        // Spacer untuk center title
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Title Input (Big Placeholder)
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "Title",
                                color = Color(0xFFBDBDBD),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
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
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(4.dp))

                    // Date and Time
                    Text(
                        text = remember {
                            val currentDate = SimpleDateFormat("d MMMM  HH:mm", Locale.getDefault())
                                .format(Date())
                            currentDate
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFBDBDBD),
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    // Content Input (No label, direct typing)
                    TextField(
                        value = content,
                        onValueChange = { content = it },
                        placeholder = {
                            Text(
                                "Start typing...",
                                color = Color(0xFFBDBDBD),
                                fontSize = 15.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 250.dp),
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
                            color = Color(0xFF2D2D2D),
                            lineHeight = 22.sp
                        ),
                        maxLines = Int.MAX_VALUE
                    )

                    Spacer(Modifier.height(24.dp))

                    // Category Section
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

                        // Category chips - FlowRow (Multi-line)
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Predefined & Custom Categories
                            allCategories.forEach { (category, color) ->
                                CreateCategoryChip(
                                    text = category,
                                    isSelected = selectedCategories.contains(category),
                                    color = color,
                                    onClick = {
                                        selectedCategories = if (selectedCategories.contains(category)) {
                                            selectedCategories - category
                                        } else {
                                            selectedCategories + category
                                        }
                                    }
                                )
                            }

                            // Add New Button
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
                }

                // Save Button dengan Gradient (Fixed at bottom)
                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty() && selectedCategories.isNotEmpty()) {
                            val noteData = hashMapOf(
                                "title" to title,
                                "note" to content,
                                "categories" to selectedCategories,
                                "user_id" to userId,
                                "created_at" to FieldValue.serverTimestamp()
                            )
                            firestore.collection("notes")
                                .add(noteData)
                                .addOnSuccessListener {
                                    onSaveClick(title, content, selectedCategories)
                                }
                                .addOnFailureListener { e ->
                                    e.printStackTrace()
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(48.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ),
                    enabled = title.isNotEmpty() && content.isNotEmpty() && selectedCategories.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        "Save",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        // Add Category Dialog dengan Blur Background
        AnimatedVisibility(
            visible = showAddCategoryDialog,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            // Blur Background Overlay
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
                properties = DialogProperties(
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
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
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
                                        brush = SolidColor(Color(0xFFEF5350))
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
    }
}

@Composable
fun CreateCategoryChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = if (isSelected) color else color.copy(alpha = 0.15f),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else color,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CreateNoteScreenPreview() {
    YourAssistantYoraTheme {
        CreateNoteScreen()
    }
}