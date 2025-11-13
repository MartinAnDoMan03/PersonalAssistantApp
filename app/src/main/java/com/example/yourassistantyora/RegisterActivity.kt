package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date
import android.widget.Toast

class RegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // initialize firebase instance
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        setContent {
            YourAssistantYoraTheme {
                RegisterScreen(
                    onLoginClick = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    },
                    onRegister = { username, email, password, onResult ->
                        registerUser(username, email, password, onResult)
                    }
                )
            }
        }
    }

    private fun registerUser(
        username: String,
        email: String,
        password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            onResult(false, "Fields cannot be empty")
        }
        // create auth user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // get uid
                    val uid = auth.currentUser?.uid
                    if (uid == null) {
                        onResult(false, "Could not get uid")
                        return@addOnCompleteListener
                    }

                    // build user map
                    val userMap = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "createdAt" to Date()
                    )

                    // write to Firestore under collection "users" document uid
                    db.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Registration success!", Toast.LENGTH_SHORT).show()
                            onResult(true, null)
                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()
                        }
                        .addOnFailureListener { e ->
                            //Delete in case of failure
                            auth.currentUser?.delete()?.addOnCompleteListener {
                                onResult(false, "Failed to save profile: ${e.message}")
                            }
                        }
                } else {
                    onResult(false, authTask.exception?.message ?: "Registration failed")
                }
            }
    }
}
