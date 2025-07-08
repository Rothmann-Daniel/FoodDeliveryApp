package com.example.fooddelivery

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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