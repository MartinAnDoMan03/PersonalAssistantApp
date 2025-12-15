//package com.example.yourassistantyora.utils
//
//object TaskCodes {
//
//    // Priority
//    fun priorityToInt(p: String): Int = when (p.trim()) {
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
//    // Status
//    fun statusToInt(s: String): Int = when (s.trim()) {
//        "Waiting" -> 0
//        "Done" -> 2
//        "Hold On" -> 3
//        "In Progress", "On Progress" -> 4
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
//    // Category
//    private val catNameToId = mapOf(
//        "Work" to 0,
//        "Study" to 1,
//        "Travel" to 2,
//        "Meeting" to 3,
//        "Project" to 4,
//        "Personal" to 5
//    )
//    private val catIdToName = catNameToId.entries.associate { (k, v) -> v to k }
//
//    fun categoryToInt(name: String): Int = catNameToId[name.trim()] ?: 0
//    fun intToCategory(v: Int): String = catIdToName[v] ?: "Work"
//
//    fun categoriesToIds(names: List<String>): List<Int> =
//        names.distinct().map { categoryToInt(it) }.distinct().ifEmpty { listOf(0) }
//
//    fun idsToCategories(ids: List<Int>): List<String> =
//        ids.distinct().map { intToCategory(it) }.ifEmpty { listOf("Work") }
//
//    // Reminder (disamain sama opsi di UI kamu)
//    fun reminderToInt(text: String): Int = when (text.trim()) {
//        "Tidak ada peringat", "Tidak ada pengingat" -> 0
//        "Ingat ketepat waktu", "Ingatkan pada waktunya" -> 1
//        "Reminder 10 minute before", "Ingatkan 10 menit sebelumnya" -> 2
//        "Reminder 20 minute before", "Ingatkan 20 menit sebelumnya" -> 3
//        "Reminder 30 minute before", "Ingatkan 30 menit sebelumnya" -> 4
//        "Reminder 1 day before", "Ingatkan 1 hari sebelumnya" -> 5
//        "Reminder 2 day before", "Ingatkan 2 hari sebelumnya" -> 6
//        "Reminder 3 day before", "Ingatkan 3 hari sebelumnya" -> 7
//        else -> 0
//    }
//
//    fun intToReminder(v: Int): String = when (v) {
//        1 -> "Ingat ketepat waktu"
//        2 -> "Reminder 10 minute before"
//        3 -> "Reminder 20 minute before"
//        4 -> "Reminder 30 minute before"
//        5 -> "Reminder 1 day before"
//        6 -> "Reminder 2 day before"
//        7 -> "Reminder 3 day before"
//        else -> "Tidak ada peringat"
//    }
//}
