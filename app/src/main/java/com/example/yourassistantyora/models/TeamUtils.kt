package com.example.yourassistantyora.models

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object TeamUtils {

    private val db = FirebaseFirestore.getInstance()

    /**
     * Sinkronisasi semua task di tim tertentu agar field `uid`
     * sama dengan daftar `members` di dokumen `teams`.
     */
    suspend fun syncTeamTasksWithMembers(teamId: String) {
        try {
            // Ambil daftar member dari dokumen tim
            val teamDoc = db.collection("teams").document(teamId).get().await()
            val members = teamDoc.get("members") as? List<String> ?: emptyList()

            // Ambil semua task dengan team_id ini
            val tasksSnapshot = db.collection("team_tasks")
                .whereEqualTo("team_id", teamId)
                .get()
                .await()

            // Update setiap task agar memiliki uid = members
            for (task in tasksSnapshot.documents) {
                db.collection("team_tasks").document(task.id)
                    .update("uid", members)
                    .await()
            }

            println("✅ Sinkronisasi berhasil untuk tim: $teamId")
        } catch (e: Exception) {
            println("❌ Gagal sinkronisasi: ${e.message}")
        }
    }
}
