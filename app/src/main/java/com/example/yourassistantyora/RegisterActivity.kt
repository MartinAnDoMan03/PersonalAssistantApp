package com.example.yourassistantyora

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent


class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RegisterScreen(
                onLoginClick = {
                    startActivity(Intent(this, LoginActivity::class.java))
                }
            )
        }
    }
}

