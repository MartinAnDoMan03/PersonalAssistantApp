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

@Composable
fun GradientElevatedButton(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    // Warna gradient sesuai permintaan Anda
    val startColor = Color(0xFF6A70D7)
    val endColor = Color(0xFF7353AD)

    val gradient = Brush.horizontalGradient(
        listOf(startColor, endColor)
    )

    Card(
        modifier = modifier.height(50.dp),
        shape = RoundedCornerShape(12.dp),
        // Elevation dikurangi saat disabled agar tidak terlihat "melayang"
        elevation = CardDefaults.cardElevation(defaultElevation = if (enabled) 4.dp else 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                // Penting: Set disabledContainer tetap transparan agar background Box terlihat
                disabledContainerColor = Color.Transparent,
                contentColor = Color.White,
                disabledContentColor = Color.White.copy(alpha = 0.6f)
            ),
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    // Background selalu ada, tapi gunakan alpha 0.5f saat disabled
                    .background(
                        brush = gradient,
                        alpha = if (enabled) 1f else 0.5f
                    ),
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinTeamScreen(
    onBackClick: () -> Unit = {},
    viewModel: com.example.yourassistantyora.viewModel.TeamViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    onJoinClick: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var inviteCode by remember { mutableStateOf("") }
    val isValidCode = inviteCode.length == 6

    val isLoading by viewModel.isLoading
    val isSuccess by viewModel.isSuccess
    val errorMessage by viewModel.errorMessage

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            android.widget.Toast.makeText(context, "Joined successfully!", android.widget.Toast.LENGTH_SHORT).show()
            viewModel.resetState()
            onBackClick()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            android.widget.Toast.makeText(context, it, android.widget.Toast.LENGTH_SHORT).show()
        }
    }

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
                    onClick = { viewModel.joinTeam(inviteCode) },
                    enabled = isValidCode && !isLoading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            "Join Team",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
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