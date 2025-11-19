package com.example.yourassistantyora.screen

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
import androidx.navigation.NavController
import com.example.yourassistantyora.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamDetailScreen(
    teamDetail: TeamDetail,
    onBackClick: () -> Unit,
    onInviteClick: () -> Unit,
    onProgressClick: () -> Unit,
    onCreateTaskClick: () -> Unit,
    onTaskClick: (TeamTask) -> Unit,
    modifier: Modifier = Modifier
) {
    val isAdmin = teamDetail.currentUserRole == "Admin"

    // Filter state
    var selectedFilter by remember { mutableStateOf<TaskStatus?>(null) }

    // Filter tasks based on selected status
    val filteredTasks = if (selectedFilter != null) {
        teamDetail.tasks.filter { it.status == selectedFilter }
    } else {
        teamDetail.tasks
    }

    // Count tasks by status
    val notStartedCount = teamDetail.tasks.count { it.status == TaskStatus.NOT_STARTED }
    val inProgressCount = teamDetail.tasks.count { it.status == TaskStatus.IN_PROGRESS }
    val doneCount = teamDetail.tasks.count { it.status == TaskStatus.DONE }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(
                    onClick = onCreateTaskClick,
                    containerColor = teamDetail.colorScheme.gradient.last(),
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
                                teamDetail.colorScheme.gradient
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
                                Icon(
                                    Icons.Filled.Close,
                                    "Back",
                                    tint = Color.White
                                )
                            }

                            if (isAdmin) {
                                Button(
                                    onClick = onInviteClick,
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
                            teamDetail.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            teamDetail.description,
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
                                        "${teamDetail.members.size} members",
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
                                    teamDetail.currentUserRole,
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
                        count = notStartedCount,
                        label = "Not Started",
                        color = Color(0xFFE8E8E8),
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        count = inProgressCount,
                        label = "In Progress",
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.weight(1f)
                    )
                    StatusCard(
                        count = doneCount,
                        label = "Done",
                        color = Color(0xFFE8F5E9),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Progress Card (Clickable)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(top = 16.dp)
                        .clickable { onProgressClick() },
                    shape = RoundedCornerShape(12.dp),
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
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Filled.BarChart,
                                null,
                                tint = teamDetail.colorScheme.gradient.last(),
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                "View Progress",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF2D2D2D)
                            )
                        }
                        Icon(
                            Icons.Filled.KeyboardArrowRight,
                            null,
                            tint = Color(0xFF9E9E9E)
                        )
                    }
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
                            selectedContainerColor = teamDetail.colorScheme.gradient.last(),
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
            items(filteredTasks) { task ->
                TeamTaskCard(
                    task = task,
                    teamColor = teamDetail.colorScheme.gradient.last(),
                    onClick = { onTaskClick(task) },
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

            items(teamDetail.members) { member ->
                MemberCard(
                    member = member,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
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
fun TeamTaskCard(
    task: TeamTask,
    teamColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Status Badge
                    Surface(
                        color = task.status.bgColor,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            task.status.displayName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = task.status.color
                        )
                    }

                    Spacer(Modifier.height(8.dp))

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
                        maxLines = 2
                    )
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

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Assigned members avatars
                Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                    task.assignedTo.take(3).forEach { member ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(teamColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                member.name.first().toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (task.comments.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.ChatBubbleOutline,
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
                    if (task.attachments.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.AttachFile,
                                null,
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                task.attachments.size.toString(),
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
                        "${member.tasksCompleted} tasks",
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

/**
 * Wrapper khusus untuk dipakai di Navigation
 * route: team_detail/{teamId}
 */
@Composable
fun TeamDetailScreen(
    navController: NavController,
    teamId: String
) {
    // Dummy data sementara (supaya UI jalan)
    val members = listOf(
        TeamMember("1", "Gladys", "Admin", tasksCompleted = 12),
        TeamMember("2", "Rizky", "Member", tasksCompleted = 5),
        TeamMember("3", "Dita", "Member", tasksCompleted = 3)
    )

    val tasks = listOf(
        TeamTask(
            id = "101",
            title = "Setup Repository",
            description = "Create GitHub repo & branches",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.HIGH,
            assignedTo = members.take(2),
            deadline = "2025-11-30",
            createdBy = "1",
            createdAt = "2025-11-15"
        ),
        TeamTask(
            id = "102",
            title = "Design Wireframe",
            description = "Create initial UI wireframe for main screens",
            status = TaskStatus.NOT_STARTED,
            priority = TaskPriority.MEDIUM,
            assignedTo = members.drop(1),
            deadline = "2025-12-05",
            createdBy = "1",
            createdAt = "2025-11-16"
        )
    )

    val teamDetail = TeamDetail(
        id = teamId,
        name = "Mobile Dev Team",
        description = "Team for mobile app development and experiments.",
        category = "Project",
        colorScheme = TeamColorScheme.BLUE,
        members = members,
        tasks = tasks,
        currentUserRole = "Admin",
        currentUserId = "1",
        inviteCode = "ABC123"
    )

    TeamDetailScreen(
        teamDetail = teamDetail,
        onBackClick = { navController.popBackStack() },
        onInviteClick = { /* TODO: open share/invite bottom sheet */ },
        onProgressClick = { /* TODO: navigate to progress screen */ },
        onCreateTaskClick = {
            navController.navigate("create_task")
        },
        onTaskClick = { task ->
            navController.navigate("task_detail/${task.id}")
        }
    )
}
