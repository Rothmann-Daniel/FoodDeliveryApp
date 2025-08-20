package com.example.fooddelivery.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.model.User
import com.example.fooddelivery.data.repository.UserRepository
import kotlinx.coroutines.launch


class DeliveryViewModel(
    private val _userRepository: UserRepository
) : ViewModel() {

    val userRepository: UserRepository
        get() = _userRepository

    private val _userData = MutableLiveData<User?>()
    val userData: LiveData<User?> = _userData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadUserData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val user = userRepository.fetchCurrentUser()
                _userData.value = user
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
                userRepository.updateUserField(field, value)
                // Обновляем локальные данные
                loadUserData()
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка обновления данных"
            }
        }
    }
}