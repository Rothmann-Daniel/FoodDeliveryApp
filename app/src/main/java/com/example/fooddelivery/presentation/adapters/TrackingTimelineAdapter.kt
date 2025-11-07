package com.example.fooddelivery.presentation.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.ItemTimelineStepBinding
import com.example.fooddelivery.presentation.fragments.history.TrackingStep

class TrackingTimelineAdapter :
    ListAdapter<TrackingStep, TrackingTimelineAdapter.TimelineViewHolder>(TimelineDiffCallback()) {

    inner class TimelineViewHolder(private val binding: ItemTimelineStepBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(step: TrackingStep, isFirst: Boolean, isLast: Boolean) {
            with(binding) {
                tvStepTitle.text = step.title
                tvStepTime.text = step.time

                // Visibility of lines
                lineTop.visibility = if (isFirst) View.INVISIBLE else View.VISIBLE
                lineBottom.visibility = if (isLast) View.INVISIBLE else View.VISIBLE

                // Step indicator
                if (step.isCompleted) {
                    ivStepIndicator.setImageResource(R.drawable.ic_check_circle)
                    ivStepIndicator.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.blue)
                    )
                } else {
                    ivStepIndicator.setImageResource(R.drawable.ic_pending)
                    ivStepIndicator.setColorFilter(
                        ContextCompat.getColor(itemView.context, R.color.grey)
                    )
                }

                // Text styling
                if (step.isCurrent) {
                    tvStepTitle.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.blue)
                    )
                } else {
                    tvStepTitle.setTextColor(
                        ContextCompat.getColor(itemView.context, R.color.black)
                    )
                }

                // Line colors
                val lineColor = if (step.isCompleted) {
                    ContextCompat.getColor(itemView.context, R.color.blue)
                } else {
                    ContextCompat.getColor(itemView.context, R.color.gray_light)
                }
                lineTop.setBackgroundColor(lineColor)
                lineBottom.setBackgroundColor(lineColor)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val binding = ItemTimelineStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TimelineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val isFirst = position == 0
        val isLast = position == itemCount - 1
        holder.bind(getItem(position), isFirst, isLast)
    }
}

class TimelineDiffCallback : DiffUtil.ItemCallback<TrackingStep>() {
    override fun areItemsTheSame(oldItem: TrackingStep, newItem: TrackingStep): Boolean {
        return oldItem.status == newItem.status
    }

    override fun areContentsTheSame(oldItem: TrackingStep, newItem: TrackingStep): Boolean {
        return oldItem == newItem
    }
}