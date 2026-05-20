package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineListScreen(
    navController: NavController
) {
    val context = LocalContext.current
    val factory = ViewModelFactory(context)
    val viewModel: MedicineViewModel = viewModel(modelClass = MedicineViewModel::class.java, factory = factory)
    
    val medicinesState by viewModel.medicinesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                AppToolbar(
                    title = "My Medications",
                    showBackButton = false
                )
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)) {
                        MedicineSearchBar(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        MedicineTypeSelector(
                            selectedType = selectedType,
                            onTypeSelected = { selectedType = it },
                            showAllOption = true
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
            }
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { navController.navigate(Screen.AddMedicine.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(32.dp), tint = Color.White)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val result = medicinesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    }
                }
                is Resource.Success -> {
                    val filteredMedicines = result.data?.filter {
                        (selectedType == "All" || it.medicineType == selectedType) &&
                        it.medicineName.contains(searchQuery, ignoreCase = true)
                    } ?: emptyList()

                    if (filteredMedicines.isEmpty()) {
                        EmptyState(
                            message = if (searchQuery.isEmpty()) "No medications yet" else "No matches found",
                            description = if (searchQuery.isEmpty()) "Click the + button to build your medication list." else "Try adjusting your search or filters."
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 100.dp)
                        ) {
                            item {
                                Text(
                                    text = "Active Reminders (${filteredMedicines.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }
                            items(filteredMedicines) { medicine ->
                                MedicineCard(
                                    medicine = medicine,
                                    onClick = {
                                        navController.navigate("medicine_details/${medicine.medicineId}")
                                    }
                                )
                            }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        ErrorMessage(message = result.message ?: "Could not load your medications.")
                    }
                }
            }
        }
    }
}
