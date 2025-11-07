package com.example.fooddelivery.presentation.activity.init

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.ActivityMainBinding
import com.example.fooddelivery.presentation.activity.registration.LoginUserActivity
import com.example.fooddelivery.service.FCMTokenManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = Firebase.auth

        if (auth.currentUser == null) {
            startActivity(Intent(this, LoginUserActivity::class.java))
            finish()
            return
        }

        // Конфигурация Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Из google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val bottomNavigationView = binding.bottomNavigationView
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragment_container) as NavHostFragment
        val navController = navHostFragment.navController

        bottomNavigationView.setupWithNavController(navController)
        // Получаем FCM токен
        lifecycleScope.launch {
            val token = FCMTokenManager.getToken(this@MainActivity)
            Log.d("FCM", "Token: $token")

            // Сохраняем токен в Firestore для текущего пользователя
            token?.let { saveUserFcmToken(it) }
        }
    }

    private suspend fun saveUserFcmToken(token: String) {
        val user = Firebase.auth.currentUser ?: return
        try {
            Firebase.firestore.collection("users")
                .document(user.uid)
                .update("fcmToken", token)
                .await()
        } catch (e: Exception) {
            Log.e("FCM", "Error saving token", e)
        }
    }
}

