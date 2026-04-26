package com.esim.travelapp.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.esim.travelapp.data.local.AppDatabase
import com.esim.travelapp.data.local.entity.NotificationEntity
import com.esim.travelapp.data.local.entity.PurchaseEntity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class DataUsageService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        Log.d("DataUsageService", "Data usage service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            simulateDataUsage()
        }
        return START_STICKY
    }

    private suspend fun simulateDataUsage() {
        while (true) {
            try {
                // Get all active plans (completed purchases with activated eSIMs)
                val activePurchases = database.purchaseDao().getAllPurchasesSync()
                    ?.filter { it.status == "completed" } ?: emptyList()

                for (purchase in activePurchases) {
                    val activation = database.esimActivationDao().getActivationByPurchaseId(purchase.id)
                    if (activation?.activationStatus == "activated") {
                        // Get or create data usage record
                        var usage = database.dataUsageDao().getUsageByActivationId(activation.id)

                        if (usage == null) {
                            // Create initial usage record
                            val plan = database.esimPlanDao().getPlanById(purchase.planId)
                            if (plan != null) {
                                val dataTotal = extractDataAmount(plan.dataAmount)
                                usage = com.esim.travelapp.data.local.entity.DataUsageEntity(
                                    activationId = activation.id,
                                    dataTotal = dataTotal,
                                    dataRemaining = dataTotal
                                )
                                database.dataUsageDao().insertUsage(usage)
                                usage = database.dataUsageDao().getUsageByActivationId(activation.id)
                            }
                        }

                        if (usage != null && usage.dataRemaining > 0) {
                            // Simulate 1% data usage per second
                            val dataToUse = usage.dataTotal * 0.01 // 1% of total data
                            val newRemaining = (usage.dataRemaining - dataToUse).coerceAtLeast(0.0)
                            val newUsed = usage.dataTotal - newRemaining

                            // Update usage
                            val updatedUsage = usage.copy(
                                dataUsed = newUsed,
                                dataRemaining = newRemaining,
                                lastUpdated = System.currentTimeMillis()
                            )
                            database.dataUsageDao().updateUsage(updatedUsage)

                            // Check for auto-renewal
                            checkAutoRenewal(purchase, updatedUsage)

                            Log.d("DataUsageService", "Updated usage for activation ${activation.id}: ${String.format("%.2f", newRemaining)}/${String.format("%.2f", usage.dataTotal)} GB remaining")
                        }
                    }
                }

                // Wait 1 second before next update
                delay(1000)
            } catch (e: Exception) {
                Log.e("DataUsageService", "Error in data usage simulation", e)
                delay(1000)
            }
        }
    }

    private suspend fun checkAutoRenewal(purchase: PurchaseEntity, usage: com.esim.travelapp.data.local.entity.DataUsageEntity) {
        try {
            val autoRenewal = database.autoRenewalDao().getAutoRenewal(purchase.userId, purchase.planId)

            // Only proceed if the user has explicitly enabled auto-renewal with a threshold
            if (autoRenewal == null || !autoRenewal.isEnabled || autoRenewal.renewalThreshold <= 0) return

            val remainingPercent = (usage.dataRemaining / usage.dataTotal) * 100.0

            if (remainingPercent <= autoRenewal.renewalThreshold) {
                val timeSinceLastRenewal = System.currentTimeMillis() - (autoRenewal.lastRenewalDate ?: 0)
                if (timeSinceLastRenewal > 60000) {
                    performAutoRenewal(purchase, autoRenewal)
                }
            }
        } catch (e: Exception) {
            Log.e("DataUsageService", "Error checking auto-renewal", e)
        }
    }

    private suspend fun performAutoRenewal(purchase: PurchaseEntity, autoRenewal: com.esim.travelapp.data.local.entity.AutoRenewalEntity) {
        try {
            // Get the plan details
            val plan = database.esimPlanDao().getPlanById(purchase.planId)
            if (plan == null) return

            // Create a new purchase for renewal
            val newPurchase = PurchaseEntity(
                userId = purchase.userId,
                planId = purchase.planId,
                paymentId = 0, // Would need payment processing in real app
                status = "completed", // Auto-renewal assumes payment success
                purchaseDate = System.currentTimeMillis()
            )

            val newPurchaseId = database.purchaseDao().insertPurchase(newPurchase).toInt()

            // Create new activation for the renewed plan
            val newActivation = com.esim.travelapp.data.local.entity.ESIMActivationEntity(
                purchaseId = newPurchaseId,
                iccid = "AUTO_${System.currentTimeMillis()}", // Generate new ICCID
                activationStatus = "activated",
                activationDate = System.currentTimeMillis()
            )

            val newActivationId = database.esimActivationDao().insertActivation(newActivation).toInt()

            // Create new data usage record with full data
            val dataTotal = extractDataAmount(plan.dataAmount)
            val newUsage = com.esim.travelapp.data.local.entity.DataUsageEntity(
                activationId = newActivationId,
                dataTotal = dataTotal,
                dataRemaining = dataTotal
            )
            database.dataUsageDao().insertUsage(newUsage)

            // Update auto-renewal last renewal date
            val updatedAutoRenewal = autoRenewal.copy(
                lastRenewalDate = System.currentTimeMillis()
            )
            database.autoRenewalDao().updateAutoRenewal(updatedAutoRenewal)

            // Send notification about auto-renewal
            val notification = NotificationEntity(
                userId = purchase.userId,
                title = "✅ Plan Auto-Renewed",
                message = "${plan.planName} was automatically renewed because your data dropped to ${String.format("%.0f", autoRenewal.renewalThreshold)}% or below. Your data has been fully topped up to ${plan.dataAmount}.",
                type = "activation"
            )
            database.notificationDao().insertNotification(notification)

            Log.d("DataUsageService", "Auto-renewed plan ${plan.planName} for user ${purchase.userId}")

        } catch (e: Exception) {
            Log.e("DataUsageService", "Error performing auto-renewal", e)
        }
    }

    private fun extractDataAmount(dataAmount: String): Double {
        // Extract numeric value from strings like "5GB", "10 GB", "1.5GB"
        val regex = Regex("([0-9]+(?:\\.[0-9]+)?)")
        val match = regex.find(dataAmount)
        return match?.groupValues?.get(1)?.toDoubleOrNull() ?: 5.0 // Default to 5GB
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Log.d("DataUsageService", "Data usage service stopped")
    }
}