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
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    detailViewModel: TeamDetailViewModel = viewModel(), // Untuk mendapatkan warna tim
    taskViewModel: TeamTaskDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val teamDetail by detailViewModel.teamDetail.collectAsState()
    val taskDetail by taskViewModel.taskDetail.collectAsState()
    val comments by taskViewModel.comments.collectAsState()
    val isLoading by taskViewModel.isLoading.collectAsState()
    val error by taskViewModel.error.collectAsState()
    var attachmentToDelete by remember { mutableStateOf<TaskAttachment?>(null) }

    // Ambil detail tim (untuk warna) dan detail task
    LaunchedEffect(key1 = teamId, key2 = taskId) {
        if (teamId.isNotBlank()) detailViewModel.loadTeamDetails(teamId)
        if (taskId.isNotBlank()) taskViewModel.loadTaskAndComments(taskId, teamId)
    }

// Launcher untuk pemilih file
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Panggil fungsi ViewModel yang baru
            taskViewModel.addAttachment(taskId, it, context)
        }
    }

// Definisikan izin yang dibutuhkan
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

// Launcher untuk meminta izin
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        if (permissions.values.any { it }) {
            // Jika izin diberikan, buka pemilih file
            filePickerLauncher.launch("*/*") // Buka untuk semua tipe file
        } else {
            Toast.makeText(context, "Storage permission is required to attach files.", Toast.LENGTH_LONG).show()
        }
    }

    // Tampilkan error
    LaunchedEffect(key1 = error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    if (isLoading || teamDetail == null || taskDetail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        // Data sudah siap
        val readyTeamDetail = teamDetail!!
        val readyTaskDetail = taskDetail!!

        var commentText by remember { mutableStateOf("") }
        var showStatusDropdown by remember { mutableStateOf(false) }

        val isAssignedToUser = readyTaskDetail.assignedTo.any { it.id == readyTeamDetail.currentUserId }
        val canUpdateStatus = readyTeamDetail.currentUserRole == "Admin" || isAssignedToUser

        val screenTitle = if (isAssignedToUser && readyTeamDetail.currentUserRole == "Member") "Your Task"
        // ✅ PERBAIKAN: Gunakan readyTaskDetail juga di sini
        else if (readyTaskDetail.assignedTo.isNotEmpty()) "${readyTaskDetail.assignedTo.first().name.split(" ").first()}'s Task"
        else "Task Details"

        if (attachmentToDelete != null) {
            AlertDialog(
                onDismissRequest = { attachmentToDelete = null }, // Tutup dialog jika klik di luar
                title = { Text("Delete Attachment") },
                text = { Text("Are you sure you want to delete '${attachmentToDelete!!.fileName}'? This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // Panggil fungsi ViewModel untuk menghapus
                            taskViewModel.deleteAttachment(taskId, attachmentToDelete!!)
                            attachmentToDelete = null // Tutup dialog setelah konfirmasi
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { attachmentToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        Scaffold(containerColor = Color(0xFFF8F9FA)) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header
                HeaderSection(
                    teamGradient = readyTeamDetail.colorScheme.gradient, // ✅ PERBAIKAN
                    title = screenTitle,
                    isAssigned = isAssignedToUser,
                    role = readyTeamDetail.currentUserRole,
                    onBackClick = { navController.popBackStack() }
                )

                // Konten Utama
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
                                val allPermissionsGranted = permissionsToRequest.all {
                                    ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
                                }
                                if (allPermissionsGranted) {
                                    filePickerLauncher.launch("*/*")
                                } else {
                                    permissionLauncher.launch(permissionsToRequest)
                                }
                            },
                            // ✅ TAMBAHKAN PARAMETER INI
                            onDeleteAttachmentClick = { attachment ->
                                attachmentToDelete = attachment // Buka dialog dengan attachment yang dipilih
                            }
                        )
                    }

                    item {
                        CommentsSection(
                            comments = comments, // Gunakan state `comments`
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


// === HELPER COMPOSABLES ===

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
            .background(
                brush = Brush.horizontalGradient(teamGradient) // ✅ PERBAIKAN: Gunakan list-nya secara langsung
            )
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
                if (isAssigned) "This task is assigned to you. You can update the status to track your progress."
                else if (role == "Admin") "Manage this task and track team progress."
                else "You are viewing this task. Only assigned members can update the status.",
                fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f), lineHeight = 16.sp
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
    onAddAttachmentClick: () -> Unit,
    onDeleteAttachmentClick: (TaskAttachment) -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        // Menggunakan Column dengan pembatas (divider) untuk tata letak yang lebih fleksibel
        Column(modifier = Modifier.padding(vertical = 16.dp)) {
            // --- Bagian Title & Description ---
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
                StatusDisplay(task.status, canUpdateStatus) { onShowStatusDropdownChange(!showStatusDropdown) }
                AnimatedVisibility(visible = showStatusDropdown && canUpdateStatus) {
                    StatusDropdown(currentStatus = task.status, onStatusChange = onStatusChange)
                }
                Spacer(Modifier.height(8.dp))
                PermissionInfo(canUpdateStatus)
            }

            Spacer(Modifier.height(16.dp))

            // --- Bagian Priority (tanpa label) & Assigned To ---
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Priority
                PriorityChip(task.priority)

                // Assigned To
                Text("Assigned to", fontSize = 13.sp, color = Color(0xFF757575))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    task.assignedTo.forEach { member ->
                        AssigneeChip(member, isCurrentUser = member.id == currentUserId)
                    }
                }
            }

            // Pembatas
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

            // --- Bagian Deadline ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Text("Deadline", fontSize = 13.sp, color = Color(0xFF757575))
                }
                Text(
                    // Format tanggal agar sesuai dengan "yyyy-MM-dd"
                    text = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(task.deadline)),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2D2D2D)
                )
            }

            // --- Bagian Attachments ---
            if (task.attachments.isNotEmpty() || canUpdateStatus) { // Tampilkan section jika ada attachment ATAU bisa update
                // Pembatas
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF0F0F0))

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Attachment, null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                        Text("${task.attachments.size} Attachments", fontSize = 13.sp, color = Color(0xFF757575))
                    }
                    // List attachment di bawahnya
                    task.attachments.forEach { attachment ->
                        AttachmentChip(
                            attachment = attachment,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(attachment.fileUrl))
                                context.startActivity(intent)
                            },
                            // Teruskan aksi hapus ke parent
                            onDeleteClick = { onDeleteAttachmentClick(attachment) },
                            // Izinkan hapus jika user bisa update status
                            canDelete = canUpdateStatus
                        )
                    }

                    // ✅ TAMBAHKAN TOMBOL "ADD ATTACHMENT" DI SINI
                    if (canUpdateStatus) {
                        Spacer(Modifier.height(8.dp))
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
            // Header
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.ChatBubbleOutline, null, tint = Color.Gray)
                Text("Comments (${comments.size})", fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(16.dp))

            // Comment List
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                comments.forEach { comment ->
                    CommentItem(comment, teamColor)
                }
            }
            Spacer(Modifier.height(16.dp))

            // Input Field
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
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (commentText.isNotBlank()) teamColor else Color.LightGray),
                    colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                ) {
                    Icon(Icons.Filled.Send, null)
                }
            }
        }
    }
}

