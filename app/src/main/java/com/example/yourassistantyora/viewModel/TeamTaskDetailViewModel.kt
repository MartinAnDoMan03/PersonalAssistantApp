package com.example.yourassistantyora.viewModel

// Tambahkan ini di bagian atas file
import android.content.Context
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourassistantyora.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID // ✅ TAMBAHKAN INI


class TeamTaskDetailViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _taskDetail = MutableStateFlow<TeamTask?>(null)
    val taskDetail = _taskDetail.asStateFlow()

    // ✅ 1. TAMBAHKAN STATEFLOW BARU UNTUK KOMENTAR
    private val _comments = MutableStateFlow<List<TaskComment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private var taskListener: ListenerRegistration? = null
    private var commentsListener: ListenerRegistration? = null

    fun loadTaskAndComments(taskId: String, teamId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        _isLoading.value = true
        _error.value = null // Reset error
        clearListeners()

        // Listener untuk Dokumen Task
        taskListener = db.collection("team_tasks").document(taskId)
            .addSnapshotListener { taskSnapshot, e ->
                if (e != null || taskSnapshot == null || !taskSnapshot.exists()) {
                    _error.value = "Failed to load task: ${e?.message}"
                    _isLoading.value = false // Set loading false jika task gagal dimuat
                    return@addSnapshotListener
                }

                val taskDb = taskSnapshot.toObject(TeamTaskDb::class.java)
                if (taskDb != null) {
                    viewModelScope.launch {
                        try {
                            val teamDoc = db.collection("teams").document(teamId).get().await()
                            val membersList = teamDoc.get("members") as? List<String> ?: emptyList()
                            val allMembers = membersList.map { memberId ->
                                val userDoc = db.collection("users").document(memberId).get().await()
                                TeamMember(id = memberId, name = userDoc.getString("username") ?: "Unknown", role = "")
                            }.toList()

                            _taskDetail.value = taskDb.toUITask(allMembers)
                        } catch (ex: Exception) {
                            _error.value = "Failed to resolve members: ${ex.message}"
                        } finally {
                            // ✅ PERBAIKAN: Set loading false HANYA setelah detail task berhasil di-resolve
                            if (_isLoading.value) {
                                _isLoading.value = false
                            }
                        }
                    }
                }
            }

        // Listener untuk Komentar
        commentsListener = db.collection("comments")
            .whereEqualTo("ctask_id", taskId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { commentsSnapshot, e ->
                if (e != null || commentsSnapshot == null) {
                    _error.value = "Failed to load comments: ${e?.message}"
                    // Jangan set loading false di sini, biarkan task listener yang mengontrol
                    return@addSnapshotListener
                }

                val commentsList = commentsSnapshot.documents.mapNotNull { doc ->
                    val content = doc.getString("content") ?: ""
                    val createdBy = doc.getString("createdBy") ?: ""
                    val userName = doc.getString("userName") ?: "User"
                    val createdAt = doc.getTimestamp("createdAt")

                    val formattedTime = createdAt?.toDate()?.let {
                        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(it)
                    } ?: ""

                    TaskComment(
                        id = doc.id,
                        comment = content,
                        userId = createdBy,
                        userName = userName,
                        timestamp = formattedTime,
                        isCurrentUser = createdBy == currentUserId
                    )
                }
                _comments.value = commentsList
            }
    }

    fun updateTaskStatus(taskId: String, newStatus: TaskStatus) {
        viewModelScope.launch {
            try {
                db.collection("team_tasks").document(taskId)
                    .update("status", newStatus.ordinal)
                    .await()
            } catch (e: Exception) {
                _error.value = "Failed to update status: ${e.message}"
            }
        }
    }

    fun addComment(taskId: String, commentText: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        val currentUsername = auth.currentUser?.displayName ?: "Anonymous"
        if (commentText.isBlank()) return

        val comment = hashMapOf(
            "content" to commentText,
            "createdAt" to FieldValue.serverTimestamp(),
            "createdBy" to currentUserId,
            "ctask_id" to taskId,
            "userName" to currentUsername // Simpan username untuk efisiensi
        )

        viewModelScope.launch {
            try {
                db.collection("comments").add(comment).await()
            } catch (e: Exception) {
                _error.value = "Failed to add comment: ${e.message}"
            }
        }
    }

    private fun clearListeners() {
        taskListener?.remove()
        commentsListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        clearListeners()
    }

    fun deleteAttachment(taskId: String, attachmentToDelete: TaskAttachment) {
        val taskRef = db.collection("team_tasks").document(taskId)

        // Buat map yang sama persis dengan yang ada di Firestore untuk dihapus
        val attachmentMap = mapOf(
            "fileName" to attachmentToDelete.fileName,
            "fileUrl" to attachmentToDelete.fileUrl
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Gunakan FieldValue.arrayRemove untuk menghapus elemen dari array 'docs'
                taskRef.update("docs", FieldValue.arrayRemove(attachmentMap)).await()
            } catch (e: Exception) {
                _error.value = "Failed to delete attachment: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addAttachment(taskId: String, uri: Uri, context: Context) {
        val currentUserId = auth.currentUser?.uid ?: run {
            _error.value = "User not authenticated."
            return
        }
        val currentUsername = auth.currentUser?.displayName?.replace(" ", "_")?.lowercase() ?: "user"

        // Set state loading saat proses dimulai
        _isLoading.value = true

        viewModelScope.launch {
            try {
                // 1. Upload file ke Cloudinary dengan nama kustom
                val secureUrl = suspendCancellableCoroutine<String> { continuation ->
                    // Ambil nama file asli dari URI
                    val fileName = getFileName(uri, context)
                    val fileNameWithoutExtension = fileName.substringBeforeLast('.')
                    val publicId = "${currentUsername}_${fileNameWithoutExtension}"

                    val requestId = MediaManager.get().upload(uri)
                        .option("public_id", publicId)
                        .unsigned("ml_default") // Ganti dengan upload preset Anda
                        .callback(object : UploadCallback {
                            override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                                val url = resultData["secure_url"] as? String
                                if (url != null) {
                                    if (continuation.isActive) continuation.resume(url)
                                } else {
                                    if (continuation.isActive) continuation.resumeWithException(Exception("Cloudinary URL not found."))
                                }
                            }

                            override fun onError(requestId: String, error: ErrorInfo) {
                                if (continuation.isActive) continuation.resumeWithException(Exception("Cloudinary upload failed: ${error.description}"))
                            }
                            // Callback lainnya bisa dibiarkan kosong
                            override fun onStart(requestId: String) {}
                            override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                            override fun onReschedule(requestId: String, error: ErrorInfo) {}
                        }).dispatch()

                    continuation.invokeOnCancellation {
                        MediaManager.get().cancelRequest(requestId)
                    }
                }

                // 2. Perbarui field 'docs' di Firestore
                val taskRef = db.collection("team_tasks").document(taskId)

                // AMBIL NAMA FILE YANG SEBENARNYA DARI URL (HASIL CLOUDINARY)
                val finalFileName = secureUrl.substringAfterLast('/')

                // BUAT MAP UNTUK DISIMPAN
                val newAttachmentMap = mapOf(
                    "fileName" to finalFileName,
                    "fileUrl" to secureUrl
                )

                // GUNAKAN arrayUnion DENGAN MAP, BUKAN STRING
                taskRef.update("docs", FieldValue.arrayUnion(newAttachmentMap)).await()

            } catch (e: Exception) {
                _error.value = "Failed to upload attachment: ${e.message}"
            } finally {
                // Set loading false setelah selesai, baik berhasil maupun gagal
                _isLoading.value = false
            }
        }
    }

    // Helper untuk mendapatkan nama file dari URI
    private fun getFileName(uri: Uri, context: Context): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        result = it.getString(displayNameIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "file_${System.currentTimeMillis()}"
    }
}

