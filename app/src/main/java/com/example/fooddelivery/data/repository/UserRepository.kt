package com.example.fooddelivery.data.repository


import com.example.fooddelivery.data.model.User
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class UserRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    suspend fun fetchCurrentUser(forceUpdate: Boolean = false): User? {
        val firebaseUser = Firebase.auth.currentUser ?: return null

        if (_currentUser.value != null && !forceUpdate) {
            return _currentUser.value
        }

        return try {
            val document = Firebase.firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (document.exists()) {
                val user = document.toObject(User::class.java)?.copy(uid = firebaseUser.uid)
                _currentUser.value = user
                user
            } else {
                val newUser = User(
                    uid = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    name = firebaseUser.displayName ?: "New User"
                )
                Firebase.firestore.collection("users")
                    .document(firebaseUser.uid)
                    .set(newUser)
                    .await()
                _currentUser.value = newUser
                newUser
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateUserField(field: String, value: Any): Boolean {
        val firebaseUser = Firebase.auth.currentUser ?: return false

        return try {
            // Сначала обновляем в Firebase
            Firebase.firestore.collection("users")
                .document(firebaseUser.uid)
                .update(field, value)
                .await()

            // Затем обновляем локальные данные
            _currentUser.value?.let { currentUser ->
                val updatedUser = when (field) {
                    "name" -> currentUser.copy(name = value as String)
                    "email" -> currentUser.copy(email = value as String)
                    "address" -> currentUser.copy(address = value as String)
                    "phone" -> currentUser.copy(phone = value as String)
                    "location" -> currentUser.copy(location = value as String)
                    "avatarUrl" -> currentUser.copy(avatarUrl = value as String)
                    else -> currentUser
                }
                // Обновляем StateFlow - это автоматически уведомит всех подписчиков
                _currentUser.value = updatedUser
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun updateMultipleFields(updates: Map<String, Any>): Boolean {
        val firebaseUser = Firebase.auth.currentUser ?: return false

        return try {
            // Обновляем все поля в Firebase одним запросом
            Firebase.firestore.collection("users")
                .document(firebaseUser.uid)
                .update(updates)
                .await()

            // Обновляем локальные данные
            _currentUser.value?.let { currentUser ->
                var updatedUser = currentUser
                updates.forEach { (field, value) ->
                    updatedUser = when (field) {
                        "name" -> updatedUser.copy(name = value as String)
                        "email" -> updatedUser.copy(email = value as String)
                        "address" -> updatedUser.copy(address = value as String)
                        "phone" -> updatedUser.copy(phone = value as String)
                        "location" -> updatedUser.copy(location = value as String)
                        "avatarUrl" -> updatedUser.copy(avatarUrl = value as String)
                        else -> updatedUser
                    }
                }
                _currentUser.value = updatedUser
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun clearUserData() {
        _currentUser.value = null
    }
}