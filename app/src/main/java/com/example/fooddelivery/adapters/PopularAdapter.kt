package com.example.fooddelivery.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddelivery.DetailsActivity
import com.example.fooddelivery.databinding.HomeFoodItemBinding
import com.example.fooddelivery.models.PopularModel

class PopularAdapter(
    val context: Context,
    val list: ArrayList<PopularModel>): RecyclerView.Adapter<PopularAdapter.PopularViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PopularAdapter.PopularViewHolder {
        val binding = HomeFoodItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return PopularViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: PopularViewHolder, position: Int) {
        val item = list[position]
        holder.foodImage.setImageResource(item.foodImage)
        holder.foodName.text = item.foodName
        holder.foodPrice.text = item.foodPrice

        holder.itemView.setOnClickListener {
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

    class PopularViewHolder(binding: HomeFoodItemBinding): RecyclerView.ViewHolder(binding.root) {

        val foodImage = binding.imageInContainerFoodItem
        val foodName = binding.tvFoodItemName
        val foodPrice = binding.tvFoodItemPrice

    }

}
