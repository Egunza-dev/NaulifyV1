package com.naulify.agent.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.naulify.agent.model.FareCollection
import com.naulify.agent.model.Route
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

interface RouteRepository {
    suspend fun createRoute(route: Route): Boolean
    suspend fun getRoute(routeId: String): Route?
    suspend fun updateRoute(route: Route): Boolean
    suspend fun deleteRoute(routeId: String): Boolean
    suspend fun getRoutesForVehicle(vehicleId: String): List<Route>
    suspend fun getFareCollections(vehicleId: String, limit: Int = 50): List<FareCollection>
    suspend fun getFareCollectionsByDateRange(
        vehicleId: String,
        startTime: Long,
        endTime: Long
    ): List<FareCollection>
}

@Singleton
class FirestoreRouteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : RouteRepository {

    private val routesCollection = firestore.collection("routes")
    private val fareCollection = firestore.collection("fare_collections")

    override suspend fun createRoute(route: Route): Boolean {
        return try {
            val newRouteRef = routesCollection.document()
            routesCollection.document(newRouteRef.id)
                .set(route.copy(id = newRouteRef.id))
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getRoute(routeId: String): Route? {
        return try {
            val document = routesCollection.document(routeId).get().await()
            document.toObject(Route::class.java)
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun updateRoute(route: Route): Boolean {
        return try {
            routesCollection.document(route.id).set(route).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteRoute(routeId: String): Boolean {
        return try {
            routesCollection.document(routeId).delete().await()
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun getRoutesForVehicle(vehicleId: String): List<Route> {
        return try {
            val documents = routesCollection
                .whereEqualTo("vehicleId", vehicleId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()
            documents.toObjects(Route::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFareCollections(vehicleId: String, limit: Int): List<FareCollection> {
        return try {
            val documents = fareCollection
                .whereEqualTo("vehicleId", vehicleId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()
            documents.toObjects(FareCollection::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun getFareCollectionsByDateRange(
        vehicleId: String,
        startTime: Long,
        endTime: Long
    ): List<FareCollection> {
        return try {
            val documents = fareCollection
                .whereEqualTo("vehicleId", vehicleId)
                .whereGreaterThanOrEqualTo("timestamp", startTime)
                .whereLessThanOrEqualTo("timestamp", endTime)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            documents.toObjects(FareCollection::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
