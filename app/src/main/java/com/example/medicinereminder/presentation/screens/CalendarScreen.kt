package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.components.AppToolbar
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(navController: NavController) {
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    Scaffold(
        topBar = {
            AppToolbar(
                title = "Medicine Calendar",
                showBackButton = true,
                onBackClick = { navController.popBackStack() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            CalendarHeader(
                currentMonth = selectedDate,
                onPreviousMonth = {
                    val newDate = selectedDate.clone() as Calendar
                    newDate.add(Calendar.MONTH, -1)
                    selectedDate = newDate
                },
                onNextMonth = {
                    val newDate = selectedDate.clone() as Calendar
                    newDate.add(Calendar.MONTH, 1)
                    selectedDate = newDate
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            CalendarGrid(selectedDate)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            LegendSection()
        }
    }
}

@Composable
fun CalendarHeader(
    currentMonth: Calendar,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthName = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time)
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(Icons.Default.ChevronLeft, contentDescription = null)
        }
        Text(
            text = monthName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        IconButton(onClick = onNextMonth) {
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

@Composable
fun CalendarGrid(calendar: Calendar) {
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfMonth = (calendar.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val firstDayOfWeek = firstDayOfMonth.get(Calendar.DAY_OF_WEEK)
    
    val days = (1 until firstDayOfWeek).map { "" } + (1..daysInMonth).map { it.toString() }
    
    val weekDays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    
    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(320.dp)
        ) {
            items(days) { day ->
                if (day.isNotEmpty()) {
                    val isToday = day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString() && 
                                  calendar.get(Calendar.MONTH) == Calendar.getInstance().get(Calendar.MONTH)
                    
                    val statusColor = when(day.toInt() % 4) {
                        0 -> Color(0xFF4CAF50)
                        1 -> Color(0xFFF44336)
                        2 -> Color(0xFFFFC107)
                        else -> Color.Transparent
                    }

                    Column(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                color = if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                        if (statusColor != Color.Transparent) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(statusColor, CircleShape)
                            )
                        }
                    }
                } else {
                    Box(modifier = Modifier.aspectRatio(1f))
                }
            }
        }
    }
}

@Composable
fun LegendSection() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        LegendItem(color = Color(0xFF4CAF50), label = "All Completed")
        LegendItem(color = Color(0xFFF44336), label = "Missed")
        LegendItem(color = Color(0xFFFFC107), label = "Partial")
    }
}

@Composable
fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
