package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ForgotPasswordActivity : AppCompatActivity() {
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            var email by remember { mutableStateOf("") }
            var loading by remember { mutableStateOf(false) }

            YourAssistantYoraTheme {
                ForgotPasswordScreen(
                    email = email,
                    loading = loading,
                    onEmailChange = { email = it },
                    snackbarHostState = snackbarHostState,
                    onBackToLogin = { finish() },
                    onSendClick = {
                        if (email.isBlank()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Please enter your email")
                            }
                            return@ForgotPasswordScreen
                        }

                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Invalid email format")
                            }
                            return@ForgotPasswordScreen
                        }

                        loading = true
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                loading = false
                                CoroutineScope(Dispatchers.Main).launch {
                                    if (task.isSuccessful) {
                                        snackbarHostState.showSnackbar("Reset link sent! Check your email")
                                        val intent = Intent(this@ForgotPasswordActivity, CheckEmailActivity::class.java)
                                        intent.putExtra("EMAIL", email)
                                        startActivity(intent)
                                        finish()
                                    } else {
                                        snackbarHostState.showSnackbar("Error: ${task.exception?.message ?: "Something went wrong"}")
                                    }
                                }
                            }
                    }
                )
            }
        }
    }
}
