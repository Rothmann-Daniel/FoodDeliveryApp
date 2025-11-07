package com.example.fooddelivery.presentation.fragments.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddelivery.databinding.FragmentOrderListBinding
import com.example.fooddelivery.presentation.adapters.OrderAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrderListFragment : Fragment() {

    private var _binding: FragmentOrderListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModel()
    private lateinit var orderAdapter: OrderAdapter
    private var isActiveOrders: Boolean = false

    companion object {
        private const val ARG_IS_ACTIVE = "is_active"

        fun newInstance(isActive: Boolean): OrderListFragment {
            return OrderListFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_IS_ACTIVE, isActive)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isActiveOrders = arguments?.getBoolean(ARG_IS_ACTIVE, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter(requireContext(), isActiveOrders)
        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            if (isActiveOrders) {
                viewModel.activeOrders.collect { orders ->
                    orderAdapter.submitList(orders)
                    checkIfEmpty(orders.isEmpty())
                }
            } else {
                viewModel.completedOrders.collect { orders ->
                    orderAdapter.submitList(orders)
                    checkIfEmpty(orders.isEmpty())
                }
            }
        }
    }

    private fun checkIfEmpty(isEmpty: Boolean) {
        binding.rvOrders.visibility = if (isEmpty) View.GONE else View.VISIBLE
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}