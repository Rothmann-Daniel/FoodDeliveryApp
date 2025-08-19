package com.example.fooddelivery.presentation.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.DialogPasswordBinding
import com.example.fooddelivery.databinding.FragmentProfileBinding
import com.example.fooddelivery.domain.utils.EmailUtils
import com.example.fooddelivery.presentation.ui.LoginUserActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    // Переменные для хранения данных из диалога
    private var pendingName: String = ""
    private var pendingEmail: String = ""
    private var pendingAddress: String = ""
    private var pendingPhone: String = ""
    private var pendingLocation: String = ""

    companion object {
        private const val PREFS_NAME = "app_prefs"
        private const val FIELDS_UPDATED_KEY = "fields_updated"
        private const val TAG = "ProfileFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserData()
        setupClickListeners()
        updateLegacyUserFields()
    }

    private fun updateLegacyUserFields() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(FIELDS_UPDATED_KEY, false)) {
            updateAllUsersWithNewFields()
            prefs.edit().putBoolean(FIELDS_UPDATED_KEY, true).apply()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        // Устанавливаем email из Firebase Auth
        binding.tvProfileEmail.text = currentUser.email ?: ""

        Firebase.firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    updateUIWithUserData(document)
                } else {
                    setDefaultProfileValues()
                }
            }
            .addOnFailureListener {
                setDefaultProfileValues()
                showErrorToast()
            }
    }

    private fun updateUIWithUserData(document: com.google.firebase.firestore.DocumentSnapshot) {
        with(binding) {
            tvProfileName.text = document.getString("name") ?: getString(R.string.default_name)

            val location = document.getString("location").orEmpty().takeIf { it.isNotEmpty() }
                ?: getString(R.string.location_not_set)
            tvProfileLocation.apply {
                text = location
                visibility = if (location == getString(R.string.location_not_set)) View.GONE else View.VISIBLE
            }

            tvProfileAddress.text = document.getString("address") ?: getString(R.string.address_not_set)
            tvProfilePhone.text = document.getString("phone") ?: getString(R.string.phone_not_set)
        }
    }
    private fun setDefaultProfileValues() {
        val currentUser = auth.currentUser ?: return
        with(binding) {
            tvProfileName.text = currentUser.displayName ?: getString(R.string.default_name)
            tvProfileLocation.text = getString(R.string.location_not_set)
            tvProfileAddress.text = getString(R.string.address_not_set)
            tvProfilePhone.text = getString(R.string.phone_not_set)

            // Показываем все поля
            tvProfileLocation.visibility = View.VISIBLE
            tvProfileAddress.visibility = View.VISIBLE
            tvProfilePhone.visibility = View.VISIBLE
        }
    }

    private fun showErrorToast() {
        Toast.makeText(
            requireContext(),
            "Ошибка загрузки данных профиля",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.btnLogout.setOnClickListener { logoutUser() }
        binding.tvSupportProfile.setOnClickListener { EmailUtils.sendSupportEmail(requireContext()) }
    }

    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profile, null)

        val emailEditText = dialogView.findViewById<TextInputEditText>(R.id.ed_email)
        val emailInfoText = dialogView.findViewById<TextView>(R.id.tv_email_change_info)
        val locationSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdownEditText)

        // Заполняем спиннер локациями
        val locations = resources.getStringArray(R.array.locations)
        val adapter = ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            R.id.text1,
            locations
        )
        locationSpinner.setAdapter(adapter)


        // Устанавливаем текущие значения
        with(dialogView) {
            findViewById<TextInputEditText>(R.id.ed_name).setText(binding.tvProfileName.text)
            findViewById<TextInputEditText>(R.id.ed_email).setText(binding.tvProfileEmail.text)
            findViewById<TextInputEditText>(R.id.ed_address).setText(binding.tvProfileAddress.text)
            findViewById<TextInputEditText>(R.id.ed_phone).setText(binding.tvProfilePhone.text)

            // Устанавливаем текущую локацию
            val currentLocation = binding.tvProfileLocation.text.toString()
            if (currentLocation != getString(R.string.location_not_set)) {
                locationSpinner.setText(currentLocation, false)
            }
        }

        // Слушатель для отображения предупреждения при изменении email
        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val newEmail = emailEditText.text.toString().trim()
                val currentEmail = auth.currentUser?.email ?: ""
                emailInfoText.visibility = if (newEmail != currentEmail) View.VISIBLE else View.GONE
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.edit_profile_title))
            .setView(dialogView)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                validateAndSaveProfileChanges(dialogView)
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun validateAndSaveProfileChanges(dialogView: View) {
        val name = dialogView.findViewById<TextInputEditText>(R.id.ed_name).text.toString().trim()
        val email = dialogView.findViewById<TextInputEditText>(R.id.ed_email).text.toString().trim()
        val address = dialogView.findViewById<TextInputEditText>(R.id.ed_address).text.toString().trim()
        val phone = dialogView.findViewById<TextInputEditText>(R.id.ed_phone).text.toString().trim()
        val location = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdownEditText).text.toString().trim()

        when {
            name.isEmpty() -> showToast("Введите имя")
            !isValidEmail(email) -> showToast("Некорректный email")
            location.isEmpty() -> showToast("Выберите локацию")
            else -> {
                // Сохраняем данные для последующего использования
                pendingName = name
                pendingEmail = email
                pendingAddress = address
                pendingPhone = phone
                pendingLocation = location // Добавляем локацию

                if (emailChanged(email)) {
                    showPasswordDialog()
                } else {
                    updateProfileData(name, email, address, phone, location)
                }
            }
        }
    }

    private fun emailChanged(newEmail: String): Boolean {
        val currentEmail = auth.currentUser?.email ?: return false
        return newEmail != currentEmail
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun showPasswordDialog() {
        val passwordBinding = DialogPasswordBinding.inflate(layoutInflater)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.confirm_password_title))
            .setView(passwordBinding.root)
            .setPositiveButton(getString(R.string.confirm), null) // Set to null and handle later
            .setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
                dialog.dismiss()
                clearPendingData()
            }
            .setOnCancelListener {
                clearPendingData()
            }
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val password = passwordBinding.edPassword.text.toString()
                when {
                    password.isEmpty() -> {
                        passwordBinding.textInputLayout.error = getString(R.string.enter_password)
                    }
                    password.length < 6 -> {
                        passwordBinding.textInputLayout.error = getString(R.string.password_too_short)
                    }
                    else -> {
                        passwordBinding.textInputLayout.error = null
                        reauthenticateAndUpdate(password)
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun reauthenticateAndUpdate(password: String) {
        val user = auth.currentUser ?: return
        val currentEmail = user.email ?: return

        showLoading(true)

        val credential = EmailAuthProvider.getCredential(currentEmail, password)

        user.reauthenticate(credential)
            .addOnSuccessListener {
                Log.d(TAG, "Reauthentication successful")
                updateEmailAndProfileData()
            }
            .addOnFailureListener { e ->
                showLoading(false)
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        showToast("Неверный пароль")
                        showPasswordDialog() // Повторно показываем диалог
                    }
                    is FirebaseAuthInvalidUserException -> {
                        showToast("Пользователь не найден")
                        logoutUser()
                    }
                    else -> {
                        showToast("Ошибка аутентификации: ${e.message}")
                        Log.e(TAG, "Reauth error", e)
                        clearPendingData()
                    }
                }
            }
    }

    private fun updateEmailAndProfileData() {
        val user = auth.currentUser ?: return

        user.updateEmail(pendingEmail)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Email updated successfully")

                    // Отправляем письмо подтверждения
                    user.sendEmailVerification()
                        .addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                showToast(getString(R.string.verification_email_sent))
                            } else {
                                Log.e(TAG, "Email verification send failed", verificationTask.exception)
                            }
                        }

                    // Обновляем данные в Firestore (включая локацию)
                    updateProfileData(pendingName, pendingEmail, pendingAddress, pendingPhone, pendingLocation)
                } else {
                    handleEmailUpdateError(task.exception)
                }
                showLoading(false)
            }
    }

    private fun handleEmailUpdateError(exception: Exception?) {
        when (exception) {
            is FirebaseAuthInvalidCredentialsException -> {
                showToast(getString(R.string.invalid_email_format))
            }
            is FirebaseAuthUserCollisionException -> {
                showToast(getString(R.string.email_already_in_use))
            }
            is FirebaseAuthRecentLoginRequiredException -> {
                showToast(getString(R.string.reauthentication_required))
            }
            else -> {
                showToast(getString(R.string.email_update_failed))
                Log.e(TAG, "Email update failed", exception)
            }
        }
        clearPendingData()
    }

    private fun updateProfileData(name: String, email: String, address: String, phone: String, location: String) {
        val user = auth.currentUser ?: return

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "email" to email,
            "address" to address,
            "phone" to phone,
            "location" to location // Добавляем локацию
        )

        Firebase.firestore.collection("users")
            .document(user.uid)
            .update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "Profile data updated successfully")
                updateUI(name, email, address, phone, location) // Обновляем UI с локацией
                showToast("Данные сохранены")
                clearPendingData()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Firestore update error", e)
                showToast("Ошибка сохранения: ${e.message}")
                clearPendingData()
            }
    }

    private fun updateUI(name: String, email: String, address: String, phone: String, location: String) {
        with(binding) {
            tvProfileName.text = name
            tvProfileEmail.text = email
            tvProfileAddress.text = address
            tvProfilePhone.text = phone
            tvProfileLocation.text = location

            // Показываем локацию, если она установлена
            tvProfileLocation.visibility = if (location != getString(R.string.location_not_set)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun logoutUser() {
        auth.signOut()
        startActivity(Intent(requireContext(), LoginUserActivity::class.java))
        requireActivity().finish()
    }

    private fun updateAllUsersWithNewFields() {
        Firebase.firestore.collection("users").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val updates = mutableMapOf<String, Any>()

                    if (!document.contains("address")) {
                        updates["address"] = getString(R.string.address_not_set)
                    }

                    if (!document.contains("phone")) {
                        updates["phone"] = getString(R.string.phone_not_set)
                    }

                    if (!document.contains("location")) {
                        updates["location"] = getString(R.string.location_not_set)
                    }

                    if (updates.isNotEmpty()) {
                        document.reference.update(updates)
                    }
                }
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnEditProfile.isEnabled = !show
        binding.btnLogout.isEnabled = !show
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun clearPendingData() {
        pendingName = ""
        pendingEmail = ""
        pendingAddress = ""
        pendingPhone = ""
        pendingLocation = ""
        showLoading(false)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}