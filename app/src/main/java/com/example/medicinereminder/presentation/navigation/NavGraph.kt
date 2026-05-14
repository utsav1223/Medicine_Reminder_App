package com.example.medicinereminder.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.medicinereminder.presentation.screens.*

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.MedicineList.route) {
            MedicineListScreen(navController = navController)
        }
        composable(
            route = Screen.AddMedicine.route + "?scannedText={scannedText}",
            arguments = listOf(navArgument("scannedText") { 
                type = NavType.StringType
                nullable = true
                defaultValue = null
            })
        ) { backStackEntry ->
            val scannedText = backStackEntry.arguments?.getString("scannedText")
            AddMedicineScreen(navController = navController, scannedText = scannedText)
        }
        composable(
            route = Screen.MedicineDetails.route,
            arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getString("medicineId") ?: ""
            MedicineDetailsScreen(medicineId = medicineId, navController = navController)
        }
        composable(
            route = Screen.EditMedicine.route,
            arguments = listOf(navArgument("medicineId") { type = NavType.StringType })
        ) { backStackEntry ->
            val medicineId = backStackEntry.arguments?.getString("medicineId") ?: ""
            EditMedicineScreen(medicineId = medicineId, navController = navController)
        }
        composable(Screen.TodayReminders.route) {
            TodayRemindersScreen(navController = navController)
        }
        composable(Screen.ReminderHistory.route) {
            HistoryScreen(navController = navController)
        }
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
        composable(Screen.Analytics.route) {
            AnalyticsDashboardScreen(navController = navController)
        }
        composable(Screen.Calendar.route) {
            CalendarScreen(navController = navController)
        }
        composable(Screen.Reports.route) {
            ReportsScreen(navController = navController)
        }
        composable(Screen.AIChat.route) {
            AIChatScreen(navController = navController)
        }
        composable(Screen.OCRScanner.route) {
            OCRScannerScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.VoiceAssistant.route) {
            VoiceAssistantScreen(navController = navController)
        }
        composable(Screen.Backup.route) {
            BackupScreen(navController = navController)
        }
        composable(Screen.Security.route) {
            SecurityScreen(navController = navController)
        }
        composable(Screen.About.route) {
            AboutScreen(navController = navController)
        }
        composable(Screen.FAQ.route) {
            FAQScreen(navController = navController)
        }
        composable(Screen.AppIntro.route) {
            AppIntroScreen(navController = navController)
        }
        composable(Screen.CaregiverDashboard.route) {
            CaregiverDashboardScreen(navController = navController)
        }
        composable(Screen.FamilyProfiles.route) {
            FamilyProfilesScreen(navController = navController)
        }
        composable(Screen.Emergency.route) {
            EmergencyScreen(navController = navController)
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(navController = navController)
        }
    }
}
