// viewmodel/AuthViewModel.kt
package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var currentUser = mutableStateOf(auth.currentUser)
        private set

    var userName = mutableStateOf<String?>(null)
        private set

    var userPhotoUrl = mutableStateOf<String?>(null)
        private set

    var isLoading = mutableStateOf(true)
        private set

    // 🔹 state khusus untuk Edit Profile
    var isProfileUpdating = mutableStateOf(false)
        private set

    var profileError = mutableStateOf<String?>(null)
        private set

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        isLoading.value = true
        val user = auth.currentUser
        currentUser.value = user

        if (user != null) {
            userName.value = user.displayName ?: user.email?.substringBefore("@") ?: "User"

            viewModelScope.launch {
                try {
                    val document = db.collection("users").document(user.uid).get().await()
                    if (document.exists()) {
                        val nameFromDb = document.getString("username")
                        val photoFromDb = document.getString("photoUrl")

                        if (nameFromDb != null) userName.value = nameFromDb
                        if (photoFromDb != null) userPhotoUrl.value = photoFromDb
                    }
                } catch (e: Exception) {
                }
            }
        } else {
            userName.value = null
            userPhotoUrl.value = null
        }

        isLoading.value = false
    }


    fun signOut() {
        auth.signOut()
        currentUser.value = null
        userName.value = null
    }

    /**
     * Dipanggil dari EditProfileScreen saat user tekan "Save"
     * - update displayName di FirebaseAuth
     * - update field "username" di Firestore (collection "users")
     */
    fun updateUserProfile(newName: String, newPhotoBase64: String?) {
        val user = auth.currentUser ?: return

        isProfileUpdating.value = true
        profileError.value = null

        viewModelScope.launch {
            try {
                val profileUpdates = userProfileChangeRequest {
                    displayName = newName
                }
                user.updateProfile(profileUpdates).await()

                val userData = mutableMapOf<String, Any>(
                    "username" to newName,
                    "email" to (user.email ?: "")
                )

                if (newPhotoBase64 != null) {
                    userPhotoUrl.value = newPhotoBase64
                }

                db.collection("users")
                    .document(user.uid)
                    .set(userData, SetOptions.merge())
                    .await()

                userName.value = newName
                currentUser.value = auth.currentUser

            } catch (e: Exception) {
                profileError.value = e.message ?: "Failed to update profile"
            } finally {
                isProfileUpdating.value = false
            }
        }
    }


}
