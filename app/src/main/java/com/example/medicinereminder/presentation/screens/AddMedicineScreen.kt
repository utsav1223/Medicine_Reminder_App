package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.data.model.Medicine
import com.example.medicinereminder.presentation.components.*
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.viewmodel.MedicineViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import com.example.medicinereminder.utils.Resource
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMedicineScreen(
    navController: NavController,
    viewModel: MedicineViewModel = viewModel(factory = ViewModelFactory(LocalContext.current)),
    scannedText: String? = null
) {
    var name by remember { mutableStateOf(scannedText ?: "") }
    var type by remember { mutableStateOf("Tablet") }
    var dosage by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("Daily") }
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000)) }
    var notes by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF006A6A.toInt()) }
    var timingList by remember { mutableStateOf(listOf("08:00 AM")) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val colors = listOf(0xFF006A6A.toInt(), 0xFFBA1A1A.toInt(), 0xFF4B607C.toInt(), 0xFF4A6363.toInt(), 0xFFE65100.toInt())

    val addState by viewModel.addMedicineState.collectAsState()

    LaunchedEffect(addState) {
        if (addState is Resource.Success) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
            }
            viewModel.resetAddMedicineState()
        }
    }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { startDate = it }
                    showStartDatePicker = false
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { endDate = it }
                    showEndDatePicker = false
                }) { Text("OK", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState()
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                    cal.set(Calendar.MINUTE, timePickerState.minute)
                    val timeString = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                    if (!timingList.contains(timeString)) {
                        timingList = (timingList + timeString).sorted()
                    }
                    showTimePicker = false
                }) { Text("Confirm", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Select Time",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    TimePicker(state = timePickerState)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            AppToolbar(
                title = "New Medication",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Medication Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )
            
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Medicine Name",
                        leadingIcon = Icons.Default.Medication
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    MedicineTypeSelector(
                        selectedType = type,
                        onTypeSelected = { type = it }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AppTextField(
                        value = dosage,
                        onValueChange = { dosage = it },
                        label = "Dosage (e.g. 1 Tablet, 5ml)",
                        leadingIcon = Icons.Default.Scale
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Schedule & Timing",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(modifier = Modifier.weight(1f)) {
                            DatePickerField(label = "Start", timestamp = startDate, onClick = { showStartDatePicker = true })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            DatePickerField(label = "End", timestamp = endDate, onClick = { showEndDatePicker = true })
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dose Timings",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        TextButton(onClick = { showTimePicker = true }) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add Time")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        timingList.forEach { time ->
                            SuggestionChip(
                                onClick = { timingList = timingList.filter { it != time } },
                                label = { Text(time) },
                                icon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                shape = RoundedCornerShape(12.dp),
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    labelColor = MaterialTheme.colorScheme.primary
                                ),
                                border = null
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Customization",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Select Color Tag",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        colors.forEach { color ->
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color(color), shape = CircleShape)
                                    .clickable { selectedColor = color }
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedColor == color) {
                                    Surface(
                                        modifier = Modifier.size(24.dp),
                                        shape = CircleShape,
                                        color = Color.White.copy(alpha = 0.3f),
                                        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    AppTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = "Notes / Instructions",
                        modifier = Modifier.height(120.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (addState is Resource.Error) {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = (addState as Resource.Error).message ?: "An error occurred",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            PrimaryButton(
                text = "Create Reminder",
                onClick = {
                    if (name.isBlank() || dosage.isBlank()) {
                        return@PrimaryButton
                    }
                    viewModel.addMedicine(
                        Medicine(
                            medicineName = name,
                            medicineType = type,
                            dosage = dosage,
                            frequency = frequency,
                            startDate = startDate,
                            endDate = endDate,
                            notes = notes,
                            colorTag = selectedColor,
                            timingList = timingList
                        )
                    )
                },
                isLoading = addState is Resource.Loading
            )
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
