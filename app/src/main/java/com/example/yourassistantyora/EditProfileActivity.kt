package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.firebase.auth.FirebaseAuth

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        setContent {
            YourAssistantYoraTheme {
                ProfileScreen(
                    userName = auth.currentUser?.displayName ?: "Unknown User",
                    userEmail = auth.currentUser?.email ?: "No Email",
                    onBackClick = { finish() },
                    onLogout = { logoutUser() },
                    onEditProfile = { /* optional: navigate to edit screen later */ },
                    onCameraClick = { /* open camera here later */ },
                    onGalleryClick = { /* open gallery here later */ }
                )
            }
        }
    }

    private fun logoutUser() {
        val googleSignInClient = com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
            this,
            com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
            ).build()
        )

        // Sign out from Firebase first
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()

        // Then sign out from Google
        googleSignInClient.signOut().addOnCompleteListener {
            // Now both Firebase and Google are fully signed out
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
