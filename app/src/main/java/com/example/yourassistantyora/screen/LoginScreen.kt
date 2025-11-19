package com.example.yourassistantyora.screen

import android.widget.Toast
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.yourassistantyora.R
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.example.yourassistantyora.viewModel.LoginViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.res.stringResource
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


@Composable
fun LoginScreen(
    navController: NavController,
    onLoginSuccess: (String) -> Unit, // Callback untuk pass username ke AuthViewModel
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current

    val activity = context as? Activity

    // Web client ID dari Firebase (string ini biasanya otomatis dibuat: default_web_client_id)
    val webClientId = stringResource(id = R.string.default_web_client_id)

    // Konfigurasi Google Sign-In
    val gso = remember(webClientId) {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
    }

    val googleSignInClient = remember {
        GoogleSignIn.getClient(context, gso)
    }

    // Launcher untuk hasil Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                if (account != null) {
                    // Panggil ViewModel → Firebase Auth
                    viewModel.loginWithGoogle(account)
                }
            } catch (e: ApiException) {
                Toast.makeText(
                    context,
                    e.localizedMessage ?: "Google sign-in failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                context,
                "Google sign-in cancelled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    val email by viewModel.email
    val password by viewModel.password
    val loading by viewModel.loading
    val loginSuccess by viewModel.loginSuccess
    val errorMessage by viewModel.errorMessage

    var passwordVisible by remember { mutableStateOf(false) }

    // Fetch username dari Firestore ketika login sukses
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                // Fetch username dari Firestore
                val username = try {
                    val db = FirebaseFirestore.getInstance()
                    val doc = db.collection("users").document(currentUser.uid).get().await()
                    doc.getString("username")
                        ?: currentUser.displayName
                        ?: currentUser.email?.split("@")?.get(0)
                        ?: "User"
                } catch (e: Exception) {
                    currentUser.displayName
                        ?: currentUser.email?.split("@")?.get(0)
                        ?: "User"
                }

                // Pass username ke parent (AppNavigation)
                onLoginSuccess(username)
            }
        }
    }

    // Show error
    LaunchedEffect(errorMessage) {
        if (errorMessage.isNotEmpty()) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 28.dp)
            .statusBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        // Logo
        Box(
            modifier = Modifier
                .size(110.dp)
                .rotate(-5f)
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
                painter = painterResource(R.drawable.ic_checkmark_putih),
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

        // Email
        Text("Email / Username", color = Color(0xFF757575), fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.onEmailChange(it) },
            placeholder = { Text("Enter your email", color = Color(0xFFB0B0B0)) },
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

        // Password
        Text("Password", color = Color(0xFF757575), fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.onPasswordChange(it) },
            placeholder = { Text("Enter your password", color = Color(0xFFB0B0B0)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF6C63FF),
                unfocusedBorderColor = Color(0xFFE0E0E0)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Forgot Password
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Forgot?",
                color = Color(0xFF6C63FF),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { navController.navigate("forgot_password") }
            )

        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = { viewModel.login() },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(),
            enabled = !loading
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF9B81FF), Color(0xFF6C63FF))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Sign In", fontSize = 16.sp, color = Color.White)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Divider
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
            Text("  OR CONTINUE WITH  ", color = Color(0xFFA0A0A0), fontSize = 12.sp)
            Divider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Google Button
        OutlinedButton(
            onClick = {
                activity?.let {
                    googleSignInClient.signOut().addOnCompleteListener {}
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)

                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = ButtonDefaults.outlinedButtonBorder
        ) {
            Image(
                painter = painterResource(id = R.drawable.googlo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "Google",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }


        Spacer(modifier = Modifier.weight(1f))

        Row(modifier = Modifier.padding(bottom = 16.dp)) {
            Text("Don't have an account? ", color = Color(0xFF757575))
            Text(
                text = "Sign Up",
                color = Color(0xFF6C63FF),
                modifier = Modifier.clickable { navController.navigate("register") },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun LoginScreenPreview() {
    YourAssistantYoraTheme {
        val navController = rememberNavController()
        LoginScreen(
            navController = navController,
            onLoginSuccess = {}
        )
    }
}