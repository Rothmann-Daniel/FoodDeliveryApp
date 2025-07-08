package com.example.fooddelivery.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fooddelivery.databinding.ActivityDetailsBinding

class DetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем все переданные данные
        val foodName = intent.getStringExtra("foodName") ?: ""
        val foodImage = intent.getIntExtra("foodImage", 0)
        //val foodPrice = intent.getStringExtra("foodPrice") ?: ""
        val description = intent.getStringExtra("foodDescription") ?: ""
        val ingredients = intent.getStringExtra("foodIngredients") ?: ""


        // Устанавливаем данные в UI
        binding.tvFoodName.text = foodName
        binding.imageFoodDetails.setImageResource(foodImage)
        binding.tvShortDescriptionText.text = description
        binding.tvIngredientsText.text = ingredients

        binding.btnBackHome.setOnClickListener {
            finish()
        }
    }
}