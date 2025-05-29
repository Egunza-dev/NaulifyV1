package com.naulify.agent.ui

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object EmailVerification : Screen("email_verification")
    object CreateProfile : Screen("create_profile")
    object Dashboard : Screen("dashboard")
    object ManageRoutes : Screen("manage_routes")
    object QRCode : Screen("qr_code")
    object Reports : Screen("reports")
}

package com.naulify.agent.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NaulifyNavigation(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToSignUp = { navController.navigate(Screen.SignUp.route) },
                onNavigateToEmailVerification = { navController.navigate(Screen.EmailVerification.route) },
                onNavigateToDashboard = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.SignUp.route) {
            SignUpScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEmailVerification = { navController.navigate(Screen.EmailVerification.route) }
            )
        }

        composable(Screen.EmailVerification.route) {
            EmailVerificationScreen(
                onNavigateToCreateProfile = { navController.navigate(Screen.CreateProfile.route) },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.CreateProfile.route) {
            CreateProfileScreen(
                onProfileCreated = {
                    navController.navigate(Screen.Dashboard.route) {
                        popUpTo(Screen.CreateProfile.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToManageRoutes = { navController.navigate(Screen.ManageRoutes.route) },
                onNavigateToQRCode = { navController.navigate(Screen.QRCode.route) },
                onNavigateToReports = { navController.navigate(Screen.Reports.route) },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Dashboard.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.ManageRoutes.route) {
            ManageRoutesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.QRCode.route) {
            QRCodeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Reports.route) {
            ReportsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
