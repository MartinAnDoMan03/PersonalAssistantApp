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
            // sementara: pakai displayName/email prefix
            userName.value = user.displayName ?: user.email?.substringBefore("@") ?: "User"
        } else {
            userName.value = null
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
    fun updateUserName(newName: String) {
        val user = auth.currentUser ?: return

        isProfileUpdating.value = true
        profileError.value = null

        viewModelScope.launch {
            try {
                // 🔹 1) update displayName di Firebase Auth
                val profileUpdates = userProfileChangeRequest {
                    displayName = newName
                }
                user.updateProfile(profileUpdates).await()

                // 🔹 2) update Firestore (merge biar field lain gak ketimpa)
                val userData = mapOf(
                    "username" to newName,
                    "email" to (user.email ?: "")
                )

                db.collection("users")
                    .document(user.uid)
                    .set(userData, SetOptions.merge())
                    .await()

                // 🔹 3) update state lokal
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
