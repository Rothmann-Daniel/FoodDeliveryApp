package com.example.fooddelivery.data.repository

import android.util.Log
import com.example.fooddelivery.data.model.Order
import com.example.fooddelivery.data.model.OrderStatus
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Singleton

@Singleton
class OrderRepository {
    private val db = Firebase.firestore
    private val ordersCollection = db.collection("orders")

    companion object {
        private const val TAG = "OrderRepository"
    }

    // Создание нового заказа
    suspend fun createOrder(order: Order): Result<String> {
        return try {
            val currentUser = Firebase.auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            // Добавляем userId к заказу
            val orderWithUserId = order.copy(userId = currentUser.uid)

            // Создаем документ с автоматическим ID
            val docRef = ordersCollection.document()
            val orderWithId = orderWithUserId.copy(orderId = docRef.id)

            docRef.set(orderWithId).await()

            Log.d(TAG, "Order created successfully: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating order", e)
            Result.failure(e)
        }
    }

    // Получение истории заказов пользователя
    suspend fun getUserOrders(): Result<List<Order>> {
        return try {
            val currentUser = Firebase.auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not authenticated"))
            }

            val snapshot = ordersCollection
                .whereEqualTo("userId", currentUser.uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .await()

            val orders = snapshot.documents.mapNotNull { doc ->
                doc.toObject(Order::class.java)
            }

            Log.d(TAG, "Retrieved ${orders.size} orders")
            Result.success(orders)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting orders", e)
            Result.failure(e)
        }
    }

    // Получение заказа по ID
    suspend fun getOrderById(orderId: String): Result<Order> {
        return try {
            val snapshot = ordersCollection.document(orderId).get().await()
            val order = snapshot.toObject(Order::class.java)

            if (order != null) {
                Result.success(order)
            } else {
                Result.failure(Exception("Order not found"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting order by ID", e)
            Result.failure(e)
        }
    }

    // Отслеживание заказа в реальном времени
    fun observeOrder(orderId: String): Flow<Order?> = callbackFlow {
        var registration: ListenerRegistration? = null

        try {
            registration = ordersCollection.document(orderId)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing order", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val order = snapshot.toObject(Order::class.java)
                        trySend(order)
                    } else {
                        trySend(null)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up order observer", e)
            close(e)
        }

        awaitClose {
            registration?.remove()
        }
    }

    // Отслеживание всех активных заказов пользователя
    fun observeActiveOrders(): Flow<List<Order>> = callbackFlow {
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            close(Exception("User not authenticated"))
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null

        try {
            registration = ordersCollection
                .whereEqualTo("userId", currentUser.uid)
                .whereIn("status", listOf(
                    OrderStatus.PENDING.name,
                    OrderStatus.CONFIRMED.name,
                    OrderStatus.PREPARING.name,
                    OrderStatus.READY.name,
                    OrderStatus.ON_THE_WAY.name
                ))
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error observing active orders", error)
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val orders = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Order::class.java)
                        }
                        trySend(orders)
                    }
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up active orders observer", e)
            close(e)
        }

        awaitClose {
            registration?.remove()
        }
    }

    // Обновление статуса заказа (для тестирования или админ-панели)
    suspend fun updateOrderStatus(orderId: String, newStatus: OrderStatus): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .update("status", newStatus.name)
                .await()

            Log.d(TAG, "Order status updated: $orderId -> $newStatus")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating order status", e)
            Result.failure(e)
        }
    }

    // Отмена заказа
    suspend fun cancelOrder(orderId: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .update(mapOf(
                    "status" to OrderStatus.CANCELLED.name,
                    "deliveredAt" to com.google.firebase.Timestamp.now()
                ))
                .await()

            Log.d(TAG, "Order cancelled: $orderId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling order", e)
            Result.failure(e)
        }
    }

    // Сохранение FCM токена для push-уведомлений
    suspend fun updateFcmToken(orderId: String, token: String): Result<Unit> {
        return try {
            ordersCollection.document(orderId)
                .update("fcmToken", token)
                .await()

            Log.d(TAG, "FCM token updated for order: $orderId")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating FCM token", e)
            Result.failure(e)
        }
    }
}