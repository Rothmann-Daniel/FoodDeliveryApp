package com.example.fooddelivery.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val address: String = "",
    val phone: String = "",
    val location: String = "",
    val avatarUrl: String = ""
)