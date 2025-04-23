package com.example.cyclelog.utils

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
fun formatDistance(distance: Double): String {
  return when {
    distance >= 1000 -> String.format("%.2f km", distance / 1000)
    else -> String.format("%d m", distance.toInt())
  }
}