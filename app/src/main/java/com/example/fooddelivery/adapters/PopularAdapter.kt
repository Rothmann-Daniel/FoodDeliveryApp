package com.example.fooddelivery.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        holder.foodImage.setImageResource(item.getFoodImage()!!)
        holder.foodName.text = item.getFoodName()
        holder.foodPrice.text = item.getFoodPrice()
    }

    class PopularViewHolder(binding: HomeFoodItemBinding): RecyclerView.ViewHolder(binding.root) {

        val foodImage = binding.imageInContainerFoodItem
        val foodName = binding.tvFoodItemName
        val foodPrice = binding.tvFoodItemPrice

    }

}
