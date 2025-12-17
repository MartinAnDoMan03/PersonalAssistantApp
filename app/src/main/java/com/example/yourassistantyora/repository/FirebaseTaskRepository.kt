package com.example.yourassistantyora.repository

import com.example.yourassistantyora.models.TaskModel
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class FirebaseTaskRepository : TaskRepository {
    private val teamNameCache = mutableMapOf<String, String>()
    private suspend fun getTeamName(teamId: String): String {
        teamNameCache[teamId]?.let { return it }

        val snap = db.collection("teams")
            .document(teamId)
            .get()
            .await()

        val name = snap.getString("name") ?: "Team"
        teamNameCache[teamId] = name
        return name
    }
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun myTasksCol() = db.collection("tasks")
    private fun teamTasksCol() = db.collection("team_tasks")

    // ====== mapper: tasks (personal) ======
    private fun docToTask(docId: String, data: Map<String, Any?>): TaskModel {
        val catNames = (data["CategoryNames"] as? List<*>)?.filterIsInstance<String>().orEmpty()
        val catCodes = (data["Categories"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }.orEmpty()

        return TaskModel(
            id = docId,
            Title = data["Title"] as? String ?: "",
            Description = data["Description"] as? String ?: "",
            Deadline = data["Deadline"] as? Timestamp,
            Priority = (data["Priority"] as? Number)?.toInt() ?: 1,
            Category = (data["Category"] as? Number)?.toInt() ?: 0,
            Categories = catCodes,
            CategoryNames = catNames,
            Status = (data["Status"] as? Number)?.toInt() ?: 1,
            Reminder = (data["Reminder"] as? Number)?.toInt() ?: 0,
            Location = data["Location"] as? String ?: "",
            userId = (data["userId"] as? String) ?: (data["UIDusers"] as? String) ?: "",
            createdAt = data["createdAt"] as? Timestamp
        )
    }

    // ====== mapper: team_tasks ======
    // Field team_tasks kamu: title, desc, deadline, priority, status, team_id, ttask_id, uid, docs
    private suspend fun docToTeamTask(
        docId: String,
        data: Map<String, Any?>
    ): TaskModel {
        val teamId = data["team_id"] as? String
        val teamName = teamId?.let { getTeamName(it) } ?: "Team"

        return TaskModel(
            id = "team_${teamId}_$docId",
            Title = data["title"] as? String ?: "",
            Description = data["desc"] as? String ?: "",
            Deadline = data["deadline"] as? Timestamp,
            Priority = (data["priority"] as? Number)?.toInt() ?: 1,
            Category = 0,
            Categories = emptyList(),
            CategoryNames = listOf("Team"),
            Status = (data["status"] as? Number)?.toInt() ?: 0,
            Reminder = 0,
            Location = teamName,   // 🔥 INI YANG DIPAKAI UI
            userId = "TEAM",
            createdAt = Timestamp.now()
        )
    }


    override fun observeMyTasks(): Flow<List<TaskModel>> = callbackFlow {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        var listA: List<TaskModel> = emptyList()     // tasks.userId == uid
        var listB: List<TaskModel> = emptyList()     // tasks.UIDusers == uid
        var listTeam: List<TaskModel> = emptyList()  // team_tasks.uid array contains uid

        fun emitCombined() {
            val merged = (listA + listB + listTeam)
                .distinctBy { it.id }
                .sortedWith(
                    compareByDescending<TaskModel> { it.createdAt?.seconds ?: 0L }
                        .thenByDescending { it.Deadline?.seconds ?: 0L }
                )

            trySend(merged)
        }

        var regA: ListenerRegistration? = null
        var regB: ListenerRegistration? = null
        var regTeam: ListenerRegistration? = null

        regA = myTasksCol()
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    listA = emptyList()
                    emitCombined()
                    return@addSnapshotListener
                }

                listA = snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    docToTask(doc.id, data)
                }
                emitCombined()
            }

        regB = myTasksCol()
            .whereEqualTo("UIDusers", uid)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    listB = emptyList()
                    emitCombined()
                    return@addSnapshotListener
                }

                listB = snap.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    docToTask(doc.id, data)
                }
                emitCombined()
            }

        // team tasks: assigned member array field "uid" berisi UID user
        regTeam = teamTasksCol()
            .whereArrayContains("uid", uid)
            .addSnapshotListener { snap, err ->
                if (err != null || snap == null) {
                    listTeam = emptyList()
                    emitCombined()
                    return@addSnapshotListener
                }

                launch {
                    val newList = mutableListOf<TaskModel>()

                    for (doc in snap.documents) {
                        val data = doc.data ?: continue
                        val rawId = (data["ttask_id"] as? String)?.ifBlank { null } ?: doc.id
                        val task = docToTeamTask(rawId, data)
                        newList.add(task)
                    }

                    listTeam = newList
                    emitCombined()
                }

                emitCombined()
            }

        awaitClose {
            regA?.remove()
            regB?.remove()
            regTeam?.remove()
        }
    }

    override suspend fun addTask(task: TaskModel) {
        val uid = auth.currentUser?.uid.orEmpty()
        val data = hashMapOf<String, Any?>(
            "Title" to task.Title,
            "Description" to task.Description,
            "Deadline" to task.Deadline,
            "Priority" to task.Priority,
            "Category" to task.Category,
            "Categories" to task.Categories,
            "CategoryNames" to task.CategoryNames,
            "Status" to task.Status,
            "Reminder" to task.Reminder,
            "Location" to task.Location,
            "userId" to uid,
            "createdAt" to Timestamp.now()
        )
        myTasksCol().add(data).await()
    }

    override suspend fun updateStatus(taskId: String, status: Int) {
        val isTeam = taskId.startsWith("team_")
        val realId = if (isTeam) taskId.removePrefix("team_") else taskId

        if (isTeam) {
            // team_tasks pakai field "status"
            teamTasksCol().document(realId).update("status", status).await()
        } else {
            // tasks pakai field "Status"
            myTasksCol().document(realId).update("Status", status).await()
        }
    }

    override suspend fun deleteTask(taskId: String) {
        val isTeam = taskId.startsWith("team_")
        val realId = if (isTeam) taskId.removePrefix("team_") else taskId

        if (isTeam) {
            teamTasksCol().document(realId).delete().await()
        } else {
            myTasksCol().document(realId).delete().await()
        }
    }



}
