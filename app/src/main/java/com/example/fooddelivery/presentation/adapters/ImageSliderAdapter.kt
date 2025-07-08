package com.example.fooddelivery.presentation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.fooddelivery.R


class ImageSliderAdapter(
    private val context: Context,
    private val imageList: ArrayList<Int>,
    private val viewPager: ViewPager2
) : RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder>() {

    override fun getItemCount(): Int = Int.MAX_VALUE // Бесконечное количество элементов

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSliderViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.image_container, parent, false)
        return ImageSliderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageSliderViewHolder, position: Int) {
        val actualPosition = position % imageList.size
        holder.imageView.setImageResource(imageList[actualPosition])
    }

    class ImageSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.image_in_container)
    }
}




