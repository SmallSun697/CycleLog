package com.example.cyclelog.utils

fun estimateCalories(distance: Double, weight: Float): Int {
  val distanceKm = distance / 1000.0
  val caloriesPerKm = when {
    weight < 55 -> 30
    weight < 70 -> 40
    else -> 50
  }

  return (caloriesPerKm * distanceKm).toInt()
}