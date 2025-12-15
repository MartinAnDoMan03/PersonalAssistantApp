package com.example.yourassistantyora.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

enum class VoiceInputState {
    IDLE, READY_TO_RECORD, LISTENING, PROCESSING, TRANSCRIBED
}

val PrimaryPurple = Color(0xFF8B5CF6)
val SoftPurple = Color(0xFFEFEFF9)
val SecondaryPink = Color(0xFFEC4899)
val RedRecording = Color(0xFFEF4444)

val GradientBrush = Brush.horizontalGradient(
    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateNoteScreen(
    onBackClick: () -> Unit = {},
    onSaveClick: (String, String, List<String>) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(listOf<String>()) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    var voiceInputState by remember { mutableStateOf(VoiceInputState.IDLE) }
    var transcribedText by remember { mutableStateOf("") }

    val firestore = remember { FirebaseFirestore.getInstance() }
    val userId = remember { FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous_user" }

    val predefinedCategories = listOf(
        "Work" to Color(0xFF667EEA),
        "Study" to Color(0xFF64B5F6),
        "Travel" to Color(0xFF4DB6AC),
        "Meeting" to Color(0xFF9575CD),
        "Project" to Color(0xFFEF5350)
    )

    var customCategories by remember { mutableStateOf(listOf<Pair<String, Color>>()) }
    val allCategories = predefinedCategories + customCategories

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = Color(0xFFF8F9FA),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "New Note",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Close",
                                tint = Color(0xFF9E9E9E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFF8F9FA)
                    ),
                    actions = {
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                        .clickable {
                            if (voiceInputState == VoiceInputState.IDLE) {
                                voiceInputState = VoiceInputState.READY_TO_RECORD
                                transcribedText = ""
                            }
                        },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice to Note",
                                tint = PrimaryPurple,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SoftPurple)
                                    .padding(6.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Voice to Note",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2D2D2D)
                                )
                                Text(
                                    "Speak and I'll write for you",
                                    fontSize = 13.sp,
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                        Button(
                            onClick = {
                                if (voiceInputState == VoiceInputState.IDLE) {
                                    voiceInputState = VoiceInputState.READY_TO_RECORD
                                    transcribedText = ""
                                }
                            },
                            modifier = Modifier.height(36.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFF3E8FF)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text(
                                "Start",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PrimaryPurple
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 0.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        placeholder = {
                            Text(
                                "Title",
                                color = Color(0xFFBDBDBD),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = PrimaryPurple
                        ),
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        ),
                        singleLine = true
                    )

                    Spacer(Modifier.height(4.dp))

                    Text(
                        text = remember {
                            SimpleDateFormat("d MMMM  HH:mm", Locale.getDefault()).format(Date())
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFFBDBDBD),
                        modifier = Modifier.padding(start = 16.dp)
                    )

                    Spacer(Modifier.height(16.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextField(
                            value = content,
                            onValueChange = { content = it },
                            placeholder = {
                                Text(
                                    "Start typing or use voice input...",
                                    color = Color(0xFFBDBDBD),
                                    fontSize = 15.sp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 250.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                cursorColor = PrimaryPurple
                            ),
                            textStyle = LocalTextStyle.current.copy(
                                fontSize = 15.sp,
                                color = Color(0xFF2D2D2D),
                                lineHeight = 22.sp
                            ),
                            maxLines = Int.MAX_VALUE
                        )

                        FloatingActionButton(
                            onClick = {
                                if (voiceInputState == VoiceInputState.IDLE) {
                                    voiceInputState = VoiceInputState.READY_TO_RECORD
                                    transcribedText = ""
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(end = 12.dp, bottom = 12.dp)
                                .size(56.dp),
                            shape = CircleShape,
                            containerColor = Color.Transparent,
                            contentColor = Color.White,
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GradientBrush, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Category",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF9E9E9E)
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allCategories.forEach { (category, color) ->
                                CreateCategoryChip(
                                    text = category,
                                    isSelected = selectedCategories.contains(category),
                                    color = color,
                                    onClick = {
                                        selectedCategories = if (selectedCategories.contains(category)) {
                                            selectedCategories - category
                                        } else {
                                            selectedCategories + category
                                        }
                                    }
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable { showAddCategoryDialog = true },
                                color = Color.Transparent,
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = "Add Category",
                                        tint = PrimaryPurple,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        "Add New",
                                        color = PrimaryPurple,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(30.dp))
                }

                Button(
                    onClick = {
                        if (title.isNotEmpty() && content.isNotEmpty() && selectedCategories.isNotEmpty()) {
                            val noteData = hashMapOf(
                                "title" to title,
                                "note" to content,
                                "categories" to selectedCategories,
                                "user_id" to userId,
                                "created_at" to FieldValue.serverTimestamp()
                            )
                            firestore.collection("notes").add(noteData)
                                .addOnSuccessListener {
                                    onSaveClick(title, content, selectedCategories)
                                }
                                .addOnFailureListener { e ->
                                    e.printStackTrace()
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .height(48.dp),
                    enabled = title.isNotEmpty() && content.isNotEmpty() && selectedCategories.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryPurple,
                        disabledContainerColor = Color(0xFFE0E0E0)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "Save",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
            }
        }

        val isVoiceOverlayVisible = voiceInputState != VoiceInputState.IDLE

        AnimatedVisibility(
            visible = isVoiceOverlayVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false) { }
            )
        }

        VoiceInputOverlay(
            voiceInputState = voiceInputState,
            transcribedText = transcribedText,
            onClose = { voiceInputState = VoiceInputState.IDLE },
            onStartRecording = {
                voiceInputState = VoiceInputState.LISTENING
                transcribedText = ""
            },
            onStopRecording = { voiceInputState = VoiceInputState.PROCESSING },
            onRecordingDone = {
                voiceInputState = VoiceInputState.TRANSCRIBED
                transcribedText = "Review design mockups for the new login screen by tomorrow"
            },
            onUseThis = {
                content = if (content.isEmpty()) transcribedText else content + "\n" + transcribedText
                voiceInputState = VoiceInputState.IDLE
            },
            onRerecord = {
                voiceInputState = VoiceInputState.READY_TO_RECORD
                transcribedText = ""
            }
        )
    }
}

@Composable
fun BoxScope.VoiceInputOverlay(
    voiceInputState: VoiceInputState,
    transcribedText: String,
    onClose: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onRecordingDone: () -> Unit,
    onUseThis: () -> Unit,
    onRerecord: () -> Unit
) {
    AnimatedVisibility(
        visible = voiceInputState != VoiceInputState.IDLE,
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn() + scaleIn(initialScale = 0.9f),
        exit = fadeOut() + scaleOut(targetScale = 0.9f)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val iconColor = when (voiceInputState) {
                        VoiceInputState.LISTENING -> RedRecording
                        else -> PrimaryPurple
                    }
                    val iconBackground = when (voiceInputState) {
                        VoiceInputState.LISTENING -> Color(0xFFFFEBEE)
                        else -> Color(0xFFF3E8FF)
                    }
                    val statusText = when (voiceInputState) {
                        VoiceInputState.READY_TO_RECORD -> "Ready to record"
                        VoiceInputState.LISTENING -> "Listening..."
                        VoiceInputState.PROCESSING -> "Processing..."
                        VoiceInputState.TRANSCRIBED -> "Ready to record"
                        else -> "Voice Input"
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(iconBackground),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = iconColor,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Voice Input",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2D2D2D)
                            )
                            Text(
                                statusText,
                                fontSize = 13.sp,
                                color = Color(0xFF9E9E9E)
                            )
                        }
                    }
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF9E9E9E)
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                when (voiceInputState) {
                    VoiceInputState.READY_TO_RECORD -> {
                        TipBox(
                            "Tip: Speak clearly and mention priority, deadline, or assignees for better AI suggestions",
                            Color(0xFFEEF2FF),
                            Color(0xFF6366F1)
                        )
                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = onStartRecording,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(0.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(GradientBrush, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Start Recording",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Start Recording",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp
                                    )
                                }
                            }
                        }
                    }

                    VoiceInputState.LISTENING -> {
                        TipBox(
                            "Tip: Speak clearly and mention priority, deadline, or assignees for better AI suggestions",
                            Color(0xFFEEF2FF),
                            Color(0xFF6366F1)
                        )
                        Spacer(Modifier.height(20.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            WaveVisualizerPlaceholder()
                        }
                        Spacer(Modifier.height(20.dp))

                        Button(
                            onClick = onStopRecording,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = RedRecording
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Stop,
                                    contentDescription = "Stop Recording",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Stop Recording",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                            }
                        }
                    }

                    VoiceInputState.PROCESSING -> {
                        LaunchedEffect(Unit) {
                            delay(2000L)
                            onRecordingDone()
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = PrimaryPurple,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Yora is processing your voice...",
                                color = Color(0xFF2D2D2D),
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(Modifier.height(16.dp))
                        TipBox(
                            "Tip: Speak clearly and mention priority, deadline, or assignees for better AI suggestions",
                            Color(0xFFEEF2FF),
                            Color(0xFF6366F1)
                        )
                    }

                    VoiceInputState.TRANSCRIBED -> {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF0FDF4)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Transcribed",
                                        tint = Color(0xFF22C55E),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Transcribed",
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF22C55E),
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    transcribedText,
                                    color = Color(0xFF374151),
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        TipBox(
                            "Tip: Speak clearly and mention priority, deadline, or assignees for better AI suggestions",
                            Color(0xFFEEF2FF),
                            Color(0xFF6366F1)
                        )
                        Spacer(Modifier.height(20.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = onRerecord,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFF3F4F6)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    "Re-record",
                                    color = Color(0xFF6B7280),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = onUseThis,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(0.dp),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(GradientBrush, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "Use This",
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}

@Composable
fun TipBox(text: String, backgroundColor: Color, contentColor: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = backgroundColor
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(12.dp),
            color = contentColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Normal,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun WaveVisualizerPlaceholder() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val heights = listOf(12, 20, 28, 36, 40, 36, 28, 20, 28, 36, 40, 36, 28, 20, 12)

        heights.forEachIndexed { index, height ->
            val color = if (index % 2 == 0) Color(0xFF8B5CF6) else Color(0xFFEC4899)
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(height.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(color)
            )
            if (index < heights.size - 1) {
                Spacer(Modifier.width(4.dp))
            }
        }
    }
}

@Composable
fun CreateCategoryChip(
    text: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        color = if (isSelected) color else color.copy(alpha = 0.15f),
        onClick = onClick,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else color,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}