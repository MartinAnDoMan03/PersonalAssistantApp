package com.example.yourassistantyora.screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items

/* ---------------- DATA ---------------- */

data class Notification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = "GENERAL",
    val isRead: Boolean = false,
    val createdAt: Long = 0L,
    val taskId: String? = null,
    val teamId: String? = null,
    val inviteCode: String? = null,
)

/* ---------------- SCREEN ---------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(navController: NavController) {

    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser

    var notifications by remember { mutableStateOf<List<Notification>>(emptyList()) }
    var filter by remember { mutableStateOf("All") }
    var isLoading by remember { mutableStateOf(true) }

    var locallyReadIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val unreadCount = notifications.count { notif ->
        !notif.isRead && notif.id !in locallyReadIds
    }

    /* ---------- FIRESTORE LISTENER ---------- */
    LaunchedEffect(Unit) {
        if (currentUser == null) return@LaunchedEffect

        db.collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                notifications = snapshot.documents.map { doc ->
                    val rawTime = doc.get("createdAt")
                    val finalTime = when (rawTime) {
                        is Long -> rawTime
                        is com.google.firebase.Timestamp -> rawTime.toDate().time
                        else -> 0L
                    }
                    Notification(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        type = doc.getString("type") ?: "GENERAL",
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = finalTime,
                        taskId = doc.getString("taskId"),
                        teamId = doc.getString("teamId"),
                        inviteCode = doc.getString("inviteCode")
                    )
                }

                isLoading = false
            }
    }

    /* ---------- FILTER ---------- */
    val filteredNotifications = when (filter) {
        "Unread" -> notifications.filter { !it.isRead && it.id !in locallyReadIds }
        "Task" -> notifications.filter {
            it.type == "ASSIGNMENT" || it.type == "TASK_COMPLETED" || it.type == "DEADLINE_REMINDER"
        }
        "Team" -> notifications.filter {
            it.type == "TEAM_INVITE" || it.type == "TEAM_INVITATION" || it.type == "TEAM_JOINED"
        }
        "Comment" -> notifications.filter { it.type == "COMMENT" }
        else -> notifications
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FD),
        topBar = {
            ModernHeader(
                unreadCount = unreadCount,
                onBack = { navController.popBackStack() }
            )
        }
    ) { padding ->

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            ModernFilterRow(filter) { filter = it }

            ModernActionRow(
                notifications = notifications,
                onMarkAllAsRead = { idsToMark ->
                    locallyReadIds = locallyReadIds + idsToMark
                }
            )

            when {
                isLoading -> LoadingView()
                filteredNotifications.isEmpty() -> ModernEmptyView()
                else -> LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notif ->
                        ModernNotificationCard(
                            notification = notif,
                            navController = navController,
                            isLocallyRead = notif.id in locallyReadIds,
                            onLocalMarkAsRead = { id ->
                                locallyReadIds = locallyReadIds + id
                            }
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- MODERN HEADER ---------------- */

@Composable
fun ModernHeader(unreadCount: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF6366F1),
                        Color(0xFF8B5CF6)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Notifications",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (unreadCount > 0) {
                        Text(
                            text = "$unreadCount new",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = Color(0xFF6366F1),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- MODERN FILTER ROW ---------------- */

@Composable
fun ModernFilterRow(
    selected: String,
    onSelect: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Unread", "Task", "Team", "Comment").forEach { filter ->
                    val isSelected = selected == filter
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onSelect(filter) },
                        color = if (isSelected) Color(0xFF6366F1) else Color.White,
                        shape = RoundedCornerShape(20.dp),
                        shadowElevation = if (isSelected) 4.dp else 1.dp
                    ) {
                        Text(
                            text = filter,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            color = if (isSelected) Color.White else Color(0xFF64748B),
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}


/* ---------------- MODERN ACTION ROW ---------------- */

@Composable
fun ModernActionRow(
    notifications: List<Notification>,
    onMarkAllAsRead: (Set<String>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var showDialog by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        isProcessing = true
                        val batch = db.batch()
                        notifications.forEach { notif ->
                            val docRef = db.collection("notifications").document(notif.id)
                            batch.delete(docRef)
                        }
                        batch.commit()
                            .addOnSuccessListener {
                                isProcessing = false
                                showDialog = false
                            }
                            .addOnFailureListener {
                                isProcessing = false
                                showDialog = false
                            }
                    },
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFFEF4444)
                        )
                    } else {
                        Text("Yes", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    enabled = !isProcessing
                ) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            title = { Text("Clear All Notifications?", fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone.", color = Color(0xFF64748B)) },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable {
                    val unreadNotifs = notifications.filter { !it.isRead }
                    if (unreadNotifs.isNotEmpty()) {
                        val idsToMark = unreadNotifs.map { it.id }.toSet()
                        onMarkAllAsRead(idsToMark)

                        val batch = db.batch()
                        unreadNotifs.forEach { notif ->
                            val docRef = db.collection("notifications").document(notif.id)
                            batch.update(docRef, "isRead", true)
                        }
                        batch.commit()
                            .addOnFailureListener { e ->
                                println("Error marking as read: ${e.message}")
                            }
                    }
                },
            color = Color.White,
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Mark all read",
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { showDialog = true },
            color = Color(0xFFFEE2E2),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Delete,
                    null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Clear all",
                    color = Color(0xFFEF4444),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/* ---------------- MODERN CARD ---------------- */

@Composable
fun ModernNotificationCard(
    notification: Notification,
    navController: NavController,
    isLocallyRead: Boolean,
    onLocalMarkAsRead: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    var showConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    isDeleting = true
                    db.collection("notifications")
                        .document(notification.id)
                        .delete()
                        .addOnSuccessListener { showConfirm = false }
                        .addOnFailureListener {
                            isDeleting = false
                            showConfirm = false
                        }
                }) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel", color = Color(0xFF64748B))
                }
            },
            title = { Text("Delete Notification?", fontWeight = FontWeight.Bold) },
            text = { Text("This action cannot be undone.", color = Color(0xFF64748B)) },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }

    val effectiveIsRead = notification.isRead || isLocallyRead
    val backgroundColor by animateColorAsState(
        targetValue = if (!effectiveIsRead) Color.White else Color(0xFFF8F9FD),
        animationSpec = tween(durationMillis = 300),
        label = "background_animation"
    )

    val (icon, iconColor, iconBg) = when (notification.type) {
        "TEAM_INVITE", "TEAM_INVITATION" -> Triple(
            Icons.Default.GroupAdd,
            Color(0xFF6366F1),
            Color(0xFFEEF2FF)
        )
        "COMMENT" -> Triple(Icons.Default.Comment, Color(0xFF8B5CF6), Color(0xFFF3E8FF))
        "ASSIGNMENT" -> Triple(Icons.Default.Assignment, Color(0xFF3B82F6), Color(0xFFDBEAFE))
        "DEADLINE_REMINDER" -> Triple(Icons.Default.Alarm, Color(0xFFF59E0B), Color(0xFFFEF3C7))
        "TASK_COMPLETED" -> Triple(Icons.Default.CheckCircle, Color(0xFF10B981), Color(0xFFD1FAE5))
        else -> Triple(Icons.Default.Notifications, Color(0xFF6366F1), Color(0xFFEEF2FF))
    }

    val formattedDate = remember(notification.createdAt) {
        val date = Date(notification.createdAt)
        android.text.format.DateFormat.format("dd MMM yyyy, HH:mm", date).toString()
    }

    fun acceptInvite() {
        if (notification.teamId == null) {
            android.util.Log.e("ACCEPT_INVITE", "Team ID is null, cannot join.")
            return
        }

        isProcessing = true
        val userId = auth.currentUser?.uid ?: return
        val teamRef = db.collection("teams").document(notification.teamId)

        // 1. Check if user is ALREADY in the team
        teamRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                val currentMembers = document.get("members") as? List<String> ?: emptyList()

                if (currentMembers.contains(userId)) {
                    // User is ALREADY a member -> Just delete the notification and navigate
                    android.util.Log.d("ACCEPT_INVITE", "User already in team. Deleting notification.")
                    db.collection("notifications").document(notification.id).delete()

                    navController.navigate("team_detail/${notification.teamId}") {
                        launchSingleTop = true
                    }
                    isProcessing = false
                } else {
                    // User is NOT a member -> Add them
                    teamRef.update("members", com.google.firebase.firestore.FieldValue.arrayUnion(userId))
                        .addOnSuccessListener {
                            // Delete notification after joining
                            db.collection("notifications").document(notification.id).delete()

                            // Navigate
                            navController.navigate("team_detail/${notification.teamId}") {
                                launchSingleTop = true
                            }
                            isProcessing = false
                        }
                        .addOnFailureListener { e ->
                            android.util.Log.e("ACCEPT_INVITE", "Error joining team: ${e.message}")
                            isProcessing = false
                        }
                }
            } else {
                // Team doesn't exist anymore (maybe deleted)
                android.util.Log.e("ACCEPT_INVITE", "Team no longer exists")
                db.collection("notifications").document(notification.id).delete() // Clean up dead notif
                isProcessing = false
            }
        }.addOnFailureListener {
            isProcessing = false
        }
    }

    fun declineInvite() {
        isProcessing = true
        db.collection("notifications").document(notification.id).delete()
    }

    if (!isDeleting) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = if (!effectiveIsRead) 3.dp else 1.dp,
                    shape = RoundedCornerShape(16.dp)
                )
                .animateContentSize()
                .clickable {
                    // 1. Mark as read locally
                    if (!notification.isRead && !isLocallyRead) {
                        onLocalMarkAsRead(notification.id)
                        db.collection("notifications")
                            .document(notification.id)
                            .update("isRead", true)
                    }

                    // 2. Safe Navigation Logic
                    when (notification.type) {
                        "ASSIGNMENT", "TASK_COMPLETED", "DEADLINE_REMINDER", "COMMENT" -> {
                            val taskId = notification.taskId
                            val teamId = notification.teamId

                            // CHECK: Ensure we actually have IDs before navigating
                            if (!taskId.isNullOrBlank() && !teamId.isNullOrBlank()) {
                                navController.navigate("team_task_detail/$teamId/$taskId") {
                                    launchSingleTop = true
                                }
                            } else if (!taskId.isNullOrBlank()) {
                                // Fallback for personal tasks if you have that route
                                navController.navigate("task_detail/$taskId") {
                                    launchSingleTop = true
                                }
                            }
                        }
                        "TEAM_INVITE", "TEAM_INVITATION", "TEAM_JOINED" -> {
                            val teamId = notification.teamId
                            if (!teamId.isNullOrBlank()) {
                                navController.navigate("team_detail/$teamId") {
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(16.dp),
            color = backgroundColor
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = notification.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = notification.description,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B),
                                lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = formattedDate,
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFEE2E2))
                            .clickable { showConfirm = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                if (notification.type == "TEAM_INVITE" || notification.type == "TEAM_INVITATION") {
                    Spacer(Modifier.height(16.dp))
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 60.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { acceptInvite() },
                            enabled = !isProcessing,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            elevation = ButtonDefaults.buttonElevation(2.dp)
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Accept", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        OutlinedButton(
                            onClick = { declineInvite() },
                            enabled = !isProcessing,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF64748B)
                            )
                        ) {
                            Text("Decline", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }

                if (!effectiveIsRead) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .padding(start = 60.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF3B82F6))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "NEW",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/* ---------------- STATE VIEWS ---------------- */

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Color(0xFF6366F1))
    }
}

@Composable
fun ModernEmptyView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp),
                    tint = Color(0xFFCBD5E1)
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                text = "No notifications yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "You're all caught up!",
                color = Color(0xFF64748B),
                fontSize = 14.sp
            )
        }
    }
}