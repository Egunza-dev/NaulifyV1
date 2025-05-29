package com.naulify.agent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naulify.agent.ui.components.*
import com.naulify.agent.util.Utils
import com.naulify.agent.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onNavigateToEmailVerification: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var rememberMe by remember { mutableStateOf(false) }

    val authState by viewModel.authState.collectAsState()
    val isEmailVerified by viewModel.isEmailVerified.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                if (isEmailVerified) {
                    onNavigateToDashboard()
                } else {
                    onNavigateToEmailVerification()
                }
            }
            is AuthState.Error -> {
                // Show error in UI
            }
            else -> {}
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Naulify",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(32.dp))

            NaulifyTextField(
                value = email,
                onValueChange = { 
                    email = it
                    emailError = if (!Utils.validateEmail(it)) "Invalid email format" else null
                },
                label = "Email",
                isError = emailError != null,
                errorMessage = emailError
            )

            Spacer(modifier = Modifier.height(16.dp))

            NaulifyTextField(
                value = password,
                onValueChange = { 
                    password = it
                    passwordError = if (it.length < 6) "Password must be at least 6 characters" else null
                },
                label = "Password",
                isError = passwordError != null,
                errorMessage = passwordError,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Text("Remember Me")
            }

            Spacer(modifier = Modifier.height(24.dp))

            NaulifyButton(
                onClick = {
                    if (emailError == null && passwordError == null) {
                        viewModel.signInWithEmail(email, password)
                    }
                },
                text = "Sign In",
                enabled = email.isNotEmpty() && password.isNotEmpty() &&
                         emailError == null && passwordError == null &&
                         authState !is AuthState.Loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToSignUp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Don't have an account? Sign Up")
            }

            when (authState) {
                is AuthState.Loading -> LoadingIndicator()
                is AuthState.Error -> {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {}
            }
        }
    }
}
