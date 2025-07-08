package com.example.fooddelivery.presentation.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddelivery.data.repository.FoodRepository
import com.example.fooddelivery.R
import com.example.fooddelivery.presentation.adapters.PopularAdapter
import com.example.fooddelivery.data.models.PopularModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomMenuFragment : BottomSheetDialogFragment() {

    private lateinit var adapter: PopularAdapter
    private lateinit var menuList: ArrayList<PopularModel>
    private lateinit var menuRecyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bottom_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Инициализация списка и RecyclerView
        menuList = FoodRepository.popularMenu as ArrayList<PopularModel>
        adapter = PopularAdapter(requireContext(), menuList)
        menuRecyclerView = view.findViewById(R.id.rv_menu)

        menuRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )
        menuRecyclerView.adapter = adapter

        // Обработка кнопки "Назад"
        view.findViewById<View>(R.id.btn_back_home).setOnClickListener {
            dismiss() // Закрывает BottomSheet
        }
    }

    // Системная кнопка "Назад"
    override fun onStart() {
        super.onStart()
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dismiss()
                true
            } else {
                false
            }
        }
    }
}