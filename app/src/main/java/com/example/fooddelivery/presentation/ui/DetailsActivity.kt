package com.example.fooddelivery.presentation.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.fooddelivery.data.models.PopularModel
import com.example.fooddelivery.databinding.ActivityDetailsBinding
import com.example.fooddelivery.domain.repository.CartRepository

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private lateinit var foodItem: PopularModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем данные
        foodItem = PopularModel(
            foodImage = intent.getIntExtra("foodImage", 0),
            foodName = intent.getStringExtra("foodName") ?: "",
            foodPrice = intent.getStringExtra("foodPrice") ?: "",
            foodDescription = intent.getStringExtra("foodDescription") ?: "",
            foodIngredients = intent.getStringExtra("foodIngredients") ?: ""
        )

        // Устанавливаем данные
        binding.tvFoodName.text = foodItem.foodName
        binding.imageFoodDetails.setImageResource(foodItem.foodImage)
        binding.tvShortDescriptionText.text = foodItem.foodDescription
        binding.tvIngredientsText.text = foodItem.foodIngredients
        binding.tvFoodPrice.text = foodItem.foodPrice

        // Обработка кнопки "Добавить в корзину"
        binding.btnAddToCartBM.setOnClickListener {
            CartRepository.addToCart(foodItem)
            Toast.makeText(this, "${foodItem.foodName} добавлен в корзину", Toast.LENGTH_SHORT).show()
        }

        binding.btnBackHome.setOnClickListener {
            finish()
        }
    }
}