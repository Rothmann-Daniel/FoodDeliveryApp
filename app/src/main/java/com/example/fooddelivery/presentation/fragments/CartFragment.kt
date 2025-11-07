package com.example.fooddelivery.presentation.fragments


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.FragmentCartBinding
import com.example.fooddelivery.presentation.adapters.CartAdapter
import com.example.fooddelivery.presentation.ui.CartViewModel
import com.example.fooddelivery.presentation.ui.DeliveryActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.NumberFormat
import java.util.Currency

class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var cartAdapter: CartAdapter
    private val viewModel: CartViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
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
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.cartItems.collect { items ->
                cartAdapter.submitList(items)
                checkIfCartEmpty()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.totalPrice.collect { price ->
                updateTotalPrice(price)
            }
        }
    }

    private fun updateTotalPrice(totalPrice: Double) {
        val format = NumberFormat.getCurrencyInstance().apply {
            currency = Currency.getInstance("USD")
            maximumFractionDigits = 2
        }

        binding.btContinueCart.text = if (viewModel.isCartEmpty()) {
            getString(R.string.continue_cart)
        } else {
            getString(R.string.continue_with_price, format.format(totalPrice))
        }
    }

    private fun checkIfCartEmpty() {
        val isEmpty = viewModel.isCartEmpty()
        binding.rvCart.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyCartView.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.btContinueCart.isEnabled = !isEmpty
    }

    private fun setupContinueButton() {
        binding.btContinueCart.setOnClickListener {
            startActivity(Intent(requireContext(), DeliveryActivity::class.java))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}