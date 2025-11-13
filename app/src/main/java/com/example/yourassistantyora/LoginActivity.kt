package com.example.yourassistantyora

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.registerForActivityResult

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var db: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        // ✅ Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


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
                    onLogin = { email, password, onResult ->
                        loginUser(email, password, onResult)
                    }
                )
            }
        }
    }

    // 🔹 Google Sign-In launcher
    private val googleSignInLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) { // ✅ Fixed Unresolved reference 'Activity'
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

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LoginDebug", "Google sign-in successful: ${auth.currentUser?.email}")

                    //Check if user exists
                    val firebaseUser = auth.currentUser
                    if (firebaseUser != null) {
                        checkAndCreateUserProfile(firebaseUser.uid, account.displayName ?: "New User", firebaseUser.email!!)
                    } else {
                        Log.e("LoginDebug", "Firebase user is null after Google Sign-in.")
                        navigateToHome("User") // Fallback
                    }

                }  else {
                    Log.e("LoginDebug", "Google sign-in failed", task.exception)
                }
            }
    }

    private fun checkAndCreateUserProfile(userId: String, displayName: String, email: String) {
        val userDocRef = db.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                // User document already exists
                Log.d("Firestore", "User profile already exists for $userId. Navigating.")
                val username = document.getString("username") ?: displayName
                navigateToHome(username)
            } else {
                // Create new profile
                Log.d("Firestore", "User profile not found for $userId. Creating new profile.")
                val newUser = hashMapOf(
                    "username" to displayName,
                    "email" to email,
                    // "createdAt" or timestamp if needed
                )

                userDocRef.set(newUser)
                    .addOnSuccessListener {
                        Log.d("Firestore", "Successfully created user profile for $userId.")
                        navigateToHome(displayName)
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firestore", "Error creating user profile for $userId", e)
                        navigateToHome(displayName) // Navigate anyway with the best name we have
                    }
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error checking for user profile", e)
            navigateToHome(displayName) // Navigate with a fallback on error
        }
    }

    // 🔹 Email/Password login
    private fun loginUser(email: String, password: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            onResult(false, "Please enter email and password")
            return
        }

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("LoginDebug", "Email login success: ${auth.currentUser?.email}")
                    fetchUserAndNavigate()
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message ?: "Login failed")
                }
            }
    }
    private fun fetchUserAndNavigate() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.e("Firestore", "Cannot fetch user data, user is not logged in.")
            navigateToHome("Unknown User")
            return
        }

        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    // Get username field
                    val username = document.getString("username") ?: "User"
                    Log.d("Firestore", "Username fetched successfully: $username")
                    navigateToHome(username)
                } else {
                    // Display name in case of failure
                    Log.d("Firestore", "User document not found, using display name.")
                    val fallbackName = auth.currentUser?.displayName ?: "New User"
                    navigateToHome(fallbackName)
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error fetching user data", e)
                navigateToHome("User")
            }
    }

    private fun navigateToHome(userName: String) {
        startActivity(Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            putExtra("USER_NAME", userName) // 👈 Pass the correct username here
        })
        finish()
    }

}
