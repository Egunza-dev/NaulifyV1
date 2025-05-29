package com.naulify.agent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naulify.agent.model.FareCollection
import com.naulify.agent.model.Route
import com.naulify.agent.ui.components.*
import com.naulify.agent.util.Utils
import com.naulify.agent.viewmodel.AuthViewModel
import com.naulify.agent.viewmodel.ProfileViewModel
import com.naulify.agent.viewmodel.RouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToManageRoutes: () -> Unit,
    onNavigateToQRCode: () -> Unit,
    onNavigateToReports: () -> Unit,
    onSignOut: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel(),
    routeViewModel: RouteViewModel = hiltViewModel()
) {
    val profileState by profileViewModel.profileState.collectAsState()
    val vehicles by profileViewModel.vehicles.collectAsState()
    val routes by routeViewModel.routes.collectAsState()
    val fareCollections by routeViewModel.fareCollections.collectAsState()

    LaunchedEffect(Unit) {
        val userId = (authViewModel.authState.value as? AuthState.Authenticated)?.userId
        userId?.let {
            profileViewModel.loadProfile(it)
        }
    }

    LaunchedEffect(vehicles) {
        vehicles.firstOrNull()?.let { vehicle ->
            routeViewModel.loadRoutes(vehicle.id)
            routeViewModel.loadFareCollections(vehicle.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = onSignOut) {
                        Icon(Icons.Default.ExitToApp, "Sign Out")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Quick Actions Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                QuickActionCard(
                    icon = Icons.Default.DirectionsBus,
                    label = "Manage Routes",
                    onClick = onNavigateToManageRoutes
                )
                QuickActionCard(
                    icon = Icons.Default.QrCode,
                    label = "QR Code",
                    onClick = onNavigateToQRCode
                )
                QuickActionCard(
                    icon = Icons.Default.Assessment,
                    label = "Reports",
                    onClick = onNavigateToReports
                )
            }

            // Recent Routes
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Recent Routes",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    routes.take(3).forEach { route ->
                        RouteItem(route = route)
                        Divider()
                    }
                }
            }

            // Recent Collections
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Recent Collections",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    fareCollections.take(3).forEach { collection ->
                        FareCollectionItem(collection = collection)
                        Divider()
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(100.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun RouteItem(route: Route) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(route.description)
        Text(Utils.formatCurrency(route.fare))
    }
}

@Composable
private fun FareCollectionItem(collection: FareCollection) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(Utils.formatDate(collection.timestamp))
        Text(Utils.formatCurrency(collection.amount))
    }
}
