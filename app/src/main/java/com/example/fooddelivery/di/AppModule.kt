package com.example.fooddelivery.di

import com.example.fooddelivery.data.repository.UserRepository
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.presentation.fragments.cart.CartViewModel
import com.example.fooddelivery.presentation.activity.delivery.DeliveryViewModel
import com.example.fooddelivery.presentation.activity.details.DetailsViewModel
import com.example.fooddelivery.presentation.fragments.home.HomeViewModel
import com.example.fooddelivery.presentation.fragments.profile.ProfileViewModel
import com.example.fooddelivery.presentation.fragments.search.SearchViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repositories - синглтоны
    single { UserRepository() }
    single { CartRepository() }

    // ViewModels
    viewModel { ProfileViewModel(get()) }
    viewModel { DeliveryViewModel(get()) }
    viewModel { CartViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { DetailsViewModel(get()) }
}