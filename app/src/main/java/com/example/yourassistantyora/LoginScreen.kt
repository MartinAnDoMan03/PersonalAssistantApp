package com.example.yourassistantyora

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onForgot: () -> Unit = {},
    onGoogle: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    var loading by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // State untuk show/hide password
    var remember by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 28.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // --- LOGO UTAMA ---
        Box(
            modifier = Modifier
                .size(110.dp)
                .rotate(degrees = -5f)
                .shadow(18.dp, RoundedCornerShape(28.dp), clip = false)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF9B81FF), Color(0xFF6C63FF))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_checkmark),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(60.dp)
                    .rotate(-5f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to YORA",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your personal assistant awaits",
            color = Color(0xFF757575),
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Label untuk Email Field
        Text(
            text = "Email / Username",
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF757575),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text(text = "Enter your email", color = Color(0xFFB0B0B0)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6C63FF),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Label untuk Password Field
        Text(
            text = "Password",
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF757575),
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Password Field dengan ikon show/hide
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text(text = "Enter your password", color = Color(0xFFB0B0B0)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6C63FF),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            ),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Checkbox(
                    checked = remember,
                    onCheckedChange = { remember = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF6C63FF))
                )
                Text(text = "Remember", color = Color(0xFF757575))
            }
            Text(
                text = "Forgot?",
                color = Color(0xFF6C63FF),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onForgot() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- TOMBOL SIGN IN ---
        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                loading = true

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        loading = false
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Login successful", Toast.LENGTH_SHORT).show()

                            val intent = Intent(context, HomeActivity::class.java)
                            intent.putExtra("USER_NAME", auth.currentUser?.email ?: "Unknown User")
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, task.exception?.message ?: "Login failed", Toast.LENGTH_LONG).show()
                        }
                    }
            },

            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues()
        )
        {
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
                Text(text = "Sign In", fontSize = 16.sp, color = Color.White)
            }
        }

        if (loading) {
            Spacer(modifier = Modifier.height(16.dp))
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text(
                text = "  OR CONTINUE WITH  ",
                color = Color(0xFFA0A0A0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Google
        OutlinedButton(
            onClick = { onGoogle() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFFE0E0E0))
        ) {
            Image(
                painter = painterResource(id = R.drawable.googlo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Google",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // Sign up
        Row(
            modifier = Modifier.padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(text = "Don't have an account? ", color = Color(0xFF757575))
            Text(
                text = "Sign Up",
                color = Color(0xFF6C63FF),
                modifier = Modifier.clickable { onSignUp() },
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    YourAssistantYoraTheme {
        LoginScreen()
    }
}
