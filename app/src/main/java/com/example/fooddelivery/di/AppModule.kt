package com.example.fooddelivery.di

import com.example.fooddelivery.data.repository.UserRepository
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.presentation.ui.CartViewModel
import com.example.fooddelivery.presentation.ui.DeliveryViewModel
import com.example.fooddelivery.presentation.ui.DetailsViewModel
import com.example.fooddelivery.presentation.ui.HomeViewModel
import com.example.fooddelivery.presentation.ui.ProfileViewModel
import com.example.fooddelivery.presentation.ui.SearchViewModel
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