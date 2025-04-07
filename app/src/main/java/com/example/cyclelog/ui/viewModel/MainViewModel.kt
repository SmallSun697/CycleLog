package com.example.cyclelog.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.cyclelog.ui.Destination

class MainViewModel : ViewModel() {
  var currentDestination by mutableStateOf(Destination.RECORD)
}