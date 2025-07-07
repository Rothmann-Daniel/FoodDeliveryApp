package com.example.fooddelivery.models

class PopularModel {

    private var foodImage: Int? = null
    private var foodName: String = ""
    private var foodPrice: String = ""

    constructor()
    constructor(
        foodImage: Int?,
        foodName: String,
        foodPrice: String
    ) {
        this.foodImage = foodImage
        this.foodName = foodName
        this.foodPrice = foodPrice
    }

    fun getFoodImage(): Int? {
        return foodImage
    }

    fun setFoodImage(foodImage: Int?) {
        this.foodImage = foodImage
    }

    fun getFoodName(): String {
        return foodName
    }

    fun setFoodName(foodName: String) {
        this.foodName = foodName
    }

    fun getFoodPrice(): String {
        return foodPrice
    }

    fun setFoodPrice(foodPrice: String) {
        this.foodPrice = foodPrice
    }
}