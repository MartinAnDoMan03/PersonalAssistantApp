package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            YourAssistantYoraTheme {
                LoginScreen(
                    onSignUp = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onForgot = {
                        startActivity(Intent(this, ForgotPasswordActivity::class.java))
                    },
                    onGoogle = {
                        // TODO: Implement Google Sign-In later
                    },
                    onLogin = { email, password, onResult ->
                        loginUser(email, password, onResult)
                    }
                )
            }
        }
    }

    private fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter email and password")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("USER_NAME", auth.currentUser?.email ?: "Unknown User")
                    startActivity(intent)
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message ?: "Login failed")
                }
            }
    }
}
