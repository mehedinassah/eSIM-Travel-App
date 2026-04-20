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

// Wishlist/Favorites
@Entity(tableName = "wishlist")
data class WishlistEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val planId: Int,
    val addedAt: Long = System.currentTimeMillis()
)

// User Location
@Entity(tableName = "user_location")
data class UserLocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val city: String,
    val lastUpdated: Long = System.currentTimeMillis()
)

// Analytics Events
@Entity(tableName = "analytics_events")
data class AnalyticsEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val eventName: String,
    val eventData: String, // JSON string
    val timestamp: Long = System.currentTimeMillis()
)

// Support Tickets
@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val subject: String,
    val message: String,
    val status: String = "open", // open, in_progress, resolved, closed
    val priority: String = "normal", // low, normal, high
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Support Messages/Chat
@Entity(tableName = "support_messages")
data class SupportMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ticketId: Int,
    val senderId: Int, // userId or support agent id
    val message: String,
    val isUserMessage: Boolean = true,
    val timestamp: Long = System.currentTimeMillis()
)

// Referrals
@Entity(tableName = "referrals")
data class ReferralEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val referrerId: Int,
    val referredUserId: Int? = null,
    val referralCode: String,
    val referralLink: String,
    val status: String = "pending", // pending, claimed, expired
    val discount: Double = 10.0, // discount percentage
    val createdAt: Long = System.currentTimeMillis(),
    val claimedAt: Long? = null
)

// Auto Topup/Renewal
@Entity(tableName = "auto_renewal")
data class AutoRenewalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: Int,
    val planId: Int,
    val isEnabled: Boolean = false,
    val renewalThreshold: Double = 10.0, // renew when data % falls below this
    val lastRenewalDate: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)
