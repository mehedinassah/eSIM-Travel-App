package com.esim.travelapp.utils

import android.content.Context
import com.esim.travelapp.data.local.entity.AnalyticsEventEntity
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Analytics manager for tracking user events
 */
class AnalyticsManager(private val context: Context) {

    private val gson = Gson()
    private val scope = CoroutineScope(Dispatchers.IO)

    /**
     * Log a custom event (usually called from repository)
     */
    fun logEvent(
        userId: Int,
        eventName: String,
        eventData: Map<String, Any> = emptyMap()
    ) {
        scope.launch {
            try {
                val dataJson = gson.toJson(eventData)
                val event = AnalyticsEventEntity(
                    userId = userId,
                    eventName = eventName,
                    eventData = dataJson,
                    timestamp = System.currentTimeMillis()
                )
                // This will be saved through repository
                // For now, just log to console in debug mode
                if (BuildConfig.DEBUG) {
                    android.util.Log.d("Analytics", "$eventName: $eventData")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Track plan view
     */
    fun trackPlanView(userId: Int, planId: Int, planName: String) {
        logEvent(userId, "plan_viewed", mapOf(
            "plan_id" to planId,
            "plan_name" to planName,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    /**
     * Track purchase
     */
    fun trackPurchase(userId: Int, planId: Int, amount: Double) {
        logEvent(userId, "purchase", mapOf(
            "plan_id" to planId,
            "amount" to amount,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    /**
     * Track wishlist add
     */
    fun trackWishlistAdd(userId: Int, planId: Int) {
        logEvent(userId, "wishlist_added", mapOf(
            "plan_id" to planId
        ))
    }

    /**
     * Track search
     */
    fun trackSearch(userId: Int, query: String) {
        logEvent(userId, "search", mapOf(
            "query" to query
        ))
    }

    /**
     * Track filter usage
     */
    fun trackFilter(userId: Int, filterType: String, filterValue: String) {
        logEvent(userId, "filter_applied", mapOf(
            "filter_type" to filterType,
            "filter_value" to filterValue
        ))
    }

    /**
     * Track support ticket creation
     */
    fun trackSupportTicket(userId: Int, subject: String) {
        logEvent(userId, "support_ticket_created", mapOf(
            "subject" to subject
        ))
    }
}

// Placeholder for BuildConfig
object BuildConfig {
    const val DEBUG = true
}
