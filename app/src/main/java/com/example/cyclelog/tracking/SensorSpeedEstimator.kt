package com.example.cyclelog.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.cyclelog.data.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

class SensorSpeedEstimator(
  context: Context,
  dataStore: DataStore<Preferences>
) : SensorEventListener {
  private val repository = SettingsRepository(dataStore)
  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  private val linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
  private val sensorDataArray = ArrayDeque<FloatArray>()
  private var lastTimestamp = 0L
  private val maxDataSize = 20
  private var noise = repository.sensorNoiseInitial()
  private var count = 0L
  private var velocity = floatArrayOf(0f, 0f, 0f)
  private var accelerationCapture = false
  private var totalAcceleration = floatArrayOf(0f, 0f, 0f)
  private var _speed = MutableStateFlow(0f)

  val speed: StateFlow<Float> = _speed

  fun startEstimator() {
    stopEstimator()
//    lastTimestamp = 0L
//    count = 0L
//    velocity = floatArrayOf(0f, 0f, 0f)
//    _speed.value = 0f
//    sensorManager.registerListener(this, linearAccelSensor, SensorManager.SENSOR_DELAY_UI)
  }

  fun stopEstimator() {
    sensorManager.unregisterListener(this)
  }

  fun setNoise() {
    CoroutineScope(Dispatchers.Default).launch {
      noise = floatArrayOf(0f, 0f, 0f)
      totalAcceleration = floatArrayOf(0f, 0f, 0f)
      accelerationCapture = true

      delay(1000)
      startEstimator()
      delay(14000)
      stopEstimator()

      accelerationCapture = false
      noise = floatArrayOf(
        totalAcceleration[0] / count,
        totalAcceleration[1] / count,
        totalAcceleration[2] / count
      )
      CoroutineScope(Dispatchers.IO).launch {
        repository.setSensorNoise(noise)
      }

      startEstimator()
    }
  }

  override fun onSensorChanged(event: SensorEvent) {
    val currentTime = event.timestamp
    val x = event.values[0] - noise[0]
    val y = event.values[1] - noise[1]
    val z = event.values[2] - noise[2]

    if (accelerationCapture) {
      count++
      totalAcceleration[0] += x
      totalAcceleration[1] += y
      totalAcceleration[2] += z
    } else if (lastTimestamp != 0L) {
      val deltaTime = (currentTime - lastTimestamp) / 1_000_000_000f

      if (sqrt(x * x + y * y + z * z) > 0f) {
        sensorDataArray.addLast(
          floatArrayOf(
            x * deltaTime,
            y * deltaTime,
            z * deltaTime
          )
        )
        if (sensorDataArray.size > maxDataSize) sensorDataArray.removeFirst()

        velocity[0] += x * deltaTime
        velocity[1] += y * deltaTime
        velocity[2] += z * deltaTime
      } else {
        velocity = floatArrayOf(0f, 0f, 0f)
      }

      _speed.value =
        sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1] + velocity[2] * velocity[2])
    }

    lastTimestamp = currentTime
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}


//package com.example.cyclelog.tracking
//
//import android.content.Context
//import android.hardware.Sensor
//import android.hardware.SensorEvent
//import android.hardware.SensorEventListener
//import android.hardware.SensorManager
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import com.example.cyclelog.data.SettingsRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlin.math.sqrt
//
//class SensorSpeedEstimator(
//  context: Context,
//  dataStore: DataStore<Preferences>
//) : SensorEventListener {
//  private val repository = SettingsRepository(dataStore)
//  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
//  private val linearAccelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
//  private val sensorDataArray = ArrayDeque<FloatArray>()
//  private var noise = repository.sensorNoiseInitial()
//  private var lastTimestamp = 0L
//  private val maxDataSize = 50
//  private val noiseThreshold = 0.01f
//  private var _speed = MutableStateFlow(0f)
//
//  val speed: StateFlow<Float> = _speed
//
//  fun startEstimator() {
//    sensorManager.registerListener(this, linearAccelSensor, SensorManager.SENSOR_DELAY_UI)
//  }
//
//  fun setNoise() {}
//
//  override fun onSensorChanged(event: SensorEvent) {
//    if (lastTimestamp != 0L) {
//      val deltaTime = (event.timestamp - lastTimestamp) / 1_000_000_000f
//
//      val x = event.values[0] - noise[0]
//      val y = event.values[1] - noise[1]
//      val z = event.values[2] - noise[2]
//
//      sensorDataArray.addLast(
//        floatArrayOf(
//          x * deltaTime,
//          y * deltaTime,
//          z * deltaTime
//        )
//      )
//      if (sensorDataArray.size > maxDataSize) sensorDataArray.removeFirst()
//
//      val sumX = sensorDataArray.fold(0f) { acc, vec -> acc + vec[0] }
//      val sumY = sensorDataArray.fold(0f) { acc, vec -> acc + vec[1] }
//      val sumZ = sensorDataArray.fold(0f) { acc, vec -> acc + vec[2] }
//
//      _speed.value = sqrt(sumX * sumX + sumY * sumY + sumZ * sumZ)
//    }
//
//    lastTimestamp = event.timestamp
//  }
//
//  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
//}