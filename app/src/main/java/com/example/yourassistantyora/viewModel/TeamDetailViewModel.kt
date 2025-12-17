package com.example.yourassistantyora.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourassistantyora.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class TeamDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var tasksListener: ListenerRegistration? = null

    // --- State Internal ---
    private val _teamDetail = MutableStateFlow<TeamDetail?>(null)
    private val _isLoading = MutableStateFlow(true)
    private val _error = MutableStateFlow<String?>(null)

    // --- State Eksternal ---
    val teamDetail = _teamDetail.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val error = _error.asStateFlow()

    fun loadTeamDetails(teamId: String) {
        if (teamId.isBlank()) {
            _error.value = "Invalid Team ID"
            _isLoading.value = false
            return
        }

        val currentUserId = auth.currentUser?.uid ?: run {
            _error.value = "User not authenticated."
            _isLoading.value = false
            return
        }

        _isLoading.value = true
        clearListeners()

        viewModelScope.launch {
            try {
                // 1. Ambil data dasar Tim dari Firestore
                val teamDoc = db.collection("teams").document(teamId).get().await()
                val teamData = teamDoc.toObject(Team::class.java)

                if (teamData == null) {
                    throw IllegalStateException("Team with ID $teamId not found.")
                }

                // 2. Pasang listener untuk tugas (tasks)
                tasksListener = db.collection("team_tasks")
                    .whereEqualTo("team_id", teamId)
                    .addSnapshotListener { snapshot, e ->
                        if (e != null) {
                            _error.value = "Failed to listen for task updates: ${e.message}"
                            _teamDetail.value = _teamDetail.value?.copy(tasks = emptyList()) // Kosongkan task jika error
                            return@addSnapshotListener
                        }
                        if (snapshot != null) {
                            // Setiap kali ada perubahan pada tugas, proses ulang semuanya.
                            viewModelScope.launch { // Gunakan coroutine baru di dalam listener
                                try {
                                    val teamTasksFromDb = snapshot.toObjects(TeamTaskDb::class.java)

                                    //  3. AMBIL PROFIL MEMBER DAN HITUNG TUGAS AKTIF DI DALAM LISTENER
                                    val memberProfilesWithTaskCount = teamData.members.map { memberId ->
                                        val userDoc = db.collection("users").document(memberId).get().await()
                                        val role = if (teamData.createdBy == memberId) "Admin" else "Member"

                                        // Hitung tugas aktif (status != 2)
                                        val activeTaskCount = teamTasksFromDb.count { taskDb ->
                                            taskDb.status != 2 && taskDb.uid.contains(memberId)
                                        }
                                        // ✅ Hitung tugas selesai (status == 2)
                                        val completedTaskCount = teamTasksFromDb.count { taskDb ->
                                            taskDb.status == 2 && taskDb.uid.contains(memberId)
                                        }

                                        TeamMember(
                                            id = memberId,
                                            name = userDoc.getString("username") ?: "Unknown Member",
                                            role = role,
                                            activeTasks = activeTaskCount,
                                            tasksCompleted = completedTaskCount
                                        )
                                    }.sortedBy { it.name }

                                    // 4. Konversi tugas DB ke tugas UI menggunakan profil member yang baru
                                    val uiTasks = teamTasksFromDb.map { it.toUITask(memberProfilesWithTaskCount) }

                                    val sortedUiTasks = uiTasks.sortedWith(
                                        // 1. Prioritaskan "Your Task" (tugas yang di-assign ke user saat ini).
                                        // `true` (1) akan di atas `false` (0).
                                        compareByDescending<TeamTask> { task ->
                                            task.assignedTo.any { member -> member.id == currentUserId }
                                        }
                                            // 2. Lalu, urutkan berdasarkan Status.
                                            // NOT_STARTED(0) < IN_PROGRESS(1) < DONE(2). `compareBy` mengurutkan dari terkecil.
                                            .thenBy { it.status.ordinal }
                                            // 3. Lalu, urutkan berdasarkan Prioritas.
                                            // HIGH(2) > MEDIUM(1) > LOW(0). `thenByDescending` mengurutkan dari terbesar.
                                            .thenByDescending { it.priority.ordinal }
                                            // 4. Terakhir, jika semua sama, urutkan berdasarkan deadline.
                                            .thenBy { it.deadline }
                                    )
                                    // 5. Bangun atau perbarui objek TeamDetail yang lengkap
                                    val currentDetail = _teamDetail.value
                                    if (currentDetail == null) {
                                        // Buat objek baru jika ini pertama kalinya
                                        _teamDetail.value = TeamDetail(
                                            id = teamId,
                                            name = teamData.name,
                                            description = teamData.description,
                                            category = teamData.category,
                                            colorScheme = teamData.colorSchemeEnum,
                                            members = memberProfilesWithTaskCount,
                                            tasks = sortedUiTasks,
                                            currentUserRole = if (teamData.createdBy == currentUserId) "Admin" else "Member",
                                            currentUserId = currentUserId,
                                            inviteCode = teamData.inviteCode
                                        )
                                    } else {
                                        _teamDetail.value = currentDetail.copy(
                                            tasks = sortedUiTasks,
                                            members = memberProfilesWithTaskCount
                                        )
                                    }
                                } catch (ex: Exception) {
                                    _error.value = "Failed to process task update: ${ex.message}"
                                } finally {
                                    _isLoading.value = false
                                }
                            }
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Failed to load team data: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun TeamTaskDb.toUITask(allMembers: List<TeamMember>): TeamTask {
        val assignedMembers = allMembers.filter { member -> this.uid.contains(member.id) }
        return TeamTask(
            id = this.ttask_id,
            title = this.title,
            description = this.desc,
            status = TaskStatus.fromInt(this.status),
            priority = TaskPriority.fromInt(this.priority),
            assignedTo = assignedMembers,
            deadline = this.deadline.toDate().toString(),
            createdBy = this.createdBy,
            createdAt = this.createdOn.toDate().toString()
        )
    }

    private fun clearListeners() {
        tasksListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }
}
