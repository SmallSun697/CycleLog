package com.example.cyclelog.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AlarmViewModel : ViewModel() {
  private var _alarm by mutableStateOf(false)
  private var _timeLeft by mutableLongStateOf(0L)
  private var _timeUp by mutableStateOf(false)

  val alarm get() = _alarm
  val timeLeft get() = _timeLeft
  val timeUp get() = _timeUp

  fun startAlarm(hour: Int, minute: Int) {
    val time = (hour * 60 + minute) * 60 * 1000L
    _alarm = true
    _timeUp = false

    viewModelScope.launch {
      while (_alarm) {
        val triggerTime = System.currentTimeMillis() + time
        _timeLeft = time

        while (_alarm && _timeLeft > 0) {
          delay(250)
          _timeLeft = if (_timeLeft > 0) triggerTime - System.currentTimeMillis() else 0
        }
        if (_alarm) _timeUp = true
      }
    }
  }

  fun resetTimeUp() {
    _timeUp = false
  }

  fun clearAlarm() {
    _alarm = false
    _timeLeft = 0L
    _timeUp = false
  }
}