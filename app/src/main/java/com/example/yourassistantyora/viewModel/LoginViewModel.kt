package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LoginViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    var email = mutableStateOf("")
    var password = mutableStateOf("")
    var loading = mutableStateOf(false)
    var loginSuccess = mutableStateOf(false)
    var errorMessage = mutableStateOf("")

    fun onEmailChange(value: String) {
        email.value = value
        errorMessage.value = "" // Clear error on input
    }

    fun onPasswordChange(value: String) {
        password.value = value
        errorMessage.value = "" // Clear error on input
    }

    // Method that matches LoginScreen's call
    fun login() {
        if (email.value.isBlank() || password.value.isBlank()) {
            errorMessage.value = "Email and password are required"
            return
        }

        loading.value = true
        errorMessage.value = ""
        loginSuccess.value = false

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email.value, password.value).await()
                val user = result.user
                if (user != null) {
                    loading.value = false
                    loginSuccess.value = true
                }
            } catch (e: Exception) {
                loading.value = false
                errorMessage.value = e.message ?: "Login failed"
                loginSuccess.value = false
            }
        }
    }

    fun loginWithGoogle(account: GoogleSignInAccount) {
        loading.value = true
        errorMessage.value = ""

        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                val result = auth.signInWithCredential(credential).await()
                val user = result.user
                if (user != null) {
                    val displayName = account.displayName ?: "New User"
                    // bikin / cek profile user di Firestore
                    checkAndCreateUserProfile(
                        userId = user.uid,
                        displayName = displayName,
                        email = user.email ?: ""
                    )
                    loading.value = false
                    loginSuccess.value = true   // ⬅️ ini yang akan ditangkap LaunchedEffect di LoginScreen
                } else {
                    loading.value = false
                    errorMessage.value = "Google login failed"
                }
            } catch (e: Exception) {
                loading.value = false
                errorMessage.value = e.message ?: "Google login failed"
            }
        }
    }


    private suspend fun checkAndCreateUserProfile(
        userId: String,
        displayName: String,
        email: String
    ): String {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            if (doc.exists()) {
                doc.getString("username") ?: displayName
            } else {
                val newUser = hashMapOf(
                    "username" to displayName,
                    "email" to email
                )
                db.collection("users").document(userId).set(newUser).await()
                displayName
            }
        } catch (e: Exception) {
            displayName // fallback
        }
    }

    private suspend fun fetchUsername(userId: String): String? {
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.getString("username")
        } catch (e: Exception) {
            null
        }
    }
}