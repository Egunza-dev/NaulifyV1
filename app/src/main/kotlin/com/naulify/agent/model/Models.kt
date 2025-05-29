package com.naulify.agent.model

data class User(
    val id: String = "",
    val email: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val isEmailVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class Vehicle(
    val id: String = "",
    val registration: String = "",
    val type: VehicleType = VehicleType.VAN,
    val mpesaShortCode: String = "",
    val ownerId: String = ""
)

enum class VehicleType {
    VAN, BUS, MINI_BUS
}

data class Route(
    val id: String = "",
    val description: String = "",
    val fare: Double = 0.0,
    val vehicleId: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class FareCollection(
    val id: String = "",
    val amount: Double = 0.0,
    val routeId: String = "",
    val vehicleId: String = "",
    val passengerId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val transactionId: String = "",
    val status: TransactionStatus = TransactionStatus.PENDING
)

enum class TransactionStatus {
    PENDING, COMPLETED, FAILED
}
