package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.components.AppToolbar
import com.example.medicinereminder.presentation.components.CaregiverCard
import com.example.medicinereminder.presentation.viewmodel.AuthViewModel
import com.example.medicinereminder.presentation.viewmodel.CaregiverViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CaregiverDashboardScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    viewModel: CaregiverViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
) {
    var showInviteDialog by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    val inviteState by viewModel.inviteState.collectAsState()

    Scaffold(
        topBar = {
            AppToolbar(
                title = "Caregivers",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showInviteDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Caregiver")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            if (inviteState is Resource.Error) {
                Text(
                    text = "Invite Failed: ${(inviteState as Resource.Error).message}",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Text(
                "My Caregivers",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "These people can monitor your medication adherence and receive emergency alerts.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                item {
                    CaregiverCard(
                        name = "Dr. Smith",
                        email = "doctor@example.com",
                        status = "ACCEPTED"
                    ) {
                        // Handle remove
                    }
                }
                item {
                    CaregiverCard(
                        name = "Sarah (Nurse)",
                        email = "sarah@example.com",
                        status = "PENDING"
                    ) {
                        // Handle remove
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite Caregiver") },
            text = {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Caregiver Email") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(onClick = {
                    val user = authViewModel.userState.value
                    if (user != null) {
                        viewModel.inviteCaregiver(email, user.name, user.uid)
                        showInviteDialog = false
                    }
                }) {
                    Text("Invite")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInviteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
