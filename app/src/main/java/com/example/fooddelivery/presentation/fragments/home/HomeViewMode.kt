package com.example.fooddelivery.presentation.fragments.home

import androidx.lifecycle.ViewModel
import com.example.fooddelivery.data.model.PopularModel
import com.example.fooddelivery.data.repository.BannerRepository
import com.example.fooddelivery.data.repository.FoodRepository
import com.example.fooddelivery.domain.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel(
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _bannerList = MutableStateFlow<List<Int>>(emptyList())
    val bannerList: StateFlow<List<Int>> = _bannerList.asStateFlow()

    private val _popularMenu = MutableStateFlow<List<PopularModel>>(emptyList())
    val popularMenu: StateFlow<List<PopularModel>> = _popularMenu.asStateFlow()

    init {
        loadBanners()
        loadPopularMenu()
    }

    private fun loadBanners() {
        _bannerList.value = BannerRepository.bannerList
    }

    private fun loadPopularMenu() {
        _popularMenu.value = FoodRepository.popularMenu
    }

    fun addToCart(item: PopularModel) {
        cartRepository.addToCart(item)
    }
}