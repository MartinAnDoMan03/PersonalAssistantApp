package com.example.yourassistantyora

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class JoinTeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAssistantYoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JoinTeamScreen(
                        onBackClick = {
                            finish() // Tutup activity dan kembali ke TeamScreen
                        },
                        onJoinClick = { inviteCode ->
                            // TODO: Implement join team logic
                            // Contoh: validate code dan join team

                            // Simulasi validasi
                            if (validateInviteCode(inviteCode)) {
                                Toast.makeText(
                                    this,
                                    "Successfully joined team!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish() // Kembali ke TeamScreen
                            } else {
                                Toast.makeText(
                                    this,
                                    "Invalid invite code",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }
        }
    }

    // Fungsi untuk validasi invite code
    private fun validateInviteCode(code: String): Boolean {
        // TODO: Implement actual validation logic
        // Contoh: cek ke API atau database

        // Dummy validation - ganti dengan logic sebenarnya
        return code.length == 6 && code.matches(Regex("[A-Z0-9]{6}"))
    }
}