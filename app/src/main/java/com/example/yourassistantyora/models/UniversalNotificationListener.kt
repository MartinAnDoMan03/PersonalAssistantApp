package com.example.yourassistantyora.models

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import java.util.*
import kotlin.random.Random

object UniversalNotificationListener {

    private var taskListener: ListenerRegistration? = null
    private var teamListener: ListenerRegistration? = null
    private var deadlineListener: ListenerRegistration? = null
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val processedIds = mutableSetOf<String>()

    fun startListening(context: Context) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        stopListening()

        listenForNewTeamTasks(userId, context)
        listenForTeamInvites(userId, context)
        listenForUpcomingDeadlines(userId, context) // Real-time deadline listener
        listenForNewComments(userId, context)
        listenForManualInvites(userId, context)
    }

    fun stopListening() {
        taskListener?.remove()
        teamListener?.remove()
        deadlineListener?.remove()
        processedIds.clear()
    }

    /** ✅ 1️⃣ Listener tugas baru */
    private fun listenForNewTeamTasks(userId: String, context: Context) {
        taskListener = db.collection("team_tasks")
            .whereArrayContains("uid", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (dc in snapshots.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val data = dc.document.data
                        val taskId = dc.document.id
                        val createdBy = data["createdBy"] as? String ?: ""
                        val title = data["title"] as? String ?: "Tugas Baru"
                        val notifiedUsers = data["notifiedUsers"] as? List<*> ?: emptyList<Any>()

                        val alreadyNotified = userId in notifiedUsers
                        val alreadyProcessed = processedIds.contains("task_$taskId")

                        if (!alreadyProcessed && !alreadyNotified && createdBy != userId) {
                            processedIds.add("task_$taskId")

                            sendNotificationToFirestore(
                                targetUserId = userId,
                                title = "Tugas Baru Ditugaskan",
                                description = "Anda ditugaskan pada tugas: $title",
                                types = listOf("ASSIGNMENT"),
                                taskId = taskId,
                                taskTitle = title,
                                teamId = data["team_id"] as? String
                            )

                            db.collection("team_tasks")
                                .document(taskId)
                                .update("notifiedUsers", FieldValue.arrayUnion(userId))

                            showLocalNotification(context, "Tugas Baru", "Anda ditugaskan pada $title")
                        }
                    }
                }
            }
    }

    /** ✅ 2️⃣ Listener undangan tim baru */
    private fun listenForTeamInvites(userId: String, context: Context) {
        teamListener = db.collection("teams")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (dc in snapshots.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val data = dc.document.data
                        val teamId = dc.document.id
                        val createdBy = data["createdBy"] as? String ?: ""
                        val name = data["name"] as? String ?: "Tim Baru"
                        val notifiedMembers = data["notifiedMembers"] as? List<*> ?: emptyList<Any>()

                        val alreadyNotified = userId in notifiedMembers
                        val alreadyProcessed = processedIds.contains("team_$teamId")

                        if (!alreadyProcessed && !alreadyNotified && createdBy != userId) {
                            processedIds.add("team_$teamId")

                            sendNotificationToFirestore(
                                targetUserId = userId,
                                title = "Undangan Tim Baru",
                                description = "Anda telah ditambahkan ke tim $name",
                                types = listOf("TEAM_INVITATION"),
                                teamId = teamId
                            )

                            db.collection("teams")
                                .document(teamId)
                                .update("notifiedMembers", FieldValue.arrayUnion(userId))

                            showLocalNotification(context, "Tim Baru", "Anda ditambahkan ke tim $name")
                        }
                    }
                }
            }
    }

    /** ✅ 3️⃣ Real-time listener untuk deadline dalam 24 jam */
    private fun listenForUpcomingDeadlines(userId: String, context: Context) {
        val now = Timestamp.now()
        val next24h = Timestamp(Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))

        deadlineListener = db.collection("team_tasks")
            .whereArrayContains("uid", userId)
            .whereGreaterThan("deadline", now)
            .whereLessThanOrEqualTo("deadline", next24h)
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (doc in snapshots.documents) {
                    val title = doc.getString("title") ?: "Tugas"
                    val taskId = doc.id
                    val deadline = doc.getTimestamp("deadline")?.toDate()
                    val notifiedUsers = doc.get("notifiedDeadlineUsers") as? List<*> ?: emptyList<Any>()

                    val alreadyNotified = userId in notifiedUsers
                    val key = "deadline_$taskId"
                    val alreadyProcessed = processedIds.contains(key)

                    // Hitung waktu tersisa
                    val hoursLeft = if (deadline != null) {
                        ((deadline.time - System.currentTimeMillis()) / (1000 * 60 * 60)).toInt()
                    } else 0

                    if (!alreadyNotified && !alreadyProcessed && hoursLeft in 0..24) {
                        processedIds.add(key)

                        val timeMessage = when {
                            hoursLeft <= 1 -> "kurang dari 1 jam lagi"
                            hoursLeft <= 6 -> "$hoursLeft jam lagi"
                            else -> "dalam 24 jam"
                        }

                        sendNotificationToFirestore(
                            targetUserId = userId,
                            title = "⏰ Deadline Mendekat!",
                            description = "Tugas \"$title\" akan jatuh tempo $timeMessage",
                            types = listOf("DEADLINE_REMINDER"),
                            taskId = taskId,
                            taskTitle = title,
                            teamId = doc.getString("team_id")
                        )

                        db.collection("team_tasks")
                            .document(taskId)
                            .update("notifiedDeadlineUsers", FieldValue.arrayUnion(userId))

                        showLocalNotification(
                            context,
                            "⏰ Deadline Mendekat!",
                            "Tugas \"$title\" akan jatuh tempo $timeMessage"
                        )
                    }
                }
            }
    }

    /** ✅ 4️⃣ Listener komentar baru */
    private fun listenForNewComments(userId: String, context: Context) {
        db.collection("comments")
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (dc in snapshots.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val data = dc.document.data
                        val commentId = dc.document.id
                        val commenterId = data["createdBy"] as? String ?: ""
                        val taskId = data["ctask_id"] as? String ?: ""
                        val commenterName = data["userName"] as? String ?: "Seseorang"
                        val content = data["content"] as? String ?: ""

                        if (commenterId == userId) continue

                        val key = "comment_${commentId}_$userId"
                        if (processedIds.contains(key)) continue
                        processedIds.add(key)

                        db.collection("team_tasks").document(taskId)
                            .get()
                            .addOnSuccessListener { taskDoc ->
                                if (!taskDoc.exists()) return@addOnSuccessListener

                                val title = taskDoc.getString("title") ?: "Tugas"
                                val teamId = taskDoc.getString("team_id") ?: ""

                                if (teamId.isBlank()) return@addOnSuccessListener

                                db.collection("teams").document(teamId)
                                    .get()
                                    .addOnSuccessListener { teamDoc ->
                                        if (!teamDoc.exists()) return@addOnSuccessListener

                                        val members = teamDoc.get("members") as? List<*> ?: emptyList<Any>()

                                        for (memberId in members) {
                                            if (memberId == commenterId) continue
                                            val member = memberId as String

                                            val sentRef = db.collection("comments")
                                                .document(commentId)
                                                .collection("sent_notifications")
                                                .document(member)

                                            sentRef.get().addOnSuccessListener { sentDoc ->
                                                if (!sentDoc.exists()) {
                                                    sendNotificationToFirestore(
                                                        targetUserId = member,
                                                        title = "💬 Komentar Baru di \"$title\"",
                                                        description = "$commenterName: \"${content.take(100)}${if (content.length > 100) "..." else ""}\"",
                                                        types = listOf("COMMENT"),
                                                        taskId = taskId,
                                                        taskTitle = title,
                                                        teamId = teamId
                                                    )

                                                    sentRef.set(mapOf("sentAt" to Timestamp.now()))

                                                    if (userId == member) {
                                                        showLocalNotification(
                                                            context,
                                                            "💬 Komentar Baru",
                                                            "$commenterName berkomentar di \"$title\""
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                            }
                    }
                }
            }
    }

    private fun listenForManualInvites(userId: String, context: Context) {
        db.collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("type", "TEAM_INVITE") // Listen specifically for manual invites
            .whereEqualTo("isRead", false) // Only unread ones
            .addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) return@addSnapshotListener

                for (dc in snapshots.documentChanges) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val data = dc.document.data
                        val notifId = dc.document.id

                        // Prevent duplicate alerts for the same ID
                        val key = "manual_invite_$notifId"
                        if (processedIds.contains(key)) continue
                        processedIds.add(key)

                        val title = data["title"] as? String ?: "Team Invitation"
                        val description = data["description"] as? String ?: "You have been invited to a team."
                        val relatedUserName = data["relatedUserName"] as? String ?: "Someone"

                        // Show the local Android notification
                        showLocalNotification(
                            context,
                            "New Invite from $relatedUserName",
                            description
                        )
                    }
                }
            }
    }

    /** Simpan notifikasi ke Firestore */
    private fun sendNotificationToFirestore(
        targetUserId: String,
        title: String,
        description: String,
        types: List<String>,
        taskId: String? = null,
        taskTitle: String? = null,
        teamId: String? = null
    ) {
        val currentUser = auth.currentUser ?: return

        val notifData = hashMapOf(
            "userId" to targetUserId,
            "title" to title,
            "description" to description,
            "type" to types.first(),
            "isRead" to false,
            "createdAt" to Timestamp.now(),
            "relatedUserId" to currentUser.uid,
            "relatedUserName" to (currentUser.displayName ?: "System"),
            "taskId" to (taskId ?: ""),
            "taskTitle" to (taskTitle ?: ""),
            "teamId" to (teamId ?: "")
        )

        db.collection("notifications").add(notifData)
    }

    /** 🔔 Tampilkan notifikasi lokal */
    private fun showLocalNotification(context: Context, title: String, message: String) {
        val channelId = "yora_notifications"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(Random.nextInt(), notification)
    }
}