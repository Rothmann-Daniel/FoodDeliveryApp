package com.example.fooddelivery.presentation.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.FragmentProfileBinding
import com.example.fooddelivery.domain.utils.EmailUtils
import com.example.fooddelivery.presentation.ui.LoginUserActivity
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
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            // Установка email (доступен сразу из auth)
            binding.tvProfileEmail.text = currentUser.email ?: ""

            // Загрузка дополнительных данных из Firestore
            Firebase.firestore.collection("users").document(currentUser.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Имя пользователя
                        val name = document.getString("name") ?:
                        currentUser.displayName ?: getString(R.string.default_name)
                        binding.tvProfileName.text = name

                        // Локация
                        val location = document.getString("location") ?:
                        getString(R.string.location_not_set)
                        binding.tvProfileLocation.text = location

                        // Если локация пустая, скрываем TextView
                        if (location == getString(R.string.location_not_set)) {
                            binding.tvProfileLocation.visibility = View.GONE
                        }
                    }
                }
                .addOnFailureListener { e ->
                    binding.tvProfileName.text = currentUser.displayName ?: getString(R.string.default_name)
                    binding.tvProfileLocation.text = getString(R.string.location_not_set)
                }
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener {
            // TODO: Реализовать редактирование профиля
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}