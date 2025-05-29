package com.naulify.agent.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.naulify.agent.model.User
import com.naulify.agent.model.Vehicle
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepository {
    suspend fun createUserProfile(user: User): Boolean
    suspend fun getUserProfile(userId: String): User?
    suspend fun updateUserProfile(user: User): Boolean
    suspend fun createVehicle(vehicle: Vehicle): Boolean
    suspend fun getVehicle(vehicleId: String): Vehicle?
    suspend fun updateVehicle(vehicle: Vehicle): Boolean
    suspend fun getVehiclesForUser(userId: String): List<Vehicle>
}

@Singleton
class FirestoreProfileRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ProfileRepository {

    private val usersCollection = firestore.collection("users")
    private val vehiclesCollection = firestore.collection("vehicles")

    override suspend fun createUserProfile(user: User): Boolean {
        return try {
            usersCollection.document(user.id).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getUserProfile(userId: String): User? {
        return try {
            val document = usersCollection.document(userId).get().await()
            document.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateUserProfile(user: User): Boolean {
        return try {
            usersCollection.document(user.id).set(user).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun createVehicle(vehicle: Vehicle): Boolean {
        return try {
            val newVehicleRef = vehiclesCollection.document()
            vehiclesCollection.document(newVehicleRef.id)
                .set(vehicle.copy(id = newVehicleRef.id))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getVehicle(vehicleId: String): Vehicle? {
        return try {
            val document = vehiclesCollection.document(vehicleId).get().await()
            document.toObject(Vehicle::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateVehicle(vehicle: Vehicle): Boolean {
        return try {
            vehiclesCollection.document(vehicle.id).set(vehicle).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getVehiclesForUser(userId: String): List<Vehicle> {
        return try {
            val documents = vehiclesCollection
                .whereEqualTo("ownerId", userId)
                .get()
                .await()
            documents.toObjects(Vehicle::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
