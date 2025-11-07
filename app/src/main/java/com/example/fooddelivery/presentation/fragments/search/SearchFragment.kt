package com.example.fooddelivery.presentation.fragments.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddelivery.databinding.FragmentSearchBinding
import com.example.fooddelivery.presentation.adapters.PopularAdapter
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SearchViewModel by viewModel()
    private lateinit var searchAdapter: PopularAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupSearch()
        setupObservers()
    }

    private fun setupRecyclerView() {
        binding.rvSearchMenu.apply {
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { results ->
                // Создаем новый адаптер с обновленными результатами
                searchAdapter = PopularAdapter(requireContext(), results)
                binding.rvSearchMenu.adapter = searchAdapter
                checkIfEmpty()
            }
        }
    }

    private fun setupSearch() {
        binding.edSearchBarMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.edSearchBarMenu.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.search(binding.edSearchBarMenu.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun checkIfEmpty() {
        if (viewModel.isSearchEmpty()) {
            // Анимация скрытия RecyclerView
            binding.rvSearchMenu.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    binding.rvSearchMenu.visibility = View.GONE
                }

            // Анимация появления emptyView
            binding.emptySearchView.alpha = 0f
            binding.emptySearchView.visibility = View.VISIBLE
            binding.emptySearchView.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        } else {
            // Анимация скрытия emptyView
            binding.emptySearchView.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction {
                    binding.emptySearchView.visibility = View.GONE
                }

            // Анимация появления RecyclerView
            binding.rvSearchMenu.alpha = 0f
            binding.rvSearchMenu.visibility = View.VISIBLE
            binding.rvSearchMenu.animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}