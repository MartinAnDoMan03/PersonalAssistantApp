package com.example.yourassistantyora.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Extended Task data class dengan field tambahan
data class TaskDetail(
    val id: Int,
    val title: String,
    val time: String,
    val priority: String,
    val category: String,
    val status: String?,
    val isCompleted: Boolean = false,
    val date: String = "Wednesday, October 23, 2025",
    val description: String = "",
    val reminder: String = "30 minutes before (09:30 AM)",
    val location: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    task: Task,
    onBackClick: () -> Unit = {},
    onEditClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onSaveChanges: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    // Convert Task to TaskDetail (you can pass additional data if needed)
    val taskDetail = TaskDetail(
        id = task.id,
        title = task.title,
        time = task.time,
        priority = task.priority,
        category = task.category,
        status = task.status,
        isCompleted = task.isCompleted,
        date = "Wednesday, October 23, 2025",
        description = "Prepare agenda and presentation materials for the quarterly team meeting. Review last month's progress, gather feedback from team members, and create action items for next quarter.",
        reminder = "30 minutes before (09:30 AM)",
        location = "Lee Polonia Hotel, Medan"
    )

    var selectedStatus by remember { mutableStateOf(taskDetail.status ?: "To do") }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Priority color
    val priorityColor = when (taskDetail.priority) {
        "High" -> Color(0xFFEF5350)
        "Medium" -> Color(0xFFFFB74D)
        "Low" -> Color(0xFF64B5F6)
        else -> Color(0xFF64B5F6)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF5F7FA),
            topBar = {
                TopAppBar(
                    title = { },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* More options */ }) {
                            Icon(
                                imageVector = Icons.Filled.MoreVert,
                                contentDescription = "More",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF6A70D7)
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header Section (Purple Background)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF6A70D7))
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    Text(
                        text = taskDetail.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Priority Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = priorityColor
                        ) {
                            Text(
                                text = "${taskDetail.priority} Priority",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                        // Category Badge
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = taskDetail.category,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Content Section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Schedule Card
                    DetailCard {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = Color(0xFF6A70D7),
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Schedule",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        DetailRow(label = "Date", value = taskDetail.date)
                        Spacer(Modifier.height(8.dp))
                        DetailRow(label = "Time", value = taskDetail.time)
                    }

                    // Description Card
                    DetailCard {
                        Text(
                            text = "Description",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = taskDetail.description,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF666666),
                            lineHeight = 20.sp
                        )
                    }

                    // Reminder Card
                    DetailCard {
                        Text(
                            text = "Reminder",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = taskDetail.reminder,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF666666)
                        )
                    }

                    // Location Card
                    DetailCard {
                        Text(
                            text = "Location",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = taskDetail.location,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF666666)
                        )
                    }

                    // Status Card
                    DetailCard {
                        Text(
                            text = "Status",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = selectedStatus,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF666666)
                        )
                        Spacer(Modifier.height(12.dp))

                        // Status Options
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusChip(
                                text = "Waiting",
                                isSelected = selectedStatus == "Waiting",
                                onClick = { selectedStatus = "Waiting" },
                                backgroundColor = Color(0xFFF3E5F5),
                                textColor = Color(0xFF7B1FA2)
                            )
                            StatusChip(
                                text = "To do",
                                isSelected = selectedStatus == "To do",
                                onClick = { selectedStatus = "To do" },
                                backgroundColor = Color(0xFFE3F2FD),
                                textColor = Color(0xFF1976D2)
                            )
                            StatusChip(
                                text = "Done",
                                isSelected = selectedStatus == "Done",
                                onClick = { selectedStatus = "Done" },
                                backgroundColor = Color(0xFFE8F5E8),
                                textColor = Color(0xFF388E3C)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            StatusChip(
                                text = "Hold On",
                                isSelected = selectedStatus == "Hold On",
                                onClick = { selectedStatus = "Hold On" },
                                backgroundColor = Color(0xFFFFF3E0),
                                textColor = Color(0xFFEF6C00)
                            )
                            StatusChip(
                                text = "In Progress",
                                isSelected = selectedStatus == "In Progress",
                                onClick = { selectedStatus = "In Progress" },
                                backgroundColor = Color(0xFFE0F2F1),
                                textColor = Color(0xFF00695C)
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { /* Add New Status */ },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF6A70D7)
                                )
                            ) {
                                Text("+ Add New", fontSize = 12.sp)
                            }
                            TextButton(
                                onClick = { /* View All */ },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFF6A70D7)
                                )
                            ) {
                                Text("View All", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Edit Button
                        OutlinedButton(
                            onClick = onEditClick,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF6A70D7)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Edit", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }

                        // Delete Button
                        OutlinedButton(
                            onClick = { showDeleteDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFF44336)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Delete", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Save Changes Button
                    Button(
                        onClick = onSaveChanges,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6A70D7)
                        )
                    ) {
                        Text(
                            "Save Changes",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(20.dp))
                }
            }
        }

        // Delete Confirmation Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Task?") },
                text = { Text("Are you sure you want to delete this task? This action cannot be undone.") },
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

@Composable
fun DetailCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

@Composable
fun DetailRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label :",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF666666)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            color = Color(0xFF2D2D2D)
        )
    }
}

@Composable
fun StatusChip(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) backgroundColor else backgroundColor.copy(alpha = 0.3f),
        onClick = onClick
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) textColor else textColor.copy(alpha = 0.6f)
        )
    }
}