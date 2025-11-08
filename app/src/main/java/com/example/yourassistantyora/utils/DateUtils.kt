package com.example.yourassistantyora.utils

import java.text.SimpleDateFormat
import java.util.*

/**
 * Utility object untuk fungsi-fungsi terkait tanggal
 */
object DateUtils {

    /**
     * Mendapatkan tanggal saat ini dalam format lengkap
     * Contoh: "Monday, 04 November 2025"
     */
    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Mendapatkan tanggal saat ini dalam format pendek
     * Contoh: "04 Nov 2025"
     */
    fun getShortDate(): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Mendapatkan hari saat ini
     * Contoh: "Monday"
     */
    fun getCurrentDay(): String {
        val sdf = SimpleDateFormat("EEEE", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Parse waktu dari string (format "HH:mm") ke jam (Int)
     * Contoh: "14:30" → 14
     */
    fun parseHourFromTime(time: String): Int {
        return try {
            time.split(":")[0].replace(Regex("[^0-9]"), "").toIntOrNull() ?: 0
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Group tasks by time period berdasarkan jam
     */
    fun getTimePeriod(hour: Int): String {
        return when (hour) {
            in 5..10 -> "Morning"
            in 11..14 -> "Afternoon"
            in 15..18 -> "Evening"
            in 19..21 -> "Night"
            else -> "Other"
        }
    }
}

/**
 * Extension function untuk String time
 * Contoh: "14:30 PM".getHour() → 14
 */
fun String.getHour(): Int {
    return DateUtils.parseHourFromTime(this)
}

/**
 * Extension function untuk String time
 * Contoh: "14:30 PM".getTimePeriod() → "Afternoon"
 */
fun String.getTimePeriod(): String {
    return DateUtils.getTimePeriod(this.getHour())
}