package com.esim.travelapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.esim.travelapp.data.local.entity.ReferralEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReferralDao {
    @Insert
    suspend fun createReferral(referral: ReferralEntity)

    @Update
    suspend fun updateReferral(referral: ReferralEntity)

    @Query("SELECT * FROM referrals WHERE referrerId = :userId ORDER BY createdAt DESC")
    fun getUserReferrals(userId: Int): Flow<List<ReferralEntity>>

    @Query("SELECT * FROM referrals WHERE referralCode = :code LIMIT 1")
    suspend fun getReferralByCode(code: String): ReferralEntity?

    @Query("SELECT COUNT(*) FROM referrals WHERE referrerId = :userId AND status = 'claimed'")
    fun getClaimedReferralCount(userId: Int): Flow<Int>

    @Query("SELECT SUM(discount) FROM referrals WHERE referrerId = :userId AND status = 'claimed'")
    fun getTotalReferralBenefits(userId: Int): Flow<Double?>
}
