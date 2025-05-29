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
fun SignUpScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEmailVerification: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    val authState by viewModel.authState.collectAsState()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.VerificationRequired -> {
                onNavigateToEmailVerification()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            NaulifyTopBar(
                title = "Create Account",
                onNavigateBack = onNavigateBack
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    confirmPasswordError = if (it != confirmPassword) "Passwords do not match" else null
                },
                label = "Password",
                isError = passwordError != null,
                errorMessage = passwordError,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(16.dp))

            NaulifyTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    confirmPasswordError = if (it != password) "Passwords do not match" else null
                },
                label = "Confirm Password",
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                visualTransformation = PasswordVisualTransformation()
            )

            Spacer(modifier = Modifier.height(32.dp))

            NaulifyButton(
                onClick = {
                    if (emailError == null && passwordError == null && confirmPasswordError == null) {
                        viewModel.signUpWithEmail(email, password)
                    }
                },
                text = "Sign Up",
                enabled = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() &&
                         emailError == null && passwordError == null && confirmPasswordError == null &&
                         authState !is AuthState.Loading
            )

            when (authState) {
                is AuthState.Loading -> LoadingIndicator()
                is AuthState.Error -> {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                else -> {}
            }
        }
    }
}
