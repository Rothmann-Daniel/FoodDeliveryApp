package com.example.fooddelivery.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.model.User
import com.example.fooddelivery.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    // Получаем пользователя из репозитория
    val user: StateFlow<User?> = userRepository.currentUser

    init {
        loadUserData()
    }

    fun loadUserData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userRepository.fetchCurrentUser(true)
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка загрузки данных: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun updateUserField(field: String, value: Any): Boolean {
        return try {
            userRepository.updateUserField(field, value)
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка обновления: ${e.message}"
            false
        }
    }

    // Новый метод для массового обновления
    suspend fun updateMultipleFields(updates: Map<String, Any>): Boolean {
        return try {
            userRepository.updateMultipleFields(updates)
        } catch (e: Exception) {
            _errorMessage.value = "Ошибка обновления: ${e.message}"
            false
        }
    }
}