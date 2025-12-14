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

class FirebaseTaskRepository : TaskRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private fun col() = db.collection("tasks")

    private fun docToTask(docId: String, data: Map<String, Any?>): TaskModel {
        val catNames = (data["CategoryNames"] as? List<*>)?.filterIsInstance<String>().orEmpty()
        val catCodes = (data["Categories"] as? List<*>)?.mapNotNull { (it as? Number)?.toInt() }.orEmpty()

        return TaskModel(
            id = docId,
            Title = data["Title"] as? String ?: "",
            Description = data["Description"] as? String ?: "",
            Deadline = data["Deadline"] as? Timestamp,
            Priority = (data["Priority"] as? Number)?.toInt() ?: 1,
            Category = (data["Category"] as? Number)?.toInt() ?: 0, // legacy single
            Categories = catCodes,                                   // legacy multi (int codes)
            CategoryNames = catNames,                                // ✅ NEW multi (string labels)
            Status = (data["Status"] as? Number)?.toInt() ?: 1,
            Reminder = (data["Reminder"] as? Number)?.toInt() ?: 0,
            Location = data["Location"] as? String ?: "",
            userId = (data["userId"] as? String) ?: (data["UIDusers"] as? String) ?: "",
            createdAt = data["createdAt"] as? Timestamp
        )
    }

    override fun observeMyTasks(): Flow<List<TaskModel>> = callbackFlow {
        val uid = auth.currentUser?.uid.orEmpty()
        if (uid.isBlank()) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        // Support 2 field names (biar aman sama data lama)
        var listA: List<TaskModel> = emptyList() // userId
        var listB: List<TaskModel> = emptyList() // UIDusers

        fun emitCombined() {
            val merged = (listA + listB)
                .distinctBy { it.id }
                .sortedWith(compareByDescending<TaskModel> { it.createdAt?.seconds ?: 0L }
                    .thenByDescending { it.Deadline?.seconds ?: 0L })
            trySend(merged)
        }

        var regA: ListenerRegistration? = null
        var regB: ListenerRegistration? = null

        regA = col()
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

        regB = col()
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

        awaitClose {
            regA?.remove()
            regB?.remove()
        }
    }

    override suspend fun addTask(task: TaskModel) {
        val uid = auth.currentUser?.uid.orEmpty()
        require(uid.isNotBlank()) { "User not logged in" }

        val payload = hashMapOf(
            "Title" to task.Title,
            "Description" to task.Description,
            "Deadline" to task.Deadline,
            "Priority" to task.Priority,
            "Category" to task.Category,                 // legacy single
            "Categories" to task.Categories,             // legacy multi int
            "CategoryNames" to task.CategoryNames,       // ✅ NEW multi string (buat Work +1)
            "Status" to task.Status,
            "Reminder" to task.Reminder,
            "Location" to task.Location,
            "userId" to uid,                             // ✅ field baru
            "UIDusers" to uid,                           // ✅ support field lama
            "createdAt" to (task.createdAt ?: Timestamp.now())
        )

        col().add(payload).await()
    }

    override suspend fun updateStatus(taskId: String, status: Int) {
        col().document(taskId)
            .update(mapOf("Status" to status))
            .await()
    }

    override suspend fun deleteTask(taskId: String) {
        col().document(taskId).delete().await()
    }
}
