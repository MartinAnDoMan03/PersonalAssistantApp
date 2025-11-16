package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.util.Log

class HomeActivity : AppCompatActivity() {
    private val profileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){ result ->
        if (result.resultCode == RESULT_OK){
            val updatedName = result.data?.getStringExtra("UPDATED_USER_NAME")
            if (!updatedName.isNullOrEmpty()){
                this.userName = updatedName
                Log.d("HOME_ACTIVITY", "Uodated username from profile: $updatedName")
            }

        }
    }

    private var userName by mutableStateOf("User")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Update Username
        val initialUserName = intent.getStringExtra("USER_NAME") ?: "User"
        this.userName = initialUserName

        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        val userName = intent.getStringExtra("USER_NAME") ?: "User"
        if (currentUser == null) {
            // No logged-in user, kick back to login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        val userEmail = currentUser.email ?: intent.getStringExtra("USER_EMAIL") ?: "tomholland@gmail.com"
        setContent {
            YourAssistantYoraTheme {
                HomeScreen(
                    userName = this.userName,
                    onNotificationClick = {
                        // TODO: Handle notification click
                    },
                    onProfileClick = {
                        val intent = Intent(this, ProfileActivity::class.java)

                        intent.putExtra("USER_NAME", this.userName)
                        intent.putExtra("USER_EMAIL", userEmail)
                        intent.putExtra("TOTAL_TASKS", 10)
                        intent.putExtra("COMPLETED_TASKS", 6)
                        // TODO: Hitung total tasks dan completed dari state
                        profileLauncher.launch(intent)

                        // 4. The old startActivity(intent) call is GONE.
                    },

                    onTaskClick = { task ->
                        // TODO: Handle task click
                    },
                    onNavigateToTasks = {
                        // Navigate ke TaskActivity
                        val intent = Intent(this, TaskActivity::class.java)
                        startActivity(intent)
                    },
                    // ✨ TAMBAHKAN 2 CALLBACK INI
                    onNavigateToNotes = {
                        // TODO: Navigate ke NoteActivity
                        val intent = Intent(this, NoteActivity::class.java)
                        startActivity(intent)

                    },
                    onNavigateToTeam = {
                        // TODO: Navigate ke TeamActivity
                        val intent = Intent(this, TeamActivity::class.java)
                        startActivity(intent)
                    }
                )
            }
        }
    }
}