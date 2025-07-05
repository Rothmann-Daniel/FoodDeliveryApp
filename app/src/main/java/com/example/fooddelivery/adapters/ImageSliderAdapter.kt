package com.example.fooddelivery.adapters

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
    private val imagesList: List<Int>,
    private val viewPager2: ViewPager2
): RecyclerView.Adapter<ImageSliderAdapter.ImageSliderViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageSliderViewHolder {
       val view = LayoutInflater.from(context).inflate(R.layout.image_container, parent, false)
        return ImageSliderViewHolder(view)
    }

    override fun getItemCount(): Int {
       return imagesList.size
    }

    override fun onBindViewHolder(holder: ImageSliderViewHolder, position: Int) {
       holder.imageView.setImageResource(imagesList[position])

        if (position == imagesList.size - 1) {
            viewPager2.post(Runnable { viewPager2.setCurrentItem(0, false) })
        }
    }

    private val runnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    class ImageSliderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView = itemView.findViewById<ImageView>(R.id.image_in_container)
    }
}