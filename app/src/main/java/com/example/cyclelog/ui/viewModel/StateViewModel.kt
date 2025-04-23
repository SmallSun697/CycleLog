package com.example.cyclelog.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cyclelog.ui.Destination
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class StateViewModel : ViewModel() {
  private var _isRecording by mutableStateOf(false)
  private var _startTime: LocalDateTime = LocalDateTime.now()
  private var _recordedTime by mutableLongStateOf(0L)

  val isRecording get() = _isRecording
  val startTime get() = _startTime
  val recordedTime get() = _recordedTime
  var currentDestination by mutableStateOf(Destination.RECORD)

  fun setRecording(value: Boolean) {
    _isRecording = value
    if (value) startRecording()
  }

  private fun startRecording() {
    _startTime = LocalDateTime.now()
    _recordedTime = 0L

    viewModelScope.launch {
      while (_isRecording) {
        delay(100)
        _startTime.let { start ->
          val currentTime = LocalDateTime.now()
          val durationInSeconds = ChronoUnit.SECONDS.between(start, currentTime)
          _recordedTime = durationInSeconds
        }
      }
    }
  }
}