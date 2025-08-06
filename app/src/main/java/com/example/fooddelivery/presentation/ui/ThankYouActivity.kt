package com.example.fooddelivery.presentation.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.fooddelivery.R
import java.text.NumberFormat
import java.util.Currency

class ThankYouActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thank_you)

        // Получаем данные из Intent
        val amount = intent.getDoubleExtra("AMOUNT", 0.0)
        val paymentMethod = intent.getStringExtra("PAYMENT_METHOD") ?: "Not specified"

        // Форматируем сумму
        val formattedAmount = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 2
        }.format(amount)

        // Находим View элементы
        val lottieAnimation = findViewById<LottieAnimationView>(R.id.lottieAnimation)
        val tvOrderDetails = findViewById<TextView>(R.id.tvOrderDetails)

        // Устанавливаем текст с деталями заказа
        tvOrderDetails.text = """
            Total: $formattedAmount
            Payment: $paymentMethod
            Your order will arrive soon!
        """.trimIndent()

        // Анимация появления текста
        tvOrderDetails.alpha = 0f
        tvOrderDetails.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(500) // Небольшая задержка после начала анимации Lottie
            .start()

        lottieAnimation.playAnimation()

        Handler(Looper.getMainLooper()).postDelayed({
            finish()
        }, 3000)
    }
}