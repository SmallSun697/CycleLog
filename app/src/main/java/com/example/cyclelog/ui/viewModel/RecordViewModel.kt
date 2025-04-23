package com.example.cyclelog.ui.viewModel

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cyclelog.tracking.SensorSpeedEstimator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Integer.min

@SuppressLint("DefaultLocale")
class RecordViewModel(
  context: Context,
  dataStore: DataStore<Preferences>
) : ViewModel() {
//  private val speedEstimator = SensorSpeedEstimator(context, dataStore)
  private var currentToneLevel = 0
  private val tone = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
  private var toneJob: Job? = null
//  private var _speedMps by mutableFloatStateOf(0f)
  private var _alarmDialogVisible by mutableStateOf(false)

//  val speedMps get() = _speedMps
//  val speedKph get() = _speedMps * 3.6f
  val alarmDialogVisible get() = _alarmDialogVisible

//  init {
//    speedEstimator.startEstimator()
//    viewModelScope.launch {
//      speedEstimator.speed.collect {
//        _speedMps = it
//      }
//    }
//  }

  fun updateDistance(left: Int, right: Int) {
    val level = when (min(left, right)) {
      in 0..30 -> 3
      in 31..60 -> 2
      in 61..90 -> 1
      else -> 0
    }
    if (level != currentToneLevel) {
      currentToneLevel = level
      startTone(level)
    }
  }

  private fun startTone(level: Int) {
    toneJob?.cancel()
    toneJob = viewModelScope.launch {
      while (true) {
        tone.stopTone()
        when (level) {
          3 -> {
            tone.startTone(ToneGenerator.TONE_SUP_ERROR, 330)
            delay(300)
          }

          2 -> {
            tone.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
            delay(350)
            tone.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
            delay(2500)
          }

          1 -> {
            tone.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
            delay(2500)
          }

          else -> {
            return@launch
          }
        }
      }
    }
  }

  fun stopTone() {
    toneJob?.cancel()
    tone.stopTone()
  }

  fun dialogShow() {
    _alarmDialogVisible = true
  }

  fun dialogClose() {
    _alarmDialogVisible = false
  }
}

@Suppress("UNCHECKED_CAST")
class RecordViewModelFactory(
  private val context: Context,
  private val dataStore: DataStore<Preferences>
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(RecordViewModel::class.java)) {
      return RecordViewModel(context, dataStore) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}