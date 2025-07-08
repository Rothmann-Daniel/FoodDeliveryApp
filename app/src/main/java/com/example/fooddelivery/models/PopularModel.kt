package com.example.fooddelivery.models

data class PopularModel(
    val foodImage: Int,          // Изображение (ресурс)
    val foodName: String,        // Название блюда
    val foodPrice: String,       // Цена
    val foodDescription: String,     // Описание
    val foodIngredients: String      // Состав
)