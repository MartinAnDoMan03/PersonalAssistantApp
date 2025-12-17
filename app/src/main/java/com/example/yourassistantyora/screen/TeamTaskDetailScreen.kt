package com.example.yourassistantyora.screen

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.models.*
import com.example.yourassistantyora.viewModel.TeamDetailViewModel
import com.example.yourassistantyora.viewModel.TeamTaskDetailViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamTaskDetailScreen(
    teamId: String,
    taskId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    detailViewModel: TeamDetailViewModel = viewModel(),
    taskViewModel: TeamTaskDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val teamDetail by detailViewModel.teamDetail.collectAsState()
    val taskDetail by taskViewModel.taskDetail.collectAsState()
    val comments by taskViewModel.comments.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()
    val error by taskViewModel.error.collectAsState()

    LaunchedEffect(key1 = teamId, key2 = taskId) {
        if (teamId.isNotBlank()) detailViewModel.loadTeamDetails(teamId)
        if (taskId.isNotBlank()) taskViewModel.loadTaskAndComments(taskId, teamId)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { taskViewModel.addAttachment(taskId, it, context) }
    }

    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            filePickerLauncher.launch("*/*")
        } else {
            Toast.makeText(context, "Permission required for attachments", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(key1 = error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    if (isLoading || teamDetail == null || taskDetail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color(0xFF6A70D7))
        }
    } else {
        val readyTeamDetail = teamDetail!!
        val readyTaskDetail = taskDetail!!
        var commentText by remember { mutableStateOf("") }
        var showStatusDropdown by remember { mutableStateOf(false) }

        val isAssignedToUser = readyTaskDetail.assignedTo.any { it.id == readyTeamDetail.currentUserId }
        val canUpdateStatus = readyTeamDetail.currentUserRole == "Admin" || isAssignedToUser

        val screenTitle = if (isAssignedToUser && readyTeamDetail.currentUserRole == "Member") "Your Task"
        else if (readyTaskDetail.assignedTo.isNotEmpty()) "${readyTaskDetail.assignedTo.first().name.split(" ").first()}'s Task"
        else "Task Details"

        Scaffold(containerColor = Color(0xFFF8F9FA)) { paddingValues ->
            Column(modifier = modifier.fillMaxSize().padding(paddingValues)) {
                HeaderSection(
                    teamGradient = readyTeamDetail.colorScheme.gradient,
                    title = screenTitle,
                    isAssigned = isAssignedToUser,
                    role = readyTeamDetail.currentUserRole,
                    onBackClick = { navController.popBackStack() }
                )

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MainTaskCard(
                            task = readyTaskDetail,
                            canUpdateStatus = canUpdateStatus,
                            showStatusDropdown = showStatusDropdown,
                            onShowStatusDropdownChange = { showStatusDropdown = it },
                            onStatusChange = { newStatus ->
                                taskViewModel.updateTaskStatus(taskId, newStatus)
                                showStatusDropdown = false
                            },
                            currentUserId = readyTeamDetail.currentUserId,
                            onAddAttachmentClick = {
                                val allGranted = permissionsToRequest.all {
                                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                                }
                                if (allGranted) filePickerLauncher.launch("*/*") else permissionLauncher.launch(permissionsToRequest)
                            }
                        )
                    }

                    item {
                        CommentsSection(
                            comments = comments,
                            commentText = commentText,
                            onCommentTextChange = { commentText = it },
                            onSendComment = {
                                taskViewModel.addComment(taskId, commentText)
                                commentText = ""
                            },
                            teamColor = readyTeamDetail.colorScheme.gradient.last()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(
    teamGradient: List<Color>,
    title: String,
    isAssigned: Boolean,
    role: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.horizontalGradient(teamGradient))
            .padding(20.dp)
    ) {
        Column {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.Close, "Back", tint = Color.White)
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Filled.CheckCircle, null, tint = Color.White, modifier = Modifier.size(24.dp))
                Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                if (isAssigned) "This task is assigned to you. You can update the status."
                else if (role == "Admin") "Manage this task and track progress."
                else "Viewing task. Only assigned members can update status.",
                fontSize = 12.sp, color = Color.White.copy(alpha = 0.85f), lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun MainTaskCard(
    task: TeamTask,
    canUpdateStatus: Boolean,
    showStatusDropdown: Boolean,
    onShowStatusDropdownChange: (Boolean) -> Unit,
    onStatusChange: (TaskStatus) -> Unit,
    currentUserId: String,
    onAddAttachmentClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text(task.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2D2D2D))
                Spacer(Modifier.height(4.dp))
                Text(task.description, fontSize = 13.sp, color = Color(0xFF757575), lineHeight = 18.sp)
            }

            Spacer(Modifier.height(20.dp))

            // --- Bagian Status ---
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("Status", fontSize = 13.sp, color = Color(0xFF757575))
                Spacer(Modifier.height(8.dp))

                StatusDisplay(
                    status = task.status,
                    canUpdate = canUpdateStatus
                ) { onShowStatusDropdownChange(!showStatusDropdown) }

                AnimatedVisibility(visible = showStatusDropdown && canUpdateStatus) {
                    StatusDropdown(currentStatus = task.status, onStatusChange = onStatusChange)
                }
                Spacer(Modifier.height(8.dp))
                PermissionInfo(canUpdateStatus)
            }

            Spacer(Modifier.height(16.dp))

            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PriorityChip(task.priority)
                Text("Assigned to", fontSize = 13.sp, color = Color(0xFF757575))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.assignedTo.forEach { member ->
                        AssigneeChip(member, isCurrentUser = member.id == currentUserId)
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Text("Deadline", fontSize = 13.sp, color = Color(0xFF757575))
                }
                Text(
                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(task.deadline)),
                    fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color(0xFF2D2D2D)
                )
            }

            if (task.attachments.isNotEmpty() || canUpdateStatus) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Attachment, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text("${task.attachments.size} Attachments", fontSize = 13.sp, color = Color(0xFF757575))
                    }
                    task.attachments.forEach { attachment ->
                        AttachmentChip(attachment) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachment.fileUrl))
                            context.startActivity(intent)
                        }
                    }
                    if (canUpdateStatus) {
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = onAddAttachmentClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                        ) {
                            Icon(Icons.Filled.Add, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Attachment", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusDisplay(status: TaskStatus, canUpdate: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(enabled = canUpdate, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(status.bgColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Label Status & Ikon Kiri
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Schedule, null, tint = status.color, modifier = Modifier.size(18.dp))
                Text(status.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = status.color)
            }

            if (canUpdate) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    null,
                    tint = status.color,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusDropdown(currentStatus: TaskStatus, onStatusChange: (TaskStatus) -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskStatus.values().filter { it != currentStatus }.forEach { status ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onStatusChange(status) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(Color(0xFFF8F9FA))
            ) {
                Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.RadioButtonUnchecked, null, tint = status.color, modifier = Modifier.size(18.dp))
                    Text(status.displayName, fontSize = 14.sp, color = Color(0xFF2D2D2D))
                }
            }
        }
    }
}

