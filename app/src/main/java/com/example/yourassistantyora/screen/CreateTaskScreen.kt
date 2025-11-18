package com.example.yourassistantyora

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.BorderStroke

// Data class untuk Status
data class StatusItem(
    val name: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: (Task) -> Unit = {}
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Oct 24, 2025") }
    var selectedTime by remember { mutableStateOf("10:00 AM") }
    var selectedPriority by remember { mutableStateOf("Medium") }
    var selectedCategory by remember { mutableStateOf("Work") }
    var selectedReminder by remember { mutableStateOf("Tidak ada peringat") }
    var location by remember { mutableStateOf("") }
    var selectedStatus by remember { mutableStateOf("To do") }

    // Dialog states
    var showNewStatusDialog by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var showReminderDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Categories and Statuses
    var categories by remember {
        mutableStateOf(listOf("Work", "Study", "Travel", "Meeting", "Project"))
    }
    var statuses by remember {
        mutableStateOf(
            listOf(
                StatusItem("Waiting", Color(0xFFF3E5F5)),
                StatusItem("To do", Color(0xFFE3F2FD)),
                StatusItem("Done", Color(0xFFE8F5E8)),
                StatusItem("Hold On", Color(0xFFFFF3E0)),
                StatusItem("In Progress", Color(0xFFE0F2F1))
            )
        )
    }

    val reminderOptions = listOf(
        "Tidak ada peringat",
        "Ingat ketepat waktu",
        "Reminder 10 minute before",
        "Reminder 20 minute before",
        "Reminder 30 minute before",
        "Reminder 1 day before",
        "Reminder 2 day before",
        "Reminder 3 day before"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F5F5),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "New Task",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF757575)
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                // Validate and save
                                if (title.isNotBlank()) {
                                    val newTask = Task(
                                        id = System.currentTimeMillis().toInt(),
                                        title = title,
                                        time = selectedTime,
                                        priority = selectedPriority,
                                        category = selectedCategory,
                                        status = selectedStatus
                                    )
                                    onSaveClick(newTask)
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = "Save",
                                tint = Color(0xFF6C63FF)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Task Details Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Task Details",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )

                        // Title
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Title",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                            OutlinedTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = {
                                    Text(
                                        "What needs to be done ?",
                                        fontSize = 14.sp,
                                        color = Color(0xFFBDBDBD)
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C63FF),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                singleLine = true
                            )
                        }

                        // Description
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Description",
                                fontSize = 12.sp,
                                color = Color(0xFF757575)
                            )
                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = {
                                    Text(
                                        "Add more details...(optional)",
                                        fontSize = 14.sp,
                                        color = Color(0xFFBDBDBD)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C63FF),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                maxLines = 4
                            )
                        }
                    }
                }

                // Schedule Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF6C63FF),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "Schedule",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1F1F1F)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Date picker
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                    .clickable { showDatePicker = true }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CalendarToday,
                                        contentDescription = null,
                                        tint = Color(0xFF757575),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        selectedDate,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                            }

                            // Time picker
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFFAFAFA))
                                    .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                                    .clickable { showTimePicker = true }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AccessTime,
                                        contentDescription = null,
                                        tint = Color(0xFF757575),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        selectedTime,
                                        fontSize = 13.sp,
                                        color = Color(0xFF1F1F1F)
                                    )
                                }
                            }
                        }
                    }
                }

                // Priority Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Priority",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            PriorityOption(
                                label = "Low",
                                color = Color(0xFF64B5F6),
                                isSelected = selectedPriority == "Low",
                                onClick = { selectedPriority = "Low" },
                                modifier = Modifier.weight(1f)
                            )
                            PriorityOption(
                                label = "Medium",
                                color = Color(0xFFFFB74D),
                                isSelected = selectedPriority == "Medium",
                                onClick = { selectedPriority = "Medium" },
                                modifier = Modifier.weight(1f)
                            )
                            PriorityOption(
                                label = "High",
                                color = Color(0xFFEF5350),
                                isSelected = selectedPriority == "High",
                                onClick = { selectedPriority = "High" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // Category Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Category",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )

                        // Category chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            categories.take(3).forEach { category ->
                                CategoryChip(
                                    label = category,
                                    isSelected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            categories.drop(3).forEach { category ->
                                CategoryChip(
                                    label = category,
                                    isSelected = selectedCategory == category,
                                    onClick = { selectedCategory = category }
                                )
                            }

                            // Add New Category
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .clickable { showNewCategoryDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "+ Add New",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6C63FF),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Reminder Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Reminder",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )

                        ExposedDropdownMenuBox(
                            expanded = showReminderDropdown,
                            onExpandedChange = { showReminderDropdown = it }
                        ) {
                            OutlinedTextField(
                                value = selectedReminder,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (showReminderDropdown)
                                            Icons.Filled.KeyboardArrowUp
                                        else
                                            Icons.Filled.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = Color(0xFF757575)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF6C63FF),
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color(0xFFFAFAFA)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 13.sp)
                            )

                            ExposedDropdownMenu(
                                expanded = showReminderDropdown,
                                onDismissRequest = { showReminderDropdown = false }
                            ) {
                                reminderOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                option,
                                                fontSize = 13.sp
                                            )
                                        },
                                        onClick = {
                                            selectedReminder = option
                                            showReminderDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                // Location Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Location",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )

                        OutlinedTextField(
                            value = location,
                            onValueChange = { location = it },
                            placeholder = {
                                Text(
                                    "Add location...(optional)",
                                    fontSize = 14.sp,
                                    color = Color(0xFFBDBDBD)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF6C63FF),
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color(0xFFFAFAFA)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }

                // Status Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Status",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1F1F1F)
                        )

                        // Status chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            statuses.take(3).forEach { status ->
                                StatusChip(
                                    label = status.name,
                                    color = status.color,
                                    isSelected = selectedStatus == status.name,
                                    onClick = { selectedStatus = status.name }
                                )
                            }
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            statuses.drop(3).forEach { status ->
                                StatusChip(
                                    label = status.name,
                                    color = status.color,
                                    isSelected = selectedStatus == status.name,
                                    onClick = { selectedStatus = status.name }
                                )
                            }

                            // Add New Status
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .clickable { showNewStatusDialog = true }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "+ Add New",
                                    fontSize = 12.sp,
                                    color = Color(0xFF6C63FF),
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // View All
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFFF5F5F5))
                                    .clickable { /* View all statuses */ }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    "View All",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Create Task Button
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            val newTask = Task(
                                id = System.currentTimeMillis().toInt(),
                                title = title,
                                time = selectedTime,
                                priority = selectedPriority,
                                category = selectedCategory,
                                status = selectedStatus
                            )
                            onSaveClick(newTask)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6C63FF)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Create Task",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }

        // New Status Dialog
        if (showNewStatusDialog) {
            NewStatusDialog(
                onDismiss = { showNewStatusDialog = false },
                onConfirm = { statusName, badgeColor ->
                    statuses = statuses + StatusItem(statusName, badgeColor)
                    showNewStatusDialog = false
                }
            )
        }

        // New Category Dialog
        if (showNewCategoryDialog) {
            NewCategoryDialog(
                onDismiss = { showNewCategoryDialog = false },
                onConfirm = { categoryName ->
                    categories = categories + categoryName
                    showNewCategoryDialog = false
                }
            )
        }
    }
}

