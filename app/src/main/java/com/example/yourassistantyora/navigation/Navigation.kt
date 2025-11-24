package com.example.yourassistantyora

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.yourassistantyora.screen.CreateNoteScreen
import com.example.yourassistantyora.screen.CreateTaskScreen
import com.example.yourassistantyora.screen.CreateTeamScreen
import com.example.yourassistantyora.screen.DailyScreen
import com.example.yourassistantyora.screen.ForgotPasswordScreen
import com.example.yourassistantyora.screen.JoinTeamScreen
import com.example.yourassistantyora.screen.MonthlyScreen
import com.example.yourassistantyora.screen.NoteDetailScreen
import com.example.yourassistantyora.screen.ProfileScreen
import com.example.yourassistantyora.screen.RegisterScreen
import com.example.yourassistantyora.screen.TaskDetailScreen
import com.example.yourassistantyora.screen.TeamDetailScreen
import com.example.yourassistantyora.screen.TeamScreen
import com.example.yourassistantyora.screen.WeeklyScreen
import com.example.yourassistantyora.viewModel.AuthViewModel
import com.example.yourassistantyora.viewModel.LoginViewModel
import com.example.yourassistantyora.viewModel.RegisterViewModel
import com.example.yourassistantyora.viewModel.ForgotPasswordViewModel
import com.example.yourassistantyora.screen.CheckEmailScreen
import com.example.yourassistantyora.screen.EditProfileScreen
import com.example.yourassistantyora.screen.LoginScreen
import com.example.yourassistantyora.screen.TaskScreen
import com.example.yourassistantyora.screen.NoteScreen
import com.example.yourassistantyora.screen.HomeScreen
import com.example.yourassistantyora.screen.Task
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.yourassistantyora.viewModel.CreateTaskViewModel // Import ViewModel

// ... and so on for all screens
/**
 * Main Navigation Graph untuk seluruh aplikasi
 *
 * @param startDestination Route awal (default "login", bisa "home" jika sudah login)
 * @param userName Username dari Firestore (opsional, untuk HomeScreen)
 */
