package com.example.medicinereminder.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.components.AppToolbar
import com.example.medicinereminder.presentation.viewmodel.AdminViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val analyticsState by viewModel.analytics.collectAsState()
    val usersState by viewModel.users.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("System Administrator", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Medicine Reminder Platform", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("login") { popUpTo(0) } }) {
                        Icon(Icons.Default.Logout, contentDescription = "Exit Admin")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SectionHeader("Platform Overview")
            }

            item {
                when (val state = analyticsState) {
                    is Resource.Loading -> Box(Modifier.fillMaxWidth().height(150.dp)) { CircularProgressIndicator(Modifier.align(Alignment.Center)) }
                    is Resource.Success -> {
                        val data = state.data!!
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                DetailedStatCard("Total Users", data.totalUsers.toString(), Icons.Default.Groups, MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                                DetailedStatCard("Active SOS", data.activeAlerts.toString(), Icons.Default.Emergency, Color(0xFFBA1A1A), Modifier.weight(1f))
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                DetailedStatCard("Doses Logged", data.totalMedicinesTracked.toString(), Icons.Default.Medication, MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                                DetailedStatCard("Avg Adherence", "${data.averageAdherence}%", Icons.Default.TrendingUp, Color(0xFF2E7D32), Modifier.weight(1f))
                            }
                        }
                    }
                    is Resource.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                SectionHeader("User Management")
            }

            when (val state = usersState) {
                is Resource.Loading -> item { Box(Modifier.fillMaxWidth().padding(20.dp)) { CircularProgressIndicator(Modifier.align(Alignment.Center)) } }
                is Resource.Success -> {
                    items(state.data ?: emptyList()) { user ->
                        AdminUserItem(user.name, user.email, user.uid)
                    }
                }
                is Resource.Error -> item { Text("Error loading users", color = MaterialTheme.colorScheme.error) }
            }
            
            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun DetailedStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun AdminUserItem(name: String, email: String, uid: String) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(name.take(1).uppercase(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline
                )
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp, start = 60.dp)) {
                    Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    UserDetailRow("User ID", uid)
                    UserDetailRow("Account Status", "Active")
                    UserDetailRow("Last Active", "Today")
                    UserDetailRow("Storage Used", "1.2 MB")
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("View History", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(4.dp)
                        ) {
                            Text("Manage", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
    }
}
