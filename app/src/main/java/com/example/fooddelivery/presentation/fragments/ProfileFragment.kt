package com.example.fooddelivery.presentation.fragments

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.fooddelivery.R
import com.example.fooddelivery.data.model.User
import com.example.fooddelivery.data.repository.UserRepository
import com.example.fooddelivery.databinding.DialogPasswordBinding
import com.example.fooddelivery.databinding.FragmentProfileBinding
import com.example.fooddelivery.domain.utils.EmailUtils
import com.example.fooddelivery.presentation.ui.LoginUserActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileFragment : Fragment() {

    // region [1. Переменные и константы]
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

    // Внедрение репозитория через Koin
    private val userRepository: UserRepository by inject()

    private var currentPhotoUri: Uri? = null

    // Временные данные для ожидания подтверждения
    private var pendingName: String = ""
    private var pendingEmail: String = ""
    private var pendingAddress: String = ""
    private var pendingPhone: String = ""
    private var pendingLocation: String = ""

    companion object {
        private const val TAG = "ProfileFragment"
        private const val PREFS_NAME = "app_prefs"
        private const val AVATAR_LOCAL_KEY = "avatar_local"
        private const val AVATAR_URL_KEY = "avatar_url"
        private const val LAST_AVATAR_PATH_KEY = "last_avatar_path"
        private const val REQUEST_CODE_PERMISSIONS = 1001

        // Переносим метод проверки Storage в companion object
        fun checkStorageAvailability(callback: (Boolean) -> Unit) {
            try {
                val testRef = Firebase.storage.reference.child("test_connection")
                testRef.downloadUrl
                    .addOnSuccessListener { callback(true) }
                    .addOnFailureListener { callback(false) }
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    // endregion

    // region [2. Launchers для разрешений и результатов]
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.all { it.value }
        if (allGranted) {
            showImageSourceDialog()
        } else {
            showToast("Разрешения не предоставлены")
        }
    }

    private val takePhotoResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            currentPhotoUri?.let { uri ->
                loadImage(uri)
                uploadImageWithFallback(uri)
            }
        }
    }

    private val pickImageResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                loadImage(uri)
                uploadImageWithFallback(uri)
            }
        }
    }
    // endregion

    // region [3. Методы жизненного цикла]
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
        setupClickListeners()
        loadUserData()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    // endregion

    // region [4. Загрузка данных пользователя]
    private fun loadUserData() {
        showLoading(true)

        // Запускаем в lifecycleScope для автоматического управления
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val user = userRepository.fetchCurrentUser()
                if (user != null) {
                    updateUIWithUserData(user)
                } else {
                    setDefaultProfileValues()
                }
            } catch (e: Exception) {
                setDefaultProfileValues()
                showToast("Ошибка загрузки данных")
            } finally {
                showLoading(false)
            }
        }
    }

    private fun updateUIWithUserData(user: User) {
        with(binding) {
            tvProfileName.text = user.name.ifEmpty { getString(R.string.default_name) }
            tvProfileEmail.text = user.email

            val location = user.location.takeIf { it.isNotEmpty() }
                ?: getString(R.string.location_not_set)
            tvProfileLocation.apply {
                text = location
                visibility = if (location == getString(R.string.location_not_set)) View.GONE else View.VISIBLE
            }

            tvProfileAddress.text = user.address.ifEmpty { getString(R.string.address_not_set) }
            tvProfilePhone.text = user.phone.ifEmpty { getString(R.string.phone_not_set) }

            // Загрузка аватара
            loadAvatar(user.avatarUrl)
        }
    }

    private fun setDefaultProfileValues() {
        val currentUser = auth.currentUser ?: return
        with(binding) {
            tvProfileName.text = currentUser.displayName ?: getString(R.string.default_name)
            tvProfileEmail.text = currentUser.email ?: ""
            tvProfileLocation.text = getString(R.string.location_not_set)
            tvProfileAddress.text = getString(R.string.address_not_set)
            tvProfilePhone.text = getString(R.string.phone_not_set)

            tvProfileLocation.visibility = View.VISIBLE
            tvProfileAddress.visibility = View.VISIBLE
            tvProfilePhone.visibility = View.VISIBLE

            loadLocalAvatar()
        }
    }
    // endregion

    // region [5. Настройка кликов]
    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.btnLogout.setOnClickListener { logoutUser() }
        binding.tvSupportProfile.setOnClickListener { EmailUtils.sendSupportEmail(requireContext()) }
        binding.ivProfileAvatar.setOnClickListener { checkPermissionsAndShowDialog() }
    }
    // endregion

    // region [6. Работа с аватаром]
    private fun checkPermissionsAndShowDialog() {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_MEDIA_IMAGES
            )
        } else {
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

        val allPermissionsGranted = permissions.all { permission ->
            ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            showImageSourceDialog()
        } else {
            requestPermissionLauncher.launch(permissions)
        }
    }

    private fun showImageSourceDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_choose_image_source, null)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите источник")
            .setView(dialogView)
            .setNegativeButton("Отмена", null)
            .show()

        dialogView.findViewById<View>(R.id.btn_camera).setOnClickListener {
            openCamera()
        }

        dialogView.findViewById<View>(R.id.btn_gallery).setOnClickListener {
            openGallery()
        }
    }

    private fun openCamera() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                currentPhotoUri = photoURI
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                takePhotoResult.launch(takePictureIntent)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir("images")
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoUri = Uri.fromFile(this)
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        val fallbackIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

        val chooserIntent = Intent.createChooser(intent, "Выберите изображение").apply {
            putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(fallbackIntent))
        }

        try {
            pickImageResult.launch(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            showToast("Не найдено приложение для выбора изображений")
        }
    }

    private fun loadImage(uri: Uri) {
        try {
            Glide.with(requireContext())
                .load(uri)
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(binding.ivProfileAvatar)
        } catch (e: Exception) {
            showToast("Ошибка загрузки изображения")
            Log.e(TAG, "Error loading image", e)
        }
    }

    private fun loadAvatar(avatarUrl: String) {
        if (avatarUrl.isNotEmpty()) {
            Glide.with(requireContext())
                .load(avatarUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .into(binding.ivProfileAvatar)
        } else {
            loadLocalAvatar()
        }
    }

    private fun loadLocalAvatar() {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastAvatarPath = prefs.getString(LAST_AVATAR_PATH_KEY, null)

        if (lastAvatarPath != null) {
            val avatarFile = File(lastAvatarPath)
            if (avatarFile.exists()) {
                Glide.with(requireContext())
                    .load(avatarFile)
                    .placeholder(R.drawable.ic_profile_placeholder)
                    .into(binding.ivProfileAvatar)
                return
            }
        }

        binding.ivProfileAvatar.setImageResource(R.drawable.ic_profile_placeholder)
    }

    private fun uploadImageWithFallback(imageUri: Uri) {
        showLoading(true)

        // Используем метод из companion object
        checkStorageAvailability { isAvailable ->
            if (isAvailable) {
                uploadImageToFirebase(imageUri)
            } else {
                saveImageLocally(imageUri)
                showToast("Облачное хранилище недоступно. Аватар сохранен локально")
                showLoading(false)
            }
        }
    }

    private fun uploadImageToFirebase(imageUri: Uri) {
        val user = auth.currentUser ?: return

        try {
            val storageRef = Firebase.storage.reference
            val avatarRef = storageRef.child("avatars/${user.uid}.jpg")

            avatarRef.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        updateAvatarUrlInFirestore(uri.toString())
                    }
                }
                .addOnFailureListener { e ->
                    showLoading(false)
                    Log.w(TAG, "Ошибка загрузки в Firebase Storage", e)
                    saveImageLocally(imageUri)
                    showToast("Аватар сохранен локально")
                }
        } catch (e: Exception) {
            showLoading(false)
            saveImageLocally(imageUri)
            showToast("Аватар сохранен локально")
        }
    }

    private fun saveImageLocally(imageUri: Uri) {
        try {
            val outputFile = createLocalImageFile()
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val outputStream = FileOutputStream(outputFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(AVATAR_LOCAL_KEY, true)
                .putString(LAST_AVATAR_PATH_KEY, outputFile.absolutePath)
                .apply()

        } catch (e: Exception) {
            Log.e(TAG, "Ошибка сохранения локального аватара", e)
        }
    }

    private fun createLocalImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = requireContext().filesDir
        return File(storageDir, "avatar_$timeStamp.jpg")
    }

    private fun updateAvatarUrlInFirestore(avatarUrl: String) {
        showLoading(true)

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val success = userRepository.updateUserField("avatarUrl", avatarUrl)
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    if (success) {
                        showToast("Аватар обновлен")
                        saveAvatarLocally(avatarUrl)
                    } else {
                        showToast("Ошибка обновления аватара")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showLoading(false)
                    showToast("Ошибка обновления аватара")
                }
            }
        }
    }

    private fun saveAvatarLocally(avatarUrl: String) {
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(AVATAR_URL_KEY, avatarUrl)
            .putBoolean(AVATAR_LOCAL_KEY, false)
            .apply()
    }
    // endregion

    // region [7. Редактирование профиля]
    private fun showEditProfileDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_edit_profile, null)

        val emailEditText = dialogView.findViewById<TextInputEditText>(R.id.ed_email)
        val emailInfoText = dialogView.findViewById<TextView>(R.id.tv_email_change_info)
        val locationSpinner = dialogView.findViewById<AutoCompleteTextView>(R.id.dropdownEditText)

        val locations = resources.getStringArray(R.array.locations)
        val adapter = ArrayAdapter(
            requireContext(),
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

            if (user.location != getString(R.string.location_not_set)) {
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
                pendingName = name
                pendingEmail = email
                pendingAddress = address
                pendingPhone = phone
                pendingLocation = location

                if (emailChanged(email)) {
                    showPasswordDialog()
                } else {
                    updateProfileData()
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
            .setPositiveButton(getString(R.string.confirm), null)
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
                        showPasswordDialog()
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

                    user.sendEmailVerification()
                        .addOnCompleteListener { verificationTask ->
                            if (verificationTask.isSuccessful) {
                                showToast(getString(R.string.verification_email_sent))
                            } else {
                                Log.e(TAG, "Email verification send failed", verificationTask.exception)
                            }
                        }

                    updateProfileData()
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

    private fun updateProfileData() {
        showLoading(true)

        // Запускаем в фоновом потоке для сетевых операций
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val updates = mapOf(
                "name" to pendingName,
                "email" to pendingEmail,
                "address" to pendingAddress,
                "phone" to pendingPhone,
                "location" to pendingLocation
            )

            var successCount = 0
            val totalUpdates = updates.size

            updates.forEach { (field, value) ->
                val success = userRepository.updateUserField(field, value)
                if (success) successCount++
            }

            // Возвращаемся в главный поток для обновления UI
            withContext(Dispatchers.Main) {
                showLoading(false)
                if (successCount == totalUpdates) {
                    loadUserData() // Перезагружаем данные для обновления UI
                    showToast("Данные сохранены")
                } else {
                    showToast("Часть данных сохранена")
                }
                clearPendingData()
            }
        }
    }
    // endregion

    // region [8. Выход и вспомогательные методы]
    private fun logoutUser() {
        auth.signOut()
        userRepository.clearUserData()
        startActivity(Intent(requireContext(), LoginUserActivity::class.java))
        requireActivity().finish()
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnEditProfile.isEnabled = !show
        binding.btnLogout.isEnabled = !show
        binding.ivProfileAvatar.isEnabled = !show
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
    // endregion
}