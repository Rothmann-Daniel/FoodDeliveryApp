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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupContinueButton()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(requireContext())
        binding.rvCart.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun setupObservers() {
        CartRepository.cartItemsLiveData.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)
            updateTotalPrice()
            checkIfCartEmpty()
        }
    }

    private fun setupContinueButton() {
        binding.btContinueCart.setOnClickListener {
            // Обработка перехода к оформлению заказа
        }
    }

    private fun updateTotalPrice() {
        val totalPrice = CartRepository.getTotalPrice()

        if (CartRepository.cartItemsLiveData.value.isNullOrEmpty()) {
            binding.btContinueCart.text = getString(R.string.continue_cart)
        } else {
            val format = NumberFormat.getCurrencyInstance().apply {
                currency = Currency.getInstance("USD")
                maximumFractionDigits = 2
            }
            binding.btContinueCart.text =
                getString(R.string.continue_with_price, format.format(totalPrice))
        }
    }

    private fun checkIfCartEmpty() {
        if (CartRepository.cartItemsLiveData.value.isNullOrEmpty()) {
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