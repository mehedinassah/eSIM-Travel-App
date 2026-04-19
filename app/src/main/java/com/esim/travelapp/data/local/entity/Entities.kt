package com.esim.travelapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

@Entity(tableName = "esim_plans")
data class ESIMPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val country: String,
    val planName: String,
    val dataAmount: String,
    val price: Double,
    val validityDays: Int,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "purchases")
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val planId: Int,
    val paymentId: Int,
    val purchaseDate: Long = System.currentTimeMillis(),
    val status: String = "pending", // pending, completed, failed
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val message: String,
    val type: String, // low_balance, activation, expiring, promo
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val purchaseId: Int,
    val amount: Double,
    val paymentMethod: String, // card, wallet, bank
    val paymentStatus: String = "processing", // processing, completed, failed, refunded
    val transactionReference: String,
    val processorTransactionId: String? = null,
    val cardBrand: String? = null,
    val lastFourDigits: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "esim_activations")
data class ESIMActivationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val purchaseId: Int,
    val iccid: String,
    val qrCodeUrl: String? = null,
    val activationStatus: String = "pending", // pending, activated, failed, expired
    val activationCode: String? = null,
    val activationDate: Long? = null,
    val expiryDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "data_usage")
data class DataUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val activationId: Int,
    val dataUsed: Double = 0.0,
    val dataTotal: Double = 0.0,
    val dataRemaining: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)
