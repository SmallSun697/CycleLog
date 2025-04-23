package com.example.cyclelog.utils

import android.annotation.SuppressLint

@SuppressLint("DefaultLocale")
fun formatDuration(seconds: Long): String {
  val days = seconds / 86400
  val hours = (seconds % 86400) / 3600
  val minutes = (seconds % 3600) / 60
  val remainingSeconds = seconds % 60

  return when {
    days > 0 -> String.format("%d:%02d:%02d:%02d", days, hours, minutes, remainingSeconds)
    hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
    else -> String.format("%02d:%02d", minutes, remainingSeconds)
  }
}