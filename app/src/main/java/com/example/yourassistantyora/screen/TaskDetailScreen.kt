package com.example.yourassistantyora.screen

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.viewModel.EditTaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: EditTaskViewModel = viewModel()
) {
    val context = LocalContext.current
    var isInEditMode by remember { mutableStateOf(false) }

    val task by viewModel.originalTask
    val isLoading by viewModel.isLoading
    val taskUpdated by viewModel.taskUpdated
    val taskDeleted by viewModel.taskDeleted
    val errorMessage by viewModel.errorMessage

    LaunchedEffect(key1 = taskId) {
        viewModel.loadTask(taskId)
    }

    LaunchedEffect(taskUpdated, taskDeleted, errorMessage) {
        if (taskUpdated || taskDeleted) {
            val message = if (taskUpdated) "Task updated!" else "Task deleted!"
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.resetEvents()
        }
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            TopAppBar(
                title = { Text(if (isInEditMode) "Edit Task" else "Task Detail", fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isInEditMode) isInEditMode = false else navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isInEditMode && task != null) {
                        IconButton(onClick = { isInEditMode = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if(isInEditMode) Color.White else Color(0xFF6A70D7),
                    titleContentColor = if(isInEditMode) Color(0xFF1F1F1F) else Color.White,
                    navigationIconContentColor = if(isInEditMode) Color(0xFF757575) else Color.White,
                    actionIconContentColor = if(isInEditMode) Color(0xFF757575) else Color.White
                )
            )
        },
        bottomBar = {
            if (isInEditMode) {
                Surface(shadowElevation = 12.dp, color = Color.White) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // Penting untuk HP dengan gestur bar
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.deleteTask() },
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Text("Delete")
                        }
                        Button(
                            onClick = { viewModel.updateTask() },
                            modifier = Modifier.weight(1.5f).height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A70D7)),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Text("Save Changes", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            if (isLoading && task == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (task != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    if (isInEditMode) EditTaskForm(viewModel) else ViewTaskDetail(task!!)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskForm(viewModel: EditTaskViewModel) {
    val title by viewModel.title
    val description by viewModel.description
    val selectedReminder by viewModel.selectedReminder
    val selectedPriority by viewModel.selectedPriority
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.ENGLISH) }

    val formattedDate by remember(viewModel.selectedDate.value) {
        derivedStateOf { viewModel.selectedDate.value?.let { dateFormatter.format(it) } ?: "Select date" }
    }
    val formattedTime by remember(viewModel.selectedTime.value) {
        derivedStateOf { viewModel.selectedTime.value?.let { timeFormatter.format(it.time) } ?: "Select time" }
    }

    // Date & Time Picker Logic (Tetap sama)
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.selectedDate.value?.time ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = {
                datePickerState.selectedDateMillis?.let { viewModel.selectedDate.value = Date(it) }
                showDatePicker = false
            }) { Text("OK") } }
        ) { DatePicker(state = datePickerState) }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = viewModel.selectedTime.value?.get(Calendar.HOUR_OF_DAY) ?: 12,
        initialMinute = viewModel.selectedTime.value?.get(Calendar.MINUTE) ?: 0
    )

    if (showTimePicker) {
        TimePickerDialog(
            state = timePickerState,
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    set(Calendar.MINUTE, timePickerState.minute)
                }
                viewModel.selectedTime.value = cal
                showTimePicker = false
            }
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // CARD 1: DETAILS (Gabung Title & Desc)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Task Details", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A70D7))
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.title.value = it },
                    placeholder = { Text("What needs to be done?", fontSize = 15.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                    singleLine = true
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), thickness = 0.5.dp)
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.description.value = it },
                    placeholder = { Text("Add more details...(optional)", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp, max = 150.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent)
                )
            }
        }

        // CARD 2: SCHEDULE & REMINDER (Gabung agar hemat tempat)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.CalendarToday, null, tint = Color(0xFF6A70D7), modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Schedule & Reminder", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormInputChip(text = formattedDate, onClick = { showDatePicker = true }, modifier = Modifier.weight(1f))
                    FormInputChip(text = formattedTime, onClick = { showTimePicker = true }, modifier = Modifier.weight(1f))
                }
                DropdownInputChip(
                    selectedText = selectedReminder,
                    // Gunakan daftar opsi yang lebih lengkap
                    options = listOf(
                        "Tidak ada pengingat",
                        "Pada waktunya",
                        "10 menit sebelumnya",
                        "20 menit sebelumnya",
                        "30 menit sebelumnya",
                        "1 hari sebelumnya",
                        "2 hari sebelumnya",
                        "3 hari sebelumnya"
                    ),
                    onOptionSelected = { viewModel.selectedReminder.value = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // CARD 3: PRIORITY
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Priority", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PriorityOption(label = "High", color = Color(0xFFEF5350), isSelected = selectedPriority == "High", onClick = { viewModel.selectedPriority.value = "High" }, modifier = Modifier.weight(1f))
                    PriorityOption(label = "Medium", color = Color(0xFFFFB74D), isSelected = selectedPriority == "Medium", onClick = { viewModel.selectedPriority.value = "Medium" }, modifier = Modifier.weight(1f))
                    PriorityOption(label = "Low", color = Color(0xFF64B5F6), isSelected = selectedPriority == "Low", onClick = { viewModel.selectedPriority.value = "Low" }, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp)) // Ruang ekstra di paling bawah
    }
}

