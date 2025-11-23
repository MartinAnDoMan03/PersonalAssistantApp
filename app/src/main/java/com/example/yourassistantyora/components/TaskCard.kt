package com.example.yourassistantyora.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.yourassistantyora.models.TaskModel

@Composable
fun TaskCard(
    task: TaskModel,
    onTaskClick: () -> Unit,
    // ✅ KEMBALIKAN KE (Boolean) -> Unit
    onCheckboxClick: (Boolean) -> Unit,
    modifier: Modifier = Modifier // Tambahkan modifier sebagai parameter
) {
    val priorityColor = when (task.Priority) {
        2 -> Color(0xFFEF5350) // High - Merah
        1 -> Color(0xFFFFB74D) // Medium - Oranye
        0 -> Color(0xFF64B5F6) // Low - Biru
        else -> Color.Gray
    }

    val textDecoration = if (task.isCompleted) TextDecoration.LineThrough else null
    val titleColor = if (task.isCompleted) Color.Gray else Color.Black

    Card(
        // Gunakan modifier dari parameter
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onTaskClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(80.dp)
                    .background(priorityColor)
            )

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.Title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = titleColor,
                        textDecoration = textDecoration,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = "Deadline time",
                            tint = Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = task.deadlineTimeFormatted,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailChip(text = task.priorityText, color = priorityColor)
                        DetailChip(text = task.categoryText, color = Color(0xFF6A70D7))
                        DetailChip(text = task.statusText, color = getStatusColor(task.statusText))
                    }
                }
            }

            Checkbox(
                checked = task.isCompleted,
                // ✅ KEMBALIKAN KE onCheckboxClick(it)
                onCheckedChange = { isChecked -> onCheckboxClick(isChecked) },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF6A70D7),
                    uncheckedColor = Color.Gray
                ),
                modifier = Modifier.padding(end = 12.dp)
            )
        }
    }
}

// Composable private lainnya tetap sama
@Composable
private fun DetailChip(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun getStatusColor(status: String): Color {
    return when (status) {
        "Waiting" -> Color(0xFFBA68C8)
        "To do" -> Color(0xFF42A5F5)
        "Hold On" -> Color(0xFFFFCA28)
        "On Progress" -> Color(0xFF26A69A)
        "Done" -> Color(0xFF66BB6A)
        else -> Color.Gray
    }
}
