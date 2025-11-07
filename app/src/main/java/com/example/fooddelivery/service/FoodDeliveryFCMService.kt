package com.example.fooddelivery.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.fooddelivery.R
import com.example.fooddelivery.presentation.activity.delivery.OrderTrackingActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FoodDeliveryFCMService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "food_delivery_channel"
        private const val CHANNEL_NAME = "Food Delivery Notifications"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token: $token")

        // Сохраняем токен в SharedPreferences
        saveFcmToken(token)

        // Здесь можно отправить токен на сервер или в Firestore
        // для привязки к пользователю
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "Message received from: ${message.from}")

        // Обрабатываем data payload
        message.data.isNotEmpty().let {
            Log.d(TAG, "Message data: ${message.data}")
            handleDataMessage(message.data)
        }

        // Обрабатываем notification payload
        message.notification?.let {
            Log.d(TAG, "Message notification: ${it.body}")
            showNotification(
                title = it.title ?: "Food Delivery",
                body = it.body ?: "",
                orderId = message.data["orderId"]
            )
        }
    }

    private fun handleDataMessage(data: Map<String, String>) {
        val notificationType = data["type"]
        val orderId = data["orderId"]
        val title = data["title"]
        val body = data["body"]

        when (notificationType) {
            "order_status_update" -> {
                showNotification(
                    title = title ?: "Order Update",
                    body = body ?: "Your order status has been updated",
                    orderId = orderId
                )
            }
            "delivery_near" -> {
                showNotification(
                    title = title ?: "Delivery Almost Here!",
                    body = body ?: "Your order will arrive in 5 minutes",
                    orderId = orderId,
                    highPriority = true
                )
            }
            "order_delivered" -> {
                showNotification(
                    title = title ?: "Order Delivered",
                    body = body ?: "Your order has been delivered. Enjoy your meal!",
                    orderId = orderId
                )
            }
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        orderId: String?,
        highPriority: Boolean = false
    ) {
        createNotificationChannel()

        val intent = Intent(this, OrderTrackingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            orderId?.let { putExtra("ORDER_ID", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Замените на ваш значок
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (highPriority) {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
        } else {
            notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifications for food delivery status updates"
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveFcmToken(token: String) {
        val prefs = getSharedPreferences("fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }
}