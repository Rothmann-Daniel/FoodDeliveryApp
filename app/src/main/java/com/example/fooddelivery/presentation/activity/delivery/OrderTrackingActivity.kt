package com.example.fooddelivery.presentation.activity.delivery

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.fooddelivery.databinding.ActivityOrderTrackingBinding
import com.example.fooddelivery.presentation.adapters.TrackingTimelineAdapter
import com.example.fooddelivery.presentation.fragments.history.OrderTrackingViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class OrderTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderTrackingBinding
    private val viewModel: OrderTrackingViewModel by viewModel()
    private lateinit var timelineAdapter: TrackingTimelineAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val orderId = intent.getStringExtra("ORDER_ID") ?: run {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        viewModel.observeOrder(orderId)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        timelineAdapter = TrackingTimelineAdapter()
        binding.rvTrackingTimeline.apply {
            layoutManager = LinearLayoutManager(this@OrderTrackingActivity)
            adapter = timelineAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.order.collect { order ->
                order?.let { displayOrderInfo(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.trackingSteps.collect { steps ->
                timelineAdapter.submitList(steps)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBarTracking.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(this@OrderTrackingActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun displayOrderInfo(order: com.example.fooddelivery.data.model.Order) {
        with(binding) {
            tvTrackingOrderId.text = "Order #${order.orderId.takeLast(6)}"
            tvTrackingStatus.text = getStatusText(order.getStatusEnum())

            val estimatedTime = order.getEstimatedDeliveryFormatted()
            tvEstimatedTime.text = if (estimatedTime.isNotEmpty()) {
                "Estimated delivery: $estimatedTime"
            } else {
                "Processing your order..."
            }

            // Courier info
            if (order.courierName.isNotEmpty() && order.courierPhone.isNotEmpty()) {
                cardCourierInfo.visibility = View.VISIBLE
                tvCourierName.text = order.courierName
                tvCourierPhone.text = order.courierPhone
            } else {
                cardCourierInfo.visibility = View.GONE
            }

            // Cancel button visibility
            btnCancelOrder.visibility = if (viewModel.canCancelOrder()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnCallCourier.setOnClickListener {
            val phone = binding.tvCourierPhone.text.toString()
            if (phone.isNotEmpty()) {
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$phone")
                }
                startActivity(intent)
            }
        }

        binding.btnCancelOrder.setOnClickListener {
            showCancelConfirmationDialog()
        }
    }

    private fun showCancelConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Cancel Order")
            .setMessage("Are you sure you want to cancel this order?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.cancelOrder()
                Toast.makeText(this, "Order cancelled", Toast.LENGTH_SHORT).show()
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun getStatusText(status: com.example.fooddelivery.data.model.OrderStatus): String {
        return when (status) {
            com.example.fooddelivery.data.model.OrderStatus.PENDING -> "Order Placed"
            com.example.fooddelivery.data.model.OrderStatus.CONFIRMED -> "Order Confirmed"
            com.example.fooddelivery.data.model.OrderStatus.PREPARING -> "Preparing Your Food"
            com.example.fooddelivery.data.model.OrderStatus.READY -> "Ready for Pickup"
            com.example.fooddelivery.data.model.OrderStatus.ON_THE_WAY -> "On the Way"
            com.example.fooddelivery.data.model.OrderStatus.DELIVERED -> "Delivered"
            com.example.fooddelivery.data.model.OrderStatus.CANCELLED -> "Cancelled"
        }
    }
}