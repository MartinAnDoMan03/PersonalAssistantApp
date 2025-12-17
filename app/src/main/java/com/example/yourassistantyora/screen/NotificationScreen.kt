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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

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
    val teamId: String? = null
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

    // State untuk tracking IDs yang sudah di-mark as read secara lokal
    var locallyReadIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val unreadCount = notifications.count { notif ->
        !notif.isRead && notif.id !in locallyReadIds
    }

    /* ---------- FIRESTORE LISTENER ---------- */
    LaunchedEffect(Unit) {
        if (currentUser == null) return@LaunchedEffect

        db.collection("notifications")
            .whereEqualTo("userId", currentUser.uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                notifications = snapshot.documents.map { doc ->
                    Notification(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        type = doc.getString("type") ?: "GENERAL",
                        isRead = doc.getBoolean("isRead") ?: false,
                        createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L,
                        taskId = doc.getString("taskId"),
                        teamId = doc.getString("teamId")
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
            it.type == "TEAM_INVITATION" || it.type == "TEAM_JOINED"
        }
        "Comment" -> notifications.filter { it.type == "COMMENT" }
        else -> notifications
    }

    Scaffold(
        topBar = {
            SimpleHeader(
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
            FilterRow(filter) { filter = it }

            ActionRow(
                notifications = notifications,
                onMarkAllAsRead = { idsToMark ->
                    locallyReadIds = locallyReadIds + idsToMark
                }
            )

            when {
                isLoading -> LoadingView()
                filteredNotifications.isEmpty() -> EmptyView()
                else -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notif ->
                        NotificationCard(
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

/* ---------------- HEADER ---------------- */

@Composable
fun SimpleHeader(unreadCount: Int, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                androidx.compose.ui.graphics.Brush.horizontalGradient(
                    listOf(Color(0xFF6A5AE0), Color(0xFF836AFF))
                )
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Spacer(Modifier.width(8.dp))

            Text(
                text = "Notifications",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )

            if (unreadCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = unreadCount.toString(),
                        color = Color(0xFF6A5AE0),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/* ---------------- FILTER ROW ---------------- */

@Composable
fun FilterRow(selected: String, onSelect: (String) -> Unit) {
    Row(
        Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        listOf("All", "Unread", "Task", "Team", "Comment").forEach {
            FilterChip(
                selected = selected == it,
                onClick = { onSelect(it) },
                label = { Text(it) }
            )
        }
    }
}

/* ---------------- ACTION BAR ---------------- */

@Composable
fun ActionRow(
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
                            color = Color.Red
                        )
                    } else {
                        Text("Yes", color = Color.Red)
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false },
                    enabled = !isProcessing
                ) {
                    Text("Cancel")
                }
            },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete all notifications?") }
        )
    }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.clickable {
                val unreadNotifs = notifications.filter { !it.isRead }
                if (unreadNotifs.isNotEmpty()) {
                    // Update UI instantly
                    val idsToMark = unreadNotifs.map { it.id }.toSet()
                    onMarkAllAsRead(idsToMark)

                    // Then update Firestore in background
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
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.CheckCircle, null)
            Spacer(Modifier.width(6.dp))
            Text("Mark all as read", fontSize = 14.sp)
        }

        Row(
            Modifier.clickable { showDialog = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Delete, null, tint = Color.Red)
            Spacer(Modifier.width(6.dp))
            Text("Clear all", color = Color.Red, fontSize = 14.sp)
        }
    }
}

/* ---------------- CARD ---------------- */

@Composable
fun NotificationCard(
    notification: Notification,
    navController: NavController,
    isLocallyRead: Boolean,
    onLocalMarkAsRead: (String) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    var showConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    isDeleting = true
                    db.collection("notifications")
                        .document(notification.id)
                        .delete()
                        .addOnSuccessListener {
                            showConfirm = false
                        }
                        .addOnFailureListener {
                            isDeleting = false
                            showConfirm = false
                        }
                }) {
                    Text("Yes", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) {
                    Text("Cancel")
                }
            },
            title = { Text("Delete Notification") },
            text = { Text("Are you sure you want to delete this notification?") }
        )
    }

    // Animasi transisi untuk perubahan isRead (termasuk locally read)
    val effectiveIsRead = notification.isRead || isLocallyRead
    val backgroundColor by animateColorAsState(
        targetValue = if (!effectiveIsRead)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
        else
            Color(0xFFFDFDFD),
        animationSpec = tween(durationMillis = 300),
        label = "background_animation"
    )

    val icon = when (notification.type) {
        "COMMENT" -> Icons.Default.Comment
        "ASSIGNMENT" -> Icons.Default.Assignment
        "TEAM_INVITATION" -> Icons.Default.Group
        "DEADLINE_REMINDER" -> Icons.Default.Alarm
        "TASK_COMPLETED" -> Icons.Default.CheckCircle
        else -> Icons.Default.Notifications
    }

    val formattedDate = remember(notification.createdAt) {
        val date = Date(notification.createdAt)
        android.text.format.DateFormat.format("dd MMM yyyy, HH:mm", date).toString()
    }

    // Jangan tampilkan card jika sedang dihapus
    if (!isDeleting) {
        Card(
            Modifier
                .fillMaxWidth()
                .shadow(1.dp, RoundedCornerShape(12.dp))
                .animateContentSize()
                .clickable {
                    // Tandai dibaca secara lokal dulu untuk instant feedback
                    if (!notification.isRead && !isLocallyRead) {
                        onLocalMarkAsRead(notification.id)

                        // Kemudian update di Firestore
                        db.collection("notifications")
                            .document(notification.id)
                            .update("isRead", true)
                            .addOnFailureListener { e ->
                                println("Error updating notification: ${e.message}")
                            }
                    }

                    // Navigasi berdasarkan tipe notifikasi
                    when (notification.type) {
                        "ASSIGNMENT", "TASK_COMPLETED", "DEADLINE_REMINDER" -> {
                            notification.taskId?.let { taskId ->
                                if (taskId.isNotBlank()) {
                                    navController.navigate("team_task_detail/$taskId") {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }

                        "COMMENT" -> {
                            notification.taskId?.let { taskId ->
                                if (taskId.isNotBlank()) {
                                    navController.navigate("team_task_detail/$taskId") {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }

                        "TEAM_INVITATION", "TEAM_JOINED" -> {
                            notification.teamId?.let { teamId ->
                                if (teamId.isNotBlank()) {
                                    navController.navigate("team_detail/$teamId") {
                                        launchSingleTop = true
                                    }
                                }
                            }
                        }
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Row(
                Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = when (notification.type) {
                            "DEADLINE_REMINDER" -> Color(0xFFFF6B6B)
                            "TASK_COMPLETED" -> Color(0xFF51CF66)
                            else -> Color(0xFF5C6BC0)
                        },
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(notification.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(notification.description, fontSize = 13.sp, color = Color.DarkGray)
                        Spacer(Modifier.height(6.dp))
                        Text(formattedDate, fontSize = 11.sp, color = Color.Gray)
                    }
                }

                IconButton(
                    onClick = { showConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

/* ---------------- STATE VIEWS ---------------- */

@Composable
fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Text("No notifications", color = Color.Gray)
        }
    }
}