package com.esim.travelapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.esim.travelapp.data.local.entity.UserLocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Insert
    suspend fun insertLocation(location: UserLocationEntity)

    @Update
    suspend fun updateLocation(location: UserLocationEntity)

    @Query("SELECT * FROM user_location WHERE userId = :userId LIMIT 1")
    fun getUserLocation(userId: Int): Flow<UserLocationEntity?>

    @Query("SELECT country FROM user_location WHERE userId = :userId LIMIT 1")
    suspend fun getUserCountry(userId: Int): String?

    @Query("SELECT * FROM user_location WHERE userId = :userId LIMIT 1")
    suspend fun getUserLocationSync(userId: Int): UserLocationEntity?
}
