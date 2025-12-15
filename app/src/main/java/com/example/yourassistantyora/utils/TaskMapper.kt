//package com.example.yourassistantyora.utils
//
//import com.example.yourassistantyora.models.TaskModel
//import com.example.yourassistantyora.models.ui.TaskUi
//import com.google.firebase.Timestamp
//import java.text.SimpleDateFormat
//import java.util.*
//
//object TaskMapper {
//
//    // ===== Priority =====
//    fun priorityToInt(p: String): Int = when (p) {
//        "Low" -> 0
//        "High" -> 2
//        else -> 1 // Medium
//    }
//    fun intToPriority(v: Int): String = when (v) {
//        0 -> "Low"
//        2 -> "High"
//        else -> "Medium"
//    }
//
//    // ===== Status =====
//    fun statusToInt(s: String): Int = when (s) {
//        "Waiting" -> 0
//        "Done" -> 2
//        "Hold On" -> 3
//        "In Progress" -> 4
//        else -> 1 // "To do"
//    }
//    fun intToStatus(v: Int): String = when (v) {
//        0 -> "Waiting"
//        2 -> "Done"
//        3 -> "Hold On"
//        4 -> "In Progress"
//        else -> "To do"
//    }
//
//    // ===== Category =====
//    // kamu bebas nambah, yang penting konsisten
//    private val catNameToId = mapOf(
//        "Work" to 0,
//        "Study" to 1,
//        "Project" to 2,
//        "Meeting" to 3,
//        "Travel" to 4,
//        "Personal" to 5
//    )
//    private val catIdToName = catNameToId.entries.associate { (k, v) -> v to k }
//
//    fun categoriesToIds(names: List<String>): List<Int> =
//        names.distinct().map { catNameToId[it] ?: 0 }.distinct().ifEmpty { listOf(0) }
//
//    fun idsToCategories(ids: List<Int>): List<String> =
//        ids.distinct().map { catIdToName[it] ?: "Work" }.ifEmpty { listOf("Work") }
//
//    // ===== Time format for UI (Daily grouping butuh HH:mm) =====
//    private val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
//
//    fun modelToUi(m: TaskModel): TaskUi {
//        val deadline = m.Deadline
//        val time = if (deadline != null) timeFmt.format(deadline.toDate()) else "00:00"
//
//        val cats = if (m.Categories.isNotEmpty()) idsToCategories(m.Categories)
//        else idsToCategories(listOf(m.Category))
//
//        val statusStr = intToStatus(m.Status)
//        return TaskUi(
//            id = m.id,
//            title = m.Title,
//            description = m.Description,
//            time = time,
//            priority = intToPriority(m.Priority),
//            categories = cats,
//            status = statusStr,
//            isCompleted = (m.Status == 2)
//        )
//    }
//
//    fun combineDateTimeToTimestamp(dateMillis: Long, hour: Int, minute: Int): Timestamp {
//        val cal = Calendar.getInstance()
//        cal.timeInMillis = dateMillis
//        cal.set(Calendar.HOUR_OF_DAY, hour)
//        cal.set(Calendar.MINUTE, minute)
//        cal.set(Calendar.SECOND, 0)
//        cal.set(Calendar.MILLISECOND, 0)
//        return Timestamp(cal.time)
//    }
//}
