package com.example.yourassistantyora

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userName: String = "Tom Holland",
    userEmail: String = "tomholland@gmail.com",
    totalTasks: Int = 10,
    completedTasks: Int = 6,
    onBackClick: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // 🔹 HEADER (kotak ungu) — versi lebih ramping
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
                    )
                )
                .padding(bottom = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 12.dp)
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Avatar
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB74D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.firstOrNull()?.toString() ?: "",
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = userName,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 🔹 CARD PUTIH (Dikecilkan)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset(y = (-20).dp)
                .padding(horizontal = 20.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp), // 🔹 batasi tinggi minimal
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 16.dp) // 🔹 lebih kecil dari sebelumnya
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = totalTasks.toString(),
                                fontSize = 18.sp, // 🔹 sebelumnya 22.sp
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D2D2D)
                            )
                            Text("Task", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(28.dp) // 🔹 lebih pendek
                                .background(Color(0xFFE0E0E0))
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = completedTasks.toString(),
                                fontSize = 18.sp, // 🔹 sebelumnya 22.sp
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2D2D2D)
                            )
                            Text("Completed", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 🔹 Tombol Edit Profile — lebih kecil
                    TextButton(
                        onClick = onEditProfile,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .height(32.dp) // 🔹 lebih pendek dari sebelumnya
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF6A70D7),
                            modifier = Modifier.size(14.dp) // 🔹 kecilkan ikon
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Edit Profile",
                            color = Color(0xFF6A70D7),
                            fontSize = 13.sp, // 🔹 kecilkan teks
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // 🔹 FORM DATA DIRI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp)
        ) {
            Text(
                text = "Full Name",
                fontSize = 13.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = userName,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Username",
                fontSize = 13.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = userName,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Email Address",
                fontSize = 13.sp,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = userEmail,
                onValueChange = {},
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.Black,
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledContainerColor = Color.White
                )
            )

            Text(
                text = "Email cannot be changed",
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 🔹 Tombol Log Out
            OutlinedButton(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp), // 🔹 sedikit lebih kecil
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE53935)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color(0xFFE53935)
                )
            ) {
                Text(
                    text = "Log Out",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // 🔹 Popup Konfirmasi Logout
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
            text = { Text("Are you sure you want to log out?", fontSize = 14.sp, color = Color(0xFF444444)) },
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

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun ProfileScreenPreview() {
    YourAssistantYoraTheme {
        ProfileScreen()
    }
}
