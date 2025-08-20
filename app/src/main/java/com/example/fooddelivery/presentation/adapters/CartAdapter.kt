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
import com.example.fooddelivery.domain.utils.toPriceString
import org.koin.java.KoinJavaComponent.inject

class CartAdapter(
    private val context: Context
) : ListAdapter<CartItem, CartAdapter.CartViewHolder>(CartItemDiffCallback()) {

    // Инжектим CartRepository через Koin
    private val cartRepository: CartRepository by inject(CartRepository::class.java)

    inner class CartViewHolder(private val binding: CartAddItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CartItem) {
            with(binding) {
                imageFoodItemCart.setImageResource(item.foodItem.foodImage)
                tvFoodItemNameCart.text = item.foodItem.foodName
                tvFoodItemPriceCart.text = item.foodItem.foodPrice.toPriceString()
                tvCountCart.text = item.quantity.toString()

                imBtnPlusCart.setOnClickListener {
                    val currentItem = getItem(bindingAdapterPosition)
                    cartRepository.updateQuantity(currentItem, currentItem.quantity + 1)
                }

                imBtnMinusCart.setOnClickListener {
                    val currentItem = getItem(bindingAdapterPosition)
                    if (currentItem.quantity > 1) {
                        cartRepository.updateQuantity(currentItem, currentItem.quantity - 1)
                    } else {
                        cartRepository.removeFromCart(currentItem)
                    }
                }

                imBtnTrashCart.setOnClickListener {
                    cartRepository.removeFromCart(item)
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