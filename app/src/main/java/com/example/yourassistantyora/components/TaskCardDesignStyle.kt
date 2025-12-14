package com.example.yourassistantyora.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consumeAllChanges

import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.yourassistantyora.models.TaskModel
import com.example.yourassistantyora.models.categoryNamesSafe
import com.example.yourassistantyora.models.priorityStripColor
import com.example.yourassistantyora.models.reminderText
import com.example.yourassistantyora.models.statusColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskCardDesignStyle(
    task: TaskModel,
    onTaskClick: () -> Unit,
    onCheckboxClick: (Boolean) -> Unit,
    onDeleteIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    isCompleted: Boolean = task.isCompleted,
    swipedTaskId: String? = null,
    onSwipeChange: (String, Boolean) -> Unit = { _, _ -> }
) {
    val deleteWidth = 80.dp
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val deleteOffset = remember { Animatable(0f) }

    val stripColor = task.priorityStripColor(isCompleted)
    val titleColor = Color(0xFF2D2D2D).copy(alpha = if (isCompleted) 0.5f else 1f)
    val secondaryTextColor = Color(0xFF9E9E9E).copy(alpha = if (isCompleted) 0.5f else 1f)

    // auto-close swipe
    LaunchedEffect(swipedTaskId) {
        val isOpen = deleteOffset.value < 0f
        if (swipedTaskId != null && swipedTaskId != task.id && isOpen) {
            deleteOffset.animateTo(0f, tween(300))
        } else if (swipedTaskId == null && isOpen) {
            deleteOffset.animateTo(0f, tween(300))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                val deleteWidthPx = with(density) { deleteWidth.toPx() }
                detectHorizontalDragGestures(
                    onDragEnd = {
                        scope.launch {
                            val target =
                                if (deleteOffset.value < -deleteWidthPx / 2) -deleteWidthPx else 0f
                            deleteOffset.animateTo(target, tween(300))
                            onSwipeChange(task.id, target != 0f)
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            deleteOffset.snapTo(0f)
                            onSwipeChange(task.id, false)
                        }
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consumeAllChanges()
                        val deleteWidthPx = with(density) { deleteWidth.toPx() }
                        val newOffset = (deleteOffset.value + dragAmount).coerceIn(-deleteWidthPx, 0f)
                        scope.launch { deleteOffset.snapTo(newOffset) }
                    }
                )
            }
    ) {
        // background delete
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(14.dp))
                .background(stripColor)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(deleteWidth)
                    .fillMaxHeight()
                    .clickable(onClick = onDeleteIconClick),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.Delete, contentDescription = "Hapus", tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("Hapus", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        val (statusBg, statusFg) = task.statusColors(isCompleted)

        // main card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(deleteOffset.value.roundToInt(), 0) }
                .zIndex(1f)
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onTaskClick),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(end = 12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(stripColor)
                )

                Spacer(Modifier.width(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 14.dp)
                ) {
                    Text(
                        text = task.Title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = titleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(6.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = secondaryTextColor, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(task.deadlineTimeFormatted, fontSize = 11.sp, color = secondaryTextColor)
                        }

                        // priority chip
                        BadgeChip(
                            text = task.priorityText,
                            backgroundColor = when (task.Priority) {
                                2 -> Color(0xFFFFEBEE)
                                1 -> Color(0xFFFFF3E0)
                                else -> Color(0xFFE3F2FD)
                            }.copy(alpha = if (isCompleted) 0.3f else 1f),
                            textColor = when (task.Priority) {
                                2 -> Color(0xFFD32F2F)
                                1 -> Color(0xFFEF6C00)
                                else -> Color(0xFF1976D2)
                            }.copy(alpha = if (isCompleted) 0.6f else 1f)
                        )

                        // ✅ ALL categories chips
                        task.categoryNamesSafe.forEach { cat ->
                            BadgeChip(
                                text = cat,
                                backgroundColor = Color(0xFFE8EAF6).copy(alpha = if (isCompleted) 0.3f else 1f),
                                textColor = Color(0xFF3949AB).copy(alpha = if (isCompleted) 0.6f else 1f)
                            )
                        }

                        // status chip
                        BadgeChip(
                            text = task.statusText,
                            backgroundColor = statusBg,
                            textColor = statusFg
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = { checked -> onCheckboxClick(checked) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF6A70D7),
                        uncheckedColor = Color(0xFFB0B0B0),
                        checkmarkColor = Color.White
                    ),
                    modifier = Modifier
                        .size(24.dp)
                        .padding(top = 12.dp)
                )
            }
        }
    }
}