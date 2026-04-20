package com.esim.travelapp.data.repository

import com.esim.travelapp.data.local.dao.AutoRenewalDao
import com.esim.travelapp.data.local.entity.AutoRenewalEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutoRenewalRepository(private val autoRenewalDao: AutoRenewalDao) {

    suspend fun enableAutoRenewal(
        userId: Int,
        planId: Int,
        renewalThreshold: Double = 10.0
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = autoRenewalDao.getAutoRenewal(userId, planId)
            val autoRenewal = if (existing != null) {
                existing.copy(
                    isEnabled = true,
                    renewalThreshold = renewalThreshold
                )
            } else {
                AutoRenewalEntity(
                    userId = userId,
                    planId = planId,
                    isEnabled = true,
                    renewalThreshold = renewalThreshold
                )
            }
            if (existing != null) {
                autoRenewalDao.updateAutoRenewal(autoRenewal)
            } else {
                autoRenewalDao.createAutoRenewal(autoRenewal)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun disableAutoRenewal(userId: Int, planId: Int): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                autoRenewalDao.deleteAutoRenewal(userId, planId)
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    fun getUserAutoRenewals(userId: Int) = autoRenewalDao.getUserAutoRenewals(userId)

    suspend fun getEnabledAutoRenewals() = withContext(Dispatchers.IO) {
        try {
            autoRenewalDao.getEnabledAutoRenewals()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateRenewalThreshold(
        userId: Int,
        planId: Int,
        newThreshold: Double
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val existing = autoRenewalDao.getAutoRenewal(userId, planId)
            if (existing != null) {
                autoRenewalDao.updateAutoRenewal(
                    existing.copy(renewalThreshold = newThreshold)
                )
                Result.success(Unit)
            } else {
                Result.failure(Exception("Auto renewal not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
