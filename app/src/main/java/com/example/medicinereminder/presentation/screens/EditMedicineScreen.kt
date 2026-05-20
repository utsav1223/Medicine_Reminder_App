package com.example.medicinereminder.presentation.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.medicinereminder.data.model.Medicine
import com.example.medicinereminder.presentation.components.*
import com.example.medicinereminder.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMedicineScreen(
    medicineId: String,
    navController: NavController,
    viewModel: MedicineViewModel = viewModel(factory = com.example.medicinereminder.presentation.viewmodel.ViewModelFactory(androidx.compose.ui.platform.LocalContext.current))
) {
    val medicineState by viewModel.currentMedicine.collectAsState()
    val addState by viewModel.addMedicineState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Tablet") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var notes by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF006A6A.toInt()) }
    var imageUri by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri?.toString()
    }

    val colors = listOf(0xFF006A6A.toInt(), 0xFFBA1A1A.toInt(), 0xFF4B607C.toInt(), 0xFF4A6363.toInt(), 0xFFE65100.toInt())

    LaunchedEffect(medicineId) {
        viewModel.getMedicineById(medicineId)
    }

    LaunchedEffect(medicineState) {
        if (medicineState is Resource.Success) {
            val medicine = (medicineState as Resource.Success<Medicine>).data!!
            name = medicine.medicineName
            type = medicine.medicineType
            dosage = medicine.dosage
            frequency = medicine.frequency
            startDate = medicine.startDate
            endDate = medicine.endDate
            notes = medicine.notes
            selectedColor = medicine.colorTag
            imageUri = medicine.imageUrl
        }
    }

    LaunchedEffect(addState) {
        if (addState is Resource.Success) {
            navController.popBackStack()
            viewModel.resetAddMedicineState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Medicine", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (medicineState is Resource.Loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { imagePickerLauncher.launch("image/*") }
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (imageUri != null) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Medicine Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Surface(
                            onClick = { imageUri = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Remove Image",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Add Image",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Medicine Name",
                    leadingIcon = Icons.Default.Medication
                )

                Spacer(modifier = Modifier.height(16.dp))

                MedicineTypeSelector(
                    selectedType = type,
                    onTypeSelected = { type = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                AppTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = "Dosage"
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        DatePickerField(label = "Start Date", timestamp = startDate, onClick = { })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        DatePickerField(label = "End Date", timestamp = endDate, onClick = { })
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Color Tag", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    colors.forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(Color(color), shape = androidx.compose.foundation.shape.CircleShape)
                                .clickable { selectedColor = color }
                                .padding(8.dp)
                        ) {
                            if (selectedColor == color) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (Optional)",
                    modifier = Modifier.height(120.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (addState is Resource.Error) {
                    Text(
                        text = (addState as Resource.Error).message ?: "An error occurred",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                PrimaryButton(
                    text = "Update Medicine",
                    onClick = {
                        viewModel.updateMedicine(
                            (medicineState as Resource.Success<Medicine>).data!!.copy(
                                medicineName = name,
                                medicineType = type,
                                dosage = dosage,
                                frequency = frequency,
                                startDate = startDate,
                                endDate = endDate,
                                notes = notes,
                                colorTag = selectedColor,
                                imageUrl = imageUri
                            )
                        )
                    },
                    isLoading = addState is Resource.Loading
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
