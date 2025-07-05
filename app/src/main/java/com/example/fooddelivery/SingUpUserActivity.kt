package com.example.fooddelivery

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fooddelivery.databinding.ActivitySingUpUserBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SingUpUserActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySingUpUserBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySingUpUserBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация Firebase Auth Email
        auth = Firebase.auth

        binding.btnCreateAccSingUp.setOnClickListener {
            val email = binding.edSingUpEmailSingUp.text.toString().trim()
            val password = binding.edPasswordSingUp.text.toString().trim()
            val name = binding.edUserNameSingUp.text.toString().trim()

            if (validateInputs(email, password, name)) {
                createUserWithEmailAndPassword(email, password, name)
            }
        }

        binding.tvQuestionSingUp.setOnClickListener {
            val intent = Intent(this, LoginUserActivity::class.java)
            startActivity(intent)
        }

        binding.tvSupportSingUp.setOnClickListener {
            EmailUtils.sendSupportEmail(this)
        }

        // Инициализация Google Sign-Up (аналогично Подключение Google Sign-In)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.btnGoogleSingUp.setOnClickListener {
            signInWithGoogle()
        }

        binding.btnFacebookSingUp.setOnClickListener {
            Toast.makeText(this, "Sorry, we're still working on this service. Use another registration method.", Toast.LENGTH_LONG).show()
        }
    }


    // Проверка введенных данных
    private fun validateInputs(email: String, password: String, name: String): Boolean {
        if (name.isEmpty()) {
            binding.edUserNameSingUp.error = "Please enter your name"
            return false
        }

        if (email.isEmpty()) {
            binding.edSingUpEmailSingUp.error = "Please enter email"
            return false
        }

        if (password.isEmpty() || password.length < 6) {
            binding.edPasswordSingUp.error = "Password must be at least 6 characters"
            return false
        }

        return true
    }

    // Регистрация пользователя
    private fun createUserWithEmailAndPassword(email: String, password: String, name: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        saveUserDataToFirestore(it.uid, name, email) { success ->
                            if (success) {
                                startActivity(Intent(this, LocationActivity::class.java))
                                finish()
                            } else {
                                Toast.makeText(
                                    this,
                                    "User created but data not saved",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Registration failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("Auth", "Registration error", task.exception)
                }
            }
    }

    // Сохранение данных пользователя в Firestore
    private fun saveUserDataToFirestore(
        uid: String,
        name: String,
        email: String,
        callback: (Boolean) -> Unit
    ) {
        val db = Firebase.firestore
        val user = hashMapOf(
            "name" to name,
            "email" to email,
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(uid)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "Document saved for $uid")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error saving user $uid", e)
                callback(false)
            }
    }

    // Подключение Google Sign-In
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    // Обработка результата Google Sign-In
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Log.w("GoogleSignIn", "Google sign in failed", e)
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    // Аутентификация с помощью Google
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Успешный вход
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }
}


