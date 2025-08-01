package com.example.fooddelivery.domain.repository

import com.example.fooddelivery.data.models.PopularModel
import com.example.fooddelivery.domain.model.CartItem

object CartRepository {
    private val _cartItems = mutableListOf<CartItem>()
    val cartItems: List<CartItem> get() = _cartItems.toList()

    fun addToCart(item: PopularModel) {
        val existingItem = _cartItems.find { it.foodItem.foodName == item.foodName }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            _cartItems.add(CartItem(item))
        }
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.remove(item)
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity > 0) {
            item.quantity = newQuantity
        } else {
            _cartItems.remove(item)
        }
    }

    fun clearCart() {
        _cartItems.clear()
    }

    fun getTotalPrice(): Double {
        return _cartItems.sumOf { item ->
            val price = item.foodItem.foodPrice.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            price * item.quantity
        }
    }
}