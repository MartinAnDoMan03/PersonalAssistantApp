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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.viewModel.EditTaskViewModel
import java.text.SimpleDateFormat
import java.util.*
import com.example.yourassistantyora.models.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    taskId: String,
    navController: NavController,
    viewModel: EditTaskViewModel = viewModel()
) {
    val context = LocalContext.current
    var isInEditMode by remember { mutableStateOf(false) }

    // --- State dari ViewModel ---
    val task by viewModel.originalTask
    val isLoading by viewModel.isLoading
    val taskUpdated by viewModel.taskUpdated
    val taskDeleted by viewModel.taskDeleted
    val errorMessage by viewModel.errorMessage

    // --- Load data saat pertama kali masuk ---
    LaunchedEffect(key1 = taskId) {
        viewModel.loadTask(taskId)
    }

    // --- Handle Events dari ViewModel ---
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
                title = { Text(if (isInEditMode) "Edit Task" else "Task Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (!isInEditMode && task != null) {
                        IconButton(onClick = { isInEditMode = true }) {
                            // ✅ PERUBAHAN: Icon Edit akan menggunakan warna tint dari TopAppBarDefaults
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if(isInEditMode) Color.White else Color(0xFF6A70D7),
                    titleContentColor = if(isInEditMode) Color(0xFF1F1F1F) else Color.White,
                    navigationIconContentColor = if(isInEditMode) Color(0xFF757575) else Color.White,
                    // ✅ TAMBAH: Aksi content color (termasuk icon Edit) diatur menjadi putih saat mode detail
                    actionIconContentColor = if(isInEditMode) Color(0xFF757575) else Color.White
                )
            )
        },
        bottomBar = {
            if (isInEditMode) {
                // Bottom bar untuk mode edit
                Surface(shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Tombol Delete dibuat Outlined
                        OutlinedButton(
                            onClick = { viewModel.deleteTask() },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red)
                        ) {
                            Text("Delete")
                        }
                        // Tombol Save disamakan
                        Button(
                            onClick = { viewModel.updateTask() },
                            modifier = Modifier
                                .weight(2f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A70D7)),
                            enabled = !isLoading
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                            } else {
                                Text("Save Changes", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        if (isLoading && task == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (task != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                if (isInEditMode) {
                    // Tampilkan form edit (mirip CreateTaskScreen)
                    EditTaskForm(viewModel)
                } else {
                    // Tampilkan detail task
                    ViewTaskDetail(task!!)
                }
            }
        }
    }
}


@Composable
fun ViewTaskDetail(task: TaskModel) {
    val priorityColor = when (task.Priority) { 2->Color(0xFFEF5350); 1->Color(0xFFFFB74D); else->Color(0xFF64B5F6) }
    // Mengambil semua kategori dari TaskModel
    val categories = task.categoryNamesSafe

    Column {
        // Header Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A70D7))
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Text(task.Title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(12.dp))

            // Row untuk menampung chip Priority dan semua Category
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()), // Memungkinkan scroll horizontal jika chip terlalu banyak
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1. Chip Prioritas
                Surface(shape = RoundedCornerShape(16.dp), color = priorityColor) {
                    Text(task.priorityText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                // 2. Chip Kategori (Looping untuk setiap nama kategori di categoryNamesSafe)
                categories.forEach { category ->
                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.2f)) {
                        Text(category, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }
        }

        // Content Section
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailCard(title = "Schedule", details = mapOf("Date" to task.deadlineDateFormatted, "Time" to task.deadlineTimeFormatted))
            DetailCard(title = "Description", content = task.Description)
            DetailCard(title = "Reminder", content = task.reminderText)
            DetailCard(title = "Status", content = task.statusText)
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = viewModel.selectedDate.value?.time ?: System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { viewModel.selectedDate.value = Date(it) }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = viewModel.selectedTime.value?.get(Calendar.HOUR_OF_DAY) ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = viewModel.selectedTime.value?.get(Calendar.MINUTE) ?: Calendar.getInstance().get(Calendar.MINUTE)
    )

    if (showTimePicker) {
        // PERBAIKI PEMANGGILAN TimePickerDialog
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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Task Details", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { viewModel.title.value = it },
                    placeholder = { Text("What needs to be done?", fontSize = 14.sp, color = Color(0xFFBDBDBD)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                HorizontalDivider()
                OutlinedTextField(
                    value = description,
                    onValueChange = { viewModel.description.value = it },
                    placeholder = { Text("Add more details...(optional)", fontSize = 14.sp, color = Color(0xFFBDBDBD)) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color.Transparent, unfocusedBorderColor = Color.Transparent),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 4
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Outlined.CalendarToday, contentDescription = null, tint = Color(0xFF6C63FF), modifier = Modifier.size(20.dp))
                    Text("Schedule", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    FormInputChip(text = formattedDate, onClick = { showDatePicker = true }, modifier = Modifier.weight(1f))
                    FormInputChip(text = formattedTime, onClick = { showTimePicker = true }, modifier = Modifier.weight(1f))
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Reminder", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                DropdownInputChip(
                    selectedText = selectedReminder,
                    options = listOf(
                        "Tidak ada pengingat", "Ingatkan pada waktunya", "Ingatkan 10 menit sebelumnya",
                        "Ingatkan 20 menit sebelumnya", "Ingatkan 30 menit sebelumnya", "Ingatkan 1 hari sebelumnya",
                        "Ingatkan 2 hari sebelumnya", "Ingatkan 3 hari sebelumnya"
                    ),
                    onOptionSelected = { viewModel.selectedReminder.value = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Priority", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PriorityOption(label = "High", color = Color(0xFFEF5350), isSelected = selectedPriority == "High", onClick = { viewModel.selectedPriority.value = "High" }, modifier = Modifier.weight(1f))
                    PriorityOption(label = "Medium", color = Color(0xFFFFB74D), isSelected = selectedPriority == "Medium", onClick = { viewModel.selectedPriority.value = "Medium" }, modifier = Modifier.weight(1f))
                    PriorityOption(label = "Low", color = Color(0xFF64B5F6), isSelected = selectedPriority == "Low", onClick = { viewModel.selectedPriority.value = "Low" }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
@Composable
private fun FormInputChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF5F7FA))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 14.sp, color = Color(0xFF424242))
    }
}

@Composable
private fun PriorityOption(
    label: String,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(if (isSelected) color.copy(alpha = 0.1f) else Color.White, label = "")
    val borderColor by animateColorAsState(if (isSelected) color else Color.LightGray.copy(alpha = 0.5f), label = "")

    Surface(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
//        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        shadowElevation = if (isSelected) 3.dp else 1.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(color))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF424242))
        }
    }
}

// Composable khusus untuk TimePickerDialog (karena M3 belum punya yang simpel)
@ExperimentalMaterial3Api
@Composable
private fun TimePickerDialog(
    state: TimePickerState, // Terima TimePickerState
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    // Gunakan BasicAlertDialog agar bisa di-custom penuh
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(top = 28.dp, start = 20.dp, end = 20.dp, bottom = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Tampilkan komponen TimePicker
            TimePicker(state = state)
            Spacer(modifier = Modifier.height(12.dp))
            // Tombol Aksi (Confirm & Cancel)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismissRequest) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(onClick = onConfirm) { Text("OK") }
            }
        }
    }
}

@Composable
fun DetailCard(title: String, content: String? = null, details: Map<String, String>? = null) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF2D2D2D))
            Spacer(Modifier.height(12.dp))
            content?.let {
                Text(it, fontSize = 14.sp, color = Color(0xFF666666), lineHeight = 20.sp)
            }
            details?.forEach { (label, value) ->
                Row {
                    Text("$label : ", fontSize = 14.sp, color = Color(0xFF666666))
                    Text(value, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun DropdownInputChip(
    selectedText: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        FormInputChip(text = selectedText, onClick = { expanded = true }, modifier = Modifier.fillMaxWidth())

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
// --- HELPER COMPOSABLES (TIDAK ADA PERUBAHAN) unused---