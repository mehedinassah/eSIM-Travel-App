package com.esim.travelapp.data.repository

import com.esim.travelapp.data.local.dao.LocationDao
import com.esim.travelapp.data.local.entity.UserLocationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LocationRepository(private val locationDao: LocationDao) {

    suspend fun saveUserLocation(
        userId: Int,
        latitude: Double,
        longitude: Double,
        country: String,
        city: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val location = UserLocationEntity(
                userId = userId,
                latitude = latitude,
                longitude = longitude,
                country = country,
                city = city,
                lastUpdated = System.currentTimeMillis()
            )
            val existing = locationDao.getUserLocationSync(userId)
            if (existing != null) {
                locationDao.updateLocation(location.copy(id = existing.id))
            } else {
                locationDao.insertLocation(location)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserLocation(userId: Int) = locationDao.getUserLocation(userId)

    suspend fun getUserCountry(userId: Int): String? =
        withContext(Dispatchers.IO) {
            try {
                locationDao.getUserCountry(userId)
            } catch (e: Exception) {
                null
            }
        }

    suspend fun getUserLocationSync(userId: Int) =
        withContext(Dispatchers.IO) {
            try {
                locationDao.getUserLocationSync(userId)
            } catch (e: Exception) {
                null
            }
        }
}
