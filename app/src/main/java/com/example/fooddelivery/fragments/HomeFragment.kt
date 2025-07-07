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
import com.example.fooddelivery.adapters.PopularAdapter
import com.example.fooddelivery.models.PopularModel


class HomeFragment : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: ImageSliderAdapter
    private lateinit var imagesList: ArrayList<Int>
    private lateinit var handler: Handler

    private lateinit var popularAdapter: PopularAdapter
    private lateinit var popularList: ArrayList<PopularModel>
    private lateinit var popularRecyclerView: RecyclerView

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
        popularRecyclerView = view.findViewById(R.id.rv_home_FH)

        //Popular List
        popularList = ArrayList()
        popularList.add(PopularModel(R.drawable.im_burger, "Burger","$10"))
        popularList.add(PopularModel(R.drawable.im_pizza, "Pizza","$15"))
        popularList.add(PopularModel(R.drawable.im_pasta, "Pasta","$20"))
        popularList.add(PopularModel(R.drawable.im_french_fries, "French Fries","$5,50"))
        popularList.add(PopularModel(R.drawable.im_sandwich, "Sandwich","$7"))
        popularList.add(PopularModel(R.drawable.im_combo, "Combo","$30"))
        popularList.add(PopularModel(R.drawable.im_hincaly, "Kavkazi Hincaly","$14"))
        popularList.add(PopularModel(R.drawable.im_chicken_wings_bbq, "Chicken Wings", "$12"))
        popularList.add(PopularModel(R.drawable.im_sushi, "Sushi","$25"))
        popularList.add(PopularModel(R.drawable.im_salad, "Salad","$8"))
        popularList.add(PopularModel(R.drawable.im_desert, "Сake","$9"))

        popularAdapter = PopularAdapter(requireContext(), popularList)
        popularRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        popularRecyclerView.adapter = popularAdapter

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
    // Image Slider
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