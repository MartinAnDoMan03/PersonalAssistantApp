package com.example.yourassistantyora

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.yourassistantyora.models.*
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class TeamDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAssistantYoraTheme {
                TeamDetailNavigation()
            }
        }
    }
}

@Composable
fun TeamDetailNavigation() {
    val navController = rememberNavController()

    // Sample Data - replace with actual ViewModel or data from intent
    val sampleTeamDetail = remember {
        TeamDetail(
            id = "1",
            name = "Mobile Dev Team",
            description = "React Native & Flutter Development",
            category = "Project",
            colorScheme = com.example.yourassistantyora.models.TeamColorScheme.BLUE,
            members = listOf(
                TeamMember("1", "Ofira Evans", "Admin", "", 5),
                TeamMember("2", "Tom Holland", "Member", "", 3),
                TeamMember("3", "Zendaya", "Member", "", 4)
            ),
            tasks = listOf(
                TeamTask(
                    id = "1",
                    title = "Design login screen mockup",
                    description = "Create high-fidelity mockups for the new login flow",
                    status = TaskStatus.IN_PROGRESS,
                    priority = TaskPriority.HIGH,
                    assignedTo = listOf(
                        TeamMember("1", "Ofira Evans", "Admin"),
                        TeamMember("3", "Zendaya", "Member")
                    ),
                    deadline = "2025-10-28",
                    attachments = listOf(
                        TaskAttachment("1", "design_mockup.fig")
                    ),
                    comments = listOf(
                        TaskComment("1", "3", "Zendaya", "", "Great! Let me know if you need any assets or resources.", "19:30 AM", false),
                        TaskComment("2", "1", "You", "", "Perfect timing. Client wants to see progress tomorrow.", "19:44 AM", true)
                    ),
                    createdBy = "1",
                    createdAt = "2025-10-20"
                ),
                TeamTask(
                    id = "2",
                    title = "Implement authentication API",
                    description = "Setup JWT authentication with refresh tokens",
                    status = TaskStatus.NOT_STARTED,
                    priority = TaskPriority.HIGH,
                    assignedTo = listOf(
                        TeamMember("2", "Tom Holland", "Member")
                    ),
                    deadline = "2025-10-28",
                    attachments = emptyList(),
                    comments = listOf(
                        TaskComment("3", "3", "Zendaya", "", "I am working on the mockups. Should have the first draft ready by EOD.", "20:05 AM", false)
                    ),
                    createdBy = "1",
                    createdAt = "2025-10-21"
                ),
                TeamTask(
                    id = "3",
                    title = "Write unit tests",
                    description = "Add test coverage for authentication module",
                    status = TaskStatus.DONE,
                    priority = TaskPriority.MEDIUM,
                    assignedTo = listOf(
                        TeamMember("1", "Ofira Evans", "Admin")
                    ),
                    deadline = "2025-10-25",
                    attachments = emptyList(),
                    comments = listOf(
                        TaskComment("4", "1", "You", "", "Great! Let me know if you need any assets or resources.", "18:04 AM", true)
                    ),
                    createdBy = "1",
                    createdAt = "2025-10-15"
                )
            ),
            currentUserRole = "Admin", // Change to "Member" to test member view
            currentUserId = "1",
            inviteCode = "ABC123"
        )
    }

    var showInviteDialog by remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = "team_detail"
    ) {
        // Team Detail Screen
        composable("team_detail") {
            TeamDetailScreen(
                teamDetail = sampleTeamDetail,
                onBackClick = {
                    // finish activity or navigate back
                },
                onInviteClick = {
                    showInviteDialog = true
                },
                onProgressClick = {
                    navController.navigate("team_progress")
                },
                onCreateTaskClick = {
                    navController.navigate("create_task")
                },
                onTaskClick = { task ->
                    navController.navigate("task_view/${task.id}")
                }
            )

            // Invite Members Dialog
            InviteMembersDialog(
                inviteCode = sampleTeamDetail.inviteCode,
                teamColor = sampleTeamDetail.colorScheme.gradient.last(),
                showDialog = showInviteDialog,
                onDismiss = { showInviteDialog = false },
                onCopyCode = {
                    // Copy to clipboard
                    showInviteDialog = false
                },
                onSendEmail = { email ->
                    // Send invitation email
                    showInviteDialog = false
                }
            )
        }

        // Team Progress Screen
        composable("team_progress") {
            TeamProgressScreen(
                teamDetail = sampleTeamDetail,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Create Task Screen
        composable("create_task") {
            CreateTeamTaskScreen(
                teamMembers = sampleTeamDetail.members,
                teamColor = sampleTeamDetail.colorScheme.gradient.last(),
                onBackClick = {
                    navController.popBackStack()
                },
                onAssignClick = { title, description, members, priority, deadline ->
                    // Handle task creation
                    navController.popBackStack()
                }
            )
        }

        // Task Detail Screen
        composable(
            route = "task_view/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")
            val task = sampleTeamDetail.tasks.find { it.id == taskId }

            task?.let {
                TeamTaskDetailScreen(
                    task = it,
                    teamColor = sampleTeamDetail.colorScheme.gradient.last(),
                    currentUserId = sampleTeamDetail.currentUserId,
                    currentUserRole = sampleTeamDetail.currentUserRole,
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onStatusChange = { newStatus ->
                        // Handle status change
                    },
                    onCommentAdd = { comment ->
                        // Handle new comment
                    }
                )
            }
        }
    }
}