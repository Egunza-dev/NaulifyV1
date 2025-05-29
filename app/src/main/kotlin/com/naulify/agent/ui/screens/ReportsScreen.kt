package com.naulify.agent.ui.screens

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
import com.naulify.agent.model.FareCollection
import com.naulify.agent.ui.components.*
import com.naulify.agent.util.Utils
import com.naulify.agent.viewmodel.ProfileViewModel
import com.naulify.agent.viewmodel.RouteViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    routeViewModel: RouteViewModel = hiltViewModel(),
    profileViewModel: ProfileViewModel = hiltViewModel()
) {
    val vehicles by profileViewModel.vehicles.collectAsState()
    val fareCollections by routeViewModel.fareCollections.collectAsState()
    val routeState by routeViewModel.routeState.collectAsState()

    var selectedPeriod by remember { mutableStateOf(ReportPeriod.TODAY) }
    var isFilterDialogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPeriod, vehicles) {
        vehicles.firstOrNull()?.let { vehicle ->
            val (startTime, endTime) = getTimeRangeForPeriod(selectedPeriod)
            routeViewModel.loadFareCollectionsByDateRange(vehicle.id, startTime, endTime)
        }
    }

    Scaffold(
        topBar = {
            NaulifyTopBar(
                title = "Reports",
                onNavigateBack = onNavigateBack,
                actions = {
                    IconButton(onClick = { isFilterDialogVisible = true }) {
                        Icon(Icons.Default.FilterList, "Filter")
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
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = selectedPeriod.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "Total Collections",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                Utils.formatCurrency(fareCollections.sumOf { it.amount }),
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column {
                            Text(
                                "Total Trips",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                fareCollections.size.toString(),
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }

            // Transactions List
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "Transactions",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                when {
                    routeState is RouteState.Loading -> {
                        item { LoadingIndicator() }
                    }
                    fareCollections.isEmpty() -> {
                        item {
                            Text(
                                "No transactions found for this period",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        items(fareCollections) { collection ->
                            TransactionCard(collection)
                        }
                    }
                }
            }
        }

        if (isFilterDialogVisible) {
            FilterDialog(
                selectedPeriod = selectedPeriod,
                onPeriodSelected = {
                    selectedPeriod = it
                    isFilterDialogVisible = false
                },
                onDismiss = { isFilterDialogVisible = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TransactionCard(collection: FareCollection) {
    Card(
        modifier = Modifier.fillMaxWidth()
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
                    text = Utils.formatDate(collection.timestamp),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Transaction ID: ${collection.transactionId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = Utils.formatCurrency(collection.amount),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun FilterDialog(
    selectedPeriod: ReportPeriod,
    onPeriodSelected: (ReportPeriod) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Period") },
        text = {
            Column {
                ReportPeriod.values().forEach { period ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = period == selectedPeriod,
                            onClick = { onPeriodSelected(period) }
                        )
                        Text(
                            text = period.title,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

enum class ReportPeriod(val title: String) {
    TODAY("Today"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month")
}

private fun getTimeRangeForPeriod(period: ReportPeriod): Pair<Long, Long> {
    val calendar = Calendar.getInstance()
    val endTime = calendar.timeInMillis

    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    val startTime = when (period) {
        ReportPeriod.TODAY -> calendar.timeInMillis
        ReportPeriod.THIS_WEEK -> {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            calendar.timeInMillis
        }
        ReportPeriod.THIS_MONTH -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.timeInMillis
        }
        ReportPeriod.LAST_MONTH -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            calendar.add(Calendar.MONTH, -1)
            calendar.timeInMillis
        }
    }

    return Pair(startTime, endTime)
}
