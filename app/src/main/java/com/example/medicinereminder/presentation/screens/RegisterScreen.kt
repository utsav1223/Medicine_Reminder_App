package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.medicinereminder.presentation.components.AppTextField
import com.example.medicinereminder.presentation.components.PasswordTextField
import com.example.medicinereminder.presentation.components.PrimaryButton
import com.example.medicinereminder.presentation.navigation.Screen
import com.example.medicinereminder.presentation.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun RegisterScreen(
    navController: NavController
) {
    val viewModel: AuthViewModel = viewModel(
        modelClass = AuthViewModel::class.java,
        factory = com.example.medicinereminder.presentation.viewmodel.ViewModelFactory(androidx.compose.ui.platform.LocalContext.current)
    )
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
                else -> {}
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Start your health journey today",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

        AppTextField(
            value = name,
            onValueChange = { name = it },
            label = "Full Name",
            leadingIcon = Icons.Default.Person
        )
        Spacer(modifier = Modifier.height(16.dp))

        AppTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email",
            leadingIcon = Icons.Default.Email
        )
        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password"
        )
        Spacer(modifier = Modifier.height(16.dp))

        PasswordTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Confirm Password"
        )

        Spacer(modifier = Modifier.height(32.dp))

        PrimaryButton(
            text = "Register",
            onClick = { viewModel.register(name, email, password, confirmPassword) },
            isLoading = viewModel.loading.value
        )

        viewModel.error.value?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Already have an account?")
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Login", fontWeight = FontWeight.Bold)
            }
        }
    }
}
