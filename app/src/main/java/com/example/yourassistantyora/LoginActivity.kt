package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            YourAssistantYoraTheme {
                LoginScreen(
                    onSignIn = { email, password ->
                        // TODO: Validate credentials, call API

                        // Navigasi ke HomeActivity
                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("USER_NAME", "Tom Holland") // atau ambil dari response API
                        startActivity(intent)
                        finish() // Tutup LoginActivity agar tidak bisa kembali
                    },
                    onSignUp = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onForgot = {
                        startActivity(Intent(this, ForgotPasswordActivity::class.java))
                    },
                    onGoogle = {
                        // TODO: Implementasi Google Sign-In
                    }
                )
            }
        }
    }
}