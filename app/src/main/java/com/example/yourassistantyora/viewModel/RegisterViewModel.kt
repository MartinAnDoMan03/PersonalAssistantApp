package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class RegisterViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    val loading = mutableStateOf(false)
    val registerSuccess = mutableStateOf(false)
    val errorMessage = mutableStateOf("")

    fun clearError() {
        errorMessage.value = ""
    }

    fun register(
        username: String,
        email: String,
        password: String
    ) {
        // Validasi minimal, validasi detail tetap di UI
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage.value = "Fields cannot be empty"
            return
        }

        loading.value = true
        errorMessage.value = ""
        registerSuccess.value = false

        viewModelScope.launch {
            try {
                // 1) Create auth user
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid
                    ?: throw IllegalStateException("Could not get uid")

                // 2) Build user map
                val userMap = hashMapOf(
                    "username" to username,
                    "email" to email,
                    "createdAt" to Date()
                )

                try {
                    // 3) Simpan ke Firestore
                    db.collection("users").document(uid).set(userMap).await()
                    registerSuccess.value = true
                } catch (e: Exception) {
                    // Kalau Firestore gagal, hapus user auth (sama seperti di Activity lama)
                    try {
                        auth.currentUser?.delete()?.await()
                    } catch (_: Exception) {
                        // ignore
                    }
                    errorMessage.value = "Failed to save profile: ${e.message ?: ""}".trim()
                }

            } catch (e: Exception) {
                errorMessage.value = e.message ?: "Registration failed"
            } finally {
                loading.value = false
            }
        }
    }
}
