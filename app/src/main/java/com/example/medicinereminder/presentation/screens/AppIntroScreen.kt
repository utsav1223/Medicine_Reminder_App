package com.example.medicinereminder.presentation.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.viewmodel.PreferencesViewModel
import com.example.medicinereminder.presentation.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

@Composable
fun AppIntroScreen(
    navController: NavController,
    preferencesViewModel: PreferencesViewModel = viewModel(factory = ViewModelFactory(LocalContext.current))
) {
    val pages = listOf(
        OnboardingPage(
            title = "Welcome to MedReminder",
            description = "Your smart healthcare companion designed to make medication management effortless and reliable.",
            icon = Icons.Default.MedicalServices,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
        ),
        OnboardingPage(
            title = "AI Smart Safety",
            description = "Experience industry-leading AI that analyzes drug interactions and provides personalized health insights.",
            icon = Icons.Default.AutoAwesome,
            backgroundColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        OnboardingPage(
            title = "Family Care System",
            description = "Easily manage medication profiles for your children, parents, or elderly family members in one place.",
            icon = Icons.Default.Groups,
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
        ),
        OnboardingPage(
            title = "SOS Emergency Alerts",
            description = "Critical situations demand fast action. One-tap SOS notifies all your linked caregivers instantly.",
            icon = Icons.Default.Emergency,
            backgroundColor = Color(0xFFFFDAD6) // Error-ish red for SOS
        ),
        OnboardingPage(
            title = "Progress & Analytics",
            description = "Track your health journey with beautiful charts, streaks, and adherence reports to stay motivated.",
            icon = Icons.Default.BarChart,
            backgroundColor = MaterialTheme.colorScheme.surfaceVariant
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Background Gradient Glow
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            pages[pagerState.currentPage].backgroundColor.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Header with Skip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = {
                    preferencesViewModel.setOnboardingCompleted(true)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AppIntro.route) { inclusive = true }
                    }
                }) {
                    Text(
                        "Skip",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Carousel Content
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) { index ->
                OnboardingPageView(pages[index])
            }

            // Bottom Navigation Area
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(16.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 40.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Modern Indicators
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(pages.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(if (isSelected) 24.dp else 8.dp, label = "")
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(8.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }

                    // Action Button
                    Button(
                        onClick = {
                            if (pagerState.currentPage < pages.size - 1) {
                                scope.launch {
                                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                                }
                            } else {
                                preferencesViewModel.setOnboardingCompleted(true)
                                navController.navigate(Screen.Login.route) {
                                    popUpTo(Screen.AppIntro.route) { inclusive = true }
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        AnimatedContent(
                            targetState = pagerState.currentPage == pages.size - 1,
                            label = ""
                        ) { isLastPage ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    if (isLastPage) "Get Started" else "Next",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (!isLastPage) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingPageView(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Animated Illustration Placeholder
        Box(
            modifier = Modifier
                .size(240.dp)
                .background(page.backgroundColor.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Subtle pulse animation logic could go here
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = page.title,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

data class OnboardingPage(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val backgroundColor: Color
)
