package com.example.fooddelivery.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddelivery.databinding.CartAddItemBinding
import com.example.fooddelivery.domain.model.CartItem
import com.example.fooddelivery.domain.repository.CartRepository

class CartAdapter(
    private val context: Context
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartItemDiffCallback()) {

    inner class CartViewHolder(private val binding: CartAddItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            with(binding) {
                imageFoodItemCart.setImageResource(item.foodItem.foodImage)
                tvFoodItemNameCart.text = item.foodItem.foodName
                tvFoodItemPriceCart.text = "$${"%.2f".format(item.foodItem.foodPrice)}"
                tvCountCart.text = item.quantity.toString()

                imBtnPlusCart.setOnClickListener {
                    val currentItem = getItem(adapterPosition)
                    CartRepository.updateQuantity(currentItem, currentItem.quantity + 1)
                }

                imBtnMinusCart.setOnClickListener {
                    val currentItem = getItem(adapterPosition)
                    CartRepository.updateQuantity(currentItem, currentItem.quantity - 1)
                }

                imBtnTrashCart.setOnClickListener {
                    CartRepository.removeFromCart(item)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartAddItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class CartItemDiffCallback : DiffUtil.ItemCallback<CartItem>() {
    override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem.foodItem.foodName == newItem.foodItem.foodName
    }

    override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
        return oldItem == newItem
    }
}