@Composable
private fun CommentsSection(
    comments: List<TaskComment>,
    commentText: String,
    onCommentTextChange: (String) -> Unit,
    onSendComment: () -> Unit,
    teamColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Text("Comments (${comments.size})", fontWeight = FontWeight.SemiBold, color = Color(0xFF2D2D2D))
            }
            Spacer(Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                comments.forEach { comment -> CommentItem(comment, teamColor) }
            }
            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = onCommentTextChange,
                    placeholder = { Text("Write a comment...", fontSize = 13.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedBorderColor = Color(0xFFE8E8E8),
                        focusedBorderColor = teamColor
                    )
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = onSendComment,
                    enabled = commentText.isNotBlank(),
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(if (commentText.isNotBlank()) teamColor else Color.LightGray),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Send, null, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun CommentItem(comment: TaskComment, teamColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(if (comment.isCurrentUser) teamColor else Color(0xFFE0E0E0)),
            contentAlignment = Alignment.Center
        ) {
            Text(comment.userName.first().toString().uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (comment.isCurrentUser) "You" else comment.userName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(comment.timestamp, fontSize = 11.sp, color = Color.Gray)
            }
            Text(comment.comment, fontSize = 13.sp, color = Color(0xFF424242))
        }
    }
}

@Composable
private fun PriorityChip(priority: TaskPriority) {
    Surface(color = priority.bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            priority.displayName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 11.sp, fontWeight = FontWeight.Bold, color = priority.color
        )
    }
}

@Composable
private fun AssigneeChip(member: TeamMember, isCurrentUser: Boolean) {
    val color = if (isCurrentUser) Color(0xFFF472B6) else Color(0xFF6A70D7)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.1f),
        border = if (isCurrentUser) BorderStroke(1.dp, color.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(modifier = Modifier.size(18.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
                Text(member.name.first().toString().uppercase(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Text(if (isCurrentUser) "You" else member.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
private fun AttachmentChip(attachment: TaskAttachment, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(Color(0xFFF8F9FA)),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.InsertDriveFile, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
            Text(attachment.fileName, fontSize = 13.sp, color = Color(0xFF424242))
        }
    }
}

@Composable
private fun PermissionInfo(canUpdate: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (canUpdate) Icons.Filled.CheckCircle else Icons.Filled.Lock, null,
            tint = if (canUpdate) Color(0xFF10B981) else Color(0xFFF59E0B),
            modifier = Modifier.size(14.dp)
        )
        Text(
            if (canUpdate) "Status can be updated" else "Viewing only (Restricted)",
            fontSize = 11.sp, color = if (canUpdate) Color(0xFF10B981) else Color(0xFFF59E0B)
        )
    }
}