// === HELPER-HELPER KECIL ===

@Composable
private fun DetailRow(label: String, content: @Composable RowScope.() -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = Color(0xFF757575),
            modifier = Modifier.width(90.dp)
        )
        content()
    }
}

@Composable
private fun StatusDisplay(status: TaskStatus, canUpdate: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canUpdate, onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(status.bgColor)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, tint = status.color, modifier = Modifier.size(18.dp))
                Text(status.displayName, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = status.color)
            }
            if (canUpdate) {
                Icon(Icons.Filled.KeyboardArrowDown, null, tint = status.color)
            }
        }
    }
}

@Composable
private fun StatusDropdown(currentStatus: TaskStatus, onStatusChange: (TaskStatus) -> Unit) {
    Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TaskStatus.values().filter { it != currentStatus }.forEach { status ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStatusChange(status) },
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
private fun PermissionInfo(canUpdate: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (canUpdate) Icons.Filled.Edit else Icons.Filled.Lock, null,
            tint = if (canUpdate) Color(0xFF16A34A) else Color(0xFFED8936),
            modifier = Modifier.size(14.dp)
        )
        Text(
            if (canUpdate) "You can update this task status" else "Only assigned members or Admins can update",
            fontSize = 11.sp, color = if (canUpdate) Color(0xFF16A34A) else Color(0xFFED8936)
        )
    }
}

@Composable
private fun PriorityChip(priority: TaskPriority) {
    Surface(color = priority.bgColor, shape = RoundedCornerShape(8.dp)) {
        Text(
            priority.displayName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            fontSize = 12.sp, fontWeight = FontWeight.Medium, color = priority.color
        )
    }
}

@Composable
private fun AssigneeChip(member: TeamMember, isCurrentUser: Boolean) {
    val color = if (isCurrentUser) Color(0xFFF472B6) else Color(0xFF6A70D7)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.15f),
        border = if (isCurrentUser) BorderStroke(1.dp, color) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(member.name.first().toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Text(if (isCurrentUser) "You" else member.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = color)
        }
    }
}

@Composable
private fun AttachmentChip(
    attachment: TaskAttachment,
    onClick: () -> Unit,
    // ✅ TAMBAHKAN PARAMETER UNTUK AKSI HAPUS DAN APAKAH BISA HAPUS
    onDeleteClick: () -> Unit,
    canDelete: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Description, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                Text(
                    attachment.fileName,
                    fontSize = 13.sp,
                    color = Color(0xFF2D2D2D),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ✅ TAMPILKAN TOMBOL HAPUS JIKA DIIZINKAN
            if (canDelete) {
                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Filled.Close, "Delete attachment", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun CommentItem(comment: TaskComment, teamColor: Color) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (comment.isCurrentUser) teamColor else Color.Gray.copy(alpha = 0.3f)),
            contentAlignment = Alignment.Center
        ) {
            Text(comment.userName.first().toString(), color = if (comment.isCurrentUser) Color.White else teamColor, fontWeight = FontWeight.Bold)
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(if (comment.isCurrentUser) "You" else comment.userName, fontWeight = FontWeight.SemiBold)
                Text(comment.timestamp, fontSize = 11.sp, color = Color.Gray) // ✅ PERBAIKAN
            }
            Text(comment.comment, fontSize = 13.sp, color = Color(0xFF616161))
        }
    }
}

