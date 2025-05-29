package com.naulify.agent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naulify.agent.ui.components.*
import com.naulify.agent.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun EmailVerificationScreen(
    onNavigateToCreateProfile: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val isEmailVerified by viewModel.isEmailVerified.collectAsState()
    val scope = rememberCoroutineScope()
    var isCheckingVerification by remember { mutableStateOf(false) }

    LaunchedEffect(isEmailVerified) {
        if (isEmailVerified) {
            onNavigateToCreateProfile()
        }
    }

    Scaffold(
        topBar = {
            NaulifyTopBar(
                title = "Email Verification",
                onNavigateBack = onNavigateToLogin
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Please verify your email address",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "We've sent you a verification email. Please check your inbox and click the verification link.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            NaulifyButton(
                onClick = {
                    scope.launch {
                        isCheckingVerification = true
                        viewModel.checkEmailVerification()
                        delay(2000) // Wait for Firebase to update
                        isCheckingVerification = false
                    }
                },
                text = "I've verified my email",
                enabled = !isCheckingVerification
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { viewModel.sendVerificationEmail() }
            ) {
                Text("Resend verification email")
            }

            when {
                isCheckingVerification -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    LoadingIndicator()
                }
                authState is AuthState.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                authState is AuthState.VerificationEmailSent -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Verification email sent!",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
