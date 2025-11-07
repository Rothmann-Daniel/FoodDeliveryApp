package com.example.fooddelivery.presentation.fragments.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.example.fooddelivery.R
import com.example.fooddelivery.data.repository.UserRepository
import com.example.fooddelivery.databinding.DialogPasswordBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ProfileDialogHelper {

    interface ProfileUpdateCallback {
        fun onProfileUpdated()
        fun onError(message: String)
    }

    fun showEditProfileDialog(
        context: Context,
        userRepository: UserRepository,
        lifecycleOwner: LifecycleOwner,
        callback: ProfileUpdateCallback
    ): AlertDialog {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_edit_profile, null)

        val auth = Firebase.auth
        val emailEditText = dialogView.findViewById<TextInputEditText>(R.id.ed_email)
        val emailInfoText = dialogView.findViewById<TextView>(R.id.tv_email_change_info)
        val locationSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdownEditText)

        val locations = context.resources.getStringArray(R.array.locations)
        val adapter = ArrayAdapter(
            context,
            R.layout.dropdown_item,
            R.id.text1,
            locations
        )
        locationSpinner.setAdapter(adapter)

        // Заполняем текущими данными из репозитория
        userRepository.currentUser.value?.let { user ->
            dialogView.findViewById<TextInputEditText>(R.id.ed_name).setText(user.name)
            dialogView.findViewById<TextInputEditText>(R.id.ed_email).setText(user.email)
            dialogView.findViewById<TextInputEditText>(R.id.ed_address).setText(user.address)
            dialogView.findViewById<TextInputEditText>(R.id.ed_phone).setText(user.phone)

            if (user.location != context.getString(R.string.location_not_set)) {
                locationSpinner.setText(user.location, false)
            }
        }

        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newEmail = emailEditText.text.toString().trim()
                val currentEmail = auth.currentUser?.email ?: ""
                emailInfoText.visibility = if (newEmail != currentEmail) View.VISIBLE else View.GONE
            }
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.edit_profile_title))
            .setView(dialogView)
            .setPositiveButton(context.getString(R.string.save)) { dialog, _ ->
                validateAndSaveProfileChanges(
                    context,
                    dialogView,
                    userRepository,
                    lifecycleOwner,
                    callback
                )
                dialog.dismiss()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()
    }

    private fun validateAndSaveProfileChanges(
        context: Context,
        dialogView: View,
        userRepository: UserRepository,
        lifecycleOwner: LifecycleOwner,
        callback: ProfileUpdateCallback
    ) {
        val name = dialogView.findViewById<TextInputEditText>(R.id.ed_name).text.toString().trim()
        val email = dialogView.findViewById<TextInputEditText>(R.id.ed_email).text.toString().trim()
        val address =
            dialogView.findViewById<TextInputEditText>(R.id.ed_address).text.toString().trim()
        val phone = dialogView.findViewById<TextInputEditText>(R.id.ed_phone).text.toString().trim()
        val location =
            dialogView.findViewById<AutoCompleteTextView>(R.id.dropdownEditText).text.toString()
                .trim()

        val auth = Firebase.auth

        when {
            name.isEmpty() -> callback.onError("Введите имя")
            !isValidEmail(email) -> callback.onError("Некорректный email")
            location.isEmpty() -> callback.onError("Выберите локацию")
            else -> {
                if (emailChanged(email, auth.currentUser?.email ?: "")) {
                    showPasswordDialog(
                        context,
                        name,
                        email,
                        address,
                        phone,
                        location,
                        userRepository,
                        lifecycleOwner,
                        callback
                    )
                } else {
                    updateProfileData(
                        name,
                        email,
                        address,
                        phone,
                        location,
                        userRepository,
                        lifecycleOwner,
                        callback
                    )
                }
            }
        }
    }

    private fun updateProfileData(
        name: String,
        email: String,
        address: String,
        phone: String,
        location: String,
        userRepository: UserRepository,
        lifecycleOwner: LifecycleOwner,
        callback: ProfileUpdateCallback
    ) {
        lifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Используем новый метод для массового обновления
                val updates = mapOf(
                    "name" to name,
                    "email" to email,
                    "address" to address,
                    "phone" to phone,
                    "location" to location
                )

                val success = userRepository.updateMultipleFields(updates)

                withContext(Dispatchers.Main) {
                    if (success) {
                        callback.onProfileUpdated()
                    } else {
                        callback.onError("Ошибка сохранения данных")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onError("Ошибка обновления: ${e.message}")
                }
            }
        }
    }

    private fun updateEmailAndProfileData(
        context: Context,
        name: String,
        email: String,
        address: String,
        phone: String,
        location: String,
        userRepository: UserRepository,
        lifecycleOwner: LifecycleOwner,
        callback: ProfileUpdateCallback
    ) {
        val auth = Firebase.auth
        val user = auth.currentUser ?: return

        user.updateEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.sendEmailVerification()
                    updateProfileData(name, email, address, phone, location, userRepository, lifecycleOwner, callback)
                    Toast.makeText(context, "Письмо с подтверждением отправлено", Toast.LENGTH_SHORT).show()
                } else {
                    handleEmailUpdateError(context, task.exception, callback)
                }
            }
    }

    private fun emailChanged(newEmail: String, currentEmail: String): Boolean {
        return newEmail != currentEmail
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showPasswordDialog(
        context: Context,
        name: String,
        email: String,
        address: String,
        phone: String,
        location: String,
        userRepository: UserRepository,
        lifecycleOwner: LifecycleOwner, // Добавляем параметр
        callback: ProfileUpdateCallback
    ) {
        val passwordBinding = DialogPasswordBinding.inflate(LayoutInflater.from(context))
        val auth = Firebase.auth

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle(context.getString(R.string.confirm_password_title))
            .setView(passwordBinding.root)
            .setPositiveButton(context.getString(R.string.confirm), null)
            .setNegativeButton(context.getString(R.string.cancel), null)
            .setOnCancelListener {
                callback.onError("Операция отменена")
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val password = passwordBinding.edPassword.text.toString()
                when {
                    password.isEmpty() -> {
                        passwordBinding.textInputLayout.error =
                            context.getString(R.string.enter_password)
                    }

                    password.length < 6 -> {
                        passwordBinding.textInputLayout.error =
                            context.getString(R.string.password_too_short)
                    }

                    else -> {
                        passwordBinding.textInputLayout.error = null
                        reauthenticateAndUpdate(
                            context,
                            password,
                            name,
                            email,
                            address,
                            phone,
                            location,
                            userRepository,
                            lifecycleOwner,
                            callback
                        )
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun reauthenticateAndUpdate(
        context: Context,
        password: String,
        name: String,
        email: String,
        address: String,
        phone: String,
        location: String,
        userRepository: UserRepository,
        lifecycleOwner: LifecycleOwner, // Добавляем параметр
        callback: ProfileUpdateCallback
    ) {
        val auth = Firebase.auth
        val user = auth.currentUser ?: return
        val currentEmail = user.email ?: return

        val credential = EmailAuthProvider.getCredential(currentEmail, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                updateEmailAndProfileData(
                    context,
                    name,
                    email,
                    address,
                    phone,
                    location,
                    userRepository,
                    lifecycleOwner,
                    callback
                )
            }
            .addOnFailureListener { e ->
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        callback.onError("Неверный пароль")
                    }

                    is FirebaseAuthInvalidUserException -> {
                        callback.onError("Пользователь не найден")
                    }

                    else -> {
                        callback.onError("Ошибка аутентификации: ${e.message}")
                    }
                }
            }
    }

    private fun handleEmailUpdateError(
        context: Context,
        exception: Exception?,
        callback: ProfileUpdateCallback
    ) {
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                callback.onError(context.getString(R.string.invalid_email_format))
            }

            is FirebaseAuthUserCollisionException -> {
                callback.onError(context.getString(R.string.email_already_in_use))
            }

            is FirebaseAuthRecentLoginRequiredException -> {
                callback.onError(context.getString(R.string.reauthentication_required))
            }

            else -> {
                callback.onError(context.getString(R.string.email_update_failed))
            }
        }
    }


}


