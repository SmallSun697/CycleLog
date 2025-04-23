package com.example.cyclelog.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun isScreenTall(): Boolean {
  return LocalConfiguration.current.screenHeightDp > 480
}