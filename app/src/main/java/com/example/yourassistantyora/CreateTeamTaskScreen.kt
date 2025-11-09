package com.example.yourassistantyora

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTeamTaskScreen(
    teamMembers: List<TeamMember>,
    teamColor: Color,
    onBackClick: () -> Unit,
    onAssignClick: (String, String, List<TeamMember>, TaskPriority, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var taskTitle by remember { mutableStateOf("") }
    var taskDescription by remember { mutableStateOf("") }
    var selectedMembers by remember { mutableStateOf(listOf<TeamMember>()) }
    var selectedPriority by remember { mutableStateOf<TaskPriority?>(null) }
    var deadline by remember { mutableStateOf("") }

    val isFormValid = taskTitle.isNotBlank() &&
            taskDescription.isNotBlank() &&
            selectedMembers.isNotEmpty() &&
            selectedPriority != null &&
            deadline.isNotBlank()

    Scaffold(
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "New Team Task",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D2D2D)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Filled.Close, "Close", tint = Color(0xFF2D2D2D))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF8F9FA)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
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
                // Task Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Task Details",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Task Title", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                            OutlinedTextField(
                                value = taskTitle,
                                onValueChange = { taskTitle = it },
                                placeholder = {
                                    Text("What needs to be done?", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = teamColor,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    cursorColor = teamColor
                                ),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Description", fontSize = 12.sp, color = Color(0xFF9E9E9E))
                            OutlinedTextField(
                                value = taskDescription,
                                onValueChange = { taskDescription = it },
                                placeholder = {
                                    Text("Add more details... (optional)", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = teamColor,
                                    unfocusedBorderColor = Color(0xFFE0E0E0),
                                    cursorColor = teamColor
                                ),
                                shape = RoundedCornerShape(10.dp),
                                maxLines = 4
                            )
                        }
                    }
                }

                // Assign to Members Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Tasks per Member",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )

                        teamMembers.forEach { member ->
                            val isSelected = selectedMembers.contains(member)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedMembers = if (isSelected) {
                                            selectedMembers - member
                                        } else {
                                            selectedMembers + member
                                        }
                                    },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    if (isSelected) teamColor.copy(alpha = 0.1f) else Color(0xFFF8F9FA)
                                ),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(teamColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                member.name.first().toString(),
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Column {
                                            Text(
                                                member.name,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Color(0xFF2D2D2D)
                                            )
                                            Text(
                                                "${member.tasksCompleted} tasks",
                                                fontSize = 11.sp,
                                                color = Color(0xFF9E9E9E)
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            Icons.Filled.CheckCircle,
                                            null,
                                            tint = teamColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Filled.RadioButtonUnchecked,
                                            null,
                                            tint = Color(0xFFBDBDBD),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Priority Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Priority",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TaskPriority.values().forEach { priority ->
                                PriorityChip(
                                    priority = priority,
                                    isSelected = selectedPriority == priority,
                                    onClick = { selectedPriority = priority },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Schedule Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(Color.White),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Schedule",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2D2D2D)
                        )

                        OutlinedTextField(
                            value = deadline,
                            onValueChange = { deadline = it },
                            placeholder = {
                                Text("Oct 24, 2025", color = Color(0xFFBDBDBD), fontSize = 14.sp)
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.CalendarToday,
                                    null,
                                    tint = Color(0xFF9E9E9E),
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { /* Open date picker */ }) {
                                    Icon(
                                        Icons.Filled.DateRange,
                                        null,
                                        tint = teamColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = teamColor,
                                unfocusedBorderColor = Color(0xFFE0E0E0),
                                cursorColor = teamColor
                            ),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }
            }

            // Assign Button
            Button(
                onClick = {
                    selectedPriority?.let { priority ->
                        onAssignClick(taskTitle, taskDescription, selectedMembers, priority, deadline)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(48.dp),
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = teamColor,
                    disabledContainerColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Assign",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PriorityChip(
    priority: TaskPriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            if (isSelected) priority.bgColor else Color(0xFFF8F9FA)
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 2.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) priority.color else priority.color.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Filled.Check,
                        null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                priority.displayName,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) priority.color else Color(0xFF757575)
            )
        }
    }
}