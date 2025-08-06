package com.example.fooddelivery.presentation.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.fooddelivery.databinding.ActivityDeliveryBinding
import com.example.fooddelivery.domain.repository.CartRepository
import java.text.NumberFormat
import java.util.Currency

class DeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeliveryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Получаем сумму из Intent
        val totalAmount = intent.getDoubleExtra("TOTAL_AMOUNT", 0.0)
        updateTotalAmount(totalAmount)

        // Если нужно отслеживать изменения в реальном времени:
        CartRepository.totalPriceLiveData.observe(this) { amount ->
            updateTotalAmount(amount)
        }
    }

    private fun updateTotalAmount(amount: Double) {
        val format = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 2
        }
        binding.tvTotalAmount.text = format.format(amount)
    }
}