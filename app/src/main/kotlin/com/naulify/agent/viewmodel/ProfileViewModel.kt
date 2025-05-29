package com.naulify.agent.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.naulify.agent.model.User
import com.naulify.agent.model.Vehicle
import com.naulify.agent.model.VehicleType
import com.naulify.agent.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _vehicles = MutableStateFlow<List<Vehicle>>(emptyList())
    val vehicles: StateFlow<List<Vehicle>> = _vehicles

    fun createProfile(
        userId: String,
        name: String,
        phoneNumber: String,
        email: String
    ) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val user = User(
                    id = userId,
                    name = name,
                    phoneNumber = phoneNumber,
                    email = email
                )
                val success = profileRepository.createUserProfile(user)
                if (success) {
                    _profileState.value = ProfileState.ProfileCreated(user)
                } else {
                    _profileState.value = ProfileState.Error("Failed to create profile")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to create profile")
            }
        }
    }

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val user = profileRepository.getUserProfile(userId)
                user?.let {
                    _profileState.value = ProfileState.ProfileLoaded(it)
                    loadVehicles(userId)
                } ?: run {
                    _profileState.value = ProfileState.ProfileNotFound
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun createVehicle(
        ownerId: String,
        registration: String,
        type: VehicleType,
        mpesaShortCode: String
    ) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                val vehicle = Vehicle(
                    ownerId = ownerId,
                    registration = registration.uppercase(),
                    type = type,
                    mpesaShortCode = mpesaShortCode
                )
                val success = profileRepository.createVehicle(vehicle)
                if (success) {
                    loadVehicles(ownerId)
                    _profileState.value = ProfileState.VehicleCreated(vehicle)
                } else {
                    _profileState.value = ProfileState.Error("Failed to create vehicle")
                }
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to create vehicle")
            }
        }
    }

    private fun loadVehicles(userId: String) {
        viewModelScope.launch {
            try {
                val vehicleList = profileRepository.getVehiclesForUser(userId)
                _vehicles.value = vehicleList
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.message ?: "Failed to load vehicles")
            }
        }
    }
}

sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    object ProfileNotFound : ProfileState()
    data class ProfileCreated(val user: User) : ProfileState()
    data class ProfileLoaded(val user: User) : ProfileState()
    data class VehicleCreated(val vehicle: Vehicle) : ProfileState()
    data class Error(val message: String) : ProfileState()
}
