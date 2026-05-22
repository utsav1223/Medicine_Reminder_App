package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.example.medicinereminder.ui.theme.GradientEnd
import com.example.medicinereminder.ui.theme.GradientStart
import com.example.medicinereminder.utils.Resource
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
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
    val interactionWarning by medicineViewModel.interactionWarning.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        reminderViewModel.eventFlow.collect { event ->
            when (event) {
                is ReminderViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val pendingCount = if (remindersState is Resource.Success) {
        (remindersState as Resource.Success<List<ReminderRecord>>).data?.count { it.status == ReminderStatus.PENDING || it.status == ReminderStatus.SNOOZED } ?: 0
    } else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Good Morning,",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = authViewModel.userState.value?.name ?: "Healthy User",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AIChat.route) }) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { navController.navigate(Screen.TodayReminders.route) }) {
                        BadgedBox(badge = { if (pendingCount > 0) Badge { Text("$pendingCount") } }) {
                            Icon(Icons.Default.NotificationsNone, contentDescription = "Notifications")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.AddMedicine.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 0. Features Carousel
            FeatureCarousel()

            // 1. Professional Daily Adherence Section
            DailyProgressSection(remindersState)

            // 2. Upcoming Dose Highlight (The "Hero" Card)
            UpcomingReminderSection(remindersState, reminderViewModel)

            // 3. Smart Health Insights (AI Generated Style)
            if (interactionWarning != null) {
                InteractionWarningCard(interactionWarning!!, onDismiss = { medicineViewModel.clearWarning() })
            } else {
                SmartInsightSection()
            }

            // 4. Quick Toolkit Grid (Modern look)
            SectionHeader(title = "Health Toolkit")
            ToolkitGrid(navController)

            // 5. Recent Medications
            SectionHeader(
                title = "My Medications",
                actionText = "View All",
                onActionClick = { navController.navigate(Screen.MedicineList.route) }
            )
            RecentMedicinesSection(medicinesState, navController)

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
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(72.dp)) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    strokeWidth = 8.dp,
                    progress = { 1f }
                )
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.primary,
                    strokeWidth = 8.dp,
                    strokeCap = StrokeCap.Round,
                    progress = { progress }
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column {
                Text(
                    text = "Daily Adherence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (total == 0) "No doses scheduled" else "$taken of $total doses taken",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun UpcomingReminderSection(state: Resource<List<ReminderRecord>>, viewModel: ReminderViewModel) {
    val upcoming = if (state is Resource.Success) {
        state.data?.filter { it.status == ReminderStatus.PENDING || it.status == ReminderStatus.SNOOZED }
            ?.sortedBy { it.scheduledTime }?.firstOrNull()
    } else null

    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
        if (upcoming != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(GradientStart, GradientEnd)))
                        .padding(24.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("UPCOMING DOSE", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(upcoming.medicineName, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                            Text("${upcoming.dosage} • ${upcoming.reminderTime}", color = Color.White.copy(alpha = 0.9f), style = MaterialTheme.typography.titleMedium)
                            
                            Spacer(modifier = Modifier.height(24.dp))
                            
                            Button(
                                onClick = { viewModel.updateStatus(upcoming.reminderId, ReminderStatus.TAKEN) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = GradientStart),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Mark as Taken", fontWeight = FontWeight.ExtraBold)
                            }
                        }

                        AsyncImage(
                            model = "https://cdn-icons-png.flaticon.com/512/883/883356.png",
                            contentDescription = null,
                            modifier = Modifier.size(80.dp).padding(8.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }
        } else {
            DashboardCard(
                title = "All Set for Today!",
                description = "You've successfully managed all your scheduled doses. Keep it up!",
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun SmartInsightSection() {
    val tips = listOf(
        "Consistency is key. Taking meds at the same time each day increases effectiveness.",
        "Stay hydrated! Proper water intake helps your body process medications better.",
        "Store medicines in a cool, dry place away from direct sunlight for maximum shelf life.",
        "Don't skip doses. If you miss one, check your prescription or consult your doctor.",
        "Walking for just 15 minutes a day can significantly improve your cardiovascular health."
    )
    val randomTip = remember { tips.random() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Lightbulb, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("Health Insight", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.secondary)
                Text(randomTip, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun InteractionWarningCard(message: String, onDismiss: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Interaction Alert", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                Text(message, style = MaterialTheme.typography.bodyMedium)
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ToolkitGrid(navController: NavController) {
    Column(modifier = Modifier.padding(horizontal = 24.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToolkitCardItem(
                title = "Analytics",
                icon = Icons.Default.BarChart,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Analytics.route) }
            )
            ToolkitCardItem(
                title = "Scan",
                icon = Icons.Default.DocumentScanner,
                color = Color(0xFFEC4899),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.OCRScanner.route) }
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ToolkitCardItem(
                title = "Calendar",
                icon = Icons.Default.CalendarMonth,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.Calendar.route) }
            )
            ToolkitCardItem(
                title = "AI Chat",
                icon = Icons.Default.AutoAwesome,
                color = Color(0xFF8B5CF6),
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.AIChat.route) }
            )
        }
    }
}

@Composable
fun ToolkitCardItem(title: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            }
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun RecentMedicinesSection(state: Resource<List<Medicine>>, navController: NavController) {
    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
        when (state) {
            is Resource.Success -> {
                val recent = state.data?.take(3) ?: emptyList()
                if (recent.isEmpty()) {
                    EmptyState(description = "Start your journey by adding your first medication.")
                } else {
                    Column {
                        recent.forEach { medicine ->
                            MedicineCard(medicine = medicine, onClick = { navController.navigate("medicine_details/${medicine.medicineId}") })
                        }
                    }
                }
            }
            is Resource.Loading -> {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            else -> {}
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FeatureCarousel() {
    val features = listOf(
        FeatureItem(
            "Smart Reminders",
            "Never miss a dose with our AI-powered notification system.",
            "https://img.freepik.com/free-vector/medical-appointment-concept-illustration_114360-6535.jpg",
            MaterialTheme.colorScheme.primary
        ),
        FeatureItem(
            "OCR Prescription",
            "Simply scan your prescription to add medications instantly.",
            "https://img.freepik.com/free-vector/doctor-character-background_1270-84.jpg",
            Color(0xFFEC4899)
        ),
        FeatureItem(
            "Health Analytics",
            "Visualize your medication adherence and health progress.",
            "https://img.freepik.com/free-vector/health-data-concept-illustration_114360-6088.jpg",
            Color(0xFF10B981)
        )
    )

    val pagerState = rememberPagerState(pageCount = { features.size })

    LaunchedEffect(pagerState.currentPage) {
        delay(4000)
        val nextPage = (pagerState.currentPage + 1) % features.size
        pagerState.animateScrollToPage(nextPage)
    }

    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 24.dp),
            pageSpacing = 16.dp
        ) { page ->
            FeatureCard(features[page])
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(Modifier.fillMaxWidth().height(8.dp), horizontalArrangement = Arrangement.Center) {
            repeat(features.size) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                Box(modifier = Modifier.padding(2.dp).clip(CircleShape).background(color).size(8.dp))
            }
        }
    }
}

data class FeatureItem(val title: String, val description: String, val imageUrl: String, val color: Color)

@Composable
fun FeatureCard(feature: FeatureItem) {
    Card(
        modifier = Modifier.fillMaxWidth().height(160.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = feature.color.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp).fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(feature.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = feature.color)
                Spacer(modifier = Modifier.height(4.dp))
                Text(feature.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.width(16.dp))
            AsyncImage(
                model = feature.imageUrl,
                contentDescription = null,
                modifier = Modifier.size(100.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
    }
}
