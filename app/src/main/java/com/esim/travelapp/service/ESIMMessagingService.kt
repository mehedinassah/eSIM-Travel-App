package com.esim.travelapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.esim.travelapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class ESIMMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Handle FCM message
        if (remoteMessage.notification != null) {
            sendNotification(
                remoteMessage.notification!!.title ?: "eSIM Travel",
                remoteMessage.notification!!.body ?: "New message"
            )
        }

        // Handle data payload
        if (remoteMessage.data.isNotEmpty()) {
            handleDataMessage(remoteMessage.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Send token to your server to store for later notification delivery
        android.util.Log.d("FCM", "Token: $token")
        
        // Store token locally
        val sharedPref = getSharedPreferences("fcm", MODE_PRIVATE)
        sharedPref.edit().apply {
            putString("fcm_token", token)
            apply()
        }
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "eSIM Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Build notification
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val messageType = data["type"]
        when (messageType) {
            "plan_activation" -> {
                // Handle plan activation notification
                val planName = data["plan_name"] ?: "Plan"
                sendNotification("Plan Activated", "$planName is now active!")
            }
            "low_balance" -> {
                val remaining = data["remaining_data"] ?: "Low"
                sendNotification("Low Balance", "You have $remaining data remaining")
            }
            "plan_expiring" -> {
                val days = data["days"] ?: "soon"
                sendNotification("Plan Expiring", "Your plan expires in $days days")
            }
            "promotional" -> {
                val promo = data["promo_message"] ?: "Special offer available"
                sendNotification("Special Offer", promo)
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "esim_notifications"
        private const val NOTIFICATION_ID = 1
    }
}
