package com.example.yourassistantyora.viewModel

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.yourassistantyora.models.Team
import com.example.yourassistantyora.models.TeamMember
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import com.example.yourassistantyora.models.TeamTaskDb

class CreateTeamTaskViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // --- UI State ---
    val title = mutableStateOf("")
    val description = mutableStateOf("")
    val selectedPriority = mutableIntStateOf(1) // Default: Medium (ordinal 1)
    val selectedDate = mutableStateOf<Date?>(null)
    val selectedTime = mutableStateOf<Calendar?>(null)
    val assignedMemberId = MutableStateFlow<String?>(null)
    val attachments = MutableStateFlow<List<Uri>>(emptyList())

    // --- Data State ---
    private val _teamMembers = MutableStateFlow<List<TeamMember>>(emptyList())
    val teamMembers = _teamMembers.asStateFlow()

    // --- Interaction State ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess = _isSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadTeamMembers(teamId: String) {
        viewModelScope.launch {
            _isLoading.value = true // Mulai loading
            try {
                // 1. Ambil semua tugas untuk tim ini terlebih dahulu
                val allTeamTasks = db.collection("team_tasks")
                    .whereEqualTo("team_id", teamId)
                    .get()
                    .await()
                    .toObjects(TeamTaskDb::class.java)

                // 2. Ambil data dasar tim
                val teamDoc = db.collection("teams").document(teamId).get().await()
                val teamData = teamDoc.toObject(Team::class.java)

                if (teamData == null) {
                    _error.value = "Team not found."
                    _isLoading.value = false
                    return@launch
                }

                // 3. Ambil profil setiap anggota dan hitung tugas mereka
                val memberProfiles = teamData.members.map { memberId ->
                    val userDoc = db.collection("users").document(memberId).get().await()
                    val role = if (teamData.createdBy == memberId) "Admin" else "Member"

                    val completedCount = allTeamTasks.count { task ->
                        task.status == 2 && task.uid.contains(memberId) // status 2 = Done
                    }
                    val activeCount = allTeamTasks.count { task ->
                        task.status != 2 && task.uid.contains(memberId)
                    }

                    TeamMember(
                        id = memberId,
                        name = userDoc.getString("username") ?: "Unknown",
                        role = role,
                        activeTasks = activeCount,
                        tasksCompleted = completedCount
                    )
                }
                _teamMembers.value = memberProfiles

            } catch (e: Exception) {
                _error.value = "Failed to load team members: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onMemberSelected(uid: String) {
        if (assignedMemberId.value == uid) {
            // Jika ID yang sama diklik, batalkan pilihan (set ke null)
            assignedMemberId.value = null
        } else {
            // Jika ID berbeda, ganti dengan yang baru
            assignedMemberId.value = uid
        }
    }


        fun addAttachment(uri: Uri, context: Context) {
        // Validasi tipe dan ukuran file
        try {
            val contentResolver = context.contentResolver
            val mimeType = contentResolver.getType(uri)
            val cursor = contentResolver.query(uri, null, null, null, null)
            val size = cursor?.use {
                val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)
                it.moveToFirst()
                it.getLong(sizeIndex)
            } ?: 0

            val isAllowedType = mimeType?.startsWith("image/") == true ||
                    mimeType == "application/pdf" ||
                    mimeType == "application/msword" ||
                    mimeType == "application/vnd.openxmlformats-officedocument.wordprocessingml.document"

            if (!isAllowedType) {
                Toast.makeText(context, "File type not supported.", Toast.LENGTH_SHORT).show()
                return
            }

            if (size > 10 * 1024 * 1024) { // 10 MB
                Toast.makeText(context, "File size exceeds 10MB limit.", Toast.LENGTH_SHORT).show()
                return
            }

            attachments.value = attachments.value + uri
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to check file details.", Toast.LENGTH_SHORT).show()
        }
    }

    fun removeAttachment(uri: Uri) {
        attachments.value = attachments.value - uri
    }

    fun createTask(teamId: String, context: Context) {
        if (!isFormValid()) {
            _error.value = "Title, deadline, and at least one member are required."
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                // 1. Upload files ke Cloudinary
                val attachmentUrls = uploadFilesToCloudinary(attachments.value)

                // 2. Siapkan data untuk Firestore
                val creatorId = auth.currentUser?.uid ?: throw IllegalStateException("User not authenticated.")
                val deadlineTimestamp = combineDateAndTime(selectedDate.value!!, selectedTime.value)
                val taskId = db.collection("team_tasks").document().id

                val taskData = hashMapOf(
                    "createdBy" to creatorId,
                    "createdOn" to Timestamp.now(),
                    "ttask_id" to taskId,
                    "team_id" to teamId,
                    "deadline" to deadlineTimestamp,
                    "title" to title.value.trim(),
                    "desc" to description.value.trim(),
                    "priority" to selectedPriority.value,
                    "status" to 0, // Default: Not Started
                    "uid" to (assignedMemberId.value?.let { listOf(it) } ?: emptyList()),
                    "docs" to attachmentUrls
                )

                // 3. Simpan data ke Firestore
                db.collection("team_tasks").document(taskId).set(taskData).await()
                _isSuccess.value = true
            } catch (e: Exception) {
                _error.value = e.message ?: "An unknown error occurred."
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun uploadFilesToCloudinary(uris: List<Uri>): List<String> {
        if (uris.isEmpty()) return emptyList()

        val uploadedUrls = mutableListOf<String>()
        for (uri in uris) {
            val url = suspendCancellableCoroutine<String> { continuation ->
                val requestId = MediaManager.get().upload(uri)
                    .unsigned("ml_default") // Ganti dengan upload preset Anda jika perlu
                    .callback(object : UploadCallback {
                        override fun onStart(requestId: String) {}
                        override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                        override fun onSuccess(requestId: String, resultData: MutableMap<Any?, Any?>) {
                            val secureUrl = resultData["secure_url"] as? String
                            if (secureUrl != null) {
                                if (continuation.isActive) continuation.resume(secureUrl)
                            } else {
                                if (continuation.isActive) continuation.resumeWithException(Exception("Cloudinary URL not found."))
                            }
                        }
                        override fun onError(requestId: String, error: ErrorInfo) {
                            if (continuation.isActive) continuation.resumeWithException(Exception("Cloudinary upload failed: ${error.description}"))
                        }
                        override fun onReschedule(requestId: String, error: ErrorInfo) {}
                    }).dispatch()

                continuation.invokeOnCancellation {
                    MediaManager.get().cancelRequest(requestId)
                }
            }
            uploadedUrls.add(url)
        }
        return uploadedUrls
    }

    private fun combineDateAndTime(date: Date, time: Calendar?): Timestamp {
        val finalCalendar = Calendar.getInstance().apply {
            timeInMillis = date.time
            time?.let {
                set(Calendar.HOUR_OF_DAY, it.get(Calendar.HOUR_OF_DAY))
                set(Calendar.MINUTE, it.get(Calendar.MINUTE))
            }
        }
        return Timestamp(finalCalendar.time)
    }


    fun isFormValid(): Boolean = title.value.isNotBlank() && selectedDate.value != null && assignedMemberId.value != null

    fun clearError() { _error.value = null }
}
