package com.example.fooddelivery.data.model

data class PopularModel(
    val foodImage: Int,          // Изображение (ресурс)
    val foodName: String,        // Название блюда
    val foodPrice:Double,       // Цена
    val foodDescription: String,     // Описание
    val foodIngredients: String      // Состав
)
