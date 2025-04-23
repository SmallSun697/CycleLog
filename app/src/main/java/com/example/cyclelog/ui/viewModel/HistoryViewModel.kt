package com.example.cyclelog.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cyclelog.data.CycleHistory
import com.example.cyclelog.data.CycleHistoryDao
import com.mapbox.geojson.Point
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class HistoryViewModel(
  private val cycleHistoryDao: CycleHistoryDao
) : ViewModel() {
  private var _weightDialogVisible by mutableStateOf(false)
  private val _cycleHistoryList = cycleHistoryDao
    .getAllCycleHistoryFlow()
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList()
    )
  private val _weight = cycleHistoryDao
    .getLatestWeightFlow()
    .map { it ?: 0f }
    .stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = 0f
    )

  val weightDialogVisible get() = _weightDialogVisible
  val weight get() = _weight
  var textFieldValue by mutableStateOf(_weight.value.toString())
  val cycleHistoryList get() = _cycleHistoryList

  fun dialogShow() {
    textFieldValue = _weight.value.toString()
    _weightDialogVisible = true
  }

  fun dialogClose() {
    _weightDialogVisible = false
  }

  fun updateWeight(weight: Float) {
    viewModelScope.launch {
      cycleHistoryDao.setWeight(weight)
    }
  }

  fun insertCycleRecord(
    date: LocalDateTime,
    duration: Long,
    distance: Double,
    path: List<Point>,
    calories: Int,
    weight: Float
  ) {
    viewModelScope.launch {
      cycleHistoryDao.insert(
        CycleHistory(
          year = date.year,
          month = date.monthValue,
          day = date.dayOfMonth,
          dateTime = date.toLocalTime(),
          duration = duration,
          distance = distance,
          path = path,
          calories = calories,
          weight = weight
        )
      )
    }
  }

  fun deleteAllHistory() {
    viewModelScope.launch {
      cycleHistoryDao.deleteAll()
    }
  }
}

@Suppress("UNCHECKED_CAST")
class CycleHistoryViewModelFactory(
  private val cycleLogDao: CycleHistoryDao
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(HistoryViewModel::class.java)) {
      return HistoryViewModel(cycleLogDao) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}