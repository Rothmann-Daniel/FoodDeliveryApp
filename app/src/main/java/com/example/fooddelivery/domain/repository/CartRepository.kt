package com.example.fooddelivery.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fooddelivery.data.models.PopularModel
import com.example.fooddelivery.domain.model.CartItem

object CartRepository {
    private val _cartItems = mutableListOf<CartItem>()
    private val _cartItemsLiveData = MutableLiveData<List<CartItem>>()
    val cartItemsLiveData: LiveData<List<CartItem>> get() = _cartItemsLiveData

    init {
        _cartItemsLiveData.value = _cartItems.toList()
    }

    fun addToCart(item: PopularModel) {
        val existingItem = _cartItems.find { it.foodItem.foodName == item.foodName }
        if (existingItem != null) {
            existingItem.quantity++
        } else {
            _cartItems.add(CartItem(item))
        }
        updateLiveData()
    }

    fun removeFromCart(item: CartItem) {
        _cartItems.remove(item)
        updateLiveData()
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        if (newQuantity > 0) {
            item.quantity = newQuantity
        } else {
            _cartItems.remove(item)
        }
        updateLiveData()
    }

    fun clearCart() {
        _cartItems.clear()
        updateLiveData()
    }

    fun getTotalPrice(): Double {
        return _cartItems.sumOf { item ->
            val price = item.foodItem.foodPrice.replace("[^0-9.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            price * item.quantity
        }
    }

    private fun updateLiveData() {
        _cartItemsLiveData.postValue(_cartItems.toList())
    }
}