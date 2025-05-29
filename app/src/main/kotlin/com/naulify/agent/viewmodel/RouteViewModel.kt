package com.naulify.agent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naulify.agent.model.FareCollection
import com.naulify.agent.model.Route
import com.naulify.agent.repository.RouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _routeState = MutableStateFlow<RouteState>(RouteState.Initial)
    val routeState: StateFlow<RouteState> = _routeState

    private val _routes = MutableStateFlow<List<Route>>(emptyList())
    val routes: StateFlow<List<Route>> = _routes

    private val _fareCollections = MutableStateFlow<List<FareCollection>>(emptyList())
    val fareCollections: StateFlow<List<FareCollection>> = _fareCollections

    fun createRoute(description: String, fare: Double, vehicleId: String) {
        viewModelScope.launch {
            _routeState.value = RouteState.Loading
            try {
                val route = Route(
                    description = description,
                    fare = fare,
                    vehicleId = vehicleId
                )
                val success = routeRepository.createRoute(route)
                if (success) {
                    loadRoutes(vehicleId)
                    _routeState.value = RouteState.RouteCreated(route)
                } else {
                    _routeState.value = RouteState.Error("Failed to create route")
                }
            } catch (e: Exception) {
                _routeState.value = RouteState.Error(e.message ?: "Failed to create route")
            }
        }
    }

    fun loadRoutes(vehicleId: String) {
        viewModelScope.launch {
            _routeState.value = RouteState.Loading
            try {
                val routeList = routeRepository.getRoutesForVehicle(vehicleId)
                _routes.value = routeList
                _routeState.value = RouteState.RoutesLoaded(routeList)
            } catch (e: Exception) {
                _routeState.value = RouteState.Error(e.message ?: "Failed to load routes")
            }
        }
    }

    fun deleteRoute(routeId: String, vehicleId: String) {
        viewModelScope.launch {
            _routeState.value = RouteState.Loading
            try {
                val success = routeRepository.deleteRoute(routeId)
                if (success) {
                    loadRoutes(vehicleId)
                    _routeState.value = RouteState.RouteDeleted
                } else {
                    _routeState.value = RouteState.Error("Failed to delete route")
                }
            } catch (e: Exception) {
                _routeState.value = RouteState.Error(e.message ?: "Failed to delete route")
            }
        }
    }

    fun loadFareCollections(vehicleId: String) {
        viewModelScope.launch {
            _routeState.value = RouteState.Loading
            try {
                val collections = routeRepository.getFareCollections(vehicleId)
                _fareCollections.value = collections
                _routeState.value = RouteState.FareCollectionsLoaded(collections)
            } catch (e: Exception) {
                _routeState.value = RouteState.Error(e.message ?: "Failed to load fare collections")
            }
        }
    }

    fun loadFareCollectionsByDateRange(vehicleId: String, startTime: Long, endTime: Long) {
        viewModelScope.launch {
            _routeState.value = RouteState.Loading
            try {
                val collections = routeRepository.getFareCollectionsByDateRange(vehicleId, startTime, endTime)
                _fareCollections.value = collections
                _routeState.value = RouteState.FareCollectionsLoaded(collections)
            } catch (e: Exception) {
                _routeState.value = RouteState.Error(e.message ?: "Failed to load fare collections")
            }
        }
    }
}

sealed class RouteState {
    object Initial : RouteState()
    object Loading : RouteState()
    object RouteDeleted : RouteState()
    data class RouteCreated(val route: Route) : RouteState()
    data class RoutesLoaded(val routes: List<Route>) : RouteState()
    data class FareCollectionsLoaded(val collections: List<FareCollection>) : RouteState()
    data class Error(val message: String) : RouteState()
}
