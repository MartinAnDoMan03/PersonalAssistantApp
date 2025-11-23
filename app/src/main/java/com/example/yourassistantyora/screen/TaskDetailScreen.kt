package com.example.yourassistantyora.screen

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
        }
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = if(isInEditMode) Color.White else Color(0xFFF5F7FA),
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
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = if(isInEditMode) Color.White else Color(0xFF6A70D7), titleContentColor = if(isInEditMode) Color.Black else Color.White, navigationIconContentColor = if(isInEditMode) Color.Black else Color.White, actionIconContentColor = if(isInEditMode) Color.Black else Color.White )
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
                        OutlinedButton(onClick = { viewModel.deleteTask() }, modifier = Modifier.weight(1f), colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red), border = BorderStroke(1.dp, Color.Red)) { Text("Delete") }
                        Button(onClick = { viewModel.updateTask() }, modifier = Modifier.weight(2f), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A70D7))) { Text("Save Changes") }
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(shape = RoundedCornerShape(16.dp), color = priorityColor) { Text(task.priorityText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
                Surface(shape = RoundedCornerShape(16.dp), color = Color.White.copy(alpha = 0.2f)) { Text(task.categoryText, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White) }
            }
        }

        // Content Section
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            DetailCard(title = "Schedule", details = mapOf("Date" to task.deadlineDateFormatted, "Time" to task.deadlineTimeFormatted))
            DetailCard(title = "Description", content = task.Description)
            DetailCard(title = "Status", content = task.statusText)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskForm(viewModel: EditTaskViewModel) {
    // --- State dari ViewModel ---
    val title by viewModel.title
    val description by viewModel.description
    val selectedDate by viewModel.selectedDate
    val selectedTime by viewModel.selectedTime
    val selectedPriority by viewModel.selectedPriority
    val selectedCategory by viewModel.selectedCategory
    val selectedStatus by viewModel.selectedStatus
    val selectedReminder by viewModel.selectedReminder

    // ✅ --- State lokal untuk mengontrol dialog bawaan Material 3 ---
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.time ?: System.currentTimeMillis()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = selectedTime?.get(Calendar.HOUR_OF_DAY) ?: 12,
        initialMinute = selectedTime?.get(Calendar.MINUTE) ?: 0,
        is24Hour = false
    )

    // ✅ --- Dialog untuk Date Picker ---
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.selectedDate.value = Date(millis)
                        }
                        showDatePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // ✅ --- Dialog untuk Time Picker ---
    if (showTimePicker) {
        // ✅ PERBAIKAN: Panggil TimePickerDialog dengan state
        TimePickerDialog(
            state = timePickerState, // Kirim state ke dialog
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                cal.set(Calendar.MINUTE, timePickerState.minute)
                viewModel.selectedTime.value = cal
                showTimePicker = false
            }
        )
    }

    // --- Helper untuk format Tanggal & Waktu ---
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH) }
    val timeFormatter = remember { SimpleDateFormat("hh:mm a", Locale.ENGLISH) }
    val formattedDate = selectedDate?.let { dateFormatter.format(it) } ?: "Select date"
    val formattedTime = selectedTime?.let { timeFormatter.format(it.time) } ?: "Select time"

    Column(
        modifier = Modifier
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- Title ---
        OutlinedTextField(
            value = title,
            onValueChange = { viewModel.title.value = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth()
        )

        // --- Description ---
        OutlinedTextField(
            value = description,
            onValueChange = { viewModel.description.value = it },
            label = { Text("Description") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        // ✅ --- Date and Time Picker (sekarang memicu state lokal) ---
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FormInputChip(
                text = formattedDate,
                onClick = { showDatePicker = true },
                modifier = Modifier.weight(1f)
            )
            FormInputChip(
                text = formattedTime,
                onClick = { showTimePicker = true },
                modifier = Modifier.weight(1f)
            )
        }

        // --- Priority, Category, Status Selector (TETAP SAMA) ---
        PrioritySelector(
            selectedPriority = selectedPriority,
            onPrioritySelected = { viewModel.selectedPriority.value = it }
        )
        CategorySelector(
            selectedCategory = selectedCategory,
            onCategorySelected = { viewModel.selectedCategory.value = it }
        )
        StatusSelector(
            selectedStatus = selectedStatus,
            onStatusSelected = { viewModel.selectedStatus.value = it }
        )
        ReminderSelector(
            selectedReminder = selectedReminder,
            onReminderSelected = { viewModel.selectedReminder.value = it }
        )
    }
}

// ✅ Composable khusus untuk TimePickerDialog (karena M3 belum punya yang simpel)
@ExperimentalMaterial3Api
@Composable
// ✅ PERBAIKAN: Ganti Composable TimePickerDialog yang lama@OptIn(ExperimentalMaterial3Api::class) // Tambahkan ini jika perlu
private fun TimePickerDialog(
    state: TimePickerState, // ✅ 1. Terima TimePickerState
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
            // ✅ 2. Tampilkan komponen TimePicker
            TimePicker(state = state)
            Spacer(modifier = Modifier.height(12.dp))
            // ✅ 3. Tombol Aksi (Confirm & Cancel)
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


// --- HELPER COMPOSABLES (TIDAK ADA PERUBAHAN) ---

@Composable
private fun FormInputChip(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        color = Color.White
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = Color.DarkGray
        )
    }
}

@Composable
private fun PrioritySelector(selectedPriority: String, onPrioritySelected: (String) -> Unit) {
    val priorities = listOf("Low", "Medium", "High")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Priority", fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            priorities.forEach { priority ->
                FilterChip(
                    selected = selectedPriority == priority,
                    onClick = { onPrioritySelected(priority) },
                    label = { Text(priority) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelector(selectedCategory: String, onCategorySelected: (String) -> Unit) {
    val categories = listOf("Work", "Study", "Project", "Meeting", "Travel")
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Category", fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatusSelector(selectedStatus: String, onStatusSelected: (String) -> Unit) {
    val statuses = listOf("To do", "On Progress", "Hold On", "Done", "Waiting")
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Status", fontWeight = FontWeight.SemiBold)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedStatus,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                statuses.forEach { status ->
                    DropdownMenuItem(
                        text = { Text(status) },
                        onClick = {
                            onStatusSelected(status)
                            expanded = false
                        }
                    )
                }
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
// ✅ TAMBAHKAN COMPOSABLE BARU INI DI BAWAH STATUSSELECTOR
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderSelector(selectedReminder: String, onReminderSelected: (String) -> Unit) {
    val reminderOptions = listOf(
        "Tidak ada pengingat",
        "Ingatkan pada waktunya",
        "Ingatkan 10 menit sebelumnya",
        "Ingatkan 20 menit sebelumnya",
        "Ingatkan 30 menit sebelumnya",
        "Ingatkan 1 hari sebelumnya",
        "Ingatkan 2 hari sebelumnya",
        "Ingatkan 3 hari sebelumnya"
    )
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Reminder", fontWeight = FontWeight.SemiBold)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedReminder,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                reminderOptions.forEach { reminder ->
                    DropdownMenuItem(
                        text = { Text(reminder) },
                        onClick = {
                            onReminderSelected(reminder)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}