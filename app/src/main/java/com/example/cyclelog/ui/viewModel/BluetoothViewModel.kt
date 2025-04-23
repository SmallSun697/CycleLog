package com.example.cyclelog.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class BluetoothViewModel : ViewModel() {
  var left by mutableIntStateOf(-1)
  var right by mutableIntStateOf(-1)
}