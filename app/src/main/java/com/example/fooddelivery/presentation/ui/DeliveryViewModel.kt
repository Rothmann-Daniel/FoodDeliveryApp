package com.example.fooddelivery.presentation.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fooddelivery.data.model.User
import com.example.fooddelivery.data.repository.UserRepository
import kotlinx.coroutines.launch


class DeliveryViewModel(val userRepository: UserRepository) : ViewModel() {

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
                _userData.postValue(user)
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка загрузки данных пользователя")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun updateUserField(field: String, value: Any) {
        viewModelScope.launch {
            try {
                val success = userRepository.updateUserField(field, value)
                if (success) {
                    // Перезагружаем данные для обновления UI
                    loadUserData()
                } else {
                    _errorMessage.postValue("Ошибка обновления данных")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Ошибка обновления: ${e.message}")
            }
        }
    }
}