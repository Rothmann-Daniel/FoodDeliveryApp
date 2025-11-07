package com.example.fooddelivery.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.model.PopularModel
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CartViewModel(private val cartRepository: CartRepository) : ViewModel() {

    val cartItems: StateFlow<List<CartItem>> = cartRepository.cartItems
    val totalPrice: StateFlow<Double> = cartRepository.totalPrice

    fun addToCart(item: PopularModel) {
        viewModelScope.launch {
            cartRepository.addToCart(item)
        }
    }

    fun removeFromCart(item: CartItem) {
        viewModelScope.launch {
            cartRepository.removeFromCart(item)
        }
    }

    fun updateQuantity(item: CartItem, newQuantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(item, newQuantity)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }

    fun isCartEmpty(): Boolean = cartItems.value.isEmpty()
}