package com.esim.travelapp.data.repository

import com.esim.travelapp.data.local.dao.ReferralDao
import com.esim.travelapp.data.local.entity.ReferralEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

class ReferralRepository(private val referralDao: ReferralDao) {

    suspend fun generateReferralCode(userId: Int, discount: Double = 10.0): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val code = "REF${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
                val referral = ReferralEntity(
                    referrerId = userId,
                    referralCode = code,
                    referralLink = "https://esimtravel.app/join?ref=$code",
                    discount = discount,
                    status = "pending",
                    createdAt = System.currentTimeMillis()
                )
                referralDao.createReferral(referral)
                Result.success(code)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getUserReferrals(userId: Int) = referralDao.getUserReferrals(userId)

    suspend fun claimReferral(code: String, referredUserId: Int): Result<Double> =
        withContext(Dispatchers.IO) {
            try {
                val referral = referralDao.getReferralByCode(code)
                    ?: return@withContext Result.failure(Exception("Invalid referral code"))

                val claimedReferral = referral.copy(
                    referredUserId = referredUserId,
                    status = "claimed",
                    claimedAt = System.currentTimeMillis()
                )
                referralDao.updateReferral(claimedReferral)
                Result.success(referral.discount)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getClaimedReferralCount(userId: Int) = referralDao.getClaimedReferralCount(userId)

    fun getTotalReferralBenefits(userId: Int) = referralDao.getTotalReferralBenefits(userId)
}
