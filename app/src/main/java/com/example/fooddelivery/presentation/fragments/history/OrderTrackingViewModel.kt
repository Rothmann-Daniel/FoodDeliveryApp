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

data class TrackingStep(
    val status: OrderStatus,
    val title: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val time: String = ""
)

class OrderTrackingViewModel(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _order = MutableStateFlow<Order?>(null)
    val order: StateFlow<Order?> = _order.asStateFlow()

    private val _trackingSteps = MutableStateFlow<List<TrackingStep>>(emptyList())
    val trackingSteps: StateFlow<List<TrackingStep>> = _trackingSteps.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun observeOrder(orderId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                orderRepository.observeOrder(orderId).collect { order ->
                    _order.value = order
                    order?.let { updateTrackingSteps(it) }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                _isLoading.value = false
            }
        }
    }

    private fun updateTrackingSteps(order: Order) {
        val currentStatus = order.getStatusEnum()
        val allStatuses = listOf(
            OrderStatus.PENDING to "Order Placed",
            OrderStatus.CONFIRMED to "Order Confirmed",
            OrderStatus.PREPARING to "Preparing Your Food",
            OrderStatus.READY to "Ready for Pickup",
            OrderStatus.ON_THE_WAY to "On the Way",
            OrderStatus.DELIVERED to "Delivered"
        )

        val steps = allStatuses.mapIndexed { index, (status, title) ->
            TrackingStep(
                status = status,
                title = title,
                isCompleted = status.ordinal <= currentStatus.ordinal,
                isCurrent = status == currentStatus,
                time = if (status == currentStatus) {
                    order.getFormattedDate()
                } else ""
            )
        }

        _trackingSteps.value = steps
    }

    fun cancelOrder() {
        val orderId = _order.value?.orderId ?: return

        viewModelScope.launch {
            try {
                val result = orderRepository.cancelOrder(orderId)
                result.onSuccess {
                    _errorMessage.value = "Order cancelled successfully"
                }.onFailure { error ->
                    _errorMessage.value = error.message ?: "Error cancelling order"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            }
        }
    }

    fun canCancelOrder(): Boolean {
        val status = _order.value?.getStatusEnum() ?: return false
        return status in listOf(
            OrderStatus.PENDING,
            OrderStatus.CONFIRMED,
            OrderStatus.PREPARING
        )
    }

    fun clearError() {
        _errorMessage.value = null
    }
}