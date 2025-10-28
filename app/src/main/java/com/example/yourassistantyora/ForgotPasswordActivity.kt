package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            YourAssistantYoraTheme {
                ForgotPasswordScreen(
                    onBackToLogin = {
                        finish() // Kembali ke LoginActivity
                    },
                    onEmailSent = { email ->
                        // Navigasi ke CheckEmailActivity dengan email
                        val intent = Intent(this, CheckEmailActivity::class.java)
                        intent.putExtra("EMAIL", email)
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}