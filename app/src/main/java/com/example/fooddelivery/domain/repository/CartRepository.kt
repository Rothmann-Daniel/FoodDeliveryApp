package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.model.PopularModel
import com.example.fooddelivery.domain.model.CartItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Singleton

@Singleton
class CartRepository {
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: StateFlow<List<CartItem>> = _cartItems.asStateFlow()

    private val _totalPrice = MutableStateFlow(0.0)
    val totalPrice: StateFlow<Double> = _totalPrice.asStateFlow()

    fun addToCart(item: PopularModel) {
        val currentList = _cartItems.value.toMutableList()
        val existingItemIndex = currentList.indexOfFirst { it.foodItem.foodName == item.foodName }

        if (existingItemIndex != -1) {
            val existingItem = currentList[existingItemIndex]
            currentList[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
        } else {
            currentList.add(CartItem(item))
        }

        _cartItems.value = currentList
        updateTotalPrice()
    }

    fun removeFromCart(item: CartItem) {
        val currentList = _cartItems.value.toMutableList()
        currentList.remove(item)
        _cartItems.value = currentList
        updateTotalPrice()
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.foodItem.foodName == item.foodItem.foodName }

        if (index != -1) {
            if (newQuantity > 0) {
                currentList[index] = item.copy(quantity = newQuantity)
            } else {
                currentList.removeAt(index)
            }
            _cartItems.value = currentList
            updateTotalPrice()
        }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _totalPrice.value = 0.0
    }

    fun getTotalPrice(): Double {
        return _cartItems.value.sumOf { it.foodItem.foodPrice * it.quantity }
    }

    private fun updateTotalPrice() {
        _totalPrice.value = getTotalPrice()
    }
}