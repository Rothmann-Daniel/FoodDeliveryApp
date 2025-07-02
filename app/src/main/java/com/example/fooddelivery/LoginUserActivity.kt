package com.example.fooddelivery

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fooddelivery.databinding.ActivityLoginBinding

class LoginUserActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

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

        binding.tvSupport.setOnClickListener {
            sendSupportEmail()
        }

        binding.tvQuestionLUA.setOnClickListener {
            val intent = Intent(this, SingUpUserActivity::class.java)
            startActivity(intent)
        }

    }


    private fun sendSupportEmail() {
        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_email_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_email_body))
        }

        when {
            packageManager.queryIntentActivities(emailIntent, 0).isEmpty() -> {
                Toast.makeText(this, R.string.no_email_app, Toast.LENGTH_LONG).show()
            }

            else -> try {
                startActivity(
                    Intent.createChooser(
                        emailIntent,
                        getString(R.string.choose_email_app)
                    )
                )
            } catch (e: Exception) {
                Toast.makeText(this, R.string.email_send_error, Toast.LENGTH_LONG).show()
            }
        }
    }
}