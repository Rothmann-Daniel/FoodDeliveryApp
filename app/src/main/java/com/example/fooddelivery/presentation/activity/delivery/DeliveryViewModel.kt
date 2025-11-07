package com.example.fooddelivery.presentation.activity.delivery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.model.Order
import com.example.fooddelivery.data.model.OrderItem
import com.example.fooddelivery.data.repository.OrderRepository
import com.example.fooddelivery.data.repository.UserRepository
import com.example.fooddelivery.domain.model.CartItem
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class DeliveryViewModel(
    val userRepository: UserRepository,
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    fun loadUserData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.fetchCurrentUser()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки данных пользователя"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserField(field: String, value: Any) {
        viewModelScope.launch {
            try {
                val success = userRepository.updateUserField(field, value)
                if (!success) {
                    _errorMessage.value = "Ошибка обновления данных"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления: ${e.message}"
            }
        }
    }

    fun createOrder(
        items: List<CartItem>,
        totalAmount: Double,
        paymentMethod: String,
        deliveryAddress: String,
        phone: String
    ) {
        viewModelScope.launch {
            try {
                val user = userRepository.currentUser.value ?: return@launch

                // Конвертируем CartItem в OrderItem
                val orderItems = items.map { cartItem ->
                    OrderItem(
                        foodName = cartItem.foodItem.foodName,
                        foodImage = cartItem.foodItem.foodImage,
                        quantity = cartItem.quantity,
                        price = cartItem.foodItem.foodPrice
                    )
                }

                // Рассчитываем примерное время доставки (30-40 минут)
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.MINUTE, (30..40).random())
                val estimatedDeliveryTime = Timestamp(calendar.time)

                val order = Order(
                    userId = user.uid,
                    userName = user.name,
                    userEmail = user.email,
                    userPhone = phone,
                    deliveryAddress = deliveryAddress,
                    location = user.location,
                    items = orderItems,
                    totalAmount = totalAmount,
                    paymentMethod = paymentMethod,
                    estimatedDeliveryTime = estimatedDeliveryTime
                )

                val result = orderRepository.createOrder(order)
                result.onFailure { error ->
                    _errorMessage.value = "Ошибка создания заказа: ${error.message}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка создания заказа: ${e.message}"
            }
        }
    }
}