package com.example.yourassistantyora.screen

import android.Manifest
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.models.TaskPriority
import com.example.yourassistantyora.models.TeamMember
import com.example.yourassistantyora.viewModel.CreateTeamTaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTeamTaskScreen(
    navController: NavController,
    teamId: String,
    createTaskViewModel: CreateTeamTaskViewModel = viewModel()
) {
    val context = LocalContext.current

    // === STATE DARI VIEWMODEL ===
    val title by createTaskViewModel.title
    val description by createTaskViewModel.description
    val selectedPriority by createTaskViewModel.selectedPriority
    val attachments by createTaskViewModel.attachments.collectAsState()
    val selectedDate by createTaskViewModel.selectedDate
    val selectedTime by createTaskViewModel.selectedTime
    val isLoading by createTaskViewModel.isLoading.collectAsState()
    val isSuccess by createTaskViewModel.isSuccess.collectAsState()
    val error by createTaskViewModel.error.collectAsState()
    val teamMembers by createTaskViewModel.teamMembers.collectAsState()

    // ✅ === PERBAIKAN LOGIKA IZIN LENGKAP ===
    val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
    } else {
        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { createTaskViewModel.addAttachment(it, context) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions: Map<String, Boolean> ->
        if (permissions.values.any { it }) {
            filePickerLauncher.launch("*/*")
        } else {
            Toast.makeText(context, "Storage permission is required to attach files.", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(key1 = teamId) {
        createTaskViewModel.loadTeamMembers(teamId)
    }

    // --- Helper dan event handler lainnya ---
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.ENGLISH) }
    val formattedDate = selectedDate?.let { dateFormatter.format(it) } ?: "Select Date"
    val formattedTime = selectedTime?.let { timeFormatter.format(it.time) } ?: "Select Time"
    val teamColor = Color(0xFF6A70D7)

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            Toast.makeText(context, "Task created successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    }
    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            createTaskViewModel.clearError()
        }
    }

    // --- STATE LOKAL UNTUK KONTROL DIALOG ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.time ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        createTaskViewModel.selectedDate.value = Date(it)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
            initialMinute = selectedTime?.get(Calendar.MINUTE) ?: Calendar.getInstance().get(Calendar.MINUTE)
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    createTaskViewModel.selectedTime.value = cal
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("New Task", fontSize = 18.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CARD TASK DETAILS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Task Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { createTaskViewModel.title.value = it },
                            placeholder = { Text("Task Title") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = teamColor
                            )
                        )
                        HorizontalDivider()
                        OutlinedTextField(
                            value = description,
                            onValueChange = { createTaskViewModel.description.value = it },
                            placeholder = { Text("Task Description") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp),
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                cursorColor = teamColor
                            )
                        )
                    }
                }

                // CARD ASSIGN MEMBERS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Assign to Member",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (teamMembers.isEmpty() && isLoading) {
                            Text("Loading team members...", color = Color.Gray)
                        } else {
                            val assignedMemberId by createTaskViewModel.assignedMemberId.collectAsState()

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                teamMembers.forEach { member ->
                                    MemberSelectionCard(
                                        member = member,
                                        isSelected = assignedMemberId == member.id,
                                        onClick = { createTaskViewModel.onMemberSelected(member.id) }
                                    )
                                }
                            }
                        }
                    }
                }

                // CARD PRIORITY
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Priority", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            TaskPriority.values().forEach { priority ->
                                PriorityChip(
                                    priority = priority,
                                    isSelected = selectedPriority == priority.ordinal,
                                    onClick = {
                                        createTaskViewModel.selectedPriority.value = priority.ordinal
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // CARD ATTACHMENTS
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Attachments", fontWeight = FontWeight.SemiBold)
                            IconButton(onClick = {
                                permissionLauncher.launch(permissionsToRequest)
                            }) {
                                Icon(Icons.Default.AttachFile, null, tint = teamColor)
                            }
                        }

                        if (attachments.isNotEmpty()) {
                            FlowRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                attachments.forEach { uri ->
                                    AttachmentChip(
                                        uri = uri,
                                        onRemove = { createTaskViewModel.removeAttachment(uri) }
                                    )
                                }
                            }
                        } else {
                            Text(
                                "No files attached. (Max 10MB)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }

                // CARD SCHEDULE
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Schedule", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            FormInputChip(
                                text = formattedDate,
                                icon = Icons.Filled.CalendarToday,
                                onClick = { showDatePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                            FormInputChip(
                                text = formattedTime,
                                icon = Icons.Filled.AccessTime,
                                onClick = { showTimePicker = true },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // === ASSIGN BUTTON WITH GRADIENT & SHAKE EFFECT ===
            var shouldShake by remember { mutableStateOf(false) }
            val shakeOffset = remember {
                androidx.compose.animation.core.Animatable(0f)
            }

            LaunchedEffect(shouldShake) {
                if (shouldShake) {
                    // Shake animation: left-right-left-right
                    repeat(3) {
                        shakeOffset.animateTo(
                            targetValue = 10f,
                            animationSpec = androidx.compose.animation.core.tween(50)
                        )
                        shakeOffset.animateTo(
                            targetValue = -10f,
                            animationSpec = androidx.compose.animation.core.tween(50)
                        )
                    }
                    shakeOffset.animateTo(
                        targetValue = 0f,
                        animationSpec = androidx.compose.animation.core.tween(50)
                    )
                    shouldShake = false
                }
            }

            Button(
                onClick = {
                    if (createTaskViewModel.isFormValid() && !isLoading) {
                        createTaskViewModel.createTask(teamId, context)
                    } else if (!isLoading) {
                        shouldShake = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(50.dp)
                    .offset(x = shakeOffset.value.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF6A70D7), Color(0xFF7353AD))
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = true
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Assign",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun MemberSelectionCard(member: TeamMember, isSelected: Boolean, onClick: () -> Unit) {
    val teamColor = Color(0xFF6A70D7)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) teamColor.copy(alpha = 0.1f) else Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(teamColor.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        member.name.take(2).uppercase(),
                        color = teamColor,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(member.name, fontWeight = FontWeight.Medium)
                    Text(
                        "${member.tasksCompleted} completed tasks",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = "Select member",
                tint = if (isSelected) teamColor else Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PriorityChip(
    priority: TaskPriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        border = if (isSelected) BorderStroke(2.dp, priority.color) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) priority.bgColor else Color(0xFFF8F9FA)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(priority.color)
            )
            Text(
                priority.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun FormInputChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        color = Color(0xFFFAFAFA)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
            Text(text, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
private fun AttachmentChip(uri: Uri, onRemove: () -> Unit) {
    val fileName = remember(uri) {
        uri.lastPathSegment?.substringAfterLast('/') ?: "file"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.LightGray.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = fileName,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 120.dp)
            )
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Remove attachment",
                modifier = Modifier
                    .size(16.dp)
                    .clickable { onRemove() },
                tint = Color.Gray
            )
        }
    }
}