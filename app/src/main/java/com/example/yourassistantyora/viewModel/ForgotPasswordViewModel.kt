// viewModel/ForgotPasswordViewModel.kt
package com.example.yourassistantyora.viewModel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ForgotPasswordViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    var email = mutableStateOf("")
        private set

    var loading = mutableStateOf(false)
        private set

    var message = mutableStateOf<String?>(null)
        private set

    var isSuccess = mutableStateOf(false)
        private set

    fun onEmailChange(value: String) {
        email.value = value
        message.value = null
        isSuccess.value = false
    }

    fun sendResetEmail() {
        val currentEmail = email.value.trim()
        if (currentEmail.isBlank()) {
            message.value = "Please enter your email"
            return
        }

        // Validasi basic format
        loading.value = true
        message.value = null
        isSuccess.value = false

        viewModelScope.launch {
            try {
                auth.sendPasswordResetEmail(currentEmail).await()
                isSuccess.value = true
                message.value = "Reset link sent! Check your email"
            } catch (e: Exception) {
                message.value = "Error: ${e.message ?: "Something went wrong"}"
            } finally {
                loading.value = false
            }
        }
    }
}
