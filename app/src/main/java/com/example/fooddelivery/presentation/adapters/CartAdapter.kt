package com.example.fooddelivery.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddelivery.databinding.CartAddItemBinding
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.repository.CartRepository

class CartAdapter(
    val context: Context,
    private val onItemRemoved: (position: Int) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    private val items = mutableListOf<CartItem>()

    fun updateItems(newItems: List<CartItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartAddItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
    }

    inner class CartViewHolder(binding: CartAddItemBinding) : RecyclerView.ViewHolder(binding.root) {
        private val foodImage = binding.imageFoodItemCart
        private val foodName = binding.tvFoodItemNameCart
        private val foodPrice = binding.tvFoodItemPriceCart
        private val plus = binding.imBtnPlusCart
        private val minus = binding.imBtnMinusCart
        private val count = binding.tvCountCart
        private val trash = binding.imBtnTrashCart

        fun bind(item: CartItem) {
            foodImage.setImageResource(item.foodItem.foodImage)
            foodName.text = item.foodItem.foodName
            foodPrice.text = item.foodItem.foodPrice
            count.text = item.quantity.toString()

            plus.setOnClickListener {
                item.quantity++
                count.text = item.quantity.toString()
                CartRepository.updateQuantity(item, item.quantity)
            }

            minus.setOnClickListener {
                if (item.quantity > 1) {
                    item.quantity--
                    count.text = item.quantity.toString()
                    CartRepository.updateQuantity(item, item.quantity)
                }
            }

            trash.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    CartRepository.removeFromCart(item)
                    items.removeAt(position)
                    notifyItemRemoved(position)
                    onItemRemoved(position)
                }
            }
        }
    }
}