package com.example.yourassistantyora.models

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Representasi data untuk sebuah task yang diambil dari Firestore.
 *
 * @param id ID unik dari dokumen di Firestore.
 * @param title Judul task.
 * @param description Deskripsi detail dari task.
 * @param deadline Batas waktu task dalam format Timestamp Firebase.
 * @param priority Level prioritas: 0=Low, 1=Medium, 2=High.
 * @param category Kategori task: 0=Work, 1=Study, 2=Travel, 3=Meeting, 4=Project.
 * @param reminder Opsi pengingat.
 * @param status Status pengerjaan task: 0=Waiting, 1=To do, 2=Done, 3=Hold On, 4=On Progress.
 * @param uidUsers ID unik dari user yang memiliki task ini.
 */
data class TaskModel(
    val id: String = "",
    val Title: String = "",
    val Description: String = "",
    val Deadline: Timestamp = Timestamp.now(),
    val Priority: Int = 1,
    val Category: Int = 0,
    val Reminder: Int = 0,
    val Status: Int = 1,
    val UIDusers: String = ""
) {
    // --- Helper Properties untuk mempermudah development di UI ---

    /**
     * Mengonversi angka `priority` menjadi teks yang mudah dibaca.
     * Contoh: 2 -> "High"
     */
    val priorityText: String
        get() = when (Priority) {
            2 -> "High"
            1 -> "Medium"
            0 -> "Low"
            else -> "Medium" // Default value
        }

    /**
     * Mengonversi angka `category` menjadi teks yang mudah dibaca.
     * Contoh: 0 -> "Work"
     */
    val categoryText: String
        get() = when (Category) {
            0 -> "Work"
            1 -> "Study"
            2 -> "Travel"
            3 -> "Meeting"
            4 -> "Project"
            else -> "Other" // Default value
        }

    /**
     * Mengonversi angka `status` menjadi teks yang mudah dibaca.
     * Contoh: 1 -> "To do"
     */
    val statusText: String
        get() = when (Status) {
            0 -> "Waiting"
            1 -> "To do"
            2 -> "Done"
            3 -> "Hold On"
            4 -> "On Progress"
            else -> "To do" // Default value
        }

    /**
     * Mengonversi angka `Reminder` menjadi teks yang mudah dibaca.
     * Contoh: 2 -> "Ingatkan 10 menit sebelumnya"
     */
    val reminderText: String
        get() = when (Reminder) {
            7 -> "Ingatkan 3 hari sebelumnya"
            6 -> "Ingatkan 2 hari sebelumnya"
            5 -> "Ingatkan 1 hari sebelumnya"
            4 -> "Ingatkan 30 menit sebelumnya"
            3 -> "Ingatkan 20 menit sebelumnya"
            2 -> "Ingatkan 10 menit sebelumnya"
            1 -> "Ingatkan pada waktunya"
            0 -> "Tidak ada pengingat"
            else -> "Tidak ada pengingat"
        }

    /**
     * Properti untuk mengecek apakah task sudah selesai (status == 2).
     * Ini akan sangat berguna untuk memisahkan task aktif dan selesai di UI.
     */
    val isCompleted: Boolean
        get() = Status == 2

    /**
     * Mengonversi `deadline` (Timestamp) menjadi format waktu yang mudah dibaca.
     * Contoh: "10:00 AM"
     */
    val deadlineTimeFormatted: String
        get() = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Deadline.toDate())

    /**
     * Mengonversi `deadline` (Timestamp) menjadi format tanggal yang mudah dibaca.
     * Contoh: "Nov 23, 2025"
     */
    val deadlineDateFormatted: String
        get() = SimpleDateFormat("MMM dd, yyyy", Locale.ENGLISH).format(Deadline.toDate())
}
