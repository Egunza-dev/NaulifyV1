package com.naulify.agent.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naulify.agent.R
import com.naulify.agent.ui.components.LoadingIndicator
import com.naulify.agent.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToDashboard: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsState()
    val isEmailVerified by viewModel.isEmailVerified.collectAsState()

    LaunchedEffect(Unit) {
        delay(2000) // Show splash for 2 seconds
        when (authState) {
            is AuthState.Authenticated -> {
                if (isEmailVerified) {
                    onNavigateToDashboard()
                } else {
                    onNavigateToLogin()
                }
            }
            else -> onNavigateToLogin()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.naulify_logo),
                    contentDescription = "Naulify Logo",
                    modifier = Modifier.size(200.dp)
                )
                Spacer(modifier = Modifier.height(32.dp))
                LoadingIndicator()
            }
        }
    }
}
