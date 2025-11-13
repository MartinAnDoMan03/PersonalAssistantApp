package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.android.gms.tasks.Task


class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        auth = FirebaseAuth.getInstance()

        setContent {
            YourAssistantYoraTheme {
                ProfileScreen(
                    userName = userName,
                    userEmail = auth.currentUser?.email ?: "No Email",
                    onBackClick = { finish() },
                    onLogout = { performLogout() },
                    onEditProfile = { /* optional: navigate to edit screen later */ },
                    onCameraClick = { /* open camera here later */ },
                    onGalleryClick = { /* open gallery here later */ }
                )
            }
        }
    }

    private fun performLogout() {
        // lifecycleScope is tied to the Activity's lifecycle.
        // It will automatically cancel the coroutine if the Activity is destroyed.
        lifecycleScope.launch {
            logoutUser()
        }
    }

    // This is now a suspend function
    private suspend fun logoutUser() {
        val googleSignInClient = GoogleSignIn.getClient(this,
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        )

        try {
            Log.d("Logout", "Starting logout process...")

            // Step 1: Revoke Google Access Token and WAIT for it to complete.
            Log.d("Logout", "Revoking Google access token...")
            googleSignInClient.revokeAccess().await() // The code pauses here until this is done
            Log.d("Logout", "Access token revoked.")

            // Step 2: Sign out from Google Client and WAIT for it to complete.
            Log.d("Logout", "Signing out from Google client...")
            googleSignInClient.signOut().await() // The code pauses here until this is done
            Log.d("Logout", "Google client signed out.")

            // Step 3: Sign out from Firebase Auth.
            Log.d("Logout", "Signing out from Firebase...")
            FirebaseAuth.getInstance().signOut()
            Log.d("Logout", "Firebase signed out.")

            // Step 4: ONLY NOW, after all steps are complete, navigate.
            Log.d("Logout", "All logout steps complete. Navigating to LoginActivity.")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

        } catch (e: Exception) {
            // Handle any errors during the logout process, e.g., network issues
            Log.e("Logout", "Error during logout: ${e.message}", e)
            // Optionally, show a toast to the user
            // Toast.makeText(this, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
