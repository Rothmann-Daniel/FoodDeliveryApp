package com.example.fooddelivery.presentation.ui

import android.content.Intent
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

        setupObservers()
        setupBackButton()
        setupSubmitButton()
    }

    private fun setupObservers() {
        CartRepository.totalPriceLiveData.observe(this) { amount ->
            updateTotalAmount(amount)
        }
    }

    private fun setupBackButton() {
        binding.btnBackToCart.setOnClickListener {
            finish()
        }
    }

    private fun setupSubmitButton() {
        binding.btnSubmit.setOnClickListener {
            if (validateInputs()) {
                placeOrder()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.edNameDelivery.text.toString().trim()
        val address = binding.edAddressDelivery.text.toString().trim()
        val phone = binding.edPhoneDelivery.text.toString().trim()

        return when {
            name.isEmpty() -> {
                binding.nameContainer.error = "Please enter your name"
                false
            }
            address.isEmpty() -> {
                binding.addressContainer.error = "Please enter your address"
                false
            }
            phone.isEmpty() -> {
                binding.phoneContainer.error = "Please enter your phone"
                false
            }
            else -> true
        }
    }

    private fun placeOrder() {
        val currentAmount = CartRepository.getTotalPrice()
        val paymentMethod = when {
            binding.chipCash.isChecked -> "Cash"
            binding.chipCard.isChecked -> "Card"
            binding.chipOnline.isChecked -> "Pay Coin"
            else -> "Not selected"
        }

        // Здесь можно добавить логику сохранения заказа

        // Очищаем корзину после успешного заказа
        CartRepository.clearCart()

        // Показываем подтверждение
        showOrderConfirmation(currentAmount, paymentMethod)
    }

    private fun showOrderConfirmation(amount: Double, paymentMethod: String) {
        val intent = Intent(this, ThankYouActivity::class.java).apply {
            putExtra("AMOUNT", amount)
            putExtra("PAYMENT_METHOD", paymentMethod)
        }
        startActivity(intent)
        finish()
    }

    private fun updateTotalAmount(amount: Double) {
        val format = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 2
        }
        binding.tvTotalAmount.text = format.format(amount)
    }
}