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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.example.medicinereminder.presentation.components.*
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.viewmodel.*
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
    val familyViewModel: FamilyProfileViewModel = viewModel(modelClass = FamilyProfileViewModel::class.java, factory = factory)

    val medicinesState by medicineViewModel.medicinesState.collectAsState()
    val remindersState by reminderViewModel.remindersState.collectAsState()
    val profileState by profileViewModel.uiState.collectAsState()
    val currentFamilyProfile by familyViewModel.currentProfile.collectAsState()
    val allFamilyProfiles by familyViewModel.profiles.collectAsState()
    val interactionWarning by medicineViewModel.interactionWarning.collectAsState()

    val pendingCount = if (remindersState is Resource.Success) {
        (remindersState as Resource.Success<List<ReminderRecord>>).data?.count { it.status == ReminderStatus.PENDING || it.status == ReminderStatus.SNOOZED } ?: 0
    } else 0
    
    val profile = profileState.profile
    val isProfileIncomplete = profile.weight == null || profile.bloodGroup.isNullOrBlank() || profile.gender.isNullOrBlank()

    val userId = authViewModel.userState.value?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) familyViewModel.loadProfiles(userId)
    }

    Scaffold(
        topBar = {
            AppToolbar(
                title = "My Health",
                actions = {
                    IconButton(
                        onClick = { navController.navigate(Screen.AIChat.route) },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = "AI Assistant", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(
                        onClick = { navController.navigate(Screen.TodayReminders.route) },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        BadgedBox(badge = { if (pendingCount > 0) Badge { Text("$pendingCount") } }) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { navController.navigate(Screen.AddMedicine.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Med", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Hero Section: Greeting & Family
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Hello,",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = authViewModel.userState.value?.name ?: "Healthy User",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = { navController.navigate(Screen.Analytics.route) },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    ) {
                        Icon(Icons.Default.AutoGraph, contentDescription = "Analytics", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Family Profile Switcher (Refined)
            if (allFamilyProfiles is Resource.Success) {
                val profiles = (allFamilyProfiles as Resource.Success<List<com.example.medicinereminder.data.model.FamilyProfile>>).data
                if (!profiles.isNullOrEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(vertical = 12.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(profiles) { profile ->
                            FamilyProfileCard(
                                profile = profile,
                                isSelected = currentFamilyProfile?.id == profile.id,
                                onClick = { familyViewModel.switchProfile(profile) }
                            )
                        }
                    }
                }
            }

            // Profile Completion Prompt (Refined)
            if (isProfileIncomplete) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Complete Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                            Text("Better AI tracking awaits", style = MaterialTheme.typography.labelSmall)
                        }
                        Button(
                            onClick = { navController.navigate(Screen.Profile.route) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Start", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // Daily Progress Section (Redesigned)
            DailyProgressSection(remindersState)

            // Smart Insight Section
            SmartInsightCard(
                title = "Smart Tip",
                description = "Taking Vitamin D with a meal improves absorption. Try scheduling it during lunch.",
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

            // Section: Quick Stats (Streak & Adherence)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCardSmall(
                    label = "Current Streak",
                    value = "3 Days",
                    icon = Icons.Default.LocalFireDepartment,
                    color = Color(0xFFF97316)
                )
                StatCardSmall(
                    label = "Adherence",
                    value = "85%",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF10B981)
                )
            }

            // Section: Next Dose Timeline
            SectionHeader(
                title = "Upcoming",
                actionText = "Timeline",
                onActionClick = { navController.navigate(Screen.TodayReminders.route) }
            )
            UpcomingReminderSection(remindersState)

            // Section: My Medications
            SectionHeader(
                title = "Medications",
                actionText = "View All",
                onActionClick = { navController.navigate(Screen.MedicineList.route) }
            )
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                when (val result = medicinesState) {
                    is Resource.Success -> {
                        val recent = result.data?.take(3) ?: emptyList()
                        if (recent.isEmpty()) {
                            DashboardCard(
                                title = "No meds added",
                                description = "Start tracking your medications by adding your first reminder.",
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

            // Section: Health Toolkit (Grid)
            Text(
                text = "Health Toolkit",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            ToolkitGrid(navController)

            // Bottom Spacer for FAB
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun DailyProgressSection(state: Resource<List<ReminderRecord>>) {
    val total = if (state is Resource.Success) state.data?.size ?: 0 else 0
    val taken = if (state is Resource.Success) state.data?.count { it.status == ReminderStatus.TAKEN } ?: 0 else 0
    val progress = if (total > 0) taken.toFloat() / total else 0f

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(84.dp)) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    strokeWidth = 10.dp,
                    progress = { 1f }
                )
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round,
                    progress = { progress }
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(24.dp))
            
            Column {
                Text(
                    text = "Daily Adherence",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = if (total == 0) "No medications today" else "$taken of $total doses taken",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun StatCardSmall(label: String, value: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(24.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ToolkitGrid(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ToolkitItem(
                title = "Calendar",
                icon = Icons.Default.CalendarMonth,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Calendar.route) }
            )
            ToolkitItem(
                title = "Reports",
                icon = Icons.Default.BarChart,
                color = Color(0xFF10B981),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Reports.route) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ToolkitItem(
                title = "Voice",
                icon = Icons.Default.Mic,
                color = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.VoiceAssistant.route) }
            )
            ToolkitItem(
                title = "Scan",
                icon = Icons.Default.DocumentScanner,
                color = Color(0xFFEC4899),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.OCRScanner.route) }
            )
        }
    }
}

@Composable
fun ToolkitItem(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = CircleShape,
                color = color.copy(alpha = 0.15f),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
            Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
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
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 2.dp,
                        border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Next Dose",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = upcoming.medicineName,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = upcoming.reminderTime,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.NotificationsActive,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    DashboardCard(
                        title = "Healthy & Done!",
                        description = "You've taken all your medications for today. Great job!",
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            else -> {}
        }
    }
}
