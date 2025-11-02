package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

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
                        "uid" to uid,
                        "username" to username,
                        "email" to email,
                        "createdAt" to Date()
                    )

                    // write to Firestore under collection "users" document uid
                    db.collection("users").document(uid)
                        .set(userMap)
                        .addOnSuccessListener {
                            onResult(true, null)
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
