package com.example.fooddelivery.presentation.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddelivery.databinding.FragmentCartBinding
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.presentation.adapters.CartAdapter


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

        setupRecyclerView()
        setupContinueButton()
        updateTotalPrice()
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
        // Здесь можно обновить отображение общей суммы в корзине
        //binding.tvTotalPrice.text = "$${"%.2f".format(totalPrice)}" // вынести логику в кнопку продолжить
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
    }
}