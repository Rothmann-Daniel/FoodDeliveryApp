package com.example.fooddelivery.presentation.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddelivery.R
import com.example.fooddelivery.presentation.activity.details.DetailsActivity
import com.example.fooddelivery.databinding.HomeFoodItemBinding
import com.example.fooddelivery.data.model.PopularModel
import com.example.fooddelivery.domain.utils.toPriceString
import com.example.fooddelivery.presentation.fragments.cart.CartViewModel
import org.koin.java.KoinJavaComponent.inject

class PopularAdapter(
    private val context: Context,
    private val list: List<PopularModel>
) : RecyclerView.Adapter<PopularAdapter.PopularViewHolder>() {

    private val cartViewModel: CartViewModel by inject(CartViewModel::class.java)

    inner class PopularViewHolder(private val binding: HomeFoodItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PopularModel) {
            with(binding) {
                imageInContainerFoodItem.setImageResource(item.foodImage)
                tvFoodItemName.text = item.foodName
                tvFoodItemPrice.text = item.foodPrice.toPriceString()

                tvBtnFoodItem.setOnClickListener {
                    cartViewModel.addToCart(item)
                    Toast.makeText(
                        context,
                        context.getString(R.string.item_added_to_cart, item.foodName),
                        Toast.LENGTH_SHORT
                    ).show()
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