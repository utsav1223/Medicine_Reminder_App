package com.example.medicinereminder.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController
) {
    val viewModel: AuthViewModel = viewModel(
        modelClass = AuthViewModel::class.java,
        factory = com.example.medicinereminder.presentation.viewmodel.ViewModelFactory(androidx.compose.ui.platform.LocalContext.current)
    )
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val credentialManager = CredentialManager.create(context)

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AuthViewModel.UiEvent.Navigate -> {
                    navController.navigate(event.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
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
            text = "Welcome Back",
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Login to your account",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))

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
        
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            TextButton(onClick = { navController.navigate(Screen.ForgotPassword.route) }) {
                Text("Forgot Password?")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        PrimaryButton(
            text = "Login",
            onClick = { viewModel.login(email, password) },
            isLoading = viewModel.loading.value
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("255519181593-8bmmvit69hb5sambvnm5bljuvc02qsu4.apps.googleusercontent.com")
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                scope.launch {
                    try {
                        val result = credentialManager.getCredential(
                            request = request,
                            context = context
                        )
                        val credential = result.credential
                        if (credential is GoogleIdTokenCredential) {
                            viewModel.onGoogleSignInResult(credential.idToken)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        val errorMessage = when (e) {
                            is androidx.credentials.exceptions.NoCredentialException -> {
                                "No Google accounts found. Ensure you are signed into the Play Store on this device."
                            }
                            is androidx.credentials.exceptions.GetCredentialException -> {
                                "Sign In Error: ${e.message}"
                            }
                            else -> "Error: ${e.message}"
                        }
                        viewModel.onError(errorMessage)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        ) {
            Image(
                painter = painterResource(id = com.example.medicinereminder.R.drawable.ic_google),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign in with Google")
        }

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
            Text("Don't have an account?")
            TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                Text("Register", fontWeight = FontWeight.Bold)
            }
        }
    }
}
