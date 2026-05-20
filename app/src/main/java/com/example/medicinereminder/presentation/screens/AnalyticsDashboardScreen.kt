package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.components.*
import com.example.medicinereminder.presentation.viewmodel.AnalyticsViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.ui.theme.GradientEnd
import com.example.medicinereminder.ui.theme.GradientStart
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
                title = "Health Analytics",
                showBackButton = false
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 40.dp)
        ) {
            when (val summaryResult = summaryState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is Resource.Success -> {
                    val summary = summaryResult.data!!
                    
                    // 1. Overall Performance Ring
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AdherenceRing(
                            progress = summary.averageAdherence / 100f,
                            modifier = Modifier.size(240.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // 2. Statistics Grid
                    SectionHeader(title = "Dose Tracking")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AnalyticsCard(
                            title = "Taken",
                            value = "${summary.totalTaken}",
                            subtitle = "Successful doses",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsCard(
                            title = "Missed",
                            value = "${summary.totalMissed}",
                            subtitle = "Skipped doses",
                            icon = Icons.Default.Cancel,
                            color = MaterialTheme.colorScheme.error,
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
                        AnalyticsCard(
                            title = "Current Streak",
                            value = "${summary.currentStreak} Days",
                            subtitle = "Consistency",
                            icon = Icons.Default.LocalFireDepartment,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f)
                        )
                        AnalyticsCard(
                            title = "Best Streak",
                            value = "12 Days",
                            subtitle = "Record high",
                            icon = Icons.Default.Star,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // 3. Activity Chart
                    SectionHeader(title = "Weekly Activity")
                    Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                        AdherenceChart(
                            modelProducer = viewModel.modelProducer,
                            modifier = Modifier.fillMaxWidth().height(220.dp)
                        )
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorMessage(message = summaryResult.message ?: "Failed to load health summary.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 4. AI Health Insights
            SectionHeader(title = "AI Health Insights")
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                when (val insightsResult = insightsState) {
                    is Resource.Loading -> {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))
                    }
                    is Resource.Success -> {
                        insightsResult.data?.forEach { insight ->
                            InsightCard(insight = insight)
                        }
                    }
                    is Resource.Error -> {
                        Text("Insights currently unavailable.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 5. Professional Actions
            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                Button(
                    onClick = { /* Export Logic */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Download Full Health Report", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
