package com.example.fooddelivery.presentation.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddelivery.presentation.ui.DetailsActivity
import com.example.fooddelivery.databinding.HomeFoodItemBinding
import com.example.fooddelivery.data.models.PopularModel
import com.example.fooddelivery.domain.repository.CartRepository

class PopularAdapter(
    private val context: Context,
    private val list: List<PopularModel>
) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {

    inner class PopularViewHolder(private val binding: HomeFoodItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PopularModel) {
            with(binding) {
                imageInContainerFoodItem.setImageResource(item.foodImage)
                tvFoodItemName.text = item.foodName
                tvFoodItemPrice.text = "$${"%.2f".format(item.foodPrice)}"

                tvBtnFoodItem.setOnClickListener {
                    CartRepository.addToCart(item)
                    Toast.makeText(context, "${item.foodName} добавлен в корзину", Toast.LENGTH_SHORT).show()
                }

                root.setOnClickListener {
                    val intent = Intent(context, DetailsActivity::class.java).apply {
                        putExtra("foodImage", item.foodImage)
                        putExtra("foodName", item.foodName)
                        putExtra("foodPrice", item.foodPrice)
                        putExtra("foodDescription", item.foodDescription)
                        putExtra("foodIngredients", item.foodIngredients)
                    }
                    context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularViewHolder {
        val binding = HomeFoodItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PopularViewHolder(binding)
    }

    override fun getItemCount(): Int = list.size

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        holder.bind(list[position])
    }
}