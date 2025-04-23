package com.example.cyclelog.ui.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cyclelog.tracking.OrientationTracker
import com.example.cyclelog.ui.CameraState
import kotlinx.coroutines.launch

class MapViewModel(
  context: Context
) : ViewModel() {
  private val azimuthTracker = OrientationTracker(context)
  private var _cameraState by mutableStateOf(CameraState.POSITION)
  private var _cameraZoom by mutableDoubleStateOf(6.5)
  private var _currentAzimuth by mutableDoubleStateOf(0.0)

  val cameraState get() = _cameraState
  val cameraZoom get() = _cameraZoom
  val currentAzimuth get() = _currentAzimuth

  init {
    azimuthTracker.startTracking()

    viewModelScope.launch {
      azimuthTracker.azimuth.collect {
        _currentAzimuth = it
      }
    }
  }

  fun setCameraState(state: CameraState) {
    _cameraState = state
  }

  fun updateCameraZoom(zoom: Double) {
    _cameraZoom = zoom
  }
}

@Suppress("UNCHECKED_CAST")
class MapViewModelFactory(
  private val context: Context
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
      return MapViewModel(context) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}