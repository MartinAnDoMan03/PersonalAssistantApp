package com.example.yourassistantyora.components



import androidx.compose.animation.core.Animatable

import androidx.compose.animation.core.tween

import androidx.compose.foundation.background

import androidx.compose.foundation.clickable

import androidx.compose.foundation.gestures.detectHorizontalDragGestures

import androidx.compose.foundation.layout.*

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.Delete

import androidx.compose.material.icons.outlined.AccessTime

import androidx.compose.material3.Card

import androidx.compose.material3.CardDefaults

import androidx.compose.material3.Checkbox

import androidx.compose.material3.CheckboxDefaults

import androidx.compose.material3.Icon

import androidx.compose.material3.Text

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.platform.LocalDensity

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.IntOffset

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.compose.ui.zIndex

import com.example.yourassistantyora.models.TaskModel

import com.example.yourassistantyora.models.categoryLabel


import com.example.yourassistantyora.models.priorityLabel

import com.example.yourassistantyora.models.priorityStripColor

import com.example.yourassistantyora.models.statusColors

import com.example.yourassistantyora.models.statusLabel

import com.example.yourassistantyora.models.timeText

import kotlinx.coroutines.launch

import kotlin.math.roundToInt



@Composable

fun SwipeableTaskCard(

    task: TaskModel,

    onTaskClick: () -> Unit,

    onSwipeToDelete: (TaskModel) -> Unit,

    onCheckboxClick: (Boolean) -> Unit,

    modifier: Modifier = Modifier,

    enableSwipe: Boolean = true,

    isCompletedOverride: Boolean? = null

) {

    val isCompleted = isCompletedOverride ?: task.isCompleted



    val deleteWidth = 84.dp

    val density = LocalDensity.current

    val scope = rememberCoroutineScope()

    val offsetX = remember { Animatable(0f) }



    val stripColor = task.priorityStripColor(isCompleted)

    val titleColor = Color(0xFF2D2D2D).copy(alpha = if (isCompleted) 0.5f else 1f)

    val secondary = Color(0xFF9E9E9E).copy(alpha = if (isCompleted) 0.6f else 1f)



    val priority = task.priorityLabel()

    val category = task.categoryLabel()

    val status = task.statusLabel()

    val (statusBg, statusFg) = task.statusColors(isCompleted)



    val priorityBg = when (priority) {

        "High" -> Color(0xFFFFEBEE)

        "Medium" -> Color(0xFFFFF3E0)

        else -> Color(0xFFE3F2FD)

    }.copy(alpha = if (isCompleted) 0.3f else 1f)



    val priorityFg = when (priority) {

        "High" -> Color(0xFFD32F2F)

        "Medium" -> Color(0xFFEF6C00)

        else -> Color(0xFF1976D2)

    }.copy(alpha = if (isCompleted) 0.6f else 1f)



    val categoryBg = Color(0xFFE8EAF6).copy(alpha = if (isCompleted) 0.3f else 1f)

    val categoryFg = Color(0xFF3949AB).copy(alpha = if (isCompleted) 0.6f else 1f)



    Box(

        modifier = modifier

            .fillMaxWidth()

            .pointerInput(enableSwipe) {

                if (!enableSwipe) return@pointerInput

                val deleteWidthPx = with(density) { deleteWidth.toPx() }

                detectHorizontalDragGestures(

                    onDragEnd = {

                        scope.launch {

                            val target = if (offsetX.value < -deleteWidthPx / 2f) -deleteWidthPx else 0f

                            offsetX.animateTo(target, tween(220))

                        }

                    },

                    onDragCancel = {

                        scope.launch { offsetX.animateTo(0f, tween(180)) }

                    },

                    onHorizontalDrag = { change, dragAmount ->

                        change.consume()

                        scope.launch {

                            val deleteWidthPx = with(density) { deleteWidth.toPx() }

                            val newOffset = (offsetX.value + dragAmount).coerceIn(-deleteWidthPx, 0f)

                            offsetX.snapTo(newOffset)

                        }

                    }

                )

            }

    ) {

        // Delete background

        if (enableSwipe) {

            Box(

                modifier = Modifier

                    .matchParentSize()

                    .clip(RoundedCornerShape(14.dp))

                    .background(Color(0xFFF44336))

            ) {

                Box(

                    modifier = Modifier

                        .align(Alignment.CenterEnd)

                        .width(deleteWidth)

                        .fillMaxHeight()

                        .clickable { onSwipeToDelete(task) },

                    contentAlignment = Alignment.Center

                ) {

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.White)

                        Spacer(Modifier.height(4.dp))

                        Text("Delete", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

                    }

                }

            }

        }



        // Foreground card

        Card(

            modifier = Modifier

                .fillMaxWidth()

                .offset { IntOffset(offsetX.value.roundToInt(), 0) }

                .zIndex(1f)

                .clip(RoundedCornerShape(14.dp))

                .clickable(onClick = onTaskClick),

            shape = RoundedCornerShape(14.dp),

            colors = CardDefaults.cardColors(containerColor = Color.White),

            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)

        ) {

            Row(

                modifier = Modifier

                    .fillMaxWidth()

                    .height(IntrinsicSize.Min),

                verticalAlignment = Alignment.CenterVertically

            ) {

                Box(

                    modifier = Modifier

                        .width(6.dp)

                        .fillMaxHeight()

                        .background(stripColor)

                )



                Checkbox(

                    checked = isCompleted,

                    onCheckedChange = { checked -> onCheckboxClick(checked) },

                    colors = CheckboxDefaults.colors(

                        checkedColor = Color(0xFF6A70D7),

                        uncheckedColor = Color(0xFFB0B0B0),

                        checkmarkColor = Color.White

                    ),

                    modifier = Modifier.padding(start = 12.dp)

                )



                Spacer(Modifier.width(12.dp))



                Column(

                    modifier = Modifier

                        .weight(1f)

                        .padding(vertical = 14.dp, horizontal = 6.dp)

                ) {

                    Text(

                        text = task.Title,

                        fontSize = 15.sp,

                        fontWeight = FontWeight.SemiBold,

                        color = titleColor,

                        maxLines = 2

                    )



                    Spacer(Modifier.height(8.dp))



                    Row(

                        horizontalArrangement = Arrangement.spacedBy(8.dp),

                        verticalAlignment = Alignment.CenterVertically

                    ) {

                        Row(verticalAlignment = Alignment.CenterVertically) {

                            Icon(Icons.Outlined.AccessTime, contentDescription = null, tint = secondary, modifier = Modifier.size(14.dp))

                            Spacer(Modifier.width(4.dp))

                            Text(task.timeText(), fontSize = 12.sp, color = secondary)

                        }



                        BadgeChip(text = priority, backgroundColor = priorityBg, textColor = priorityFg)

                        BadgeChip(text = category, backgroundColor = categoryBg, textColor = categoryFg)

                        BadgeChip(text = status, backgroundColor = statusBg, textColor = statusFg)

                    }

                }



                Spacer(Modifier.width(10.dp))

            }

        }

    }

}