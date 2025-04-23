package com.example.cyclelog.tracking

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.view.Display
import android.view.Surface
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class OrientationTracker(
  private val context: Context
) : SensorEventListener {
  private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
  private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
  private val _azimuth = MutableStateFlow(0.0)

  val azimuth: StateFlow<Double> = _azimuth

  fun startTracking() {
    sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_GAME)
  }

  override fun onSensorChanged(event: SensorEvent?) {
    if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
      val rotationMatrix = FloatArray(9)
      SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

      val orientation = FloatArray(3)
      SensorManager.getOrientation(rotationMatrix, orientation)

      val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
      val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)
      val rotation = display?.rotation ?: Surface.ROTATION_0

      val correction = when (rotation) {
        Surface.ROTATION_90 -> 90
        Surface.ROTATION_270 -> -90
        else -> 0
      }

      val azimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
      val normalized = (azimuthDegrees + correction + 360) % 360

      _azimuth.value = normalized.toDouble()
    }
  }

  override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}