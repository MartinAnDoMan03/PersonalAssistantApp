package com.example.yourassistantyora.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import com.example.yourassistantyora.ui.theme.YourAssistantYoraTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    currentName: String,
    currentEmail: String,
    onSaveProfile: (String) -> Unit,
    isSaving: Boolean = false,
    errorMessage: String? = null,
    onErrorShown: () -> Unit = {},
    modifier: Modifier = Modifier,
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {}
) {
    var editedName by remember { mutableStateOf(currentName) }
    var editedUsername by remember { mutableStateOf(currentName) }

    var showCancelConfirmDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.White
    ) { scaffoldPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .background(Color.White)
        ) {
            // Top bar
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { showCancelConfirmDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xFF2D2D2D)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = "Edit Profile",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2D2D)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Update your personal information and preferences",
                fontSize = 14.sp,
                color = Color(0xFF9E9E9E)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Profile Picture Section
            Text(
                text = "Profile Picture",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D2D2D),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFB74D)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = editedName.firstOrNull()?.toString() ?: "",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // Change Photo Buttons (Camera + Gallery)
                Column {
                    Text(
                        text = "Change Photo",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF2D2D2D),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // --- CAMERA BUTTON ---
                        val cameraInteraction = remember { MutableInteractionSource() }
                        val cameraPressed by cameraInteraction.collectIsPressedAsState()
                        val cameraScale by animateFloatAsState(
                            targetValue = if (cameraPressed) 0.96f else 1f,
                            animationSpec = tween(durationMillis = 120)
                        )
                        val cameraElevation by animateDpAsState(
                            targetValue = if (cameraPressed) 8.dp else 0.dp,
                            animationSpec = tween(durationMillis = 120)
                        )
                        val cameraIconTint by animateColorAsState(
                            targetValue = if (cameraPressed) Color(0xFF6A70D7) else Color(0xFF2D2D2D),
                            animationSpec = tween(durationMillis = 120)
                        )
                        val cameraBg = if (cameraPressed) Color(0xFFEEF2FF) else Color.Transparent
                        val cameraBorder = if (cameraPressed) Color(0xFF6A70D7) else Color(0xFFE0E0E0)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .scale(cameraScale)
                                .shadow(cameraElevation, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = cameraInteraction,
                                    indication = null
                                ) {
                                    onCameraClick()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Opening Camera...")
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(cameraBg)
                                    .border(
                                        width = 1.dp,
                                        color = cameraBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CameraAlt,
                                        contentDescription = "Camera",
                                        tint = cameraIconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Camera",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2D2D2D)
                                    )
                                }
                            }
                        }

                        // --- GALLERY BUTTON ---
                        val galleryInteraction = remember { MutableInteractionSource() }
                        val galleryPressed by galleryInteraction.collectIsPressedAsState()
                        val galleryScale by animateFloatAsState(
                            targetValue = if (galleryPressed) 0.96f else 1f,
                            animationSpec = tween(durationMillis = 120)
                        )
                        val galleryElevation by animateDpAsState(
                            targetValue = if (galleryPressed) 8.dp else 0.dp,
                            animationSpec = tween(durationMillis = 120)
                        )
                        val galleryIconTint by animateColorAsState(
                            targetValue = if (galleryPressed) Color(0xFFFFB74D) else Color(0xFF2D2D2D),
                            animationSpec = tween(durationMillis = 120)
                        )
                        val galleryBg = if (galleryPressed) Color(0xFFFFF6E5) else Color.Transparent
                        val galleryBorder = if (galleryPressed) Color(0xFFFFB74D) else Color(0xFFE0E0E0)

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .scale(galleryScale)
                                .shadow(galleryElevation, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(
                                    interactionSource = galleryInteraction,
                                    indication = null
                                ) {
                                    onGalleryClick()
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Opening Gallery...")
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(galleryBg)
                                    .border(
                                        width = 1.dp,
                                        color = galleryBorder,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Image,
                                        contentDescription = "Gallery",
                                        tint = galleryIconTint,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Gallery",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2D2D2D)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Username
            Text(
                text = "Username",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D2D2D),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = editedUsername,
                onValueChange = { editedUsername = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF6A70D7),
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color(0xFFFAFAFA),
                    unfocusedContainerColor = Color(0xFFFAFAFA)
                ),
                singleLine = true
            )

            Text(
                text = "This is how others will see you",
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Email Address
            Text(
                text = "Email Address",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF2D2D2D),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            OutlinedTextField(
                value = currentEmail,
                onValueChange = { },
                enabled = false,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color(0xFF9E9E9E),
                    disabledBorderColor = Color(0xFFE0E0E0),
                    disabledContainerColor = Color(0xFFFAFAFA)
                ),
                singleLine = true
            )

            Text(
                text = "Contact support to change email",
                fontSize = 11.sp,
                color = Color(0xFF9E9E9E),
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button
                val cancelInteraction = remember { MutableInteractionSource() }
                val cancelPressed by cancelInteraction.collectIsPressedAsState()
                val cancelScale by animateFloatAsState(
                    targetValue = if (cancelPressed) 0.98f else 1f,
                    animationSpec = tween(durationMillis = 120)
                )

                val cancelBg = if (cancelPressed) Color(0xFFFFEBEE) else Color.Transparent
                val cancelBorder = Color(0xFFE53935)

                OutlinedButton(
                    onClick = { showCancelConfirmDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .scale(cancelScale),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = cancelBg,
                        contentColor = Color(0xFFE53935)
                    ),
                    border = BorderStroke(1.dp, cancelBorder),
                    interactionSource = cancelInteraction
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Save Button
                val saveInteraction = remember { MutableInteractionSource() }
                val savePressed by saveInteraction.collectIsPressedAsState()
                val saveScale by animateFloatAsState(
                    targetValue = if (savePressed) 0.98f else 1f,
                    animationSpec = tween(durationMillis = 120)
                )

                val saveBg = if (savePressed) Color(0xFF5B61D0) else Color(0xFF6A70D7)

                Button(
                    onClick = {
                        if (!isSaving) {             // ⬅️ cegah user tap berkali-kali
                            onSaveProfile(editedUsername)
                            navController.popBackStack()
                        }
                    },
                    enabled = !isSaving,            // ⬅️ disable visual saat saving
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .scale(saveScale),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSaving) Color(0xFF9FA5E2) else saveBg,
                        contentColor = Color.White
                    ),
                    interactionSource = saveInteraction
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Save",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Cancel confirmation dialog
        if (showCancelConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showCancelConfirmDialog = false },
                title = { Text("Discard changes?") },
                text = { Text("Are you sure you want to discard your changes and go back?") },
                confirmButton = {
                    TextButton(onClick = {
                        showCancelConfirmDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Yes, discard", color = Color(0xFFE53935))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelConfirmDialog = false }) {
                        Text("Keep editing")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
private fun EditProfileScreenPreview() {
    YourAssistantYoraTheme {
        val navController = rememberNavController()
        EditProfileScreen(
            navController = navController,
            currentName = "Tom Holland",
            currentEmail = "tomholland@gmail.com",
            onSaveProfile = {}
        )
    }
}
