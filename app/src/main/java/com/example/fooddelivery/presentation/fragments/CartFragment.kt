package com.example.fooddelivery.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.FragmentCartBinding
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.presentation.adapters.CartAdapter
import java.text.NumberFormat
import java.util.Currency


class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var cartAdapter: CartAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        showLoading()
        setupRecyclerView()
        setupContinueButton()
        updateTotalPrice()
        hideLoading() // Данные загружаются мгновенно, поэтому можно сразу скрыть (далее асинхронно на корутины)
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(requireContext()) {
            updateTotalPrice()
            checkIfCartEmpty()
        }

        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }

        // Загружаем данные из репозитория
        cartAdapter.updateItems(CartRepository.cartItems)
        checkIfCartEmpty()
    }

    private fun setupContinueButton() {
        binding.btContinueCart.setOnClickListener {
            // Обработка перехода к оформлению заказа
        }
    }

    private fun updateTotalPrice() {
        val totalPrice = CartRepository.getTotalPrice()

        if (CartRepository.cartItems.isEmpty()) {
            binding.btContinueCart.text = getString(R.string.continue_cart)
        } else {
            // Используем NumberFormat для правильного форматирования валюты
            val format: NumberFormat = NumberFormat.getCurrencyInstance()
            format.currency = Currency.getInstance("USD") //  получаем валюту из настроек
            format.maximumFractionDigits = 2

            val formattedPrice = format.format(totalPrice)

            // Используем строку с подстановкой цены
            binding.btContinueCart.text = getString(R.string.continue_with_price, formattedPrice)
        }
    }

    private fun checkIfCartEmpty() {
        if (CartRepository.cartItems.isEmpty()) {
            // Показать сообщение о пустой корзине
            binding.rvCart.visibility = View.GONE
            binding.emptyCartView.visibility = View.VISIBLE
            binding.btContinueCart.isEnabled = false
        } else {
            binding.rvCart.visibility = View.VISIBLE
            binding.emptyCartView.visibility = View.GONE
            binding.btContinueCart.isEnabled = true
        }
        updateTotalPrice() // Обновляем текст кнопки
    }

    private fun showLoading() {
        binding.progressBar.visibility = View.VISIBLE
        binding.rvCart.visibility = View.GONE
        binding.emptyCartView.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.progressBar.visibility = View.GONE
    }
}