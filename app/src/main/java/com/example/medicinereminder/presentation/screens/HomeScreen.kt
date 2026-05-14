package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.data.model.Medicine
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.data.model.ReminderStatus
import com.example.medicinereminder.presentation.components.AppToolbar
import com.example.medicinereminder.presentation.components.DashboardCard
import com.example.medicinereminder.presentation.components.MedicineCard
import com.example.medicinereminder.presentation.components.SmartInsightCard
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.viewmodel.AuthViewModel
import com.example.medicinereminder.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminder.presentation.viewmodel.ProfileViewModel
import com.example.medicinereminder.presentation.viewmodel.ReminderViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val factory = ViewModelFactory(context)
    
    val authViewModel: AuthViewModel = viewModel(modelClass = AuthViewModel::class.java, factory = factory)
    val medicineViewModel: MedicineViewModel = viewModel(modelClass = MedicineViewModel::class.java, factory = factory)
    val reminderViewModel: ReminderViewModel = viewModel(modelClass = ReminderViewModel::class.java, factory = factory)
    val profileViewModel: ProfileViewModel = viewModel(modelClass = ProfileViewModel::class.java, factory = factory)
    val familyViewModel: com.example.medicinereminder.presentation.viewmodel.FamilyProfileViewModel = viewModel(modelClass = com.example.medicinereminder.presentation.viewmodel.FamilyProfileViewModel::class.java, factory = factory)

    val medicinesState by medicineViewModel.medicinesState.collectAsState()
    val remindersState by reminderViewModel.remindersState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val currentFamilyProfile by familyViewModel.currentProfile.collectAsState()
    val allFamilyProfiles by familyViewModel.profiles.collectAsState()
    val interactionWarning by medicineViewModel.interactionWarning.collectAsState()

    val medicineCount = if (medicinesState is Resource.Success) (medicinesState as Resource.Success<List<Medicine>>).data?.size ?: 0 else 0
    val pendingCount = if (remindersState is Resource.Success) {
        (remindersState as Resource.Success<List<ReminderRecord>>).data?.count { it.status == ReminderStatus.PENDING || it.status == ReminderStatus.SNOOZED } ?: 0
    } else 0
    
    val profile = profileState.profile
    val isProfileIncomplete = profile.weight == null || profile.bloodGroup.isNullOrBlank() || profile.gender.isNullOrBlank()

    val userId = authViewModel.userState.value?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) familyViewModel.loadProfiles(userId)
    }

    LaunchedEffect(key1 = true) {
        authViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            AppToolbar(
                title = "My Health",
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AIChat.route) }) {
                        Icon(Icons.Default.Chat, contentDescription = "AI Assistant")
                    }
                    IconButton(onClick = { navController.navigate(Screen.TodayReminders.route) }) {
                        BadgedBox(badge = { if (pendingCount > 0) Badge { Text("$pendingCount") } }) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { navController.navigate(Screen.OCRScanner.route) },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.DocumentScanner, contentDescription = "Scan Prescription")
                }
                Spacer(modifier = Modifier.height(16.dp))
                ExtendedFloatingActionButton(
                    onClick = { navController.navigate(Screen.AddMedicine.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Med") }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Family Profile Switcher
            if (allFamilyProfiles is Resource.Success) {
                val profiles = (allFamilyProfiles as Resource.Success<List<com.example.medicinereminder.data.model.FamilyProfile>>).data
                if (!profiles.isNullOrEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp)
                    ) {
                        items(profiles) { profile ->
                            com.example.medicinereminder.presentation.components.FamilyProfileCard(
                                profile = profile,
                                isSelected = currentFamilyProfile?.id == profile.id,
                                onClick = { familyViewModel.switchProfile(profile) }
                            )
                        }
                    }
                }
            }

            // Profile Completion Prompt
            if (isProfileIncomplete) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Complete Your Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Add health details for better AI insights", style = MaterialTheme.typography.labelSmall)
                        }
                        TextButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            Text("Complete", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Hero Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Good Morning,",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = authViewModel.userState.value?.name ?: "Healthy User",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.Analytics.route) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.AutoGraph, contentDescription = "Analytics", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }

            // Smart Insight Section
            SmartInsightCard(
                title = "Smart Tip",
                description = "Taking Vitamin D with a meal improves absorption. Try scheduled it during lunch.",
                onClick = { navController.navigate(Screen.AIChat.route) }
            )

            if (interactionWarning != null) {
                SmartInsightCard(
                    title = "Safety Alert",
                    description = interactionWarning!!,
                    isWarning = true,
                    onClick = { navController.navigate(Screen.AIChat.route) }
                )
            }

            // Daily Progress Section
            DailyProgressCard(remindersState)

            Spacer(modifier = Modifier.height(24.dp))

            // Streak & Quick Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = Color(0xFFFF9800))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("3 Day", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Streak", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f))
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Color(0xFF4CAF50))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("85%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Adherence", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timeline Preview
            SectionHeader(
                title = "Today's Timeline",
                actionText = "Timeline",
                onActionClick = { navController.navigate(Screen.TodayReminders.route) }
            )

            UpcomingReminderSection(remindersState)

            Spacer(modifier = Modifier.height(32.dp))

            // Health Tracking Grid
            Text(
                text = "Health Tracking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionCard(
                    title = "Calendar",
                    icon = Icons.Default.CalendarMonth,
                    color = Color(0xFF673AB7),
                    onClick = { navController.navigate(Screen.Calendar.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Reports",
                    icon = Icons.Default.BarChart,
                    color = Color(0xFF009688),
                    onClick = { navController.navigate(Screen.Reports.route) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuickActionCard(
                    title = "Voice",
                    icon = Icons.Default.Mic,
                    color = Color(0xFFFF5722),
                    onClick = { navController.navigate(Screen.VoiceAssistant.route) },
                    modifier = Modifier.weight(1f)
                )
                QuickActionCard(
                    title = "Scan",
                    icon = Icons.Default.DocumentScanner,
                    color = Color(0xFFE91E63),
                    onClick = { navController.navigate(Screen.OCRScanner.route) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // My Medications
            SectionHeader(
                title = "My Medications",
                actionText = "View All",
                onActionClick = { navController.navigate(Screen.MedicineList.route) }
            )

            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                when (val result = medicinesState) {
                    is Resource.Success -> {
                        val recent = result.data?.take(3) ?: emptyList()
                        if (recent.isEmpty()) {
                            DashboardCard(
                                title = "No medicines yet",
                                description = "Add your first medicine reminder to get started.",
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        } else {
                            Column {
                                recent.forEach { medicine ->
                                    MedicineCard(
                                        medicine = medicine,
                                        onClick = {
                                            navController.navigate("medicine_details/${medicine.medicineId}")
                                        }
                                    )
                                }
                            }
                        }
                    }
                    is Resource.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    else -> {}
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // History Log Button
            Card(
                onClick = { navController.navigate(Screen.ReminderHistory.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 100.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onTertiary)
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Medicine Logs", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Review your adherence history", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun DailyProgressCard(remindersState: Resource<List<ReminderRecord>>) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val total = if (remindersState is Resource.Success) remindersState.data?.size ?: 0 else 0
            val taken = if (remindersState is Resource.Success) remindersState.data?.count { it.status == ReminderStatus.TAKEN } ?: 0 else 0
            val missed = if (remindersState is Resource.Success) remindersState.data?.count { it.status == ReminderStatus.MISSED } ?: 0 else 0
            val progress = if (total > 0) taken.toFloat() / total else 0f

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                    strokeWidth = 8.dp,
                    progress = { 1f }
                )
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                    progress = { progress }
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Column {
                Text(
                    text = "Daily Progress",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = when {
                        total == 0 -> "No meds today"
                        missed > 0 -> "$taken of $total taken • $missed missed"
                        else -> "$taken of $total doses taken"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, containerColor: Color, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = title, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        TextButton(onClick = onActionClick) {
            Text(actionText, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
fun UpcomingReminderSection(state: Resource<List<ReminderRecord>>) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        when (val result = state) {
            is Resource.Success -> {
                val upcoming = result.data?.filter { 
                    it.status == ReminderStatus.PENDING || 
                    it.status == ReminderStatus.SNOOZED 
                }?.sortedBy { it.scheduledTime }?.firstOrNull()

                if (upcoming != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Row(
                            modifier = Modifier.padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Next Dose",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = upcoming.medicineName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = upcoming.reminderTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                } else {
                    DashboardCard(
                        title = "All done for today!",
                        description = "You've taken all your medications for now.",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
            else -> {}
        }
    }
}
