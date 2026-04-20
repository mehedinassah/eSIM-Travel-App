package com.esim.travelapp.data.repository

import com.esim.travelapp.data.local.dao.AnalyticsDao
import com.esim.travelapp.data.local.entity.AnalyticsEventEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AnalyticsRepository(private val analyticsDao: AnalyticsDao) {

    suspend fun logEvent(
        userId: Int,
        eventName: String,
        eventData: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val event = AnalyticsEventEntity(
                userId = userId,
                eventName = eventName,
                eventData = eventData,
                timestamp = System.currentTimeMillis()
            )
            analyticsDao.logEvent(event)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getUserEvents(userId: Int) = analyticsDao.getUserEvents(userId)

    suspend fun getEventsByName(eventName: String) =
        withContext(Dispatchers.IO) {
            try {
                analyticsDao.getEventsByName(eventName)
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun cleanOldEvents(olderThanDays: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val olderThan = System.currentTimeMillis() - (olderThanDays * 24 * 60 * 60 * 1000)
                analyticsDao.deleteOldEvents(olderThan)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
}
