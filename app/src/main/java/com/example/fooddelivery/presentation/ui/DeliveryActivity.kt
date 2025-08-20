package com.example.fooddelivery.presentation.ui


import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.example.fooddelivery.databinding.ActivityDeliveryBinding
import com.example.fooddelivery.domain.repository.CartRepository
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Currency

class DeliveryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDeliveryBinding
    private val viewModel: DeliveryViewModel by viewModel()
    private val cartRepository: CartRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeliveryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupObservers()
        setupBackButton()
        setupSubmitButton()
        setupTextWatchers()
        setupEditProfileButton()

        viewModel.loadUserData()
    }

    private fun setupObservers() {
        // Используем инжектированный cartRepository
        cartRepository.totalPriceLiveData.observe(this) { amount ->
            updateTotalAmount(amount)
        }

        viewModel.userData.observe(this) { user ->
            user?.let { populateUserData(it) }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                showToast(error)
            }
        }
    }


    private fun populateUserData(user: com.example.fooddelivery.data.model.User) {
        with(binding) {
            edNameDelivery.setText(user.name.takeIf { it.isNotEmpty() } ?: "")
            edAddressDelivery.setText(user.address.takeIf { it.isNotEmpty() } ?: "")
            edPhoneDelivery.setText(user.phone.takeIf { it.isNotEmpty() } ?: "")

            nameContainer.error = null
            addressContainer.error = null
            phoneContainer.error = null
        }
    }

    private fun setupTextWatchers() {
        binding.edNameDelivery.addTextChangedListener { text ->
            text?.toString()?.takeIf { it.isNotEmpty() }?.let { name ->
                viewModel.updateUserField("name", name)
            }
        }

        binding.edAddressDelivery.addTextChangedListener { text ->
            text?.toString()?.takeIf { it.isNotEmpty() }?.let { address ->
                viewModel.updateUserField("address", address)
            }
        }

        binding.edPhoneDelivery.addTextChangedListener { text ->
            text?.toString()?.takeIf { it.isNotEmpty() }?.let { phone ->
                viewModel.updateUserField("phone", phone)
            }
        }
    }

    private fun setupEditProfileButton() {
        binding.btnEditProfile.setOnClickListener {
            showEditProfileDialog()
        }
    }

    private fun showEditProfileDialog() {
        val dialog = ProfileDialogHelper.showEditProfileDialog(
            this,
            viewModel.userRepository, // Нужно добавить public метод в ViewModel
            object : ProfileDialogHelper.ProfileUpdateCallback {
                override fun onProfileUpdated() {
                    viewModel.loadUserData() // Перезагружаем данные
                    showToast("Данные сохранены")
                }

                override fun onError(message: String) {
                    showToast(message)
                }
            }
        )
        dialog.show()
    }

    // Остальные методы остаются без изменений
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

        var isValid = true

        if (name.isEmpty()) {
            binding.nameContainer.error = "Please enter your name"
            isValid = false
        } else {
            binding.nameContainer.error = null
        }

        if (address.isEmpty()) {
            binding.addressContainer.error = "Please enter your address"
            isValid = false
        } else {
            binding.addressContainer.error = null
        }

        if (phone.isEmpty()) {
            binding.phoneContainer.error = "Please enter your phone"
            isValid = false
        } else {
            binding.phoneContainer.error = null
        }

        return isValid
    }

    private fun placeOrder() {
        // Используем инжектированный cartRepository
        val currentAmount = cartRepository.getTotalPrice()
        val paymentMethod = when {
            binding.chipCash.isChecked -> "Cash"
            binding.chipCard.isChecked -> "Card"
            binding.chipOnline.isChecked -> "Pay Coin"
            else -> "Not selected"
        }

        val name = binding.edNameDelivery.text.toString().trim()
        val address = binding.edAddressDelivery.text.toString().trim()
        val phone = binding.edPhoneDelivery.text.toString().trim()
        val additionalInfo = binding.edAddInfoCourier.text.toString().trim()

        cartRepository.clearCart()
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

    private fun showToast(message: String) {
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }
}