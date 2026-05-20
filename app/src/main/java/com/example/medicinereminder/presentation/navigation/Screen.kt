package com.example.medicinereminder.presentation.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object VerifyOtp : Screen("verify_otp/{email}") {
        fun passEmail(email: String) = "verify_otp/$email"
    }
    object ResetPassword : Screen("reset_password/{code}") {
        fun passCode(code: String) = "reset_password/$code"
    }
    object Home : Screen("home")
    object MedicineList : Screen("medicine_list")
    object AddMedicine : Screen("add_medicine")
    object EditMedicine : Screen("edit_medicine/{medicineId}")
    object MedicineDetails : Screen("medicine_details/{medicineId}")
    object TodayReminders : Screen("today_reminders")
    object ReminderHistory : Screen("reminder_history")
    object Settings : Screen("settings")
    object Analytics : Screen("analytics")
    object Reports : Screen("reports")
    object Calendar : Screen("calendar")
    object AIChat : Screen("ai_chat")
    object OCRScanner : Screen("ocr_scanner")
    object VoiceAssistant : Screen("voice_assistant")
    object Profile : Screen("profile")
    object Backup : Screen("backup")
    object Security : Screen("security")
    object About : Screen("about")
    object FAQ : Screen("faq")
    object AppIntro : Screen("app_intro")
    object CaregiverDashboard : Screen("caregiver_dashboard")
    object FamilyProfiles : Screen("family_profiles")
    object Emergency : Screen("emergency")
    object AdminDashboard : Screen("admin_dashboard")
}
