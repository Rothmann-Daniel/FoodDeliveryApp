package com.example.fooddelivery.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private val _totalAmount = MutableLiveData<Double>(0.0)  // Начальное значение 0.0
    val totalAmount: LiveData<Double> = _totalAmount

    fun setTotalAmount(amount: Double) {
        _totalAmount.value = amount
    }
}