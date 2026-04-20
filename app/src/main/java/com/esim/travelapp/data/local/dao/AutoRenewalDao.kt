package com.esim.travelapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.esim.travelapp.data.local.entity.AutoRenewalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutoRenewalDao {
    @Insert
    suspend fun createAutoRenewal(autoRenewal: AutoRenewalEntity)

    @Update
    suspend fun updateAutoRenewal(autoRenewal: AutoRenewalEntity)

    @Query("SELECT * FROM auto_renewal WHERE userId = :userId")
    fun getUserAutoRenewals(userId: Int): Flow<List<AutoRenewalEntity>>

    @Query("SELECT * FROM auto_renewal WHERE userId = :userId AND planId = :planId LIMIT 1")
    suspend fun getAutoRenewal(userId: Int, planId: Int): AutoRenewalEntity?

    @Query("SELECT * FROM auto_renewal WHERE isEnabled = 1")
    suspend fun getEnabledAutoRenewals(): List<AutoRenewalEntity>

    @Query("DELETE FROM auto_renewal WHERE userId = :userId AND planId = :planId")
    suspend fun deleteAutoRenewal(userId: Int, planId: Int)
}
