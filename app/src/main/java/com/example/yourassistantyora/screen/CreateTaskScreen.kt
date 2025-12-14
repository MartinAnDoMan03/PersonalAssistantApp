package com.example.yourassistantyora.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.yourassistantyora.viewModel.CreateTaskViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class StatusItem(val name: String, val color: Color)

// Warna Aksen Utama (digunakan untuk border, ikon, dan teks Add New)
private val AccentColor = Color(0xFF6A70D7)

// Warna untuk Gradien Tombol "Create Task"
private val GradientStartColor = Color(0xFF6A70D7)
private val GradientEndColor = Color(0xFF7353AD)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskScreen(
    navController: NavController,
    viewModel: CreateTaskViewModel = viewModel()
) {
    val isLoading by viewModel.isLoading
    val err by viewModel.errorMessage
    val created by viewModel.created

    var showNewStatusDialog by remember { mutableStateOf(false) }
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var showReminderDropdown by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    var categories by remember {
        mutableStateOf(listOf("Work", "Study", "Travel", "Meeting", "Project", "Personal"))
    }
    var statuses by remember {
        mutableStateOf(
            listOf(
                StatusItem("Waiting", Color(0xFFF3E5F5)),
                StatusItem("To do", Color(0xFFE3F2FD)),
                StatusItem("Done", Color(0xFFE8F5E8)),
                StatusItem("Hold On", Color(0xFFFFF3E0)),
                StatusItem("In Progress", Color(0xFFE0F2F1))
            )
        )
    }

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

    val dateFmt = remember { SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH) }
    val timeFmt = remember { SimpleDateFormat("hh:mm a", Locale.ENGLISH) }

    val dateText = viewModel.selectedDate.value?.let { dateFmt.format(it) } ?: "Select date"
    val timeText = viewModel.selectedTime.value?.time?.let { timeFmt.format(it) } ?: "Select time"

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = viewModel.selectedDate.value?.time ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { viewModel.selectedDate.value = Date(it) }
                        showDatePicker = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = AccentColor)
                ) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    val timeState = rememberTimePickerState(
        initialHour = viewModel.selectedTime.value?.get(Calendar.HOUR_OF_DAY)
            ?: Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
        initialMinute = viewModel.selectedTime.value?.get(Calendar.MINUTE)
            ?: Calendar.getInstance().get(Calendar.MINUTE)
    )
    if (showTimePicker) {
        TimePickerDialog(
            state = timeState,
            onDismissRequest = { showTimePicker = false },
            onConfirm = {
                val cal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, timeState.hour)
                    set(Calendar.MINUTE, timeState.minute)
                }
                viewModel.selectedTime.value = cal
                showTimePicker = false
            }
        )
    }

    LaunchedEffect(created) {
        if (created) {
            viewModel.resetCreated()
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F5F5),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "New Task",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F1F1F)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color(0xFF757575))
                    }
                },
                actions = {
                    IconButton(onClick = { if (!isLoading) viewModel.createTask() }) {
                        Icon(Icons.Filled.Check, contentDescription = "Save", tint = AccentColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!err.isNullOrBlank()) {
                Text(err ?: "", color = Color.Red, modifier = Modifier.fillMaxWidth())
            }

            CardBlock(title = "Task Details") {
                LabeledField("Title") {
                    OutlinedTextField(
                        value = viewModel.title.value,
                        onValueChange = { viewModel.title.value = it },
                        placeholder = { Text("What needs to be done ?", fontSize = 14.sp, color = Color(0xFFBDBDBD)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = outlinedColors(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
                LabeledField("Description") {
                    OutlinedTextField(
                        value = viewModel.description.value,
                        onValueChange = { viewModel.description.value = it },
                        placeholder = { Text("Add more details...(optional)", fontSize = 14.sp, color = Color(0xFFBDBDBD)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        colors = outlinedColors(),
                        shape = RoundedCornerShape(8.dp),
                        maxLines = 4
                    )
                }
            }

            CardBlock(
                title = "Schedule",
                leadingIcon = {
                    Icon(
                        Icons.Outlined.CalendarToday,
                        null,
                        tint = AccentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ScheduleChip(
                        text = dateText,
                        icon = { Icon(Icons.Outlined.CalendarToday, null, tint = Color(0xFF757575), modifier = Modifier.size(18.dp)) },
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                    ScheduleChip(
                        text = timeText,
                        icon = { Icon(Icons.Outlined.AccessTime, null, tint = Color(0xFF757575), modifier = Modifier.size(18.dp)) },
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            CardBlock(title = "Priority") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PriorityOption2(
                        label = "Low",
                        color = Color(0xFF64B5F6),
                        selected = viewModel.selectedPriority.value == "Low",
                        onClick = { viewModel.selectedPriority.value = "Low" },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityOption2(
                        label = "Medium",
                        color = Color(0xFFFFB74D),
                        selected = viewModel.selectedPriority.value == "Medium",
                        onClick = { viewModel.selectedPriority.value = "Medium" },
                        modifier = Modifier.weight(1f)
                    )
                    PriorityOption2(
                        label = "High",
                        color = Color(0xFFEF5350),
                        selected = viewModel.selectedPriority.value == "High",
                        onClick = { viewModel.selectedPriority.value = "High" },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            CardBlock(title = "Category (multi)") {
                val selected = viewModel.selectedCategories.value
                ChunkedChipsMulti(
                    items = categories,
                    selected = selected.toSet(),
                    onToggle = { viewModel.toggleCategory(it) },
                    addLabel = "+ Add New",
                    onAdd = { showNewCategoryDialog = true }
                )
            }

            CardBlock(title = "Reminder") {
                ExposedDropdownMenuBox(
                    expanded = showReminderDropdown,
                    onExpandedChange = { showReminderDropdown = it }
                ) {
                    OutlinedTextField(
                        value = viewModel.selectedReminder.value,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = if (showReminderDropdown) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                                contentDescription = null,
                                tint = Color(0xFF757575)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = outlinedColors(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    ExposedDropdownMenu(
                        expanded = showReminderDropdown,
                        onDismissRequest = { showReminderDropdown = false }
                    ) {
                        reminderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option, fontSize = 13.sp) },
                                onClick = {
                                    viewModel.selectedReminder.value = option
                                    showReminderDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            CardBlock(title = "Location") {
                OutlinedTextField(
                    value = viewModel.location.value,
                    onValueChange = { viewModel.location.value = it },
                    placeholder = { Text("Add location...(optional)", fontSize = 14.sp, color = Color(0xFFBDBDBD)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = outlinedColors(),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
            }

            CardBlock(title = "Status") {
                ChunkedStatus(
                    items = statuses,
                    selected = viewModel.selectedStatus.value,
                    onPick = { viewModel.selectedStatus.value = it },
                    onAdd = { showNewStatusDialog = true }
                )
            }

            // MODIFIKASI: Tombol Utama "Create Task" menggunakan Linear Gradient
            Button(
                onClick = { if (!isLoading) viewModel.createTask() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    // Menerapkan gradien linier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStartColor, GradientEndColor)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ),
                // containerColor harus transparan agar background gradien terlihat
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White)
                } else {
                    Text("Create Task", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }

    if (showNewStatusDialog) {
        NewStatusDialog(
            onDismiss = { showNewStatusDialog = false },
            onConfirm = { name, color ->
                statuses = statuses + StatusItem(name, color)
                showNewStatusDialog = false
            }
        )
    }

    if (showNewCategoryDialog) {
        NewCategoryDialog(
            onDismiss = { showNewCategoryDialog = false },
            onConfirm = { cat ->
                categories = categories + cat
                showNewCategoryDialog = false
            }
        )
    }
}

/* ---------- UI helpers ---------- */

@Composable
private fun CardBlock(
    title: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                leadingIcon?.invoke()
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1F1F1F))
            }
            content()
        }
    }
}

@Composable
private fun LabeledField(label: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = 12.sp, color = Color(0xFF757575))
        content()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    state: TimePickerState,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    BasicAlertDialog(onDismissRequest = onDismissRequest) {
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
                Spacer(Modifier.height(12.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismissRequest) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    TextButton(
                        onClick = onConfirm,
                        colors = ButtonDefaults.textButtonColors(contentColor = AccentColor)
                    ) { Text("OK") }
                }
            }
        }
    }
}

@Composable
private fun ScheduleChip(
    text: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFFAFAFA))
            .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            icon()
            Text(text, fontSize = 13.sp, color = Color(0xFF1F1F1F))
        }
    }
}

@Composable
private fun PriorityOption2(
    label: String,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) color.copy(alpha = 0.1f) else Color(0xFFFAFAFA)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (selected) BorderStroke(2.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(Modifier.size(40.dp).clip(CircleShape).background(color))
            Text(label, fontSize = 13.sp, fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) AccentColor else Color(0xFFF5F5F5))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) Color.White else Color(0xFF757575),
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun ChunkedChipsMulti(
    items: List<String>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    addLabel: String,
    onAdd: () -> Unit
) {
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEachIndexed { idx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { c ->
                    CategoryChip(label = c, selected = selected.contains(c)) { onToggle(c) }
                }
                if (idx == rows.lastIndex) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable(onClick = onAdd)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(addLabel, fontSize = 12.sp, color = AccentColor, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) color else color.copy(alpha = 0.3f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            label,
            fontSize = 12.sp,
            color = if (selected) Color(0xFF1F1F1F) else Color(0xFF757575),
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
private fun ChunkedStatus(
    items: List<StatusItem>,
    selected: String,
    onPick: (String) -> Unit,
    onAdd: () -> Unit
) {
    val rows = items.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEachIndexed { idx, row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { s ->
                    StatusChip(label = s.name, color = s.color, selected = selected == s.name) { onPick(s.name) }
                }
                if (idx == rows.lastIndex) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable(onClick = onAdd)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+ Add New", fontSize = 12.sp, color = AccentColor, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
fun NewStatusDialog(onDismiss: () -> Unit, onConfirm: (String, Color) -> Unit) {
    var statusName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(Color(0xFFE3F2FD)) }
    val badgeColors = listOf(
        Color(0xFFF3E5F5), Color(0xFFE8EAF6), Color(0xFFFFF9C4),
        Color(0xFFE8F5E8), Color(0xFFE3F2FD), Color(0xFFE1F5FE)
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("New Status", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = statusName, onValueChange = { statusName = it }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    badgeColors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(c)
                                .border(if (selectedColor == c) 3.dp else 0.dp, AccentColor, RoundedCornerShape(8.dp))
                                .clickable { selectedColor = c }
                        )
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { if (statusName.isNotBlank()) onConfirm(statusName, selectedColor) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                    ) { Text("Create", color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun NewCategoryDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var categoryName by remember { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("New Category", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = categoryName, onValueChange = { categoryName = it }, modifier = Modifier.fillMaxWidth())
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Cancel") }
                    Button(
                        onClick = { if (categoryName.isNotBlank()) onConfirm(categoryName) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                    ) { Text("Create", color = Color.White) }
                }
            }
        }
    }
}

@Composable
private fun outlinedColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = AccentColor,
    unfocusedBorderColor = Color(0xFFE0E0E0),
    focusedContainerColor = Color.White,
    unfocusedContainerColor = Color(0xFFFAFAFA)
)