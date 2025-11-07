package com.example.fooddelivery.presentation.fragments.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.model.Order
import com.example.fooddelivery.data.model.OrderStatus
import com.example.fooddelivery.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _activeOrders = MutableStateFlow<List<Order>>(emptyList())
    val activeOrders: StateFlow<List<Order>> = _activeOrders.asStateFlow()

    private val _completedOrders = MutableStateFlow<List<Order>>(emptyList())
    val completedOrders: StateFlow<List<Order>> = _completedOrders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        loadOrders()
        observeActiveOrders()
    }

    fun loadOrders() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val result = orderRepository.getUserOrders()
                result.onSuccess { ordersList ->
                    _orders.value = ordersList

                    // Разделяем на активные и завершенные
                    val active = ordersList.filter { order ->
                        order.getStatusEnum() in listOf(
                            OrderStatus.PENDING,
                            OrderStatus.CONFIRMED,
                            OrderStatus.PREPARING,
                            OrderStatus.READY,
                            OrderStatus.ON_THE_WAY
                        )
                    }

                    val completed = ordersList.filter { order ->
                        order.getStatusEnum() in listOf(
                            OrderStatus.DELIVERED,
                            OrderStatus.CANCELLED
                        )
                    }

                    _activeOrders.value = active
                    _completedOrders.value = completed
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Error loading orders"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun observeActiveOrders() {
        viewModelScope.launch {
            try {
                orderRepository.observeActiveOrders().collect { orders ->
                    _activeOrders.value = orders
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            try {
                val result = orderRepository.cancelOrder(orderId)
                result.onSuccess {
                    loadOrders() // Перезагружаем список
                    _errorMessage.value = null
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Error cancelling order"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}