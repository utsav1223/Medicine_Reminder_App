package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.data.model.UserProfile
import com.example.medicinereminder.presentation.viewmodel.AuthViewModel
import com.example.medicinereminder.presentation.viewmodel.ProfileViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    authViewModel: AuthViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isUpdateSuccess) {
        if (uiState.isUpdateSuccess) {
            viewModel.resetUpdateSuccess()
        }
    }

    LaunchedEffect(key1 = true) {
        authViewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { navController.navigate(com.example.medicinereminder.presentation.navigation.Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    TextButton(onClick = { viewModel.updateProfile(uiState.profile) }) {
                        Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Header Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            IconButton(
                                onClick = { /* Image Picker */ },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = uiState.profile.name.ifEmpty { "Welcome!" },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = uiState.profile.email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Account Section
                SectionTitle("Account")
                ProfileCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .clickable { navController.navigate(com.example.medicinereminder.presentation.navigation.Screen.Settings.route) },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text("App Settings & Theme", style = MaterialTheme.typography.bodyLarge)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null)
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Personal Details
                SectionTitle("Personal Details")
                ProfileCard {
                    ProfileFieldItem("Full Name", uiState.profile.name, Icons.Default.Badge) {
                        viewModel.updateProfile(uiState.profile.copy(name = it))
                    }
                    ProfileFieldItem("Weight (kg)", uiState.profile.weight?.toString() ?: "", Icons.Default.Scale) {
                        viewModel.updateProfile(uiState.profile.copy(weight = it.toFloatOrNull()))
                    }
                    ProfileFieldItem("Gender", uiState.profile.gender ?: "", Icons.Default.Wc) {
                        viewModel.updateProfile(uiState.profile.copy(gender = it))
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Medical History
                SectionTitle("Medical Information")
                ProfileCard {
                    ProfileFieldItem("Blood Group", uiState.profile.bloodGroup ?: "", Icons.Default.Bloodtype) {
                        viewModel.updateProfile(uiState.profile.copy(bloodGroup = it))
                    }
                    ProfileFieldItem("Allergies", uiState.profile.allergies.joinToString(", "), Icons.Default.Warning) {
                        viewModel.updateProfile(uiState.profile.copy(allergies = it.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }))
                    }
                    ProfileFieldItem("Health Conditions", uiState.profile.healthConditions.joinToString(", "), Icons.Default.MedicalServices) {
                        viewModel.updateProfile(uiState.profile.copy(healthConditions = it.split(",").map { s -> s.trim() }.filter { s -> s.isNotEmpty() }))
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = { authViewModel.logout() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "App Version 1.0.0",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, start = 4.dp)
    )
}

@Composable
fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(content = content)
    }
}

@Composable
fun ProfileFieldItem(label: String, value: String, icon: ImageVector, onValueChange: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }
    
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        BasicTextField(
            value = text,
            onValueChange = { 
                text = it
                onValueChange(it) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, start = 32.dp),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface)
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
    }
}
