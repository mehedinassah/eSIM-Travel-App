package com.esim.travelapp.data.repository

import com.esim.travelapp.data.local.dao.UserDao
import com.esim.travelapp.data.local.entity.UserEntity
import com.esim.travelapp.domain.model.User
import com.esim.travelapp.utils.PasswordUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val userDao: UserDao) {

    suspend fun register(name: String, email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                // Check if email already exists
                if (userDao.isEmailExists(email) > 0) {
                    return@withContext Result.failure(Exception("Email already registered"))
                }

                val passwordHash = PasswordUtils.hashPassword(password)
                val userEntity = UserEntity(
                    name = name,
                    email = email,
                    passwordHash = passwordHash
                )

                val userId = userDao.insertUser(userEntity).toInt()
                val user = User(
                    id = userId,
                    name = name,
                    email = email
                )
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun login(email: String, password: String): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val passwordHash = PasswordUtils.hashPassword(password)
                val userEntity = userDao.authenticateUser(email, passwordHash)
                    ?: return@withContext Result.failure(Exception("Invalid email or password"))

                val user = User(
                    id = userEntity.id,
                    name = userEntity.name,
                    email = userEntity.email
                )
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun getUserById(userId: Int): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(userId)?.let {
            User(
                id = it.id,
                name = it.name,
                email = it.email
            )
        }
    }

    suspend fun resetPassword(email: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val user = userDao.getUserByEmail(email)
                ?: return@withContext Result.failure(Exception("Email not found"))
            // In a real app, you'd send a reset link via email
            Result.success("Password reset link sent to $email")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class ESIMPlanRepository(private val planDao: com.esim.travelapp.data.local.dao.ESIMPlanDao) {
    fun getAllPlans() = planDao.getAllPlans()
    fun getPlansByCountry(country: String) = planDao.getPlansByCountry(country)
    suspend fun getPlanById(planId: Int) = planDao.getPlanById(planId)
}

class PurchaseRepository(private val purchaseDao: com.esim.travelapp.data.local.dao.PurchaseDao) {
    fun getUserPurchases(userId: Int) = purchaseDao.getUserPurchases(userId)
    suspend fun getPurchaseById(purchaseId: Int) = purchaseDao.getPurchaseById(purchaseId)
    
    suspend fun createPurchase(userId: Int, planId: Int): Result<Int> {
        return try {
            val purchase = com.esim.travelapp.data.local.entity.PurchaseEntity(
                userId = userId,
                planId = planId,
                paymentId = 0, // Will be updated after payment
                status = "pending"
            )
            val purchaseId = purchaseDao.insertPurchase(purchase).toInt()
            Result.success(purchaseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

class NotificationRepository(private val notificationDao: com.esim.travelapp.data.local.dao.NotificationDao) {
    fun getUserNotifications(userId: Int) = notificationDao.getUserNotifications(userId)
    
    suspend fun addNotification(notification: com.esim.travelapp.data.local.entity.NotificationEntity) {
        notificationDao.insertNotification(notification)
    }
}
