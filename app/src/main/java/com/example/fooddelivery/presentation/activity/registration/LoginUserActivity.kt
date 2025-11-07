package com.example.fooddelivery.presentation.activity.registration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.ActivityLoginBinding
import com.example.fooddelivery.domain.utils.EmailUtils
import com.example.fooddelivery.presentation.activity.init.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class LoginUserActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.tvSupportSingIn.setOnClickListener {
            EmailUtils.sendSupportEmail(this)
        }

        binding.tvQuestionSingIn.setOnClickListener {
            val intent = Intent(this, SingUpUserActivity::class.java)
            startActivity(intent)
        }

        binding.btnLoginSingIn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Инициализация Firebase Auth
        auth = Firebase.auth

        binding.btnLoginSingIn.setOnClickListener {
            val email = binding.edSingUpEmailSingIn.text.toString().trim()
            val password = binding.edPasswordSingIn.text.toString().trim()

            if (validateInputs(email, password)) {
                signInWithEmailAndPassword(email, password)
            }
        }

        // Инициализация Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Обработчик кнопки Google
        binding.btnGoogleSingIn.setOnClickListener {
            signInWithGoogle()
        }

    }

    private fun validateInputs(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.edSingUpEmailSingIn.error = "Please enter email"
            return false
        }

        if (password.isEmpty()) {
            binding.edPasswordSingIn.error = "Please enter password"
            return false
        }

        return true
    }

    private fun signInWithEmailAndPassword(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    checkUserExistsInFirestore(auth.currentUser?.uid ?: "") { exists ->
                        if (exists) {
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "User data not found in database",
                                Toast.LENGTH_SHORT
                            ).show()
                            auth.signOut()
                        }
                    }
                } else {
                    Toast.makeText(
                        this,
                        "Authentication failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun checkUserExistsInFirestore(uid: String, callback: (Boolean) -> Unit) {
        if (uid.isEmpty()) {
            callback(false)
            return
        }

        Firebase.firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { document ->
                callback(document.exists())
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error checking user", e)
                callback(false)
            }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

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

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        Firebase.auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        // Проверяем, есть ли пользователь в Firestore
                        checkUserExistsInFirestore(it.uid) { exists ->
                            if (!exists) {
                                // Если пользователя нет, сохраняем данные
                                saveGoogleUserData(it)
                            } else {
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveGoogleUserData(user: FirebaseUser) {
        val db = Firebase.firestore
        val userData = hashMapOf<String, Any>(  // Явно указываем типы
            "name" to (user.displayName ?: "User"),
            "email" to (user.email ?: ""),
            "createdAt" to FieldValue.serverTimestamp()
        )

        db.collection("users").document(user.uid)
            .set(userData)
            .addOnSuccessListener {
                startActivity(Intent(this, LocationActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Error saving user data", e)
            }
    }
}



