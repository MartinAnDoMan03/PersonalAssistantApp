//package com.example.yourassistantyora.models
//
//import androidx.compose.ui.graphics.Color
//import java.text.SimpleDateFormat
//import java.util.Locale
//
//// ✅ property so you can use: task.isCompleted
//val TaskModel.isCompleted: Boolean
//    get() = this.Status == 2 // 2 = Done
//
//fun TaskModel.priorityLabel(): String = when (this.Priority) {
//    2 -> "High"
//    1 -> "Medium"
//    0 -> "Low"
//    else -> "Low"
//}
//
//fun TaskModel.priorityStripColor(isCompleted: Boolean): Color {
//    val base = when (this.Priority) {
//        2 -> Color(0xFFEF5350)
//        1 -> Color(0xFFFFB74D)
//        else -> Color(0xFF64B5F6)
//    }
//    return base.copy(alpha = if (isCompleted) 0.4f else 0.9f)
//}
//
//fun TaskModel.statusLabel(): String = when (this.Status) {
//    0 -> "Waiting"
//    1 -> "To do"
//    2 -> "Done"
//    3 -> "Hold On"
//    4 -> "In Progress"
//    else -> "To do"
//}
//
//fun TaskModel.statusColors(isCompleted: Boolean): Pair<Color, Color> {
//    val (bg, fg) = when (this.Status) {
//        0 -> Color(0xFFF3E5F5) to Color(0xFF6A1B9A)
//        1 -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
//        2 -> Color(0xFFE8F5E8) to Color(0xFF2E7D32)
//        3 -> Color(0xFFFFF3E0) to Color(0xFFEF6C00)
//        4 -> Color(0xFFE0F2F1) to Color(0xFF00695C)
//        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
//    }
//    val aBg = if (isCompleted) 0.3f else 1f
//    val aFg = if (isCompleted) 0.6f else 1f
//    return bg.copy(alpha = aBg) to fg.copy(alpha = aFg)
//}
//
//fun TaskModel.primaryCategoryCode(): Int {
//    return this.Categories.firstOrNull() ?: this.Category
//}
//
//fun TaskModel.categoryLabel(): String = when (primaryCategoryCode()) {
//    0 -> "Work"
//    1 -> "Study"
//    2 -> "Travel"
//    3 -> "Meeting"
//    4 -> "Project"
//    5 -> "Personal"
//    else -> "Other"
//}
//
//fun TaskModel.timeText(): String {
//    val d = this.Deadline?.toDate() ?: return "--:--"
//    return SimpleDateFormat("HH:mm", Locale.getDefault()).format(d)
//}
