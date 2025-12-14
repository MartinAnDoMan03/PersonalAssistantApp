//package com.example.yourassistantyora.models
//
//import com.google.firebase.Timestamp
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//
//// ----------------------------
//// Safe reflection helpers
//// (biar tidak error walau nama field beda-beda)
//// ----------------------------
//private fun Any?.asDateOrNull(): Date? = when (this) {
//    is Date -> this
//    is Long -> Date(this)
//    is Timestamp -> this.toDate()
//    else -> null
//}
//
//private fun TaskModel.readAny(vararg names: String): Any? {
//    val cls = this.javaClass
//    for (n in names) {
//        // try field
//        runCatching {
//            val f = cls.getDeclaredField(n)
//            f.isAccessible = true
//            val v = f.get(this)
//            if (v != null) return v
//        }
//        // try getter method
//        runCatching {
//            val m = cls.methods.firstOrNull { it.name.equals("get$n", ignoreCase = true) && it.parameterTypes.isEmpty() }
//            val v = m?.invoke(this)
//            if (v != null) return v
//        }
//    }
//    return null
//}
//
//private fun TaskModel.readString(vararg names: String): String? =
//    readAny(*names) as? String
//
//private fun TaskModel.readInt(vararg names: String): Int? =
//    when (val v = readAny(*names)) {
//        is Int -> v
//        is Long -> v.toInt()
//        is String -> v.toIntOrNull()
//        else -> null
//    }
//
//// ----------------------------
//// UI helper text (yang kamu panggil di TaskDetail)
//// ----------------------------
//
//val TaskModel.priorityText: String
//    get() = when (this.Priority) {
//        2 -> "High"
//        1 -> "Medium"
//        else -> "Low"
//    }
//
//val TaskModel.categoryText: String
//    get() {
//        // coba ambil dari field yang mungkin ada
//        val rawString = readString("Category", "category", "selectedCategory", "CategoryText")
//        if (!rawString.isNullOrBlank()) return rawString
//
//        val rawInt = readInt("Category", "category", "categoryId", "selectedCategoryId")
//        return when (rawInt) {
//            1 -> "Work"
//            2 -> "Study"
//            3 -> "Project"
//            4 -> "Meeting"
//            5 -> "Travel"
//            6 -> "Personal"
//            else -> "Uncategorized"
//        }
//    }
//
//val TaskModel.statusText: String
//    get() {
//        val rawString = readString("Status", "status", "selectedStatus", "StatusText")
//        if (!rawString.isNullOrBlank()) return rawString
//
//        val rawInt = readInt("Status", "status", "statusId", "selectedStatusId")
//        return when (rawInt) {
//            0 -> "To do"
//            1 -> "In Progress"
//            2 -> "Waiting"
//            3 -> "Hold On"
//            4 -> "Done"
//            else -> "To do"
//        }
//    }
//
//val TaskModel.reminderText: String
//    get() {
//        val rawString = readString("Reminder", "reminder", "selectedReminder", "ReminderText")
//        if (!rawString.isNullOrBlank()) return rawString
//
//        val rawInt = readInt("Reminder", "reminder", "reminderMinutes", "reminderCode")
//        return when (rawInt) {
//            null, 0 -> "No reminder"
//            1 -> "Remind at time"
//            10 -> "10 minutes before"
//            20 -> "20 minutes before"
//            30 -> "30 minutes before"
//            60 -> "1 hour before"
//            1440 -> "1 day before"
//            2880 -> "2 days before"
//            4320 -> "3 days before"
//            else -> "Reminder set"
//        }
//    }
//
//private fun TaskModel.deadlineDateOrNull(): Date? {
//    // di project kamu Deadline sering Timestamp (lihat WeeklyScreen: Deadline?.toDate())
//    val any = readAny("Deadline", "deadline", "dueDate", "DueDate", "date")
//    return any.asDateOrNull()
//}
//
//val TaskModel.deadlineDateFormatted: String
//    get() {
//        val d = deadlineDateOrNull() ?: return "—"
//        return SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(d)
//    }
//
//val TaskModel.deadlineTimeFormatted: String
//    get() {
//        val d = deadlineDateOrNull() ?: return "—"
//        return SimpleDateFormat("hh:mm a", Locale.getDefault()).format(d)
//    }
