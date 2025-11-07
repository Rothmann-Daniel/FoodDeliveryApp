package com.example.fooddelivery.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeliveryViewModel(val userRepository: UserRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    fun loadUserData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                userRepository.fetchCurrentUser()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки данных пользователя"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserField(field: String, value: Any) {
        viewModelScope.launch {
            try {
                val success = userRepository.updateUserField(field, value)
                if (!success) {
                    _errorMessage.value = "Ошибка обновления данных"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления: ${e.message}"
            }
        }
    }
}