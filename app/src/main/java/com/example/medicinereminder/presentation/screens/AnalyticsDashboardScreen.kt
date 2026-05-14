package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.example.medicinereminder.presentation.components.*
import com.example.medicinereminder.presentation.viewmodel.AnalyticsViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsDashboardScreen(
    navController: NavController,
    viewModel: AnalyticsViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val summaryState by viewModel.summaryState.collectAsState()
    val insightsState by viewModel.insightsState.collectAsState()

    Scaffold(
        topBar = {
            AppToolbar(
                title = "Analytics & Insights",
                showBackButton = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            when (val summaryResult = summaryState) {
                is Resource.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is Resource.Success -> {
                    val summary = summaryResult.data!!
                    
                    // Main Adherence Ring
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        AdherenceRing(
                            progress = summary.averageAdherence / 100f,
                            modifier = Modifier.size(200.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Text(text = "Weekly Adherence", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    AdherenceChart(
                        modelProducer = viewModel.modelProducer,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Stats Grid
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AnalyticsCard(
                            title = "Taken",
                            value = "${summary.totalTaken}",
                            subtitle = "Total doses",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsCard(
                            title = "Missed",
                            value = "${summary.totalMissed}",
                            subtitle = "Doses missed",
                            icon = Icons.Default.Cancel,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        AnalyticsCard(
                            title = "Current Streak",
                            value = "${summary.currentStreak}",
                            subtitle = "Days in a row",
                            icon = Icons.Default.LocalFireDepartment,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsCard(
                            title = "Monthly",
                            value = "85%",
                            subtitle = "Avg adherence",
                            icon = Icons.Default.Assessment,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is Resource.Error -> {
                    Text("Error: ${summaryResult.message}", color = MaterialTheme.colorScheme.error)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(text = "Health Insights", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            when (val insightsResult = insightsState) {
                is Resource.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is Resource.Success -> {
                    insightsResult.data?.forEach { insight ->
                        InsightCard(insight = insight)
                    }
                }
                is Resource.Error -> {
                    Text("Error: ${insightsResult.message}")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { /* Export Logic */ },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.IosShare, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export Health Report (PDF)")
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
