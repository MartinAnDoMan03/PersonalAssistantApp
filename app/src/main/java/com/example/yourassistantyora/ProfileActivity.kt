package com.example.yourassistantyora

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue


class ProfileActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val totalTasks = 5       // Placeholder value, you can fetch this from your database later
        val completedTasks = 2   // Placeholder value
        val initialUserName = intent.getStringExtra("USER_NAME") ?: "User"
        var userName by mutableStateOf(initialUserName)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            YourAssistantYoraTheme {
                ProfileScreen(
                    userName = userName,
                    userEmail = auth.currentUser?.email ?: "No Email",
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    onBackClick = {
                        val resultIntent = Intent()
                        resultIntent.putExtra("UPDATED_USER_NAME", userName)
                        setResult(RESULT_OK, resultIntent)
                        finish() },
                    onLogout = { performLogout() },   // <-- Keeping your GOOD logout log
                    onSave = { newName -> performProfileUpdate(newName){
                        userName = newName
                    } },
                    onCameraClick = { /* open camera here later */ },
                    onGalleryClick = { /* open gallery here later */ }
                )
            }
        }
    }
    private fun performLogout() {
        Log.d("LOGOUT_PROCESS", "------------- STARTING IMMEDIATE LOGOUT -------------")
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("logged_out_manually", true)
            apply()
        }

        auth.signOut()

        googleSignInClient.signOut()

        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        // Step 4: Close the current profile activity.
        finish()
    }

// In ProfileActivity.kt

    private fun performProfileUpdate(newName: String, onSuccess: () -> Unit) {
        Log.d("PROFILE_UPDATE", "--- STARTING UPDATE PROCESS ---")
        Log.d("PROFILE_UPDATE", "Attempting to update username to: '$newName'")
        val user = auth.currentUser
        if (user == null) {
            Log.e("PROFILE_UPDATE", "User not logged in, cannot update profile.")
            return
        }
        db.collection("users").document(user.uid) // <-- Also use user.uid, not user.id
            .update("username", newName)
            .addOnSuccessListener {
                Log.d("PROFILE_UPDATE", "Firestore username updated successfully to $newName")

                onSuccess()

                val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
                    displayName = newName
                }

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("PROFILE_UPDATE", "Firebase Auth displayName updated successfully.")
                        } else {
                            Log.e("PROFILE_UPDATE", "Failed to update Firebase Auth displayName.", task.exception)
                        }
                    }
            }
            .addOnFailureListener { e ->
                Log.e("PROFILE_UPDATE", "Error updating Firestore username", e)
            }
    }


}
