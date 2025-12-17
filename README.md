# YORA

Aplikasi mobile berbasis Android yang membantu pengguna mengatur jadwal, catatan, dan tugas secara pribadi maupun kolaboratif dengan tim. Aplikasi ini dirancang untuk meningkatkan produktivitas dengan fitur manajemen tugas yang komprehensif, pengingat otomatis, dan kolaborasi tim real-time.

---

## 👥 Anggota Kelompok

| Nama                           | NIM       |
|--------------------------------|-----------|
| Martin Anugrah Dominick Manalu | 231402045 |
| Rama Davy Fahrezy Kacaribu     | 231402054 |
| Reka Oktavia                   | 231402063 |
| Fahri Alhamda                  | 231402066 |
| Marvitha Khairani              | 231402103 |
---

## ✨ Fitur Utama

### 🔐 1. Manajemen User & Autentikasi
- **Login/Register** dengan validasi email dan password
- **Forgot Password** untuk reset password via email
- **Profil User** dengan upload foto dan edit informasi
- **Mode Penggunaan**: Pribadi atau Tim (toggle sesuai kebutuhan)

### 📅 2. Penjadwalan & To-Do List
- **Tambah Task** dengan detail lengkap (judul, deskripsi, tanggal, jam, prioritas, kategori)
- **3 Mode Tampilan**: List View, Daily View, dan Weekly View
- **Edit/Hapus Task** dengan konfirmasi
- **Mark as Done** dengan animasi dan pemindahan ke History
- **Reminder/Notifikasi** yang dapat dikustomisasi (10 menit, 30 menit, 1 jam sebelum task)
- Filter berdasarkan kategori dan prioritas
- Sort berdasarkan tanggal, prioritas, atau alfabetis

### 📝 3. Catatan Cepat (Quick Notes)
- **Tambah Catatan** dengan rich text editor
- **Tag Manual & Otomatis** menggunakan AI untuk kategorisasi
- **Pencarian Cepat** dengan highlight keyword
- Preview 2 baris pertama di list notes
- Filter berdasarkan tag (Work, Meeting, Idea, Shopping, Study)

### 🏠 4. Dashboard & Agenda Harian
- **Greeting Personal** berdasarkan waktu (Good morning/afternoon/evening)
- **Today's Tasks** dengan progress bar completion
- **Quick Notes Preview** menampilkan 2 catatan terbaru
- **FAB Shortcuts** untuk akses cepat (Add Task, Add Note, Add Team Task)

### 👥 5. Fitur Kolaboratif Tim

#### Manajemen Tim/Grup
- **Buat Tim Baru** dengan nama, deskripsi, dan kategori
- **Invite Members** via invite code atau email
- **2 Role System**: Admin (full control) dan Member (limited access)
- Member list dengan badge role

#### Task Delegation
- **Assign Task** ke multiple members
- **Update Status**: Not Started, In Progress, Done dengan color indicator
- **Komentar/Diskusi** real-time di setiap task
- Push notification untuk assignment dan update

#### Progress Tracking
- **Team Dashboard** dengan progress bar dan completion percentage
- **Data Visualization**: Pie chart dan bar chart untuk task breakdown
- **Filter View** berdasarkan member, deadline, prioritas, atau status
- **Reminder Otomatis** untuk overdue tasks

### 🔔 6. Notifikasi & Reminder
- Reminder Deadline Task
- Notifikasi Pemberian Tugas
- Notifikasi Komentar
- Notifikasi Undangan Tim

### 🔍 7. Filter, Sort & Pencarian
- Filter by date, priority, status, dan category
- Sort by deadline, alphabetical, creation date
- Global search dengan highlight keyword
- Recent searches cache

---

## 🚀 Fitur Opsional/Advanced (Pengembangan Lanjutan)

### 🎨 Tampilan & Personalisasi
- **Theme**: Light Mode, Dark Mode, System Default
- **Color Accent** yang dapat dipilih
- **Home Screen Widget** untuk quick view today's tasks
- **Status Aktivitas** untuk team visibility

---

## 🛠️ Deskripsi Project

### Platform
- **Native Android** menggunakan Kotlin
- Target: Smartphone & Tablet Android

### Teknologi & Tools

#### Development Environment
- **Android Studio**: Ladybug | 2024.2.1+
- **Minimum SDK**: API 27 (Android 8.1 Oreo)
- **Target & Compile SDK**: API 36 (Android 16 Preview)
- **Language**: Kotlin dengan Jetpack Compose (Full Declarative UI)
- **JDK Version**: JDK 11

#### Libraries & Dependencies
- **Jetpack Components**:
    - Lifecycle & ViewModel
    - Navigation Component
    - DataStore (untuk session management)
    - WorkManager (untuk background tasks & reminders)

- **UI/UX**:
    - Jetpack Compose
    - Material Design 3 (Compose)
    - Navigation Compose
    - LazyColumn & LazyRow (pengganti RecyclerView)
    - Navigation Compose
    - Compose Animation (animateItemPlacement, transition & state animation)

#### Cloud & Backend Integration
-**Firebase Ecosystem**
 - Firebase Authentication (Email/Password & Google Sign-In)
 - Cloud Firestore (Real-time NoSQL Database)
-**Media Management**
 - Cloudinary (Cloud-based Image & Video Storage)
 - Coil (Image Loading untuk Jetpack Compose)

#### Networking & Data Processing
- Gson (JSON Parsing & Serialization)
   
---
