package com.esim.travelapp.data.local.dao

import androidx.room.*
import com.esim.travelapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    suspend fun getUserById(userId: Int): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun authenticateUser(email: String, passwordHash: String): UserEntity?

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    suspend fun isEmailExists(email: String): Int
}

@Dao
interface ESIMPlanDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: com.esim.travelapp.data.local.entity.ESIMPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSync(plan: com.esim.travelapp.data.local.entity.ESIMPlanEntity): Long

    @Query("SELECT * FROM esim_plans")
    fun getAllPlans(): Flow<List<com.esim.travelapp.data.local.entity.ESIMPlanEntity>>

    @Query("SELECT * FROM esim_plans")
    fun getAllPlansSync(): List<com.esim.travelapp.data.local.entity.ESIMPlanEntity>?

    @Query("SELECT * FROM esim_plans WHERE country = :country")
    fun getPlansByCountry(country: String): Flow<List<com.esim.travelapp.data.local.entity.ESIMPlanEntity>>

    @Query("SELECT * FROM esim_plans WHERE id = :planId LIMIT 1")
    suspend fun getPlanById(planId: Int): com.esim.travelapp.data.local.entity.ESIMPlanEntity?
}

@Dao
interface PurchaseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: com.esim.travelapp.data.local.entity.PurchaseEntity): Long

    @Query("SELECT * FROM purchases WHERE userId = :userId")
    fun getUserPurchases(userId: Int): Flow<List<com.esim.travelapp.data.local.entity.PurchaseEntity>>

    @Query("SELECT * FROM purchases WHERE id = :purchaseId LIMIT 1")
    suspend fun getPurchaseById(purchaseId: Int): com.esim.travelapp.data.local.entity.PurchaseEntity?

    @Update
    suspend fun updatePurchase(purchase: com.esim.travelapp.data.local.entity.PurchaseEntity)
}

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: com.esim.travelapp.data.local.entity.NotificationEntity): Long

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserNotifications(userId: Int): Flow<List<com.esim.travelapp.data.local.entity.NotificationEntity>>

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :notificationId")
    suspend fun markAsRead(notificationId: Int)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllAsRead(userId: Int)

    @Query("DELETE FROM notifications WHERE userId = :userId")
    suspend fun clearAllNotifications(userId: Int)
}

@Dao
interface PaymentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: com.esim.travelapp.data.local.entity.PaymentEntity): Long

    @Query("SELECT * FROM payments WHERE id = :paymentId LIMIT 1")
    suspend fun getPaymentById(paymentId: Int): com.esim.travelapp.data.local.entity.PaymentEntity?

    @Query("SELECT * FROM payments WHERE userId = :userId ORDER BY createdAt DESC")
    fun getUserPayments(userId: Int): Flow<List<com.esim.travelapp.data.local.entity.PaymentEntity>>

    @Update
    suspend fun updatePayment(payment: com.esim.travelapp.data.local.entity.PaymentEntity)
}

@Dao
interface ESIMActivationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivation(activation: com.esim.travelapp.data.local.entity.ESIMActivationEntity): Long

    @Query("SELECT * FROM esim_activations WHERE purchaseId = :purchaseId LIMIT 1")
    suspend fun getActivationByPurchaseId(purchaseId: Int): com.esim.travelapp.data.local.entity.ESIMActivationEntity?

    @Query("SELECT * FROM esim_activations WHERE id = :activationId LIMIT 1")
    suspend fun getActivationById(activationId: Int): com.esim.travelapp.data.local.entity.ESIMActivationEntity?

    @Update
    suspend fun updateActivation(activation: com.esim.travelapp.data.local.entity.ESIMActivationEntity)
}

@Dao
interface DataUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsage(usage: com.esim.travelapp.data.local.entity.DataUsageEntity): Long

    @Query("SELECT * FROM data_usage WHERE activationId = :activationId LIMIT 1")
    suspend fun getUsageByActivationId(activationId: Int): com.esim.travelapp.data.local.entity.DataUsageEntity?

    @Update
    suspend fun updateUsage(usage: com.esim.travelapp.data.local.entity.DataUsageEntity)
}
