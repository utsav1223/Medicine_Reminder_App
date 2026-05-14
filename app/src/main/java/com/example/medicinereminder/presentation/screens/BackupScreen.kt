package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.components.BackupCard
import com.example.medicinereminder.presentation.components.SyncStatusCard
import com.example.medicinereminder.presentation.viewmodel.SyncViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    navController: NavController,
    viewModel: SyncViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val syncState by viewModel.syncState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp)
        ) {
            SyncStatusCard(
                lastSyncTime = "Recently synced", // In real app, get from DataStore
                isSyncing = syncState is Resource.Loading,
                onSyncClick = { viewModel.syncData() }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Manual Backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            BackupCard(
                title = "Export Data",
                description = "Download your medicine history and settings as a JSON file for local backup.",
                icon = Icons.Default.FileDownload,
                buttonText = "Export to Device",
                onClick = { /* Export logic */ }
            )
            
            BackupCard(
                title = "Restore Data",
                description = "Restore your medicines and history from a previously exported file.",
                icon = Icons.Default.FileUpload,
                buttonText = "Select File to Restore",
                onClick = { /* Restore logic */ }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                "Cloud Backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Surface(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Automatic Cloud Backup", style = MaterialTheme.typography.titleSmall)
                    }
                    Text(
                        "Your data is automatically synced with your account securely.",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
