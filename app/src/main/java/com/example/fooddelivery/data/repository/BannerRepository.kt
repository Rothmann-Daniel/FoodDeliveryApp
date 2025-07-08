package com.example.fooddelivery.data.repository

import com.example.fooddelivery.R

object BannerRepository {
    val bannerList = mutableListOf<Int>()
    init {
        bannerList.addAll(listOf(
            R.drawable.banner_1,
            R.drawable.banner_2,
            R.drawable.banner_3,
            R.drawable.banner_4,
            R.drawable.banner_5,
            R.drawable.banner_6,
            R.drawable.banner_7,
            R.drawable.banner_8,
            R.drawable.banner_9,
            R.drawable.banner_10
        ))
    }
}