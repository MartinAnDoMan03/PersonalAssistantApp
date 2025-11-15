package com.example.yourassistantyora

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        db = FirebaseFirestore.getInstance()

        // Google Sign in Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        googleSignInLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                    try {
                        val account = task.getResult(ApiException::class.java)!!
                        firebaseAuthWithGoogle(account)
                    } catch (e: ApiException) {
                        Log.e("GoogleSignIn", "Sign-in failed", e)
                    }
                } else {
                    Log.w("GoogleSignIn", "Google sign-in flow cancelled or failed. Result code: ${result.resultCode}")
                }
            }

        setContent {
            YourAssistantYoraTheme {
                LoginScreen(
                    onSignUp = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                    },
                    onForgot = {
                        startActivity(Intent(this, ForgotPasswordActivity::class.java))
                    },
                    onGoogle = {
                        signInWithGoogle()
                    },
                    // This logic is now clean and correct.
                    onLogin = { email, password, onResult ->
                        loginUser(email, password) { success, error ->
                            if (success) {
                                // On success, relaunch the splash activity to handle routing.
                                Log.d("LoginDebug", "Login successful. Relaunching SplashActivity.")
                                navigateToHome()
                            } else {
                                // On failure, report error back to the UI.
                                onResult(false, error)
                            }
                        }
                    }
                )
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LoginDebug", "Google sign-in successful. Relaunching SplashActivity.")
                    // Use the same robust strategy: let SplashActivity handle it.
                    val intent = Intent(this, SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.e("LoginDebug", "Google sign-in failed", task.exception)
                    // Optionally, you can show an error toast here.
                }
            }
    }

    // THIS FUNCTION IS NOW CLEAN AND ONLY REPORTS A RESULT.
    private fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Email and password cannot be empty.")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LoginDebug", "Email login success. Reporting success to UI.")
                    onResult(true, null)
                } else {
                    Log.w("LoginDebug", "Email login failed", task.exception)
                    onResult(false, task.exception?.message ?: "Login failed.")
                }
            }
    }


    private fun navigateToHome() {
        val user = auth.currentUser
        if (user == null) {
            Log.e("LoginFinalFix", "Critical error: User is null after login.")
            // Still go to login to prevent a crash
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        db.collection("users").document(user.uid).get()
            .addOnSuccessListener { document ->
                // We use the EXACT logic from SplashActivity, including the key "username"
                val fetchedUserName = if (document != null && document.exists()) {
                    document.getString("username") ?: user.displayName ?: "User"
                } else {
                    user.displayName ?: "User"
                }

                Log.d("LoginFinalFix", "Username fetched: $fetchedUserName. Navigating to Home.")

                // Now we navigate with the correctly fetched name
                val intent = Intent(this, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("USER_NAME", fetchedUserName)
                }
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Log.e("LoginFinalFix", "Firestore failed to get username, using fallback.", e)
                val fallbackName = user.displayName ?: "User"
                val intent = Intent(this, HomeActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    putExtra("USER_NAME", fallbackName)
                }
                startActivity(intent)
                finish()
            }
    }
}