@Composable
fun AppNavigation(
    startDestination: String = "login",
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {

        // ============================================
        // AUTHENTICATION FLOW
        // ============================================

        /**
         * Login Screen - Entry point untuk user yang belum login
         * Route: "login"
         */
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                navController = navController,
                onLoginSuccess = { username ->
                    authViewModel.updateUserProfile(username, null)
                    navController.navigateAndClearBackStack("home")
                }
            )
        }

        /**
         * Register Screen - Pendaftaran user baru
         * Route: "register"
         */
        composable("register") {
            val registerViewModel: RegisterViewModel = viewModel()
            RegisterScreen(
                navController = navController,
                viewModel = registerViewModel
            )
        }


        /**
         * Forgot Password Screen - Reset password
         * Route: "forgot_password"
         */
        composable("forgot_password") {
            val forgotViewModel: ForgotPasswordViewModel = viewModel()
            ForgotPasswordScreen(
                navController = navController,
                viewModel = forgotViewModel
            )
        }

        /**
         * Check Email Screen - Konfirmasi setelah forgot password
         * Route: "check_email"
         */
        composable(
            route = "check_email/{email}",
            arguments = listOf(
                navArgument("email") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val rawEmail = backStackEntry.arguments?.getString("email") ?: ""
            CheckEmailScreen(
                navController = navController,
                email = rawEmail
            )
        }


        // ============================================
        // MAIN APP SCREENS
        // ============================================

        /**
         * Home Screen - Dashboard utama aplikasi
         * Route: "home"
         */
        composable("home") {
            HomeScreen(
                navController = navController,
            )
        }

        /**
         * Profile Screen - Lihat profil user
         * Route: "profile"
         */
        composable("profile") {
            ProfileScreen(
                navController = navController,
                userName = authViewModel.userName.value ?: "User",
                userEmail = authViewModel.currentUser.value?.email ?: "user@example.com",
                userPhotoUrl = authViewModel.userPhotoUrl.value,
                totalTasks = 10,         // sementara dummy
                completedTasks = 6,      // sementara dummy
                onLogout = {
                    authViewModel.signOut()
                    navController.navigateAndClearBackStack("login")
                }
            )
        }


        /**
         * Edit Profile Screen - Edit data profil
         * Route: "edit_profile"
         */
        composable("edit_profile") {
            EditProfileScreen(
                navController = navController,
                currentName = authViewModel.userName.value ?: "User",
                currentEmail = authViewModel.currentUser.value?.email ?: "user@example.com",
                currentPhotoUrl = authViewModel.userPhotoUrl.value,
                onSaveProfile = { newName, newPhotoUrl ->
                    authViewModel.updateUserProfile(newName, newPhotoUrl)
                }
            )
        }




        // ============================================
        // TASK MANAGEMENT SCREENS
        // ============================================

        /**
         * Task List Screen - List view semua tasks
         * Route: "task_list"
         */
        composable("task_list") {
            TaskScreen(
                navController = navController
            )
        }

        // ✅ BARU: Tambahkan route untuk TaskDetailScreen
        /**
         * Task Detail Screen - Menampilkan detail dan form edit
         * Route: "task_detail/{taskId}"
         */
        composable(
            route = "task_detail/{taskId}",
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId") ?: ""
            TaskDetailScreen(taskId = taskId, navController = navController)
        }
        /**
         * Daily Task Screen - View tasks per hari
         * Route: "task_daily"
         */
        composable("daily_tasks") {
            DailyScreen(navController = navController)
        }

        /**
         * Weekly Task Screen - View tasks per minggu
         * Route: "task_weekly"
         */
        composable("weekly_tasks") {
            WeeklyScreen(navController = navController)
        }

        /**
         * Monthly Task Screen - View tasks per bulan (Calendar view)
         * Route: "task_monthly"
         */
        composable("monthly_tasks") {
            MonthlyScreen(navController = navController)
        }

        /**
         * Task Detail Screen - Detail task dengan parameter ID
         * Route: "task_detail/{taskId}"
         *
         * Cara navigate:
         * navController.navigate("task_detail/${task.id}")
         */


        /**
         * Create Task Screen - Buat task baru
         * Route: "create_task"
         */
        composable("create_task") {
            // ViewModel akan otomatis dibuat dan di-scope ke NavBackStackEntry ini
            val createTaskViewModel: CreateTaskViewModel = viewModel()
            CreateTaskScreen(
                navController = navController,
                viewModel = createTaskViewModel
            )
        }


        // ============================================
        // NOTES SCREENS
        // ============================================

        /**
         * Notes List Screen - List semua notes
         * Route: "notes"
         */
        composable("notes") {
            NoteScreen(
                navController = navController
            )
        }

        /**
         * Note Detail Screen - Detail note dengan parameter ID
         * Route: "note_detail/{noteId}"
         *
         * Cara navigate:
         * navController.navigate("note_detail/${note.id}")
         */
        composable(
            route = "note_detail/{noteId}",
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")

            NoteDetailScreen(
                noteId = noteId,
                navController = navController
            )
        }

        /**
         * Create Note Screen - Buat note baru
         * Route: "create_note"
         */
        composable("create_note") {
            CreateNoteScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onSaveClick = { title, content, categories ->
                    // Di sini nanti kamu simpan ke database / ViewModel
                    val categoriesString = categories.joinToString(", ")

                    // TODO: Simpan note ke DB / Firestore / dsb
                    // contoh:
                    // val newNote = Note(
                    //     id = 0,
                    //     title = title,
                    //     content = content,
                    //     category = categoriesString,
                    //     time = getCurrentFormattedTime()
                    // )
                    // viewModel.insert(newNote)

                    // Setelah save, balik ke layar sebelumnya (Notes)
                    navController.popBackStack()
                }
            )
        }


        // ============================================
        // TEAM COLLABORATION SCREENS
        // ============================================

        /**
         * Team List Screen - List semua teams
         * Route: "team"
         */
        composable("team") {
            TeamScreen(
                onNavigateToHome = {
                    navController.navigateSingleTop("home")
                },
                onNavigateToTasks = {
                    navController.navigateSingleTop("task_list")
                },
                onNavigateToNotes = {
                    navController.navigateSingleTop("notes")
                },
                onNavigateToTeam = {
                    // sudah di screen ini, biasanya kosong saja
                },
                onCreateTeam = {
                    navController.navigate("create_team")
                },
                onJoinTeam = {
                    navController.navigate("join_team")
                },
                onTeamClick = { teamId ->
                    navController.navigate("team_detail/$teamId")
                }
            )
        }


        /**
         * Create Team Screen - Buat team baru
         * Route: "create_team"
         */
        composable("create_team") {
            CreateTeamScreen(
                onBackClick = {
                    // tombol Close / Cancel → balik ke screen sebelumnya
                    navController.popBackStack()
                },
                onCreateClick = { teamName, description, categories, colorScheme ->
                    // TODO: di sini nanti kamu simpan ke Firestore / Room / dsb

                    // Untuk sekarang: setelah sukses bikin tim, balik ke TeamScreen
                    navController.popBackStack() // balik ke list team
                }
            )
        }


        /**
         * Join Team Screen - Join team dengan kode
         * Route: "join_team"
         */
        composable("join_team") {
            val context = LocalContext.current

            JoinTeamScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onJoinClick = { inviteCode ->
                    // Validasi simple dulu (bawa logika dari Activity lama)
                    val isValid = inviteCode.length == 6 && inviteCode.matches(Regex("[A-Z0-9]{6}"))

                    if (isValid) {
                        Toast.makeText(
                            context,
                            "Successfully joined team!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Setelah join berhasil → balik ke TeamScreen
                        navController.popBackStack()
                    } else {
                        Toast.makeText(
                            context,
                            "Invalid invite code",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            )
        }


        /**
         * Team Detail Screen - Detail team dengan parameter ID
         * Route: "team_detail/{teamId}"
         *
         * Cara navigate:
         * navController.navigate("team_detail/${team.id}")
         */
        composable(
            route = "team_detail/{teamId}",
            arguments = listOf(
                navArgument("teamId") {
                    type = NavType.StringType
                    nullable = false
                }
            )
        ) { backStackEntry ->
            val teamId = backStackEntry.arguments?.getString("teamId")

            TeamDetailScreen(
                navController = navController,
                teamId = teamId ?: ""
            )
        }
    }
}


// ============================================
// HELPER: Navigation Extension Functions
// ============================================

/**
 * Navigate dengan clear back stack (untuk logout/login flow)
 *
 * Usage:
 * navController.navigateAndClearBackStack("login")
 */
fun androidx.navigation.NavController.navigateAndClearBackStack(route: String) {
    this.navigate(route) {
        popUpTo(0) { inclusive = true }
        launchSingleTop = true
    }
}

/**
 * Navigate dengan single top (prevent duplicate screen)
 *
 * Usage:
 * navController.navigateSingleTop("home")
 */
fun androidx.navigation.NavController.navigateSingleTop(route: String) {
    this.navigate(route) {
        launchSingleTop = true
    }
}