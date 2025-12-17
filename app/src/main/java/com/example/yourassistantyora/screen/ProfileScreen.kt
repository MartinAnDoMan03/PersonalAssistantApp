package com.example.yourassistantyora.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.yourassistantyora.navigateSingleTop
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    userName: String,
    userEmail: String,
    userPhotoUrl: String? = null,
    totalTasks: Int,
    completedTasks: Int,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    var currentName by remember { mutableStateOf(userName) }
    var currentPhotoUrl by remember { mutableStateOf(userPhotoUrl) }

    var realTotalTasks by remember { mutableIntStateOf(0) }
    var realCompletedTasks by remember { mutableIntStateOf(0) }

    val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
    val currentUser = auth.currentUser


    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null) {
            // Listen for User Profile Updates
            db.collection("users").document(currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        currentName = snapshot.getString("username") ?: "User"
                        currentPhotoUrl = snapshot.getString("photoUrl")
                    }
                }

            // --- COMBINED TASK COUNTING ---
            var personalTotal = 0
            var personalCompleted = 0
            var teamTotal = 0
            var teamCompleted = 0

            fun updateStats() {
                realTotalTasks = personalTotal + teamTotal
                realCompletedTasks = personalCompleted + teamCompleted
            }

            // Listen for PERSONAL Tasks
            db.collection("tasks")
                .whereEqualTo("userId", currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val docs = snapshot.documents
                        personalTotal = docs.size
                        personalCompleted = docs.count {
                            // Check both Status (Capital) and status (lowercase)
                            val statusVal = it.get("Status") ?: it.get("status")
                            val statusInt = when (statusVal) {
                                is Number -> statusVal.toInt()
                                is String -> statusVal.toIntOrNull() ?: 0
                                else -> 0
                            }
                            statusInt == 2
                        }
                        updateStats()
                    }
                }

            // Listen for TEAM Tasks
            db.collection("team_tasks")
                .whereArrayContains("assignees", currentUser.uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        val docs = snapshot.documents
                        teamTotal = docs.size
                        teamCompleted = docs.count {
                            val statusVal = it.get("Status") ?: it.get("status")

                            val statusInt = when (statusVal) {
                                is Number -> statusVal.toInt()
                                is String -> statusVal.toIntOrNull() ?: 0
                                else -> 0
                            }
                            statusInt == 2
                        }
                        updateStats()
                    }
                }
        }
    }



    Scaffold(
        containerColor = Color(0xFFF5F7FA),
        topBar = {
            CenterAlignedTopAppBar(
                windowInsets = WindowInsets.statusBars,
                title = {
                    Text(
                        "Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6A70D7)
                )
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
                        )
                    )
                    .padding(vertical = 24.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB74D)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!currentPhotoUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = currentPhotoUrl,
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Text(
                                text = if (currentName.isNotEmpty()) currentName.take(1).uppercase() else "U",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = currentName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                }
            }

            // 🔹 CARD STATISTIK
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-20).dp)
                    .padding(horizontal = 20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = realTotalTasks.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D2D2D)
                                )
                                Text("Task (Personal)", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                            }

                            Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFFE0E0E0)))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = realCompletedTasks.toString(),
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2D2D2D)
                                )
                                Text("Completed (Personal)", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(
                            onClick = { navController.navigateSingleTop("edit_profile") },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Filled.Edit, null, tint = Color(0xFF6A70D7), modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile", color = Color(0xFF6A70D7), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // 🔹 DATA DIRI SECTION
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .heightIn(min = 400.dp)
            ) {
                Text(text = "Username", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = currentName,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color(0xFFE0E0E0),
                        disabledContainerColor = Color(0xFFF9F9F9)
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Email Address", fontSize = 13.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                OutlinedTextField(
                    value = userEmail,
                    onValueChange = {},
                    enabled = false,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = Color.Black,
                        disabledBorderColor = Color(0xFFE0E0E0),
                        disabledContainerColor = Color(0xFFF9F9F9)
                    )
                )
                Text("Email cannot be changed", fontSize = 12.sp, color = Color(0xFF9E9E9E), modifier = Modifier.padding(top = 6.dp))

                Spacer(modifier = Modifier.height(60.dp))

                // 🔹 TOMBOL LOG OUT
                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, Color(0xFFE53935)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                ) {
                    Text("Log Out", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }

    // 🔹 POPUP LOGOUT
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Yes, Log Out", color = Color(0xFFE53935), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel", color = Color(0xFF6A70D7))
                }
            }
        )
    }
}