package com.example.fooddelivery.presentation.ui

import androidx.lifecycle.ViewModel
import com.example.fooddelivery.data.model.PopularModel
import com.example.fooddelivery.data.repository.FoodRepository
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val originalList = FoodRepository.popularMenu

    private val _searchResults = MutableStateFlow<List<PopularModel>>(originalList)
    val searchResults: StateFlow<List<PopularModel>> = _searchResults.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        _searchResults.value = originalList
    }

    fun search(query: String) {
        _searchQuery.value = query

        _searchResults.value = if (query.isEmpty()) {
            originalList
        } else {
            originalList.filter {
                it.foodName.contains(query, ignoreCase = true)
            }
        }
    }

    fun addToCart(item: PopularModel) {
        cartRepository.addToCart(item)
    }

    fun isSearchEmpty(): Boolean = _searchResults.value.isEmpty()
}