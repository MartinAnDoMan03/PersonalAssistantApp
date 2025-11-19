package com.example.yourassistantyora.screen

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.yourassistantyora.R
import com.example.yourassistantyora.navigateAndClearBackStack
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

@Composable
fun CheckEmailScreen(
    navController: NavController,
    email: String
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun openEmailApp() {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_EMAIL)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("mailto:")
                }
                context.startActivity(fallbackIntent)
            } catch (e: Exception) {
                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun resendEmail() {
        val auth = FirebaseAuth.getInstance()
        scope.launch {
            try {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Reset link resent to $email")
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Error: ${task.exception?.message ?: "Something went wrong"}"
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                snackbarHostState.showSnackbar("Error: ${e.message ?: "Something went wrong"}")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7FA))
                .padding(paddingValues)
                .padding(horizontal = 28.dp)
                .verticalScroll(rememberScrollState())
                .statusBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Back Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(
                    onClick = { navController.navigateAndClearBackStack("login") },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF757575)
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Checkmark Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .shadow(12.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF9B81FF), Color(0xFF6C63FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_check_icon),
                    contentDescription = "Checkmark",
                    modifier = Modifier.size(65.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Title
            Text(
                text = "Check Your Email",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val annotatedText = buildAnnotatedString {
                append("We've sent a password reset link to\n")
                withStyle(
                    style = SpanStyle(
                        color = Color(0xFF6C63FF),
                        fontWeight = FontWeight.SemiBold
                    )
                ) {
                    append(email)
                }
            }

            Text(
                text = annotatedText,
                fontSize = 14.sp,
                color = Color(0xFF757575),
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE3F2FD))
                    .padding(16.dp)
            ) {
                Column {
                    Text(
                        text = "What's next?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1976D2)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Check your inbox for the reset email",
                        fontSize = 13.sp,
                        color = Color(0xFF1976D2),
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "2. Click the reset link (valid for 30 minutes)",
                        fontSize = 13.sp,
                        color = Color(0xFF1976D2),
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "3. Create your new password",
                        fontSize = 13.sp,
                        color = Color(0xFF1976D2),
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Open Email App Button
            Button(
                onClick = { openEmailApp() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFF9B81FF), Color(0xFF6C63FF))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Open Email App", fontSize = 16.sp, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resend Email Link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Didn't receive the email? ",
                    color = Color(0xFF757575),
                    fontSize = 14.sp
                )
                Text(
                    text = "Resend Email",
                    color = Color(0xFF6C63FF),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { resendEmail() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
