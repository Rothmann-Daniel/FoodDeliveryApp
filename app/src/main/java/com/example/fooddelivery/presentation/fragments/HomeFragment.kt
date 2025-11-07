package com.example.fooddelivery.presentation.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.example.fooddelivery.R
import com.example.fooddelivery.databinding.FragmentHomeBinding
import com.example.fooddelivery.presentation.adapters.ImageSliderAdapter
import com.example.fooddelivery.presentation.adapters.PopularAdapter
import com.example.fooddelivery.presentation.ui.HomeViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModel()

    private lateinit var imageSliderAdapter: ImageSliderAdapter
    private lateinit var popularAdapter: PopularAdapter
    private lateinit var handler: Handler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        handler = Handler(Looper.myLooper()!!)

        setupObservers()
        setupImageSlider()
        setupGoMenuButton()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.bannerList.collect { banners ->
                if (banners.isNotEmpty()) {
                    initImageSlider(ArrayList(banners))
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.popularMenu.collect { menu ->
                if (menu.isNotEmpty()) {
                    setupPopularMenu(menu)
                }
            }
        }
    }

    private fun setupPopularMenu(menu: List<com.example.fooddelivery.data.model.PopularModel>) {
        popularAdapter = PopularAdapter(requireContext(), menu)
        binding.rvHomeFH.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = popularAdapter
        }
    }

    private fun setupImageSlider() {
        binding.imageSlider.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                handler.removeCallbacks(runnable)
                handler.postDelayed(runnable, 3000)
            }
        })
    }

    private fun initImageSlider(images: ArrayList<Int>) {
        val initialPosition = Int.MAX_VALUE / 2 - (Int.MAX_VALUE / 2 % images.size)

        imageSliderAdapter = ImageSliderAdapter(requireContext(), images, binding.imageSlider)
        binding.imageSlider.apply {
            adapter = imageSliderAdapter
            setCurrentItem(initialPosition, false)
            offscreenPageLimit = 3
            clipToPadding = false
            clipChildren = false
            getChildAt(0).overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        setTransformer()
    }

    private fun setTransformer() {
        val transformer = CompositePageTransformer()
        transformer.addTransformer(MarginPageTransformer(10))
        transformer.addTransformer { page, position ->
            val r = 1 - Math.abs(position)
            page.scaleY = 0.85f + r * 0.15f
        }
        binding.imageSlider.setPageTransformer(transformer)
    }

    private fun setupGoMenuButton() {
        binding.btnGoMenu.setOnClickListener {
            val bottomSheetMenu = BottomMenuFragment()
            bottomSheetMenu.show(parentFragmentManager, "bottomSheetMenu")
        }
    }

    private val runnable = Runnable {
        val current = binding.imageSlider.currentItem
        binding.imageSlider.setCurrentItem(current + 1, true)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable)
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed(runnable, 3000)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacks(runnable)
        _binding = null
    }
}