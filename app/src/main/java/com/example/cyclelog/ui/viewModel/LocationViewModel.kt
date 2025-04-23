package com.example.cyclelog.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.Location
import com.mapbox.common.location.LocationObserver
import com.mapbox.common.location.LocationProvider
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement

class LocationViewModel : ViewModel() {
  private var _location by mutableStateOf<Location?>(null)
  private var _locationPoint by mutableStateOf<Point?>(null)
  private var _pathPoints by mutableStateOf<List<Point>>(emptyList())
  private var _distance by mutableDoubleStateOf(0.0)
  private var _speedMps by mutableFloatStateOf(0f)
  private var locationProvider: LocationProvider? = null
  private val locationObserver = LocationObserver { locations ->
    locations.lastOrNull()?.let { location ->
      val point = Point.fromLngLat(location.longitude, location.latitude)

      _location = location
      _locationPoint?.let { lastPoint ->
        _distance += TurfMeasurement.distance(lastPoint, point, TurfConstants.UNIT_METERS)
      }
      _locationPoint = point
      _pathPoints += point
      _speedMps = location.speed?.toFloat() ?: 0f
    }
  }

  val location get() = _location
  val locationPoint: Point? get() = _locationPoint ?: Point.fromLngLat(121.0, 23.7)
  val pathPoints get() = _pathPoints
  val distance get() = _distance
  val speedMps get() = _speedMps
  val speedKph get() = _speedMps * 3.6f

  init {
    startLocationUpdates()
  }

  private fun startLocationUpdates() {
    val request = LocationProviderRequest.Builder()
      .interval(
        IntervalSettings.Builder()
          .interval(300L)
          .minimumInterval(100L)
          .maximumInterval(5000L)
          .build()
      )
      .displacement(0F)
      .accuracy(AccuracyLevel.HIGHEST)
      .build()
    val service = LocationServiceFactory.getOrCreate()
    val result = service.getDeviceLocationProvider(request)

    if (result.isValue) {
      locationProvider = result.value!!
      locationProvider?.addLocationObserver(locationObserver)
    }
  }

  fun initialize() {
    _distance = 0.0
    _pathPoints = emptyList()
  }

  fun setLocationPoint(point: Point) {
    _locationPoint = point
  }

  override fun onCleared() {
    super.onCleared()
    locationProvider?.removeLocationObserver(locationObserver)
  }
}