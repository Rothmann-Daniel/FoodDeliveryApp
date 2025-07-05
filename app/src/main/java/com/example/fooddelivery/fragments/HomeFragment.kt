package com.example.fooddelivery.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.fooddelivery.R
import com.example.fooddelivery.adapters.ImageSliderAdapter


class HomeFragment : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: ImageSliderAdapter
    private lateinit var imagesList: ArrayList<Int>
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        viewPager2 = view.findViewById(R.id.imageSlider)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
        setTransformer()
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 3000)
            }
        })
    }

    private val runnable = Runnable {
        viewPager2.currentItem = viewPager2.currentItem + 1
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 3000)

    }

    private fun init() {
        imagesList = ArrayList()
        imagesList.add(R.drawable.banner_1)
        imagesList.add(R.drawable.banner_2)
        imagesList.add(R.drawable.banner_3)
        imagesList.add(R.drawable.banner_4)
        imagesList.add(R.drawable.banner_5)
        imagesList.add(R.drawable.banner_6)
        imagesList.add(R.drawable.banner_7)
        imagesList.add(R.drawable.banner_8)
        imagesList.add(R.drawable.banner_9)
        imagesList.add(R.drawable.banner_10)

        adapter = ImageSliderAdapter(requireContext(), imagesList, viewPager2)
        viewPager2.adapter = adapter
        handler = Handler(Looper.myLooper()!!)
        viewPager2.offscreenPageLimit = 3
        viewPager2.clipToPadding = false
        viewPager2.clipChildren = false
        viewPager2.getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER


    }

    private fun setTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(10))
        transformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r*0.15f
    }
        viewPager2.setPageTransformer(transformer)
    }




}