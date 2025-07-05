package com.example.fooddelivery

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.fooddelivery.databinding.ActivityMainBinding
import com.example.fooddelivery.fragments.CartFragment
import com.example.fooddelivery.fragments.HistoryFragment
import com.example.fooddelivery.fragments.HomeFragment
import com.example.fooddelivery.fragments.ProfileFragment
import com.example.fooddelivery.fragments.SearchFragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
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

        changeFragment(HomeFragment())

        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.bottom_menu_home-> {
                   changeFragment(HomeFragment())
                    true
                }
                R.id.shopping_cart-> {
                    changeFragment(CartFragment())
                    true
                }
                R.id.search-> {
                    changeFragment(SearchFragment())
                    true
                }
                R.id.history-> {
                    changeFragment(HistoryFragment())
                    true
                }
                R.id.profile-> {
                    changeFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }

    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }
}