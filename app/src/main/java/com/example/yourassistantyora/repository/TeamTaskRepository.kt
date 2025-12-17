package com.example.yourassistantyora.data.repository

import android.util.Log
import com.example.yourassistantyora.models.TeamTaskDb
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class TeamTaskRepository {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    fun createNewTeamTask(taskData: TeamTaskDb) {
        val currentUser = auth.currentUser ?: return

        db.collection("team_tasks")
            .add(taskData)
            .addOnSuccessListener { docRef ->
                Log.d("TeamTaskRepository", "Tugas berhasil dibuat: ${docRef.id}")
            }
            .addOnFailureListener {
                Log.e("TeamTaskRepository", it.message ?: "Gagal menambahkan tugas")
            }
    }
}
