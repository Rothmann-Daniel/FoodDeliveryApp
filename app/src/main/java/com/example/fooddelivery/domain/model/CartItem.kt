package com.example.fooddelivery.domain.model

import com.example.fooddelivery.data.models.PopularModel

data class CartItem(
    val foodItem: PopularModel,
    val quantity: Int = 1
) {
    fun withQuantity(newQuantity: Int): CartItem = this.copy(quantity = newQuantity)
}