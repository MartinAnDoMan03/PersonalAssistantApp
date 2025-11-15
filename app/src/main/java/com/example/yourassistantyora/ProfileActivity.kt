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


class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val totalTasks = 5       // Placeholder value, you can fetch this from your database later
        val completedTasks = 2   // Placeholder value

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val userName = intent.getStringExtra("USER_NAME") ?: auth.currentUser?.displayName ?: "User"

        setContent {
            YourAssistantYoraTheme {
                ProfileScreen(
                    userName = userName,
                    userEmail = auth.currentUser?.email ?: "No Email",
                    totalTasks = totalTasks,
                    completedTasks = completedTasks,
                    onBackClick = { finish() },
                    onLogout = { performLogout() },   // <-- Keeping your GOOD logout log
                    onEditProfile = { /* optional: navigate to edit screen later */ },
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
}
