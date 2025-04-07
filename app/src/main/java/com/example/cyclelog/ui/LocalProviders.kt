package com.example.cyclelog.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.cyclelog.ui.viewModel.MainViewModel

val LocalMainViewModel = staticCompositionLocalOf<MainViewModel> {
  error("MainViewModel not provided")
}