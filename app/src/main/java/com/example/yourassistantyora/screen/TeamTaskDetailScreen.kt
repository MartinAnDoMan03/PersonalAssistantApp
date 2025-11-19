package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamTaskDetailScreen(
    task: TeamTask,
    teamColor: Color,
    currentUserId: String,
    currentUserRole: String,
    onBackClick: () -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    onCommentAdd: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var commentText by remember { mutableStateOf("") }
    var showStatusDropdown by remember { mutableStateOf(false) }

    // Check if current user is assigned to this task
    val isAssignedToUser = task.assignedTo.any { it.id == currentUserId }
    val canUpdateStatus = currentUserRole == "Admin" || isAssignedToUser

    // Determine title based on assignment
    val screenTitle = if (isAssignedToUser && currentUserRole == "Member") {
        "Your Task"
    } else if (!isAssignedToUser && currentUserRole == "Member") {
        "${task.assignedTo.firstOrNull()?.name?.split(" ")?.firstOrNull() ?: "Team"} Task"
    } else {
        "Task Details"
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header with gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.horizontalGradient(
                            listOf(teamColor, teamColor.copy(alpha = 0.8f))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Filled.ArrowBack, "Back", tint = Color.White) // Ganti Close menjadi ArrowBack
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (canUpdateStatus) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            screenTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Spacer(Modifier.height(4.dp))

                    Text(
                        if (isAssignedToUser) {
                            "This task is assigned to you. You can update the status to track your progress."
                        } else if (currentUserRole == "Member") {
                            "This task is assigned to ${task.assignedTo.firstOrNull()?.name ?: "someone"}."
                        } else {
                            "Manage this task and track team progress."
                        },
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        lineHeight = 16.sp
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Task Title & Description Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                task.title,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D2D2D)
                            )
                            Text(
                                task.description,
                                fontSize = 13.sp,
                                color = Color(0xFF757575),
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                // Status Card with Dropdown
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Status",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )

                            // Current Status Display (Clickable if can update)
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(enabled = canUpdateStatus) {
                                        showStatusDropdown = !showStatusDropdown
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(task.status.bgColor),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            when (task.status) {
                                                TaskStatus.NOT_STARTED -> Icons.Filled.RadioButtonUnchecked
                                                TaskStatus.IN_PROGRESS -> Icons.Filled.Schedule
                                                TaskStatus.DONE -> Icons.Filled.CheckCircle
                                            },
                                            null,
                                            tint = task.status.color,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            task.status.displayName,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = task.status.color
                                        )
                                    }

                                    if (canUpdateStatus) {
                                        Icon(
                                            Icons.Filled.KeyboardArrowDown,
                                            null,
                                            tint = task.status.color
                                        )
                                    }
                                }
                            }

                            // Status Dropdown Options
                            AnimatedVisibility(visible = showStatusDropdown && canUpdateStatus) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TaskStatus.values().filter { it != task.status }.forEach { status ->
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    onStatusChange(status)
                                                    showStatusDropdown = false
                                                },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = CardDefaults.cardColors(Color(0xFFF8F9FA)),
                                            elevation = CardDefaults.cardElevation(0.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(14.dp),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    when (status) {
                                                        TaskStatus.NOT_STARTED -> Icons.Filled.RadioButtonUnchecked
                                                        TaskStatus.IN_PROGRESS -> Icons.Filled.Schedule
                                                        TaskStatus.DONE -> Icons.Filled.CheckCircle
                                                    },
                                                    null,
                                                    tint = status.color,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(
                                                    status.displayName,
                                                    fontSize = 14.sp,
                                                    color = Color(0xFF2D2D2D)
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Permission Info
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    if (canUpdateStatus) Icons.Filled.Edit else Icons.Filled.Lock,
                                    null,
                                    tint = if (canUpdateStatus) Color(0xFF16A34A) else Color(0xFFED8936),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    if (canUpdateStatus) {
                                        "You can update this task status"
                                    } else {
                                        "Only assigned members can update the status"
                                    },
                                    fontSize = 11.sp,
                                    color = if (canUpdateStatus) Color(0xFF16A34A) else Color(0xFFED8936)
                                )
                            }
                        }
                    }
                }

                // Priority Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Priority",
                                fontSize = 13.sp,
                                color = Color(0xFF757575)
                            )
                            Surface(
                                color = task.priority.bgColor,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    task.priority.displayName,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = task.priority.color
                                )
                            }
                        }
                    }
                }

                // Assigned To Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Assigned to",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )

                            task.assignedTo.forEach { member ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(teamColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            member.name.first().toString(),
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Column {
                                        Text(
                                            member.name,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF2D2D2D)
                                        )
                                        if (member.id == currentUserId) {
                                            Text(
                                                "You",
                                                fontSize = 11.sp,
                                                color = teamColor,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Deadline Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.CalendarToday,
                                    null,
                                    tint = Color(0xFF9E9E9E),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    "Deadline",
                                    fontSize = 13.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                            Text(
                                task.deadline,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2D2D2D)
                            )
                        }
                    }
                }

                // Attachments Card
                if (task.attachments.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(Color.White),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    "${task.attachments.size} Attachments",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2D2D2D)
                                )

                                task.attachments.forEach { attachment ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Filled.AttachFile,
                                            null,
                                            tint = teamColor,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            attachment.fileName,
                                            fontSize = 12.sp,
                                            color = Color(0xFF2D2D2D)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Comments Section
                item {
                    Text(
                        "Comments (${task.comments.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                }

                items(task.comments) { comment ->
                    CommentItem(
                        comment = comment,
                        teamColor = teamColor
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }

            // Comment Input (Fixed at bottom)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.Bottom
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = {
                            Text("Write a comment...", fontSize = 13.sp)
                        },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = teamColor,
                            unfocusedBorderColor = Color(0xFFE0E0E0),
                            cursorColor = teamColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onCommentAdd(commentText)
                                commentText = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(teamColor, RoundedCornerShape(12.dp)),
                        enabled = commentText.isNotBlank()
                    ) {
                        Icon(
                            Icons.Filled.Send,
                            "Send",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: TaskComment,
    teamColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (comment.isCurrentUser) teamColor else Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                comment.userName.first().toString(),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    comment.userName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D2D2D)
                )
                Text(
                    comment.timestamp,
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }

            Text(
                comment.comment,
                fontSize = 13.sp,
                color = Color(0xFF2D2D2D),
                lineHeight = 18.sp
            )
        }
    }
}