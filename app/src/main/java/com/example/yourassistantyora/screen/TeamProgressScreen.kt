package com.example.yourassistantyora.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamProgressScreen(
    teamDetail: TeamDetail,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTasks = teamDetail.tasks.size
    val completedTasks = teamDetail.tasks.count { it.status == TaskStatus.DONE }
    val progressPercentage = if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0

    // Count by status
    val doneCount = teamDetail.tasks.count { it.status == TaskStatus.DONE }
    val inProgressCount = teamDetail.tasks.count { it.status == TaskStatus.IN_PROGRESS }
    val notStartedCount = teamDetail.tasks.count { it.status == TaskStatus.NOT_STARTED }

    // Tasks per member
    val tasksPerMember = teamDetail.members.map { member ->
        val memberTasks = teamDetail.tasks.count { task ->
            task.assignedTo.any { it.id == member.id }
        }
        member to memberTasks
    }.sortedByDescending { it.second }

    // Overdue tasks (simple check based on current date - simplified for demo)
    val overdueTasks = 3 // This would be calculated based on actual deadline comparison

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
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.ArrowBack, "Back", tint = Color(0xFF2D2D2D))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        }
    ) { paddingValues ->
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

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$progressPercentage %",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = teamDetail.colorScheme.gradient.last()
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
                                    .fillMaxWidth(progressPercentage / 100f)
                                    .fillMaxHeight()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            teamDetail.colorScheme.gradient
                                        )
                                    )
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
                            icon = Icons.Filled.Circle,
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
                                teamColor = teamDetail.colorScheme.gradient.last()
                            )
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            }

            // Overdue Warning Card (if any)
            if (overdueTasks > 0) {
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
                                    "These tasks have passed their deadlines. Review and reassign!",
                                    fontSize = 12.sp,
                                    color = Color(0xFF757575)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBreakdownItem(
    icon: ImageVector,
    label: String,
    count: Int,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                label,
                fontSize = 13.sp,
                color = Color(0xFF2D2D2D)
            )
        }
        Text(
            "$count Tasks",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF2D2D2D)
        )
    }
}

@Composable
fun MemberProgressItem(
    member: TeamMember,
    taskCount: Int,
    teamColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
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
                Text(
                    member.role,
                    fontSize = 11.sp,
                    color = Color(0xFF9E9E9E)
                )
            }
        }

        Surface(
            color = teamColor.copy(alpha = 0.1f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                "$taskCount tasks",
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = teamColor
            )
        }
    }
}