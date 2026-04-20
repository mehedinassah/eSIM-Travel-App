package com.esim.travelapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.esim.travelapp.data.local.entity.AnalyticsEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {
    @Insert
    suspend fun logEvent(event: AnalyticsEventEntity)

    @Query("SELECT * FROM analytics_events WHERE userId = :userId ORDER BY timestamp DESC LIMIT 100")
    fun getUserEvents(userId: Int): Flow<List<AnalyticsEventEntity>>

    @Query("SELECT * FROM analytics_events WHERE eventName = :eventName ORDER BY timestamp DESC LIMIT 50")
    suspend fun getEventsByName(eventName: String): List<AnalyticsEventEntity>

    @Query("DELETE FROM analytics_events WHERE timestamp < :olderThan")
    suspend fun deleteOldEvents(olderThan: Long)
}
