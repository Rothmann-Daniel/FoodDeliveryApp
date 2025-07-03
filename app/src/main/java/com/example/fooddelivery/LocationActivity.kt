package com.example.fooddelivery

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.fooddelivery.databinding.ActivityLocationBinding

class LocationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLocationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)


        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val location = resources.getStringArray(R.array.locations)
        val adapter = ArrayAdapter(
            this,
            R.layout.dropdown_item,
            R.id.text1,
            location
        )
        binding.dropdownEditText.setAdapter(adapter)

        binding.dropdownEditText.setOnItemClickListener { _, _, position, _ ->
            val selectedLocation = location[position] as String
            showDialogLocation(selectedLocation)
        }



    }

    fun showDialogLocation(location: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_location, null)
        val dialogBuilder = AlertDialog.Builder(this)
        val dialog = dialogBuilder.create()
        dialogView.findViewById<Button>(R.id.btn_positive_LA).setOnClickListener {
            Toast.makeText(this, "Location: $location", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            startActivityWithLocation(location)
        }

        dialogView.findViewById<Button>(R.id.btn_negative_LA).setOnClickListener {
            dialog.dismiss()
        }
        dialog.setView(dialogView)
        dialog.show()

    }

    fun startActivityWithLocation(location: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("location", location)
        startActivity(intent)
        finish()
    }

}