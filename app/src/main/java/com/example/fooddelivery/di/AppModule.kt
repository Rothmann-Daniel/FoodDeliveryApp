package com.example.fooddelivery.di

import com.example.fooddelivery.data.repository.UserRepository
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.presentation.ui.DeliveryViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // single создает синглтон на все приложение
    single { UserRepository() }
    single { CartRepository() }

    //  ViewModel
    // viewModel { ProfileViewModel(get()) }
     viewModel { DeliveryViewModel(get()) }
}