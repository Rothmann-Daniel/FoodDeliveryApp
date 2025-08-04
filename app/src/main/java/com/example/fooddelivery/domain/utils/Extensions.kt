package com.example.fooddelivery.domain.utils

fun Double.toPriceString(): String = "$${"%.2f".format(this)}"