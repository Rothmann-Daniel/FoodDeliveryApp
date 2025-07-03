package com.example.fooddelivery

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
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

        try {
            val locations = resources.getStringArray(R.array.locations).toList()

            val adapter = object : ArrayAdapter<String>(
                this,
                R.layout.dropdown_item,
                R.id.text1,  // Используем ID из dropdown_item.xml
                locations
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(R.id.text1)  // Используем R.id.text1
                    // Дополнительные настройки TextView
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(R.id.text1)  // Используем R.id.text1
                    // Дополнительные настройки TextView для выпадающего списка
                    return view
                }
            }

            binding.dropdownEditText.apply {
                setAdapter(adapter)
                setOnItemClickListener { _, _, position, _ ->
                    val selected = adapter.getItem(position)
                    Toast.makeText(this@LocationActivity, "Выбрано: $selected", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка инициализации: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }
}