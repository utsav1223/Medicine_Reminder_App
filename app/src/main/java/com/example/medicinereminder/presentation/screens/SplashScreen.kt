package com.example.medicinereminder.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.viewmodel.SplashViewModel
import com.example.medicinereminder.ui.theme.GradientEnd
import com.example.medicinereminder.ui.theme.GradientStart
import kotlinx.coroutines.flow.collectLatest

@Composable
fun SplashScreen(
    navController: NavController
) {
    val viewModel: SplashViewModel = viewModel(
        modelClass = SplashViewModel::class.java,
        factory = com.example.medicinereminder.presentation.viewmodel.ViewModelFactory(androidx.compose.ui.platform.LocalContext.current)
    )
    val scale = rememberInfiniteTransition().animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is SplashViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(GradientStart, GradientEnd)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.MedicalServices,
                contentDescription = "Logo",
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale.value),
                tint = Color.White
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "MedReminder",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.displayLarge
            )
            Text(
                text = "Your Health, Our Priority",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 16.sp,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
