package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    navController: NavController,
    viewModel: MedicineViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val medicinesState by viewModel.medicinesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("All") }

    Scaffold(
        topBar = {
            AppToolbar(
                title = "My Medications",
                showBackButton = false
            )
        },
        floatingActionButton = {
            LargeFloatingActionButton(
                onClick = { navController.navigate(Screen.AddMedicine.route) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine", modifier = Modifier.size(30.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                    MedicineSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    MedicineTypeSelector(
                        selectedType = selectedType,
                        onTypeSelected = { selectedType = it },
                        showAllOption = true
                    )
                }
            }

            when (val result = medicinesState) {
                is Resource.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
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
                            description = if (searchQuery.isEmpty()) "Tap the + button to add your first medicine reminder" else "Try searching for a different name or type"
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(24.dp)
                        ) {
                            items(filteredMedicines) { medicine ->
                                MedicineCard(
                                    medicine = medicine,
                                    onClick = {
                                        navController.navigate("medicine_details/${medicine.medicineId}")
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
                is Resource.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Error: ${result.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
