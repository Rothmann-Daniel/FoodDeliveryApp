package com.example.fooddelivery.presentation.fragments.history

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.fooddelivery.databinding.FragmentHistoryBinding
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupObservers()
    }

    private fun setupViewPager() {
        val adapter = HistoryPagerAdapter(this)
        binding.viewPagerHistory.adapter = adapter

        TabLayoutMediator(binding.tabLayoutHistory, binding.viewPagerHistory) { tab, position ->
            tab.text = when (position) {
                0 -> "Active Orders"
                1 -> "History"
                else -> ""
            }
        }.attach()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBarHistory.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadOrders()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class HistoryPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> OrderListFragment.newInstance(true)  // Active orders
                1 -> OrderListFragment.newInstance(false) // Completed orders
                else -> OrderListFragment.newInstance(false)
            }
        }
    }
}