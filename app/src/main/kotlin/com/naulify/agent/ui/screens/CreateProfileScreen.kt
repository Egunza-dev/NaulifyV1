package com.naulify.agent.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.naulify.agent.model.VehicleType
import com.naulify.agent.ui.components.*
import com.naulify.agent.util.Utils
import com.naulify.agent.viewmodel.AuthViewModel
import com.naulify.agent.viewmodel.ProfileViewModel
import com.naulify.agent.viewmodel.ProfileState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileScreen(
    onProfileCreated: () -> Unit,
    profileViewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var vehicleRegistration by remember { mutableStateOf("") }
    var mpesaShortCode by remember { mutableStateOf("") }
    var selectedVehicleType by remember { mutableStateOf(VehicleType.VAN) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var registrationError by remember { mutableStateOf<String?>(null) }
    var mpesaError by remember { mutableStateOf<String?>(null) }

    val profileState by profileViewModel.profileState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(profileState) {
        when (profileState) {
            is ProfileState.ProfileCreated -> onProfileCreated()
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            NaulifyTopBar(title = "Create Profile")
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
                value = name,
                onValueChange = { 
                    name = it
                    nameError = if (it.length < 3) "Name must be at least 3 characters" else null
                },
                label = "Full Name",
                isError = nameError != null,
                errorMessage = nameError
            )

            Spacer(modifier = Modifier.height(16.dp))

            NaulifyTextField(
                value = phoneNumber,
                onValueChange = { 
                    phoneNumber = it
                    phoneError = if (!Utils.validatePhoneNumber(it)) "Invalid phone number" else null
                },
                label = "Phone Number",
                isError = phoneError != null,
                errorMessage = phoneError
            )

            Spacer(modifier = Modifier.height(16.dp))

            NaulifyTextField(
                value = vehicleRegistration,
                onValueChange = { 
                    vehicleRegistration = it.uppercase()
                    registrationError = if (!Utils.validateVehicleRegistration(it.uppercase())) 
                        "Invalid registration (e.g., KAA 123A)" else null
                },
                label = "Vehicle Registration",
                isError = registrationError != null,
                errorMessage = registrationError
            )

            Spacer(modifier = Modifier.height(16.dp))

            ExposedDropdownMenuBox(
                expanded = false,
                onExpandedChange = {},
            ) {
                Column {
                    Text(
                        "Vehicle Type",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        VehicleType.values().forEach { type ->
                            FilterChip(
                                selected = selectedVehicleType == type,
                                onClick = { selectedVehicleType = type },
                                label = { Text(type.name.lowercase().capitalize()) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NaulifyTextField(
                value = mpesaShortCode,
                onValueChange = { 
                    mpesaShortCode = it
                    mpesaError = if (!Utils.validateMpesaShortCode(it)) 
                        "Invalid M-Pesa short code" else null
                },
                label = "M-Pesa Short Code",
                isError = mpesaError != null,
                errorMessage = mpesaError
            )

            Spacer(modifier = Modifier.height(32.dp))

            NaulifyButton(
                onClick = {
                    if (authState is AuthState.Authenticated) {
                        val userId = (authState as AuthState.Authenticated).userId
                        profileViewModel.createProfile(
                            userId = userId,
                            name = name,
                            phoneNumber = phoneNumber,
                            email = authViewModel.getCurrentUser()?.email ?: ""
                        )
                        profileViewModel.createVehicle(
                            ownerId = userId,
                            registration = vehicleRegistration,
                            type = selectedVehicleType,
                            mpesaShortCode = mpesaShortCode
                        )
                    }
                },
                text = "Create Profile",
                enabled = name.isNotEmpty() && phoneNumber.isNotEmpty() && 
                         vehicleRegistration.isNotEmpty() && mpesaShortCode.isNotEmpty() &&
                         nameError == null && phoneError == null && 
                         registrationError == null && mpesaError == null &&
                         profileState !is ProfileState.Loading
            )

            when (profileState) {
                is ProfileState.Loading -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    LoadingIndicator()
                }
                is ProfileState.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (profileState as ProfileState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> {}
            }
        }
    }
}
