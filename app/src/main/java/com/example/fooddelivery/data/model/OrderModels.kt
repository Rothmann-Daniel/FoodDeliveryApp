package com.example.fooddelivery.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

// Статусы заказа
enum class OrderStatus {
    PENDING,        // Ожидает подтверждения
    CONFIRMED,      // Подтвержден
    PREPARING,      // Готовится
    READY,          // Готов к доставке
    ON_THE_WAY,     // В пути
    DELIVERED,      // Доставлен
    CANCELLED       // Отменен
}

// Элемент заказа
data class OrderItem(
    val foodName: String = "",
    val foodImage: Int = 0,
    val quantity: Int = 0,
    val price: Double = 0.0
)

// Модель заказа
data class Order(
    @DocumentId
    val orderId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userEmail: String = "",
    val userPhone: String = "",
    val deliveryAddress: String = "",
    val location: String = "",

    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val paymentMethod: String = "",

    val status: String = OrderStatus.PENDING.name,

    @ServerTimestamp
    val createdAt: Timestamp? = null,
    val estimatedDeliveryTime: Timestamp? = null,
    val deliveredAt: Timestamp? = null,

    // Для отслеживания
    val courierName: String = "",
    val courierPhone: String = "",
    val trackingUrl: String = "",

    // FCM токен для push-уведомлений
    val fcmToken: String = ""
) {
    fun getStatusEnum(): OrderStatus {
        return try {
            OrderStatus.valueOf(status)
        } catch (e: Exception) {
            OrderStatus.PENDING
        }
    }

    fun getFormattedDate(): String {
        return createdAt?.toDate()?.let { date ->
            java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault())
                .format(date)
        } ?: ""
    }

    fun getEstimatedDeliveryFormatted(): String {
        return estimatedDeliveryTime?.toDate()?.let { date ->
            java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                .format(date)
        } ?: ""
    }
}

// Обновление статуса заказа (для реального времени)
data class OrderStatusUpdate(
    val orderId: String = "",
    val status: String = "",
    val message: String = "",
    val timestamp: Timestamp? = null
)