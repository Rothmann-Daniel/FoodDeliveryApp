package com.example.fooddelivery.di

import com.example.fooddelivery.data.repository.UserRepository
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // single создает синглтон на все приложение
    single { UserRepository() }

    //  ViewModel
    // viewModel { ProfileViewModel(get()) }
    // viewModel { DeliveryViewModel(get()) }
}