package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.viewmodel.PreferencesViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: PreferencesViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val themeMode by viewModel.themeMode.collectAsState()
    val dynamicColors by viewModel.dynamicColors.collectAsState()
    val notificationsEnabled by viewModel.notificationsEnabled.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            SettingSectionHeader("Preferences")
            
            SettingToggleItem(
                title = "Push Notifications",
                description = "Receive reminders for your medications",
                icon = Icons.Default.Notifications,
                checked = notificationsEnabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
            )
            
            SettingToggleItem(
                title = "Dark Mode",
                description = "Enable dark theme for the app",
                icon = Icons.Default.DarkMode,
                checked = themeMode == "dark",
                onCheckedChange = { isDark ->
                    viewModel.setThemeMode(if (isDark) "dark" else "light")
                }
            )

            SettingToggleItem(
                title = "Dynamic Colors",
                description = "Use system dynamic color palette",
                icon = Icons.Default.Palette,
                checked = dynamicColors,
                onCheckedChange = { viewModel.setDynamicColors(it) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            SettingSectionHeader("Data & Privacy")
            
            SettingItem(
                title = "Backup & Restore",
                description = "Sync your data with cloud storage",
                icon = Icons.Default.CloudUpload,
                onClick = { navController.navigate(Screen.Backup.route) }
            )
            
            SettingItem(
                title = "Security & App Lock",
                description = "Protect your health data with PIN",
                icon = Icons.Default.Security,
                onClick = { navController.navigate(Screen.Security.route) }
            )

            Spacer(modifier = Modifier.height(24.dp))
            SettingSectionHeader("Healthcare Ecosystem")
            
            SettingItem(
                title = "Family Profiles",
                description = "Manage medicines for family members",
                icon = Icons.Default.Groups,
                onClick = { navController.navigate(Screen.FamilyProfiles.route) }
            )
            
            SettingItem(
                title = "Caregivers",
                description = "Invite doctors or family to monitor",
                icon = Icons.Default.HealthAndSafety,
                onClick = { navController.navigate(Screen.CaregiverDashboard.route) }
            )
            
            SettingItem(
                title = "Emergency (SOS)",
                description = "Manage emergency contacts and alerts",
                icon = Icons.Default.Sos,
                onClick = { navController.navigate(Screen.Emergency.route) }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            SettingSectionHeader("About")
            
            SettingItem(
                title = "Version",
                description = "1.0.0 (Premium AI Edition)",
                icon = Icons.Default.Info,
                onClick = { navController.navigate(Screen.About.route) }
            )
            
            SettingItem(
                title = "Privacy Policy",
                description = "How we handle your health data",
                icon = Icons.Default.Lock,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 16.dp)
    )
}

@Composable
fun SettingToggleItem(
    title: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
