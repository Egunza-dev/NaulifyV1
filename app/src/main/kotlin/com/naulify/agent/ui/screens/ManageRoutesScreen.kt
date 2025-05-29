package com.naulify.agent.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naulify.agent.model.Route
import com.naulify.agent.ui.components.*
import com.naulify.agent.util.Utils
import com.naulify.agent.viewmodel.ProfileViewModel
import com.naulify.agent.viewmodel.RouteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageRoutesScreen(
    onNavigateBack: () -> Unit,
    routeViewModel: RouteViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    var showAddRouteDialog by remember { mutableStateOf(false) }
    var routeToDelete by remember { mutableStateOf<Route?>(null) }
    
    val routes by routeViewModel.routes.collectAsState()
    val vehicles by profileViewModel.vehicles.collectAsState()
    val routeState by routeViewModel.routeState.collectAsState()

    LaunchedEffect(vehicles) {
        vehicles.firstOrNull()?.let { vehicle ->
            routeViewModel.loadRoutes(vehicle.id)
        }
    }

    Scaffold(
        topBar = {
            NaulifyTopBar(
                title = "Manage Routes",
                onNavigateBack = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddRouteDialog = true }) {
                Icon(Icons.Default.Add, "Add Route")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                routeState is RouteState.Loading -> LoadingIndicator()
                routeState is RouteState.Error -> {
                    ErrorMessage(
                        message = (routeState as RouteState.Error).message,
                        onRetry = {
                            vehicles.firstOrNull()?.let { vehicle ->
                                routeViewModel.loadRoutes(vehicle.id)
                            }
                        }
                    )
                }
                routes.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            "No routes added yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tap the + button to add a new route",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(routes) { route ->
                            RouteCard(
                                route = route,
                                onDeleteClick = { routeToDelete = route }
                            )
                        }
                    }
                }
            }
        }

        if (showAddRouteDialog) {
            AddRouteDialog(
                onDismiss = { showAddRouteDialog = false },
                onConfirm = { description, fare ->
                    vehicles.firstOrNull()?.let { vehicle ->
                        routeViewModel.createRoute(description, fare, vehicle.id)
                    }
                    showAddRouteDialog = false
                }
            )
        }

        routeToDelete?.let { route ->
            DeleteRouteDialog(
                onDismiss = { routeToDelete = null },
                onConfirm = {
                    vehicles.firstOrNull()?.let { vehicle ->
                        routeViewModel.deleteRoute(route.id, vehicle.id)
                    }
                    routeToDelete = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RouteCard(
    route: Route,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = route.description,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = Utils.formatCurrency(route.fare),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Default.Delete, "Delete Route")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddRouteDialog(
    onDismiss: () -> Unit,
    onConfirm: (description: String, fare: Double) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var fareText by remember { mutableStateOf("") }
    var descriptionError by remember { mutableStateOf<String?>(null) }
    var fareError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Route") },
        text = {
            Column {
                NaulifyTextField(
                    value = description,
                    onValueChange = { 
                        description = it
                        descriptionError = if (it.length < 3) "Description too short" else null
                    },
                    label = "Route Description",
                    isError = descriptionError != null,
                    errorMessage = descriptionError
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                NaulifyTextField(
                    value = fareText,
                    onValueChange = { 
                        fareText = it
                        fareError = try {
                            it.toDouble()
                            null
                        } catch (e: NumberFormatException) {
                            "Invalid fare amount"
                        }
                    },
                    label = "Fare Amount",
                    isError = fareError != null,
                    errorMessage = fareError
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val fare = fareText.toDoubleOrNull() ?: 0.0
                    onConfirm(description, fare)
                },
                enabled = description.isNotEmpty() && fareText.isNotEmpty() &&
                         descriptionError == null && fareError == null
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun DeleteRouteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Route") },
        text = { Text("Are you sure you want to delete this route?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
