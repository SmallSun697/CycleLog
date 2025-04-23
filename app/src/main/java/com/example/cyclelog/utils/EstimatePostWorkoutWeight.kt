package com.example.cyclelog.utils

fun estimateWeight(
  weight: Float,
  duration: Long,
  distance: Double,
  calories: Int
): Float {
  val speedKmPerHr = (distance / 1000.0) / (duration / 60.0)
  val sweatLossPerMin = when {
    speedKmPerHr < 12 -> 7
    speedKmPerHr < 20 -> 12
    else -> 20
  }
  val sweatLossKg = (sweatLossPerMin * duration) / 1000f
  val fatLossKg = calories / 7700f
  val resultWeight = weight - sweatLossKg - fatLossKg

  return maxOf(resultWeight, 0f)
}