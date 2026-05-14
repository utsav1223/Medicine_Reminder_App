package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.data.model.ReminderStatus
import com.example.medicinereminder.presentation.components.AppToolbar
import com.example.medicinereminder.presentation.components.ReminderCard
import com.example.medicinereminder.presentation.components.SnoozeDialog
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.viewmodel.ReminderViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayRemindersScreen(
    navController: NavController,
    viewModel: ReminderViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val remindersState by viewModel.remindersState.collectAsState()
    var selectedReminderId by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }

    if (selectedReminderId != null) {
        SnoozeDialog(
            onDismiss = { selectedReminderId = null },
            onSnoozeSelected = { mins ->
                viewModel.snooze(selectedReminderId!!, mins)
                selectedReminderId = null
            }
        )
    }

    Scaffold(
        topBar = {
            AppToolbar(
                title = "Today's Timeline",
                showBackButton = true,
                onBackClick = { navController.popBackStack() },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Analytics.route) }) {
                        Icon(Icons.Default.BarChart, contentDescription = "Analytics")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Today, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    ScrollableTabRow(
                        selectedTabIndex = when(selectedFilter) {
                            "All" -> 0
                            "Pending" -> 1
                            "Taken" -> 2
                            else -> 0
                        },
                        edgePadding = 24.dp,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {}
                    ) {
                        FilterTab(selected = selectedFilter == "All", text = "All", count = null) { selectedFilter = "All" }
                        FilterTab(selected = selectedFilter == "Pending", text = "Pending", count = null) { selectedFilter = "Pending" }
                        FilterTab(selected = selectedFilter == "Taken", text = "Taken", count = null) { selectedFilter = "Taken" }
                    }
                }
            }

            when (val result = remindersState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val allReminders = result.data ?: emptyList()
                    val filteredReminders = when (selectedFilter) {
                        "Pending" -> allReminders.filter { it.status == ReminderStatus.PENDING || it.status == ReminderStatus.SNOOZED }
                        "Taken" -> allReminders.filter { it.status == ReminderStatus.TAKEN }
                        else -> allReminders
                    }
                    
                    val statsText = when(selectedFilter) {
                        "Pending" -> "${filteredReminders.size} doses remaining"
                        "Taken" -> "${filteredReminders.size} doses completed"
                        else -> "${allReminders.size} total doses for today"
                    }

                    if (filteredReminders.isEmpty()) {
                        EmptyTimelineState(selectedFilter)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            item {
                                Text(
                                    text = statsText,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(filteredReminders) { reminder ->
                                ReminderCard(
                                    reminder = reminder,
                                    onTake = { viewModel.updateStatus(reminder.reminderId, ReminderStatus.TAKEN) },
                                    onSkip = { viewModel.updateStatus(reminder.reminderId, ReminderStatus.SKIPPED) },
                                    onSnooze = { selectedReminderId = reminder.reminderId }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${result.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTab(selected: Boolean, text: String, count: Int?, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { 
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text)
                if (count != null) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("($count)", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        modifier = Modifier.padding(end = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun EmptyTimelineState(filter: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val message = when(filter) {
            "Pending" -> "No pending doses left for today!"
            "Taken" -> "You haven't taken any medicines yet."
            else -> "No medicines scheduled for today."
        }
        Icon(
            imageVector = Icons.Default.EventNote,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = message, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
