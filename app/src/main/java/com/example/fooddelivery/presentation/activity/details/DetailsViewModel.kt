package com.example.fooddelivery.presentation.activity.details

import androidx.lifecycle.ViewModel
import com.example.fooddelivery.data.model.PopularModel
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetailsViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _foodItem = MutableStateFlow<PopularModel?>(null)
    val foodItem: StateFlow<PopularModel?> = _foodItem.asStateFlow()

    fun setFoodItem(item: PopularModel) {
        _foodItem.value = item
    }

    fun addToCart() {
        _foodItem.value?.let { item ->
            cartRepository.addToCart(item)
        }
    }
}