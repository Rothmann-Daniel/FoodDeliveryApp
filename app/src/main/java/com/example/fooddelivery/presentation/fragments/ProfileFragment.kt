package com.example.fooddelivery.presentation.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.FragmentProfileBinding
import com.example.fooddelivery.domain.utils.EmailUtils
import com.example.fooddelivery.presentation.ui.LoginUserActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth

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

        // Однократное обновление старых пользователей
        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        if (!prefs.getBoolean("fields_updated", false)) {
            updateAllUsersWithNewFields()
            prefs.edit().putBoolean("fields_updated", true).apply()
        }
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        binding.tvProfileEmail.text = currentUser.email ?: ""

        Firebase.firestore.collection("users").document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Имя
                    binding.tvProfileName.text = document.getString("name") ?: getString(R.string.default_name)

                    // Локация
                    val location = document.getString("location").takeIf { !it.isNullOrEmpty() }
                        ?: getString(R.string.location_not_set)
                    binding.tvProfileLocation.apply {
                        text = location
                        visibility = if (location == getString(R.string.location_not_set)) View.GONE else View.VISIBLE
                    }

                    // Адрес - проверяем на пустоту
                    val address = document.getString("address").takeIf { !it.isNullOrEmpty() }
                        ?: getString(R.string.address_not_set)
                    binding.tvProfileAddress.apply {
                        text = address
                        // Показываем только если есть адрес или заглушка
                        visibility = if (address == getString(R.string.address_not_set)) View.VISIBLE else View.VISIBLE
                    }

                    // Телефон - аналогично
                    val phone = document.getString("phone").takeIf { !it.isNullOrEmpty() }
                        ?: getString(R.string.phone_not_set)
                    binding.tvProfilePhone.apply {
                        text = phone
                        visibility = View.VISIBLE
                    }
                }
            }
            .addOnFailureListener {
                // Обработка ошибок с заглушками
                binding.tvProfileName.text = currentUser.displayName ?: getString(R.string.default_name)
                binding.tvProfileLocation.text = getString(R.string.location_not_set)
                binding.tvProfileAddress.text = getString(R.string.address_not_set)
                binding.tvProfilePhone.text = getString(R.string.phone_not_set)

                // Показываем все поля даже при ошибке
                binding.tvProfileLocation.visibility = View.VISIBLE
                binding.tvProfileAddress.visibility = View.VISIBLE
                binding.tvProfilePhone.visibility = View.VISIBLE
            }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)

            val edName = dialogView.findViewById<TextInputEditText>(R.id.ed_name)
            val edAddress = dialogView.findViewById<TextInputEditText>(R.id.ed_address)
            val edPhone = dialogView.findViewById<TextInputEditText>(R.id.ed_phone)

            edName.setText(binding.tvProfileName.text)
            edAddress.setText(binding.tvProfileAddress.text)
            edPhone.setText(binding.tvProfilePhone.text)

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Редактирование профиля")
                .setView(dialogView)
                .setPositiveButton("Сохранить") { _, _ ->
                    saveProfileData(
                        edName.text.toString(),
                        edAddress.text.toString(),
                        edPhone.text.toString()
                    )
                }
                .setNegativeButton("Отмена", null)
                .show()
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            startActivity(Intent(requireContext(), LoginUserActivity::class.java))
            requireActivity().finish()
        }

        binding.tvSupportProfile.setOnClickListener {
            EmailUtils.sendSupportEmail(requireContext())
        }
    }

    private fun saveProfileData(name: String, address: String, phone: String) {
        val currentUser = auth.currentUser ?: return

        val updates = hashMapOf<String, Any>(
            "name" to name,
            "address" to address,
            "phone" to phone
        )

        Firebase.firestore.collection("users").document(currentUser.uid)
            .update(updates)
            .addOnSuccessListener {
                binding.tvProfileName.text = name
                binding.tvProfileAddress.text = address
                binding.tvProfilePhone.text = phone
                Toast.makeText(requireContext(), "Данные сохранены", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка сохранения", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAllUsersWithNewFields() {
        val db = Firebase.firestore

        db.collection("users").get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Проверяем, есть ли уже поля, чтобы не перезаписать существующие
                    val updates = hashMapOf<String, Any>()
                    if (!document.contains("address")) {
                        updates["address"] = getString(R.string.address_not_set)//""// или getString(R.string.address_not_set)
                    }
                    if (!document.contains("phone")) {
                        updates["phone"] = ""  // или getString(R.string.phone_not_set)
                    }

                    if (updates.isNotEmpty()) {
                        document.reference.update(updates)
                            .addOnSuccessListener {
                                Log.d("Firestore", "Поля добавлены для ${document.id}")
                            }
                    }
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Ошибка обновления документов", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}