@Composable
private fun PriorityOption(label: String, color: Color, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val backgroundColor by animateColorAsState(if (isSelected) color.copy(alpha = 0.15f) else Color.White)
    val borderColor by animateColorAsState(if (isSelected) color else Color(0xFFEEEEEE))

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.size(16.dp).clip(CircleShape).background(color))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// Komponen Pembantu Lainnya (Sama seperti sebelumnya namun dioptimasi sedikit)
@Composable
private fun FormInputChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F7FA))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 13.sp, color = Color.DarkGray)
    }
}

@Composable
fun ViewTaskDetail(task: TaskModel) {
    val priorityColor = when (task.Priority) { 2->Color(0xFFEF5350); 1->Color(0xFFFFB74D); else->Color(0xFF64B5F6) }
    val categories = task.categoryNamesSafe

    Column {
        Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF6A70D7)).padding(24.dp)) {
            Text(task.Title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = priorityColor) {
                    Text(task.priorityText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 11.sp, color = Color.White)
                }
                categories.forEach { category ->
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Text(category, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            DetailCard(title = "Schedule", details = mapOf("Date" to task.deadlineDateFormatted, "Time" to task.deadlineTimeFormatted))
            DetailCard(title = "Description", content = task.Description)
            DetailCard(title = "Reminder", content = task.reminderText)
        }
    }
}

@Composable
fun DetailCard(title: String, content: String? = null, details: Map<String, String>? = null) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(12.dp)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF6A70D7))
            if (content != null) {
                Spacer(Modifier.height(4.dp))
                Text(content, fontSize = 14.sp, color = Color.Gray)
            }
            details?.forEach { (label, value) ->
                Row(Modifier.padding(top = 4.dp)) {
                    Text("$label: ", fontSize = 13.sp, color = Color.Gray)
                    Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(state: TimePickerState, onDismissRequest: () -> Unit, onConfirm: () -> Unit) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                TimePicker(state = state)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) { Text("Cancel") }
                    TextButton(onClick = onConfirm) { Text("OK") }
                }
            }
        }
    }
}

@Composable
private fun DropdownInputChip(selectedText: String, options: List<String>, onOptionSelected: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FormInputChip(text = selectedText, onClick = { expanded = true }, modifier = Modifier.fillMaxWidth())
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color.White)) {
            options.forEach { option ->
                DropdownMenuItem(text = { Text(option, fontSize = 14.sp) }, onClick = { onOptionSelected(option); expanded = false })
            }
        }
    }
}