package com.esim.travelapp.domain.model

data class User(
    val id: Int = 0,
    val name: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ESIMPlan(
    val id: Int = 0,
    val country: String,
    val planName: String,
    val dataAmount: String,
    val price: Double,
    val validityDays: Int,
    val description: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Purchase(
    val id: Int = 0,
    val userId: Int,
    val planId: Int,
    val paymentId: Int,
    val purchaseDate: Long = System.currentTimeMillis(),
    val status: String = "pending",
    val createdAt: Long = System.currentTimeMillis()
)

data class Notification(
    val id: Int = 0,
    val userId: Int,
    val title: String,
    val message: String,
    val type: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
