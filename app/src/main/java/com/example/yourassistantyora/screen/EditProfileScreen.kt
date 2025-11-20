package com.example.yourassistantyora.screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navController: NavController,
    currentName: String,
    currentEmail: String,
    currentPhotoUrl: String?,
    onSaveProfile: (newName: String, newPhotoUrl: String?) -> Unit,
    modifier: Modifier = Modifier,
    onCameraClick: () -> Unit = {},
    onGalleryClick: () -> Unit = {}
) {
    var editedUsername by remember { mutableStateOf(currentName) }
    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    var selectedImageBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Profile Picture
    val context = LocalContext.current

    var imageBytesToUpload by remember { mutableStateOf<ByteArray?>(null) }


    // Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, it))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                selectedImageBitmap = bitmap // Update UI
                imageBytesToUpload = compressImage(context, it)
                snackbarHostState.showSnackbar("Image selected!")
            }
        }
    }

    // Camera Launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            selectedImageBitmap = bitmap
            scope.launch {
                val outputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)
                imageBytesToUpload = outputStream.toByteArray()
                snackbarHostState.showSnackbar("Photo taken!")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color.White
    ) { scaffoldPadding ->

        Box(modifier = Modifier.fillMaxSize()) {

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
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFB74D)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedImageBitmap != null) {
                            Image(
                                bitmap = selectedImageBitmap!!.asImageBitmap(),
                                contentDescription = "Selected Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = editedUsername.firstOrNull()?.toString()?.uppercase() ?: "",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(20.dp))

                    // Change Photo Buttons
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
                            // Camera Button UI
                            val cameraInteraction = remember { MutableInteractionSource() }
                            val cameraPressed by cameraInteraction.collectIsPressedAsState()

                            CameraButtonUI(
                                isPressed = cameraPressed,
                                interactionSource = cameraInteraction,
                                onClick = {
                                    cameraLauncher.launch(null)
                                    onCameraClick()
                                }
                            )

                            // Gallery Button UI
                            val galleryInteraction = remember { MutableInteractionSource() }
                            val galleryPressed by galleryInteraction.collectIsPressedAsState()

                            GalleryButtonUI(
                                isPressed = galleryPressed,
                                interactionSource = galleryInteraction,
                                onClick = {
                                    onGalleryClick()
                                    galleryLauncher.launch("image/*")
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Username Field
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

                Spacer(modifier = Modifier.height(24.dp))

                // Email Field (Read Only)
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
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE0E0E0),
                        unfocusedBorderColor = Color(0xFFE0E0E0),
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(48.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = { showCancelConfirmDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF757575))
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    // Save Button
                    Button(
                        onClick = {
                            if (!isSaving) {
                                scope.launch {
                                    isSaving = true
                                    var base64Image: String? = null

                                    // --- NEW BASE64 LOGIC ---
                                    if (imageBytesToUpload != null) {
                                        try {
                                            // Convert the compressed bytes directly to a Base64 String
                                            val base64String = android.util.Base64.encodeToString(
                                                imageBytesToUpload,
                                                android.util.Base64.DEFAULT
                                            )
                                            // We add a prefix so image loaders know how to read it
                                            base64Image = "data:image/jpeg;base64,$base64String"

                                        } catch (e: Exception) {
                                            isSaving = false
                                            snackbarHostState.showSnackbar("Encoding error: ${e.message}")
                                            return@launch
                                        }
                                    }

                                    // Pass the Base64 string instead of a URL
                                    onSaveProfile(editedUsername, base64Image)

                                    isSaving = false
                                    navController.popBackStack()
                                }
                            }
                        },
                        // ... rest of your button modifiers
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A70D7)),
                        enabled = !isSaving
                    ) {
                        // ... content (Text/Progress Indicator) stays the same
                        if (isSaving) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save", fontWeight = FontWeight.SemiBold)
                        }
                    }

                }

                Spacer(modifier = Modifier.height(50.dp))
            }

            if (isSaving) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                        .clickable(enabled = false) {}
                )
            }
        }

        if (showCancelConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showCancelConfirmDialog = false },
                title = { Text("Discard changes?") },
                text = { Text("Are you sure you want to discard your changes?") },
                confirmButton = {
                    TextButton(onClick = {
                        showCancelConfirmDialog = false
                        navController.popBackStack()
                    }) {
                        Text("Discard", color = Color(0xFFE53935))
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

@Composable
fun CameraButtonUI(isPressed: Boolean, interactionSource: MutableInteractionSource, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")
    val elevation by animateDpAsState(targetValue = if (isPressed) 8.dp else 0.dp, label = "elevation")
    val tint by animateColorAsState(targetValue = if (isPressed) Color(0xFF6A70D7) else Color(0xFF2D2D2D), label = "tint")
    val bg = if (isPressed) Color(0xFFEEF2FF) else Color.Transparent
    val border = if (isPressed) Color(0xFF6A70D7) else Color(0xFFE0E0E0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .border(width = 1.dp, color = border, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Filled.CameraAlt, contentDescription = "Camera", tint = tint, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Camera", fontSize = 12.sp, color = Color(0xFF2D2D2D))
            }
        }
    }
}

@Composable
fun GalleryButtonUI(isPressed: Boolean, interactionSource: MutableInteractionSource, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.96f else 1f, label = "scale")
    val elevation by animateDpAsState(targetValue = if (isPressed) 8.dp else 0.dp, label = "elevation")
    val tint by animateColorAsState(targetValue = if (isPressed) Color(0xFFFFB74D) else Color(0xFF2D2D2D), label = "tint")
    val bg = if (isPressed) Color(0xFFFFF6E5) else Color.Transparent
    val border = if (isPressed) Color(0xFFFFB74D) else Color(0xFFE0E0E0)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(bg)
                .border(width = 1.dp, color = border, shape = RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(imageVector = Icons.Filled.Image, contentDescription = "Gallery", tint = tint, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = "Gallery", fontSize = 12.sp, color = Color(0xFF2D2D2D))
            }
        }
    }
}


fun compressImage(context: android.content.Context, uri: Uri): ByteArray {
    val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
    } else {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
    }

    val maxSize = 512
    var width = bitmap.width
    var height = bitmap.height
    val bitmapRatio = width.toFloat() / height.toFloat()

    if (bitmapRatio > 1) {
        width = maxSize
        height = (width / bitmapRatio).toInt()
    } else {
        height = maxSize
        width = (height * bitmapRatio).toInt()
    }
    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, true)
    val outputStream = ByteArrayOutputStream()
    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream)

    return outputStream.toByteArray()
}

fun decodeBase64ToBitmap(base64Str: String): Bitmap? {
    try {
        // Remove the prefix if present
        val pureBase64 = base64Str.substringAfter(",")
        val decodedBytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    } catch (e: Exception) {
        return null
    }
}
