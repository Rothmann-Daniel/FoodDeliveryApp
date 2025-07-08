package com.example.fooddelivery

import com.example.fooddelivery.models.PopularModel

object FoodRepository {
    val popularMenu = mutableListOf<PopularModel>()

    init {
        popularMenu.addAll(listOf(
            PopularModel(R.drawable.im_burger, "Burger","$10"),
            PopularModel(R.drawable.im_pizza, "Pizza","$15"),
            PopularModel(R.drawable.im_pasta, "Pasta","$20"),
            PopularModel(R.drawable.im_french_fries, "French Fries","$5,50"),
            PopularModel(R.drawable.im_sandwich, "Sandwich","$7"),
            PopularModel(R.drawable.im_combo, "Combo","$30"),
            PopularModel(R.drawable.im_hincaly, "Kavkazi Hincaly","$14"),
            PopularModel(R.drawable.im_chicken_wings_bbq, "Chicken Wings", "$12"),
            PopularModel(R.drawable.im_sushi, "Sushi","$25"),
            PopularModel(R.drawable.im_salad, "Salad","$8"),
            PopularModel(R.drawable.im_desert, "Сake","$9"),

        ))
    }
}