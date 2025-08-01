package com.example.fooddelivery.domain.model

import com.example.fooddelivery.data.models.PopularModel

data class CartItem(
    val foodItem: PopularModel,
    var quantity: Int = 1
)