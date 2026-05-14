package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.medicinereminder.data.model.FamilyProfile
import com.example.medicinereminder.presentation.components.AppToolbar
import com.example.medicinereminder.presentation.components.FamilyProfileCard
import com.example.medicinereminder.presentation.viewmodel.AuthViewModel
import com.example.medicinereminder.presentation.viewmodel.FamilyProfileViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyProfilesScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    viewModel: FamilyProfileViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val profilesState by viewModel.profiles.collectAsState()
    val currentProfile by viewModel.currentProfile.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    val userId = authViewModel.userState.value?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.loadProfiles(userId)
    }

    Scaffold(
        topBar = {
            AppToolbar(
                title = "Family Profiles",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp)) {
            Text(
                "Manage Profiles",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Switch between profiles to manage medicines for family members.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            when (val state = profilesState) {
                is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                is Resource.Success -> {
                    LazyRow {
                        items(state.data ?: emptyList()) { profile ->
                            FamilyProfileCard(
                                profile = profile,
                                isSelected = currentProfile?.id == profile.id,
                                onClick = { viewModel.switchProfile(profile) }
                            )
                        }
                        item {
                            Surface(
                                modifier = Modifier.width(100.dp).padding(8.dp),
                                onClick = { showAddDialog = true },
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null)
                                    Text("Add New", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                is Resource.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            currentProfile?.let { profile ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Active Profile: ${profile.name}", fontWeight = FontWeight.Bold)
                        Text("Relationship: ${profile.relationship}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.navigate("medicine_list") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Manage Medicines")
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var relation by remember { mutableStateOf("Child") }
        
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Family Member") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    // Simplification: In real app, use a proper dropdown
                    OutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text("Relationship (Child, Parent, etc.)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addProfile(FamilyProfile(name = name, relationship = relation, userId = userId))
                    showAddDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
