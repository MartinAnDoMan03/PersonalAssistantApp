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

class CreateTeamActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            YourAssistantYoraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreateTeamScreen(
                        onBackClick = {
                            finish()
                        },
                        onCreateClick = { teamName, description, category, colorScheme ->
                            // TODO: Save team to database
                            Toast.makeText(
                                this,
                                "Team \"$teamName\" created successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    )
                }
            }
        }
    }
}