package com.example.fooddelivery

import android.app.Application
import com.google.firebase.FirebaseApp


class FoodDeliveryApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}