package com.example.yourassistantyora.screen

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.models.*
import com.example.yourassistantyora.viewModel.TeamDetailViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamProgressScreen(
    teamId: String, // ✅ Diambil dari NavController
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: TeamDetailViewModel = viewModel() // ✅ Gunakan ViewModel yang sama
) {
    val context = LocalContext.current
    val teamDetail by viewModel.teamDetail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Load data saat pertama kali masuk
    LaunchedEffect(key1 = teamId) {
        viewModel.loadTeamDetails(teamId)
    }

    // Tampilkan error jika ada
    LaunchedEffect(key1 = error) {
        error?.let { Toast.makeText(context, it, Toast.LENGTH_LONG).show() }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Team Progress",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D2D2D)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFF2D2D2D))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (isLoading && teamDetail == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (teamDetail != null) {
            // Jika data sudah siap, tampilkan konten
            val detail = teamDetail!!

            // --- Logika Kalkulasi ---
            val totalTasks = detail.tasks.size
            val completedTasks = detail.tasks.count { it.status == TaskStatus.DONE }
            val progressPercentage = if (totalTasks > 0) (completedTasks.toFloat() / totalTasks.toFloat()) else 0f

            val doneCount = detail.tasks.count { it.status == TaskStatus.DONE }
            val inProgressCount = detail.tasks.count { it.status == TaskStatus.IN_PROGRESS }
            val notStartedCount = detail.tasks.count { it.status == TaskStatus.NOT_STARTED }

            val tasksPerMember = detail.members.map { member ->
                val memberTasks = detail.tasks.count { task ->
                    task.assignedTo.any { it.id == member.id }
                }
                member to memberTasks
            }.sortedByDescending { it.second }

            // Cek tugas yang overdue (lewat dari hari ini dan statusnya belum DONE)
            val overdueTasks = detail.tasks.count {
                val deadlineDate = try {
                    // Coba parsing dengan format yang mungkin
                    SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(it.deadline)
                } catch (e: Exception) {
                    null // Jika format tidak sesuai, anggap tidak overdue
                }
                deadlineDate != null && deadlineDate.before(Date()) && it.status != TaskStatus.DONE
            }

            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Overall Progress Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Overall Progress",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )
                            Spacer(Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End // Letakkan di ujung kanan
                            ) {
                                Text(
                                    text = "${(progressPercentage * 100).toInt()}%",
                                    fontSize = 14.sp, // Ukuran font lebih kecil
                                    fontWeight = FontWeight.SemiBold,
                                    color = detail.colorScheme.gradient.last()
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFFE8E8E8))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(progressPercentage)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Brush.horizontalGradient(detail.colorScheme.gradient))
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "$completedTasks of $totalTasks tasks completed",
                                fontSize = 12.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                }

                // Status Breakdown Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Status Breakdown",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )
                            Spacer(Modifier.height(16.dp))
                            StatusBreakdownItem(
                                icon = Icons.Filled.CheckCircle,
                                label = "Done",
                                count = doneCount,
                                color = TaskStatus.DONE.color
                            )
                            Spacer(Modifier.height(12.dp))
                            StatusBreakdownItem(
                                icon = Icons.Filled.Schedule,
                                label = "In Progress",
                                count = inProgressCount,
                                color = TaskStatus.IN_PROGRESS.color
                            )
                            Spacer(Modifier.height(12.dp))
                            StatusBreakdownItem(
                                icon = Icons.Filled.RadioButtonUnchecked, // Icon lebih sesuai
                                label = "Not Started",
                                count = notStartedCount,
                                color = TaskStatus.NOT_STARTED.color
                            )
                        }
                    }
                }

                // Tasks per Member Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(Color.White),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Tasks per Member",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )
                            Spacer(Modifier.height(16.dp))
                            tasksPerMember.forEach { (member, taskCount) ->
                                MemberProgressItem(
                                    member = member,
                                    taskCount = taskCount,
                                    teamColor = detail.colorScheme.gradient.last()
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    }
                }

                // Overdue Warning Card (if any)
                if (overdueTasks > 0 && detail.currentUserRole == "Admin") {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(Color(0xFFFFEBEE)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Warning,
                                    null,
                                    tint = Color(0xFFEF5350),
                                    modifier = Modifier.size(24.dp)
                                )
                                Column {
                                    Text(
                                        "$overdueTasks Overdue Tasks",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFEF5350)
                                    )
                                    Text(
                                        "Some tasks have passed their deadlines. Review and reassign!",
                                        fontSize = 12.sp,
                                        color = Color(0xFF757575)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Tampilan jika terjadi error atau teamDetail null setelah loading
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "Failed to load team progress.")
            }
        }
    }
}
/**
 * Composable untuk menampilkan satu baris dalam "Status Breakdown".
 * Contoh: Done ..... 8 Tasks
 */
@Composable
private fun StatusBreakdownItem(
    icon: ImageVector,
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = Color(0xFF616161)
            )
        }
        Text(
            text = "$count Tasks",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D2D2D)
        )
    }
}

/**
 * Composable untuk menampilkan progres per anggota tim.
 * Contoh: (Avatar) Chris Evans (Admin)
 *           5 active tasks
 */
@Composable
private fun MemberProgressItem(
    member: TeamMember,
    taskCount: Int, // Ini adalah total task yang di-assign ke dia
    teamColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(teamColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.name.take(2).uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = teamColor
                )
            }
            // Nama dan jumlah task
            Column {
                Text(
                    text = member.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2D2D2D)
                )
                Text(
                    text = "${member.activeTasks} active tasks", // Menggunakan activeTasks dari model
                    fontSize = 12.sp,
                    color = Color(0xFF757575)
                )
            }
        }
        // Role Badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (member.role == "Admin") teamColor.copy(alpha = 0.15f) else Color(0xFFE8E8E8),
        ) {
            Text(
                text = member.role,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = if (member.role == "Admin") teamColor else Color(0xFF616161)
            )
        }
    }
}