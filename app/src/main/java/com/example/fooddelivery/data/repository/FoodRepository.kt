package com.example.fooddelivery.data.repository

import com.example.fooddelivery.R
import com.example.fooddelivery.data.models.PopularModel

object FoodRepository {
    val popularMenu = mutableListOf(
        PopularModel(
            R.drawable.im_burger,
            "Burger",
            "$10",
            "Juicy beef patty with fresh vegetables",
            "Beef patty, bun, lettuce, tomato, onion, pickles, sauce"
        ),
        PopularModel(
            R.drawable.im_pizza,
            "Pizza",
            "$15",
            "Traditional Italian pizza with thin crust",
            "Dough, tomato sauce, mozzarella, pepperoni, basil"
        ),
        PopularModel(
            R.drawable.im_pasta,
            "Pasta",
            "$20",
            "Classic pasta with rich sauce",
            "Pasta, cream, parmesan, mushrooms, garlic"
        ),
        PopularModel(
            R.drawable.im_french_fries,
            "French Fries",
            "$5,50",
            "Crispy golden fries",
            "Potatoes, salt, vegetable oil"
        ),
        PopularModel(
            R.drawable.im_sandwich,
            "Sandwich",
            "$7",
            "Fresh sandwich for quick snack",
            "Bread, chicken, lettuce, tomato, mayonnaise"
        ),
        PopularModel(
            R.drawable.im_combo,
            "Combo",
            "$30",
            "Perfect meal set for company",
            "Burger, fries, drink and dessert"
        ),
        PopularModel(
            R.drawable.im_hincaly,
            "Caucasian Khinkali",
            "$14",
            "Traditional Caucasian dumplings",
            "Dough, minced lamb/beef, water, spices"
        ),
        PopularModel(
            R.drawable.im_chicken_wings_bbq,
            "Chicken Wings BBQ",
            "$12",
            "Spicy BBQ chicken wings",
            "Chicken wings, BBQ sauce, spices"
        ),
        PopularModel(
            R.drawable.im_sushi,
            "Sushi",
            "$25",
            "Fresh Japanese sushi assortment",
            "Rice, nori, salmon, tuna, avocado, cream cheese"
        ),
        PopularModel(
            R.drawable.im_salad,
            "Salad",
            "$8",
            "Fresh vegetable salad",
            "Lettuce, cucumber, tomato, olive oil, lemon juice"
        ),
        PopularModel(
            R.drawable.im_desert,
            "Сake",
            "$9",
            "Homemade delicious cake",
            "Flour, eggs, sugar, cream, berries"
        )
    )
}