package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.medicinereminder.data.model.EmergencyContact
import com.example.medicinereminder.presentation.components.AppToolbar
import com.example.medicinereminder.presentation.components.EmergencyButton
import com.example.medicinereminder.presentation.components.EmergencyContactCard
import com.example.medicinereminder.presentation.viewmodel.AuthViewModel
import com.example.medicinereminder.presentation.viewmodel.EmergencyViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    viewModel: EmergencyViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val contactsState by viewModel.emergencyContacts.collectAsState()
    var showAddContactDialog by remember { mutableStateOf(false) }
    val userId = authViewModel.userState.value?.uid ?: ""

    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) viewModel.loadContacts(userId)
    }

    Scaffold(
        topBar = {
            AppToolbar(
                title = "Emergency",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Need Help?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Trigger an SOS to alert your caregivers and emergency contacts instantly.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            EmergencyButton(onClick = {
                val userName = authViewModel.userState.value?.name ?: "User"
                viewModel.triggerSOS(userId, userName)
            })
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Emergency Contacts", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showAddContactDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Contact")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            when (val state = contactsState) {
                is Resource.Loading -> CircularProgressIndicator()
                is Resource.Success -> {
                    if (state.data.isNullOrEmpty()) {
                        Text("No emergency contacts added yet.")
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxWidth()) {
                            items(state.data) { contact ->
                                EmergencyContactCard(
                                    name = contact.name,
                                    phone = contact.phoneNumber,
                                    relation = contact.relationship,
                                    onCall = { /* Intent to call */ }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }

    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var relation by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            title = { Text("Add Emergency Contact") },
            text = {
                Column {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = relation, onValueChange = { relation = it }, label = { Text("Relationship") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.addContact(EmergencyContact(name = name, phoneNumber = phone, relationship = relation, userId = userId))
                    showAddContactDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