// Helper untuk konversi, bisa ditaruh di luar kelas atau di file terpisah
fun TeamTaskDb.toUITask(allMembers: List<TeamMember>): TeamTask {
    val assignedMembers = allMembers.filter { member -> this.uid.contains(member.id) }
    val attachments = this.docs.mapNotNull { attachmentData ->
        when (attachmentData) {
            // Kasus 1: Data BARU dalam format Map
            is Map<*, *> -> {
                val fileName = attachmentData["fileName"] as? String
                val fileUrl = attachmentData["fileUrl"] as? String
                if (fileName != null && fileUrl != null) {
                    TaskAttachment(
                        id = UUID.randomUUID().toString(),
                        fileName = fileName,
                        fileUrl = fileUrl
                    )
                } else {
                    null // Map tidak valid, abaikan
                }
            }
            // Kasus 2: Data LAMA dalam format String (hanya URL)
            is String -> {
                TaskAttachment(
                    id = UUID.randomUUID().toString(),
                    fileUrl = attachmentData, // URL adalah string itu sendiri
                    fileName = attachmentData.substringAfterLast('/') // Ambil nama file dari URL sebagai fallback
                )
            }
            // Abaikan tipe data lain
            else -> null
        }
    }

    return TeamTask(
        id = this.ttask_id,
        title = this.title,
        description = this.desc,
        status = TaskStatus.fromInt(this.status),
        priority = TaskPriority.fromInt(this.priority),
        assignedTo = assignedMembers,
        deadline = this.deadline.toDate().toString(),
        attachments = attachments, // Pastikan variabel ini yang digunakan
        createdBy = this.createdBy,
        createdAt = this.createdOn.toDate().toString()
    )
}


