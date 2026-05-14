package com.example.medicinereminder.presentation.screens

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(navController: NavController) {
    var spokenText by remember { mutableStateOf("Tap the mic and say something...") }
    var isListening by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        val data = result.data
        val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        spokenText = results?.get(0) ?: "I didn't catch that. Try again?"
        
        // Simple command parsing
        if (spokenText.contains("add medicine", ignoreCase = true)) {
            navController.navigate(Screen.AddMedicine.route)
        } else if (spokenText.contains("today", ignoreCase = true)) {
            navController.navigate(Screen.TodayReminders.route)
        }
    }

    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.5f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Voice Assistant", fontWeight = FontWeight.Bold) },
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
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = spokenText,
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 64.dp)
            )

            Box(contentAlignment = Alignment.Center) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .scale(scale)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape)
                    )
                }
                
                FloatingActionButton(
                    onClick = {
                        isListening = true
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "How can I help you?")
                        }
                        speechLauncher.launch(intent)
                    },
                    shape = CircleShape,
                    containerColor = if (isListening) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(80.dp)
                ) {
                    Icon(
                        Icons.Default.Mic,
                        contentDescription = "Mic",
                        modifier = Modifier.size(40.dp),
                        tint = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = if (isListening) "Listening..." else "Tap to Speak",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "Try saying:\n\"Add medicine\"\n\"Show today's reminders\"",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
