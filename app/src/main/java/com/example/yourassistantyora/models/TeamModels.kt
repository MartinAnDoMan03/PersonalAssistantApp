package com.example.yourassistantyora.models

import androidx.compose.ui.graphics.Color

// Team Color Scheme
enum class TeamColorScheme(val gradient: List<Color>) {
    BLUE(listOf(Color(0xFF5774FF), Color(0xFF4957EA))),
    PINK(listOf(Color(0xFFAB53F0), Color(0xFFBF3FBB), Color(0xFFD32E8B))),
    GREEN(listOf(Color(0xFF21C360), Color(0xFF18AE73), Color(0xFF0E9884))),
    ORANGE(listOf(Color(0xFFFAA774), Color(0xFFF49877), Color(0xFFED857C)))
}

// Task Status
enum class TaskStatus(val displayName: String, val color: Color, val bgColor: Color) {
    NOT_STARTED("Not Started", Color(0xFFEF5350), Color(0xFFFFEBEE)),
    IN_PROGRESS("In Progress", Color(0xFFFF9800), Color(0xFFFFF3E0)),
    DONE("Done", Color(0xFF4CAF50), Color(0xFFE8F5E9))
}

// Task Priority
enum class TaskPriority(val displayName: String, val color: Color, val bgColor: Color) {
    LOW("Low", Color(0xFF2196F3), Color(0xFFE3F2FD)),
    MEDIUM("Medium", Color(0xFFFFB300), Color(0xFFFFF8E1)),
    HIGH("High", Color(0xFFEF5350), Color(0xFFFFEBEE))
}

// Team Member
data class TeamMember(
    val id: String,
    val name: String,
    val role: String, // "Admin" or "Member"
    val avatar: String = "",
    val tasksCompleted: Int = 0
)

// Team Task
data class TeamTask(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val assignedTo: List<TeamMember>,
    val deadline: String,
    val attachments: List<TaskAttachment> = emptyList(),
    val comments: List<TaskComment> = emptyList(),
    val createdBy: String,
    val createdAt: String
)

// Task Attachment
data class TaskAttachment(
    val id: String,
    val fileName: String,
    val fileUrl: String = ""
)

// Task Comment
data class TaskComment(
    val id: String,
    val userId: String,
    val userName: String,
    val userAvatar: String = "",
    val comment: String,
    val timestamp: String,
    val isCurrentUser: Boolean = false
)

// Team Detail (Full information)
data class TeamDetail(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val colorScheme: TeamColorScheme,
    val members: List<TeamMember>,
    val tasks: List<TeamTask>,
    val currentUserRole: String, // "Admin" or "Member"
    val currentUserId: String,
    val inviteCode: String
)