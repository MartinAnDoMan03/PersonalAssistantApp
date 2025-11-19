package com.example.yourassistantyora.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme

// --- Gradient Button Composable Helper (Diperbarui untuk Card Elevation) ---
@Composable
fun GradientElevatedButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val gradient = Brush.horizontalGradient(
        listOf(Color(0xFF6A70D7), Color(0xFFC870D7)) // Gradient Ungu ke Pink/Violet
    )

    Card(
        modifier = modifier.height(50.dp), // Height dari Button
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // Shadow untuk Card
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Card transparan untuk gradient di Button
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(), // Button mengisi Card
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent, // Container harus transparan agar gradient terlihat
                disabledContainerColor = Color(0xFFE0E0E0), // Warna solid saat disabled
                contentColor = if (enabled) Color.White else Color(0xFF9E9E9E), // Warna teks/ikon
                disabledContentColor = Color(0xFF9E9E9E)
            ),
            contentPadding = PaddingValues(0.dp), // Hapus padding default Button
            shape = RoundedCornerShape(12.dp) // Shape Button sesuai Card
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = if (enabled) gradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))), // Gradient di Box
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    content = content
                )
            }
        }
    }
}
// ------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTeamScreen(
    onBackClick: () -> Unit = {},
    onJoinClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var inviteCode by remember { mutableStateOf("") }
    val isValidCode = inviteCode.length == 6

    Scaffold(
        containerColor = Color(0xFFF8F9FA)
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Header (Tidak berubah banyak)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .heightIn(min = 56.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF2D2D2D)
                        )
                    }
                    Text(
                        "Join Team",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2D2D)
                    )
                    Spacer(Modifier.width(48.dp))
                }
            }
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(Modifier.height(24.dp))

                // Icon Box
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Groups,
                            contentDescription = null,
                            tint = Color(0xFF6A70D7),
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(0xFFE8EAFD), RoundedCornerShape(12.dp))
                                .padding(8.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Instructions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8EAFD)
                    ),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "How to Join",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF4A7CFF)
                        )
                        Text(
                            "Enter the 6-digit invite code shared by your team admin to join instantly.",
                            fontSize = 13.sp,
                            color = Color(0xFF4A7CFF).copy(alpha = 0.8f),
                            lineHeight = 18.sp
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))

                // Invite Code Input
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 16.dp, horizontal = 16.dp)
                    ) {
                        Text(
                            "Enter Invite Code",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2D2D2D)
                        )
                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = inviteCode,
                            onValueChange = {
                                if (it.length <= 6) {
                                    inviteCode = it.uppercase()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(
                                    "ABC123",
                                    fontSize = 18.sp,
                                    color = Color(0xFFBDBDBD),
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = Color(0xFF6A70D7),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                letterSpacing = 8.sp
                            ),
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Characters
                            )
                        )
                        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "6-digit code",
                            fontSize = 11.sp,
                            color = Color(0xFF9E9E9E),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Join Button (Fixed at bottom) - MENGGUNAKAN GradientElevatedButton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 24.dp)
            ) {
                GradientElevatedButton(
                    onClick = { onJoinClick(inviteCode) },
                    enabled = isValidCode,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Join Team",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun JoinTeamScreenPreview() {
    YourAssistantYoraTheme {
        JoinTeamScreen()
    }
}