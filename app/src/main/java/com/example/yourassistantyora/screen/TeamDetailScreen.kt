package com.example.yourassistantyora.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.yourassistantyora.models.*
import com.example.yourassistantyora.viewModel.TeamDetailViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamId: String,
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TeamDetailViewModel = viewModel()
) {
    val context = LocalContext.current
    val teamDetail by viewModel.teamDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Picu pemuatan data saat pertama kali masuk
    LaunchedEffect(key1 = teamId) {
        if (teamId.isNotBlank()) {
            viewModel.loadTeamDetails(teamId)
        }
    }

    // Tampilkan pesan error jika ada
    LaunchedEffect(key1 = error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
        }
    }

    var selectedFilter by remember { mutableStateOf<TaskStatus?>(null) }
    var showInviteDialog by remember { mutableStateOf(false) }
//    var showAccessDeniedDialog by remember { mutableStateOf(false) }

    // Tampilkan UI berdasarkan state
    if (isLoading && teamDetail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else if (teamDetail != null) {
        val detail = teamDetail!!
        val isAdmin = detail.currentUserRole == "Admin"
        val sortedMembers = remember(detail.members) {
            detail.members.sortedWith(
                compareByDescending<TeamMember> { it.role == "Admin" } // Admin (true) di atas
                    .thenBy { it.name.lowercase() } // Sisanya urut ABC
            )
        }


        val filteredTasks = if (selectedFilter != null) {
            detail.tasks.filter { it.status == selectedFilter }
        } else {
            detail.tasks
        }
        val doneCount = detail.tasks.count { it.status == TaskStatus.DONE }
        val activeCount = detail.tasks.count { it.status != TaskStatus.DONE }

        Scaffold(
            containerColor = Color(0xFFF8F9FA),
            floatingActionButton = {
                if (isAdmin) {
                    FloatingActionButton(
                        onClick = { navController.navigate("create_team_task/${detail.id}") },
                        containerColor = detail.colorScheme.gradient.last(),
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Filled.Add, "Create Task")
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Header with gradient
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.horizontalGradient(
                                    detail.colorScheme.gradient
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
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(
                                        Icons.Filled.ArrowBack,
                                        "Back",
                                        tint = Color.White
                                    )
                                }

                                if (isAdmin) {
                                    Button(
                                        onClick = { showInviteDialog = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.White.copy(alpha = 0.2f)
                                        ),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text(
                                            "Invite",
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            Text(
                                detail.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                detail.description,
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Spacer(Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.People,
                                            null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            "${detail.members.size} members",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Surface(
                                    color = Color.White.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        detail.currentUserRole,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // Status Cards
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatusCard(
                            count = activeCount,
                            label = "Active",
                            color = Color(0xFFE3F2FD),
                            modifier = Modifier.weight(1f)
                        )
                        StatusCard(
                            count = doneCount,
                            label = "Done",
                            color = Color(0xFFE8F5E9),
                            modifier = Modifier.weight(1f)
                        )
                        ProgressCard(
                            onClick = { navController.navigate("team_progress/${detail.id}") },
                            color = detail.colorScheme.gradient.last(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Filter Chips
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .padding(top = 20.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("All", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = detail.colorScheme.gradient.last(),
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == TaskStatus.NOT_STARTED,
                            onClick = { selectedFilter = TaskStatus.NOT_STARTED },
                            label = { Text("Not Started", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TaskStatus.NOT_STARTED.color,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == TaskStatus.IN_PROGRESS,
                            onClick = { selectedFilter = TaskStatus.IN_PROGRESS },
                            label = { Text("In Progress", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TaskStatus.IN_PROGRESS.color,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = selectedFilter == TaskStatus.DONE,
                            onClick = { selectedFilter = TaskStatus.DONE },
                            label = { Text("Done", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = TaskStatus.DONE.color,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Section Title
                item {
                    Text(
                        "Team Tasks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // Task List
                items(filteredTasks, key = { it.id }) { task ->
                    TeamTaskCard(
                        task = task,
                        teamColor = detail.colorScheme.gradient.last(),
                        currentUserId = detail.currentUserId,
                        onClick = {
                            navController.navigate("team_task_detail/${detail.id}/${task.id}")
                        },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                    )
                }

                // Team Members Section
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Team Members",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                // Menggunakan sortedMembers yang sudah diproses di atas
                items(sortedMembers, key = { it.id }) { member ->
                    MemberCard(
                        member = member,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }

                item { Spacer(Modifier.height(80.dp)) }
            }

//            if (showAccessDeniedDialog) {
//                AccessDeniedDialog(onDismiss = { showAccessDeniedDialog = false })
//            }

            // Invite Dialog
            if (showInviteDialog) {
                InviteMembersDialog(
                    inviteCode = detail.inviteCode,
                    teamColor = detail.colorScheme.gradient.last(),
                    showDialog = showInviteDialog,
                    onDismiss = { showInviteDialog = false },
                    onCopyCode = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Invite Code", detail.inviteCode)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code Copied", Toast.LENGTH_SHORT).show()
                    },
                    onSendEmail = { email ->
                        val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                            data = android.net.Uri.parse("mailto:$email")
                            putExtra(android.content.Intent.EXTRA_SUBJECT, "Join my team '${detail.name}' on Yora")
                            putExtra(android.content.Intent.EXTRA_TEXT, "Hey! Let's join my team '${detail.name}' using this code: ${detail.inviteCode}")
                        }
                        context.startActivity(intent)
                        showInviteDialog = false
                    }
                )
            }
        }
    } else {
        // Tampilan jika terjadi error atau teamDetail null setelah loading
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    error ?: "Team not found or failed to load.",
                    color = Color.Gray
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    count: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(color),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )
            Text(
                label,
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun ProgressCard(
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFE8E8E8))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.BarChart,
                contentDescription = "Progress",
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Text(
                "Progress",
                fontSize = 11.sp,
                color = Color(0xFF757575)
            )
        }
    }
}

@Composable
fun TeamTaskCard(
    task: TeamTask,
    teamColor: Color,
    currentUserId: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row {
            // Garis vertikal di kiri
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(teamColor)
            )

            // Konten utama kartu
            Column(
                modifier = Modifier
                    .padding(14.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Baris Atas: "Your Task" badge dan Priority badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    // Badge "Your Task" hanya muncul jika tugas ini milik user saat ini
                    if (task.assignedTo.any { it.id == currentUserId }) {
                        Surface(
                            color = teamColor,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    null,
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    "Your Task",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Spacer agar priority badge tetap di kanan
                        Spacer(modifier = Modifier)
                    }

                    // Priority Badge
                    Surface(
                        color = task.priority.bgColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            task.priority.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = task.priority.color
                        )
                    }
                }

                // Judul dan Deskripsi
                Column {
                    Text(
                        task.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        task.description,
                        fontSize = 12.sp,
                        color = Color(0xFF757575),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Baris Bawah: Status, Avatars, dan Comments
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Status Badge
                        Surface(shape = RoundedCornerShape(16.dp), color = task.status.bgColor) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(task.status.color)
                                )
                                Text(
                                    task.status.displayName,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = task.status.color
                                )
                            }
                        }

                        // Avatar Members
                        if (task.assignedTo.isNotEmpty()) {
                            Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                                task.assignedTo.take(3).forEach { member ->
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(teamColor.copy(alpha = 0.2f))
                                            .border(BorderStroke(1.dp, Color.White), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            member.name.firstOrNull()?.uppercase() ?: "?",
                                            color = teamColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Comments count
                    if (task.comments.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChatBubbleOutline,
                                null,
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                task.comments.size.toString(),
                                fontSize = 11.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MemberCard(
    member: TeamMember,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (member.role == "Admin") Color(0xFFAB53F0)
                            else Color(0xFF4CAF50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        member.name.first().toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        member.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D2D2D)
                    )
                    Text(
                        "${member.activeTasks} active tasks",
                        fontSize = 11.sp,
                        color = Color(0xFF9E9E9E)
                    )
                }
            }

            Surface(
                color = if (member.role == "Admin") Color(0xFFF3E5F5) else Color(0xFFE8F5E9),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    member.role,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (member.role == "Admin") Color(0xFF9C27B0) else Color(0xFF4CAF50)
                )
            }
        }
    }
}

@Composable
private fun AccessDeniedDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Lock, contentDescription = null, modifier = Modifier.size(32.dp)) },
        title = { Text("Access Denied", fontWeight = FontWeight.Bold) },
        text = { Text("This task is not assigned to you. You can only view details of your own tasks.") },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A70D7))
            ) {
                Text("OK")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
//
//@Composable
//fun InviteMembersDialog(
//    inviteCode: String,
//    teamColor: Color,
//    showDialog: Boolean,
//    onDismiss: () -> Unit,
//    onCopyCode: () -> Unit,
//    onSendEmail: (String) -> Unit
//) {
//    var emailInput by remember { mutableStateOf("") }
//
//    if (showDialog) {
//        AlertDialog(
//            onDismissRequest = onDismiss,
//            title = {
//                Text(
//                    "Invite Members",
//                    fontWeight = FontWeight.Bold
//                )
//            },
//            text = {
//                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
//                    Text(
//                        "Share this code with your team members:",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//
//                    // Invite Code Display
//                    Surface(
//                        color = teamColor.copy(alpha = 0.1f),
//                        shape = RoundedCornerShape(8.dp),
//                        border = BorderStroke(1.dp, teamColor.copy(alpha = 0.3f))
//                    ) {
//                        Text(
//                            inviteCode,
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .padding(16.dp),
//                            fontSize = 18.sp,
//                            fontWeight = FontWeight.Bold,
//                            color = teamColor
//                        )
//                    }
//
//                    Divider(modifier = Modifier.padding(vertical = 8.dp))
//
//                    // Email Input
//                    Text(
//                        "Or send invite via email:",
//                        fontSize = 14.sp,
//                        color = Color.Gray
//                    )
//
//                    OutlinedTextField(
//                        value = emailInput,
//                        onValueChange = { emailInput = it },
//                        placeholder = { Text("Enter email address") },
//                        modifier = Modifier.fillMaxWidth(),
//                        singleLine = true
//                    )
//                }
//            },
//            confirmButton = {
//                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    Button(
//                        onClick = onCopyCode,
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = teamColor
//                        )
//                    ) {
//                        Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(16.dp))
//                        Spacer(Modifier.width(4.dp))
//                        Text("Copy")
//                    }
//
//                    if (emailInput.isNotBlank()) {
//                        Button(
//                            onClick = { onSendEmail(emailInput) },
//                            colors = ButtonDefaults.buttonColors(
//                                containerColor = teamColor
//                            )
//                        ) {
//                            Icon(Icons.Default.Email, null, modifier = Modifier.size(16.dp))
//                            Spacer(Modifier.width(4.dp))
//                            Text("Send")
//                        }
//                    }
//                }
//            },
//            dismissButton = {
//                TextButton(onClick = onDismiss) {
//                    Text("Close")
//                }
//            }
//        )
//    }
//}