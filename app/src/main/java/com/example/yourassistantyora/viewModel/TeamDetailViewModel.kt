package com.example.yourassistantyora.viewModel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.map
//import androidx.lifecycle.viewModelScope
//// ✅ HAPUS: import androidx.lifecycle.map (tidak digunakan)
//import com.example.yourassistantyora.models.* // ✅ PASTIKAN MENGGUNAKAN MODEL DARI SINI
//import com.google.firebase.auth.FirebaseAuth
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.ListenerRegistration
//import com.google.firebase.firestore.toObject
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.asStateFlow
//import kotlinx.coroutines.flow.combine
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.tasks.await
//
//class TeamDetailViewModel : ViewModel() {
//    private val db = FirebaseFirestore.getInstance()
//    private val auth = FirebaseAuth.getInstance()
//    private var tasksListener: ListenerRegistration? = null
//
//    // --- State Internal ---
//    private val _teamDetail = MutableStateFlow<TeamDetail?>(null)
//    private val _isLoading = MutableStateFlow(true)
//    private val _error = MutableStateFlow<String?>(null)
//
//    // --- State yang akan Digunakan oleh UI ---
//    val teamDetail = _teamDetail.asStateFlow()
//    val isLoading = _isLoading.asStateFlow()
//    val error = _error.asStateFlow()
//
//    // ✅ PERBAIKAN: Ambil langsung dari teamDetail.members yang sudah ada
//    val teamMembers = teamDetail.combine(MutableStateFlow<List<TeamMember>>(emptyList())) { detail, _ ->
//        detail?.members ?: emptyList()
//    }
//
//    // State untuk filter di UI
//    val selectedFilter = MutableStateFlow<TaskStatus?>(null) // Gunakan Enum langsung, null untuk "All"
//
//    // Flow untuk mengambil tasks dari teamDetail dan memfilternya
//    val filteredTasks = combine(teamDetail, selectedFilter) { detail, filter ->
//        val allTasks = detail?.tasks ?: emptyList()
//        if (filter == null) {
//            allTasks
//        } else {
//            // ✅ PERBAIKAN: Membandingkan enum dengan enum sudah benar
//            allTasks.filter { it.status == filter }
//        }
//    }
//
//    /**
//     * Memuat semua data yang dibutuhkan untuk TeamDetailScreen secara real-time.
//     */
//    fun loadTeamDetails(teamId: String) {
//        if (teamId.isBlank()) {
//            _error.value = "Invalid Team ID"
//            return
//        }
//
//        val currentUserId = auth.currentUser?.uid ?: run {
//            _error.value = "User not authenticated."
//            return
//        }
//
//        _isLoading.value = true
//        clearListeners()
//
//        viewModelScope.launch {
//            try {
//                // 1. Ambil data dasar Tim dari Firestore
//                val teamDoc = db.collection("teams").document(teamId).get().await()
//                // ✅ PERBAIKAN: Gunakan nama data class yang benar 'Team'
//                val teamData = teamDoc.toObject<com.example.yourassistantyora.models.Team>()
//
//                if (teamData == null) {
//                    throw IllegalStateException("Team with ID $teamId not found.")
//                }
//
//                // 2. Ambil data profil semua anggota tim
//                // ✅ PERBAIKAN: Gunakan properti yang benar dari 'Team' ('members' adalah List<String>)
//                val memberProfiles = teamData.members.map { memberId ->
//                    val userDoc = db.collection("users").document(memberId).get().await()
//                    // ✅ PERBAIKAN: 'createdBy' ada di 'teamData'
//                    val role = if (teamData.createdBy == memberId) "Admin" else "Member"
//                    TeamMember(
//                        id = memberId,
//                        name = userDoc.getString("username") ?: "Unknown Member",
//                        role = role
//                    )
//                }.sortedBy { it.name }
//
//                // 3. Pasang listener untuk tugas (tasks)
//                tasksListener = db.collection("team_tasks")
//                    .whereEqualTo("team_id", teamId)
//                    .addSnapshotListener { snapshot, e ->
//                        if (e != null) {
//                            _error.value = "Failed to listen for task updates: ${e.message}"
//                            return@addSnapshotListener
//                        }
//                        if (snapshot != null) {
//                            // ✅ PERBAIKAN: Gunakan nama data class yang benar 'TeamTaskDb'
//                            val teamTasksFromDb = snapshot.toObjects(TeamTaskDb::class.java).map { taskDb ->
//                                taskDb.apply { ttask_id = snapshot.documents.find { it.id == taskDb.ttask_id }?.id ?: taskDb.ttask_id }
//                            }
//
//                            // 4. Ubah Task dari DB menjadi Task untuk UI
//                            val uiTasks = teamTasksFromDb.map { it.toUITask(memberProfiles) }
//
//                            // 5. Bangun objek TeamDetail yang lengkap
//                            val fullTeamDetail = TeamDetail(
//                                id = teamId,
//                                name = teamData.name,
//                                description = teamData.description,
//                                category = teamData.category,
//                                // ✅ PERBAIKAN: Gunakan helper 'colorSchemeEnum' dari 'Team'
//                                colorScheme = teamData.colorSchemeEnum,
//                                members = memberProfiles,
//                                tasks = uiTasks.sortedBy { it.deadline },
//                                currentUserRole = if (teamData.createdBy == currentUserId) "Admin" else "Member",
//                                currentUserId = currentUserId,
//                                // ✅ PERBAIKAN: Gunakan 'inviteCode'
//                                inviteCode = teamData.inviteCode
//                            )
//                            _teamDetail.value = fullTeamDetail
//                        }
//                        _isLoading.value = false
//                    }
//            } catch (e: Exception) {
//                _error.value = "Failed to load team data: ${e.message}"
//                _isLoading.value = false
//            }
//        }
//    }
//
//    /**
//     * Helper function untuk mengubah TeamTaskDb (dari Firestore) menjadi TeamTask (untuk UI).
//     */
//    // ✅ PERBAIKAN: Helper ini sekarang menerima 'TeamTaskDb'
//    private fun TeamTaskDb.toUITask(allMembers: List<TeamMember>): com.example.yourassistantyora.models.TeamTask {
//        val assignedMembers = allMembers.filter { member -> this.uid.contains(member.id) }
//        return com.example.yourassistantyora.models.TeamTask(
//            id = this.ttask_id,
//            title = this.title,
//            // ✅ PERBAIKAN: Gunakan properti yang benar ('desc')
//            description = this.desc,
//            status = TaskStatus.fromInt(this.status),
//            priority = TaskPriority.fromInt(this.priority),
//            assignedTo = assignedMembers,
//            deadline = this.deadline.toDate().toString(),
//            createdBy = this.createdBy,
//            createdAt = this.createdOn.toDate().toString()
//        )
//    }
//
//    fun setFilter(status: TaskStatus?) {
//        selectedFilter.value = status
//    }
//
//    private fun clearListeners() {
//        tasksListener?.remove()
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        clearListeners()
//    }
//}
