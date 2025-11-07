package com.example.fooddelivery.presentation.adapters

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddelivery.R
import com.example.fooddelivery.data.model.Order
import com.example.fooddelivery.data.model.OrderStatus
import com.example.fooddelivery.databinding.ItemOrderBinding
import com.example.fooddelivery.presentation.activity.delivery.OrderTrackingActivity
import java.text.NumberFormat
import java.util.Currency

class OrderAdapter(
    private val context: Context,
    private val isActiveOrders: Boolean = false
) : ListAdapter<Order, OrderAdapter.OrderViewHolder>(OrderDiffCallback()) {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            with(binding) {
                // Order ID
                tvOrderId.text = "Order #${order.orderId.takeLast(6)}"

                // Date
                tvOrderDate.text = order.getFormattedDate()

                // Status
                val status = order.getStatusEnum()
                tvOrderStatus.text = getStatusText(status)
                tvOrderStatus.setBackgroundResource(getStatusBackground(status))

                // Items count
                val itemsCount = order.items.sumOf { it.quantity }
                tvItemsCount.text = "$itemsCount items"

                // Total amount
                val format = NumberFormat.getCurrencyInstance().apply {
                    currency = Currency.getInstance("USD")
                    maximumFractionDigits = 2
                }
                tvOrderTotal.text = format.format(order.totalAmount)

                // Delivery address
                tvDeliveryAddress.text = order.deliveryAddress

                // Track button visibility
                btnTrackOrder.visibility = if (isActiveOrders) View.VISIBLE else View.GONE

                // Click listeners
                root.setOnClickListener {
                    openTrackingScreen(order.orderId)
                }

                btnTrackOrder.setOnClickListener {
                    openTrackingScreen(order.orderId)
                }
            }
        }

        private fun openTrackingScreen(orderId: String) {
            val intent = Intent(context, OrderTrackingActivity::class.java).apply {
                putExtra("ORDER_ID", orderId)
            }
            context.startActivity(intent)
        }

        private fun getStatusText(status: OrderStatus): String {
            return when (status) {
                OrderStatus.PENDING -> "Pending"
                OrderStatus.CONFIRMED -> "Confirmed"
                OrderStatus.PREPARING -> "Preparing"
                OrderStatus.READY -> "Ready"
                OrderStatus.ON_THE_WAY -> "On the Way"
                OrderStatus.DELIVERED -> "Delivered"
                OrderStatus.CANCELLED -> "Cancelled"
            }
        }

        private fun getStatusBackground(status: OrderStatus): Int {
            return when (status) {
                OrderStatus.PENDING -> R.drawable.badge_pending
                OrderStatus.CONFIRMED -> R.drawable.badge_confirmed
                OrderStatus.PREPARING -> R.drawable.badge_preparing
                OrderStatus.READY, OrderStatus.ON_THE_WAY -> R.drawable.badge_on_the_way
                OrderStatus.DELIVERED -> R.drawable.badge_delivered
                OrderStatus.CANCELLED -> R.drawable.badge_cancelled
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class OrderDiffCallback : DiffUtil.ItemCallback<Order>() {
    override fun areItemsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem.orderId == newItem.orderId
    }

    override fun areContentsTheSame(oldItem: Order, newItem: Order): Boolean {
        return oldItem == newItem
    }
}