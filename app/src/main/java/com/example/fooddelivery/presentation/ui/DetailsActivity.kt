package com.example.fooddelivery.presentation.ui


import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.fooddelivery.R
import com.example.fooddelivery.data.model.PopularModel
import com.example.fooddelivery.databinding.ActivityDetailsBinding
import com.example.fooddelivery.domain.utils.toPriceString
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailsBinding
    private val viewModel: DetailsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем данные из Intent
        val foodItem = PopularModel(
            foodImage = intent.getIntExtra("foodImage", 0),
            foodName = intent.getStringExtra("foodName") ?: "",
            foodPrice = intent.getDoubleExtra("foodPrice", 0.0),
            foodDescription = intent.getStringExtra("foodDescription") ?: "",
            foodIngredients = intent.getStringExtra("foodIngredients") ?: ""
        )

        viewModel.setFoodItem(foodItem)

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.foodItem.collect { item ->
                item?.let { displayFoodItem(it) }
            }
        }
    }

    private fun displayFoodItem(item: PopularModel) {
        with(binding) {
            tvFoodName.text = item.foodName
            imageFoodDetails.setImageResource(item.foodImage)
            tvShortDescriptionText.text = item.foodDescription
            tvIngredientsText.text = item.foodIngredients
            tvFoodPrice.text = item.foodPrice.toPriceString()
        }
    }

    private fun setupClickListeners() {
        binding.btnAddToCartBM.setOnClickListener {
            viewModel.addToCart()
            viewModel.foodItem.value?.let { item ->
                Toast.makeText(
                    this,
                    getString(R.string.item_added_to_cart, item.foodName),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnBackHome.setOnClickListener {
            finish()
        }
    }
}