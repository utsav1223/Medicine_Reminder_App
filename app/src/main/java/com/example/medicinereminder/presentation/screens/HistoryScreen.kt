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
import com.example.medicinereminder.data.model.ReminderRecord
import com.example.medicinereminder.data.model.ReminderStatus
import com.example.medicinereminder.presentation.components.AppToolbar
import com.example.medicinereminder.presentation.components.ReminderCard
import com.example.medicinereminder.presentation.viewmodel.HistoryViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val historyState by viewModel.historyState.collectAsState()

    Scaffold(
        topBar = {
            AppToolbar(
                title = "Adherence Logs",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val result = historyState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val history = result.data ?: emptyList()
                    
                    // Adherence Stats Card
                    AdherenceStatsCard(history)

                    if (history.isEmpty()) {
                        EmptyHistoryState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            val groupedHistory = history.groupBy { 
                                val date = Date(it.reminderDate)
                                SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(date)
                            }

                            groupedHistory.forEach { (date, records) ->
                                item {
                                    Text(
                                        text = date,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                                items(records) { record ->
                                    ReminderCard(
                                        reminder = record,
                                        showActions = false
                                    )
                                }
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
fun AdherenceStatsCard(history: List<ReminderRecord>) {
    val total = history.count { it.status != ReminderStatus.PENDING && it.status != ReminderStatus.SNOOZED }
    val taken = history.count { it.status == ReminderStatus.TAKEN }
    val adherenceRate = if (total > 0) (taken.toFloat() / total * 100).toInt() else 0

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Adherence Rate",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Text(
                    text = "$adherenceRate%",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { adherenceRate / 100f },
                    modifier = Modifier.size(60.dp),
                    color = MaterialTheme.colorScheme.secondary,
                    strokeWidth = 6.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun EmptyHistoryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Assignment,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No history available",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
