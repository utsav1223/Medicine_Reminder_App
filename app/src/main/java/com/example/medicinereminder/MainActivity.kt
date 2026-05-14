package com.example.medicinereminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.medicinereminder.presentation.components.OfflineBanner
import com.example.medicinereminder.presentation.components.GlobalErrorDialog
import com.example.medicinereminder.presentation.components.LockScreen
import com.example.medicinereminder.presentation.navigation.BottomNavItem
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.navigation.SetupNavGraph
import com.example.medicinereminder.presentation.viewmodel.*
import com.example.medicinereminder.ui.theme.MedicineReminderTheme
import com.example.medicinereminder.utils.BiometricHelper
import com.example.medicinereminder.utils.ErrorManager
import com.example.medicinereminder.worker.DailyReminderWorker
import com.example.medicinereminder.worker.SyncWorker
import com.google.firebase.FirebaseApp

import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : FragmentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        DailyReminderWorker.enqueue(this)
        SyncWorker.enqueue(this)
        askNotificationPermission()
        enableEdgeToEdge()
        
        val biometricHelper = BiometricHelper(this)

        setContent {
            val factory = ViewModelFactory(this)
            val preferencesViewModel: PreferencesViewModel = viewModel(factory = factory)
            val networkViewModel: NetworkViewModel = viewModel(factory = factory)
            val securityViewModel: SecurityViewModel = viewModel(factory = factory)
            
            val themeMode by preferencesViewModel.themeMode.collectAsStateWithLifecycle()
            val dynamicColors by preferencesViewModel.dynamicColors.collectAsStateWithLifecycle()
            val isOnline by networkViewModel.isOnline.collectAsStateWithLifecycle()
            
            val isAppLockEnabled by securityViewModel.isAppLockEnabled.collectAsStateWithLifecycle()
            val isAuthorized by securityViewModel.isAuthorized.collectAsStateWithLifecycle()
            val isBiometricEnabled by securityViewModel.isBiometricEnabled.collectAsStateWithLifecycle()

            //dark mode and light mode logic
            val isDarkTheme = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }
            
            MedicineReminderTheme(
                darkTheme = isDarkTheme,
                dynamicColor = dynamicColors
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (isAppLockEnabled && !isAuthorized) {
                        LockScreen(
                            onPinEntered = { securityViewModel.authenticatePin(it) },
                            onBiometricClick = {
                                biometricHelper.showBiometricPrompt(
                                    activity = this@MainActivity,
                                    onSuccess = { securityViewModel.authenticateBiometric() },
                                    onError = { /* Handle error if needed */ }
                                )
                            },
                            isBiometricAvailable = isBiometricEnabled && biometricHelper.isBiometricAvailable()
                        )
                    } else {
                        val navController = rememberNavController()
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = navBackStackEntry?.destination?.route
                        
                        val bottomNavScreens = listOf(
                            Screen.Home.route,
                            Screen.MedicineList.route,
                            Screen.Analytics.route,
                            Screen.Profile.route
                        )
                        
                        val showBottomBar = currentRoute in bottomNavScreens

                        var globalErrorMessage by remember { mutableStateOf<String?>(null) }
                        
                        LaunchedEffect(Unit) {
                            ErrorManager.errorFlow.collect { message ->
                                globalErrorMessage = message
                            }
                        }

                        if (globalErrorMessage != null) {
                            GlobalErrorDialog(
                                message = globalErrorMessage!!,
                                onDismiss = { globalErrorMessage = null }
                            )
                        }
                    //bottom bar navigation....
                        Scaffold(
                            bottomBar = {
                                if (showBottomBar) {
                                    NavigationBar {
                                        val items = listOf(
                                            BottomNavItem.Home,
                                            BottomNavItem.Medicines,
                                            BottomNavItem.Analytics,
                                            BottomNavItem.Profile
                                        )
                                        items.forEach { item ->
                                            val isSelected = currentRoute == item.route
                                            NavigationBarItem(
                                                icon = { Icon(item.icon, contentDescription = item.title) },
                                                label = { Text(item.title) },
                                                selected = isSelected,
                                                onClick = {
                                                    if (!isSelected) {
                                                        navController.navigate(item.route) {
                                                            popUpTo(navController.graph.findStartDestination().id) {
                                                                saveState = true
                                                            }
                                                            launchSingleTop = true
                                                            restoreState = true
                                                        }
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Column(modifier = Modifier.padding(innerPadding)) {
                                OfflineBanner(isOffline = !isOnline)
                                Box(modifier = Modifier.weight(1f)) {
                                    SetupNavGraph(navController = navController)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    //notification Permission
    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}
