package com.example.fooddelivery.domain.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fooddelivery.data.model.PopularModel
import com.example.fooddelivery.domain.model.CartItem
import javax.inject.Singleton


@Singleton
class CartRepository {
    private val _cartItems = mutableListOf<CartItem>()
    private val _cartItemsLiveData = MutableLiveData<List<CartItem>>()

    private val _totalPriceLiveData = MutableLiveData<Double>(0.0) // Добавляем LiveData для суммы
    val cartItemsLiveData: LiveData<List<CartItem>> get() = _cartItemsLiveData
    val totalPriceLiveData: LiveData<Double> get() = _totalPriceLiveData // Публичное свойство

    init {
        _cartItemsLiveData.value = _cartItems.toList()
    }

    fun addToCart(item: PopularModel) {
        val existingItemIndex = _cartItems.indexOfFirst { it.foodItem.foodName == item.foodName }
        if (existingItemIndex != -1) {
            // Вместо изменения существующего элемента, создаем новый с увеличенным quantity
            val existingItem = _cartItems[existingItemIndex]
            _cartItems[existingItemIndex] = existingItem.copy(quantity = existingItem.quantity + 1)
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
        val index = _cartItems.indexOfFirst { it.foodItem.foodName == item.foodItem.foodName }
        if (index != -1) {
            if (newQuantity > 0) {
                _cartItems[index] = item.copy(quantity = newQuantity)
            } else {
                _cartItems.removeAt(index)
            }
            updateLiveData()
        }
    }

    fun clearCart() {
        _cartItems.clear()
        updateLiveData()
    }

    fun getTotalPrice(): Double {
        return _cartItems.sumOf { it.foodItem.foodPrice * it.quantity }
    }

    private fun updateLiveData() {
        _cartItemsLiveData.value = _cartItems.toList()
        _totalPriceLiveData.value = getTotalPrice() // Обновляем сумму при каждом изменении
    }
}