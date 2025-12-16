package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourassistantyora.models.TeamColorScheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class TeamViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Fetch Data
    private val _teams = mutableStateOf<List<com.example.yourassistantyora.screen.Team>>(emptyList())

    private val _selectedTeam = mutableStateOf<com.example.yourassistantyora.models.TeamDetail?>(null)
    val selectedTeam: androidx.compose.runtime.State<com.example.yourassistantyora.models.TeamDetail?> = _selectedTeam

    val teams: androidx.compose.runtime.State<List<com.example.yourassistantyora.screen.Team>> = _teams

    // Loading Spinner
    var isLoading = mutableStateOf(false)

    // Error Tracker
    var errorMessage = mutableStateOf<String?>(null)
        private set

    // Success Tracker
    var isSuccess = mutableStateOf(false)
        private set

    fun resetState() {
        isSuccess.value = false
        errorMessage.value = null
        isLoading.value = false
    }

    /**
     * Fetches the teams the current user belongs to.
     */
    fun fetchTeams() {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        isLoading.value = true
        errorMessage.value = null // Reset error setiap kali fetch

        viewModelScope.launch {
            try {
                // 1. Ambil ID tim dari dokumen pengguna (Logika yang sudah ada)
                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val teamIds = userDoc.get("teams") as? List<String> ?: emptyList()

                if (teamIds.isEmpty()) {
                    _teams.value = emptyList()
                    isLoading.value = false
                    return@launch
                }

                // 2. ✅ LOGIKA BARU: Ambil semua tugas yang relevan dalam satu query
                // Kita query koleksi root 'team_tasks'
                val allTasksSnapshot = db.collection("team_tasks")
                    .whereIn("team_id", teamIds) // Filter tugas berdasarkan semua teamId
                    .get()
                    .await()

                // Buat Map untuk mengelompokkan tugas berdasarkan team_id agar efisien
                val tasksByTeamId = allTasksSnapshot.documents.groupBy { it.getString("team_id") ?: "" }

                val fetchedTeams = mutableListOf<com.example.yourassistantyora.screen.Team>()

                // 3. Loop melalui setiap ID tim untuk membangun objek UI
                for (id in teamIds) {
                    val teamDoc = db.collection("teams").document(id).get().await()

                    if (teamDoc.exists()) {
                        // ✅ LOGIKA BARU: Lakukan perhitungan di sini
                        val tasksForThisTeam = tasksByTeamId[id] ?: emptyList()
                        val totalTasks = tasksForThisTeam.size
                        // Status '2' dianggap "Done"
                        val completedTasks = tasksForThisTeam.count { (it.get("status") as? Long ?: 0) == 2L }
                        val activeTasks = totalTasks - completedTasks
                        val progress = if (totalTasks > 0) completedTasks.toFloat() / totalTasks.toFloat() else 0f

                        // --- Logika yang sudah ada di bawah ini tidak diubah, hanya diisi dengan hasil perhitungan ---
                        val colorName = teamDoc.getString("colorScheme") ?: "BLUE"
                        val colorEnum = try { TeamColorScheme.valueOf(colorName) } catch (e: Exception) { TeamColorScheme.BLUE }
                        val creatorId = teamDoc.getString("createdBy")
                        val role = if (creatorId == currentUser.uid) "Admin" else "Member"
                        val memberCount = (teamDoc.get("members") as? List<*>)?.size ?: 1

                        // Buat objek UI Team dengan data yang sudah dihitung
                        val teamObj = com.example.yourassistantyora.screen.Team(
                            id = teamDoc.id,
                            name = teamDoc.getString("name") ?: "Unknown",
                            description = teamDoc.getString("description") ?: "",
                            // Mengambil kategori pertama dari list untuk ditampilkan di UI
                            category = (teamDoc.get("categories") as? List<String>)?.firstOrNull() ?: "General",
                            colorScheme = colorEnum,
                            members = memberCount,
                            activeTasks = activeTasks,           // <-- Diisi dari hasil hitungan
                            completedTasks = completedTasks,     // <-- Diisi dari hasil hitungan
                            progress = progress,                 // <-- Diisi dari hasil hitungan
                            role = role
                        )
                        fetchedTeams.add(teamObj)
                    }
                }

                _teams.value = fetchedTeams.sortedBy { it.name }

            } catch (e: Exception) {
                errorMessage.value = "Error fetching teams: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
    fun createTeam(
        name: String,
        description: String,
        categories: List<String>,
        colorScheme: TeamColorScheme
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            errorMessage.value = "User not logged in"
            return
        }

        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val teamId = UUID.randomUUID().toString()
                val inviteCode = generateInviteCode()

                val teamData = hashMapOf(
                    "id" to teamId,
                    "name" to name,
                    "description" to description,
                    "categories" to categories,
                    "colorScheme" to colorScheme.name,
                    "createdBy" to currentUser.uid,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "inviteCode" to inviteCode,
                    "members" to listOf(currentUser.uid)
                )

                val batch = db.batch()
                val teamRef = db.collection("teams").document(teamId)
                batch.set(teamRef, teamData)

                val userRef = db.collection("users").document(currentUser.uid)
                batch.update(userRef, "teams", FieldValue.arrayUnion(teamId))

                batch.commit().await()
                isSuccess.value = true
            } catch (e: Exception) {
                errorMessage.value = "Failed to create team: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun generateInviteCode(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789"
        return (1..6)
            .map { allowedChars.random() }
            .joinToString("")
    }


    fun fetchTeamDetails(teamId: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) return

        isLoading.value = true
        _selectedTeam.value = null

        viewModelScope.launch {
            try {
                val teamDoc = db.collection("teams").document(teamId).get().await()
                if (!teamDoc.exists()) return@launch

                val name = teamDoc.getString("name") ?: "Unknown"
                val description = teamDoc.getString("description") ?: ""
                val category = (teamDoc.get("categories") as? List<String>)?.firstOrNull() ?: "General"
                val inviteCode = teamDoc.getString("inviteCode") ?: ""
                val creatorId = teamDoc.getString("createdBy") ?: ""

                val colorName = teamDoc.getString("colorScheme") ?: "BLUE"
                val colorScheme = try{ TeamColorScheme.valueOf(colorName) } catch (e: Exception) { TeamColorScheme.BLUE}

                val memberIds = teamDoc.get("members") as? List<String> ?: emptyList()
                val memberList = mutableListOf<com.example.yourassistantyora.models.TeamMember>()

                for (uid in memberIds) {
                    val userDoc = db.collection("users").document(uid).get().await()
                    val userName = userDoc.getString("fullName") ?: userDoc.getString("email") ?: "Unknown"
                    val userAvatar = userDoc.getString("photoUrl") ?: ""

                    val role = if(uid == creatorId) "Admin" else "Member"

                    memberList.add(
                        com.example.yourassistantyora.models.TeamMember(uid, userName, role, userAvatar,0)
                    )
                }

                val tasksSnapshot = db.collection("teams").document(teamId).collection("tasks").get().await()
                val taskList = tasksSnapshot.documents.map {doc ->
                    val statusStr = doc.getString("status") ?: "NOT_STARTED"
                    val priorityStr = doc.getString("priority") ?: "MEDIUM"

                    com.example.yourassistantyora.models.TeamTask(
                        id = doc.id,
                        title = doc.getString("title") ?: "No Title",
                        description = doc.getString("description") ?: "",
                        status = try { com.example.yourassistantyora.models.TaskStatus.valueOf(statusStr)} catch (e: java.lang.Exception) {com.example.yourassistantyora.models.TaskStatus.NOT_STARTED},
                        priority = try {com.example.yourassistantyora.models.TaskPriority.valueOf(priorityStr)} catch (e: Exception) {com.example.yourassistantyora.models.TaskPriority.MEDIUM},
                        assignedTo = emptyList(),
                        deadline = doc.getString("deadline") ?: "",
                        createdBy =  doc.getString("createdBy") ?: "",
                        createdAt = ""
                    )
                }

                val myRole = if (currentUser.uid == creatorId) "Admin" else "Member"

                _selectedTeam.value = com.example.yourassistantyora.models.TeamDetail(
                    id = teamId,
                    name = name,
                    description = description,
                    category = category,
                    colorScheme = colorScheme,
                    members = memberList,
                    tasks = taskList,
                    currentUserRole = myRole,
                    currentUserId = currentUser.uid,
                    inviteCode = inviteCode
                )
                }catch (e: Exception){
                errorMessage.value = "Failed to load details: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

    fun joinTeam(inviteCode: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) return
        isLoading.value = true
        errorMessage.value = null

        viewModelScope.launch {
            try {
                val query = db.collection("teams").whereEqualTo("inviteCode", inviteCode).get().await()

                if (query.isEmpty) {
                    errorMessage.value = "Invalid invite code"
                    isLoading.value = false
                    return@launch
                }

                val teamDoc = query.documents[0]
                val teamId = teamDoc.id

                val members = teamDoc.get("members") as? List<String> ?: emptyList()
                if (members.contains(currentUser.uid)) {
                    errorMessage.value = "You are already a member of this team"
                    isLoading.value = false
                    return@launch
                }
                val batch = db.batch()
                batch.update(db.collection("teams").document(teamId), "members", FieldValue.arrayUnion(currentUser.uid))
                batch.update(db.collection("users").document(currentUser.uid), "teams", FieldValue.arrayUnion(teamId))
                batch.commit().await()

                isSuccess.value = true
            } catch (e: Exception) {
                errorMessage.value = "Failed to join: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }

}
