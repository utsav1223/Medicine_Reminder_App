package com.example.medicinereminder.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val title: String,
    val route: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem("Home", Screen.Home.route, Icons.Default.Home)
    object Medicines : BottomNavItem("Meds", Screen.MedicineList.route, Icons.Default.Medication)
    object Analytics : BottomNavItem("Stats", Screen.Analytics.route, Icons.Default.Analytics)
    object Profile : BottomNavItem("Profile", Screen.Profile.route, Icons.Default.Person)
}
