package com.example.fooddelivery.presentation.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.FragmentCartBinding
import com.example.fooddelivery.domain.repository.CartRepository
import com.example.fooddelivery.presentation.adapters.CartAdapter
import com.example.fooddelivery.presentation.ui.DeliveryActivity
import org.koin.android.ext.android.inject
import java.text.NumberFormat
import java.util.Currency

class CartFragment : Fragment() {
    private lateinit var binding: FragmentCartBinding
    private lateinit var cartAdapter: CartAdapter
    private val cartRepository: CartRepository by inject() // Инжектим CartRepository

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
        // Используем инжектированный cartRepository
        cartRepository.cartItemsLiveData.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items.toList())
            updateTotalPrice()
            checkIfCartEmpty()
        }
    }

    private fun updateTotalPrice() {
        // Используем инжектированный cartRepository
        val totalPrice = cartRepository.getTotalPrice()
        val format = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 2
        }

        // Используем инжектированный cartRepository
        binding.btContinueCart.text = if (cartRepository.cartItemsLiveData.value.isNullOrEmpty()) {
            getString(R.string.continue_cart)
        } else {
            getString(R.string.continue_with_price, format.format(totalPrice))
        }
    }

    private fun checkIfCartEmpty() {
        // Используем инжектированный cartRepository
        val isEmpty = cartRepository.cartItemsLiveData.value.isNullOrEmpty()
        binding.rvCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyCartView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.btContinueCart.isEnabled = !isEmpty
    }

    private fun setupContinueButton() {
        binding.btContinueCart.setOnClickListener {
            startActivity(Intent(requireContext(), DeliveryActivity::class.java))
        }
    }
}