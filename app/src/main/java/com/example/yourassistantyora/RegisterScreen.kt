package com.example.yourassistantyora

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun RegisterScreen(onLoginClick: () -> Unit = {}, onBackClick: () -> Unit = {}) {
    //Input State
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    //Color State
    val pageBg = Color(0xFFFFFFFF)
    val inputBg = Color(0xFFF5F7FA)
    val linkColor = Color(0xFF1976D2)

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(pageBg) // ⬅️ Tambahkan ini
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(8.dp))

        // Small Title
        Text(
            text = "Regis Form",
            fontSize = 12.sp,
            color = Color.Gray
        )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ionic_ionicons_arrow_back_circle_outline),
                    contentDescription = "Back",
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { onBackClick() }
                )
            }



            Spacer(modifier = Modifier.height(8.dp))

        // Title
        Text(
            text = "Create Account",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Fill in the details to get started",
            fontSize = 20.sp,
            color = Color.DarkGray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username
        Text("Username", fontSize = 12.sp, color = Color.Gray)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                placeholder = { Text("Enter your Username") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = inputBg,
                    unfocusedContainerColor = inputBg,
                    unfocusedBorderColor = Color.LightGray,
                    focusedBorderColor = linkColor
                ),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )


            Spacer(modifier = Modifier.height(12.dp))

        // Email
        Text("Email", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Enter your Email") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = inputBg,
                unfocusedContainerColor = inputBg,
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = linkColor
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password
        Text("Password", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Enter your Password") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = inputBg,
                unfocusedContainerColor = inputBg,
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = linkColor
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        Spacer(modifier = Modifier.height(12.dp))

// Confirm Password
        Text("Confirm Password", fontSize = 12.sp, color = Color.Gray)
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            placeholder = { Text("Confirm your Password") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = inputBg,
                unfocusedContainerColor = inputBg,
                unfocusedBorderColor = Color.LightGray,
                focusedBorderColor = linkColor
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )


        Spacer(modifier = Modifier.height(16.dp))

        // Terms and Conditions
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = termsAccepted,
                onCheckedChange = { termsAccepted = it }
            )
            Text("I agree to Terms and Conditions")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Button
            Button(
                onClick = {
                    when {
                        username.isBlank() ||
                                email.isBlank() ||
                                password.isBlank() ||
                                confirmPassword.isBlank() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Please fill all fields")
                            }
                        }
                        !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Invalid email format")
                            }
                        }
                        password.length < 6 -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Password must be at least 6 characters")
                            }
                        }
                        password != confirmPassword -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Passwords do not match")
                            }
                        }
                        !termsAccepted -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("You must accept the terms")
                            }
                        }
                        else -> {
                            scope.launch {
                                snackbarHostState.showSnackbar("Account created! ✅")
                            }
//                             TODO: lanjut proses register ke backend nanti
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create Account")
            }


            Spacer(modifier = Modifier.height(16.dp))

        // Footer
        Text("Already have an account?", modifier = Modifier.align(Alignment.CenterHorizontally))
        Text(
            "Login",
            color = Color(0xFF1976D2),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onLoginClick() }
        )
    }
}}

