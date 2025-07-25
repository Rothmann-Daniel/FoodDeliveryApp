package com.example.fooddelivery.presentation.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddelivery.data.models.PopularModel
import com.example.fooddelivery.data.repository.FoodRepository
import com.example.fooddelivery.databinding.FragmentSearchBinding
import com.example.fooddelivery.presentation.adapters.PopularAdapter


class SearchFragment : Fragment() {
    private lateinit var binding: FragmentSearchBinding
    private lateinit var searchAdapter: PopularAdapter
    private val searchList = ArrayList<PopularModel>()
    private val originalList = ArrayList<PopularModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация данных
        originalList.addAll(FoodRepository.popularMenu)
        searchList.addAll(originalList)

        // Настройка RecyclerView
        searchAdapter = PopularAdapter(requireContext(), searchList)
        binding.rvSearchMenu.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }

        // Настройка поиска
        setupSearch()
    }

    private fun setupSearch() {
        binding.edSearchBarMenu.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filter(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // Обработка кнопки поиска на клавиатуре
        binding.edSearchBarMenu.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                filter(binding.edSearchBarMenu.text.toString())
                true
            } else {
                false
            }
        }
    }

    private fun filter(text: String) {
        searchList.clear()
        if (text.isEmpty()) {
            searchList.addAll(originalList)
        } else {
            for (item in originalList) {
                if (item.foodName.contains(text, ignoreCase = true)) {
                    searchList.add(item)
                }
            }
        }
        searchAdapter.notifyDataSetChanged()
    }
}