@Composable
fun PriorityOption(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.1f) else Color(0xFFFAFAFA)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(
                label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = Color(0xFF1F1F1F)
            )
        }
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFF6C63FF) else Color(0xFFF5F5F5)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = if (isSelected) Color.White else Color(0xFF757575),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun StatusChip(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) color else color.copy(alpha = 0.3f)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp,
            color = if (isSelected) Color(0xFF1F1F1F) else Color(0xFF757575),
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun NewStatusDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Color) -> Unit
) {
    var statusName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFFE3F2FD)) }

    val badgeColors = listOf(
        Color(0xFFF3E5F5),
        Color(0xFFE8EAF6),
        Color(0xFFFFF9C4),
        Color(0xFFE8F5E8),
        Color(0xFFE3F2FD),
        Color(0xFFE1F5FE)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "New Status",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Status Name",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    OutlinedTextField(
                        value = statusName,
                        onValueChange = { statusName = it },
                        placeholder = { Text("Under Review", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6C63FF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Badge Color",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        badgeColors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(color)
                                    .border(
                                        width = if (selectedColor == color) 3.dp else 0.dp,
                                        color = Color(0xFF6C63FF),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedColor = color }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF5350)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (statusName.isNotBlank()) {
                                onConfirm(statusName, selectedColor)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Create", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun NewCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "New Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F1F1F)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Category Name",
                        fontSize = 13.sp,
                        color = Color(0xFF757575)
                    )
                    OutlinedTextField(
                        value = categoryName,
                        onValueChange = { categoryName = it },
                        placeholder = { Text("Holiday", fontSize = 14.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6C63FF),
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFEF5350)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontSize = 14.sp)
                    }

                    Button(
                        onClick = {
                            if (categoryName.isNotBlank()) {
                                onConfirm(categoryName)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6C63FF)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Create", fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}