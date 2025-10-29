package com.example.fooddelivery.presentation.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.fooddelivery.data.repository.BannerRepository
import com.example.fooddelivery.data.repository.FoodRepository
import com.example.fooddelivery.R
import com.example.fooddelivery.presentation.adapters.ImageSliderAdapter
import com.example.fooddelivery.presentation.adapters.PopularAdapter
import com.example.fooddelivery.data.model.PopularModel


class HomeFragment : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var adapter: ImageSliderAdapter
    private lateinit var imagesList: ArrayList<Int>
    private lateinit var handler: Handler

    private lateinit var popularAdapter: PopularAdapter
    private lateinit var popularList: ArrayList<PopularModel>
    private lateinit var popularRecyclerView: RecyclerView
    private lateinit var goMenu: Button

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
        goMenu = view.findViewById(R.id.btn_go_menu)

        //Popular List
        popularList = FoodRepository.popularMenu as ArrayList<PopularModel>

        popularAdapter = PopularAdapter(requireContext(), popularList)
        popularRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        popularRecyclerView.adapter = popularAdapter

        //Go Menu
        goMenu.setOnClickListener {
           val bottomSheetMenu = BottomMenuFragment()
            bottomSheetMenu.show(parentFragmentManager, "bottomSheetMenu")
        }

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
        val current = viewPager2.currentItem
        viewPager2.setCurrentItem(current + 1, true)
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
        imagesList = BannerRepository.bannerList as ArrayList<Int>

        // Устанавливаем начальную позицию в середину "бесконечного" списка
        val initialPosition = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % imagesList.size)

        adapter = ImageSliderAdapter(requireContext(), imagesList, viewPager2)
        viewPager2.adapter = adapter
        viewPager2.setCurrentItem(initialPosition, false)
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