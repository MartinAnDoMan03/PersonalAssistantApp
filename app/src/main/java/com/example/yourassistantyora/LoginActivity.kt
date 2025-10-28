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
                        startActivity(Intent(this, MainActivity::class.java))
                        finish() // Tutup LoginActivity agar tidak bisa kembali
                    },
                    onSignUp = {
                        // Arahkan ke RegisterActivity saat "Sign Up" diklik
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onForgot = {
                        // placeholder untuk Forgot Password
                    },
                    onGoogle = {
                        // placeholder untuk Google Sign-In
                    }
                )
            }
        }
    }
}
