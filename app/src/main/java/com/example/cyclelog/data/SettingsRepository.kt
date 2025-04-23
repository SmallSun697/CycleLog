package com.example.cyclelog.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class DarkTheme(val text: String) {
  ENABLE("啟用"),
  DISABLE("停用"),
  SYSTEM("跟隨系統")
}

class SettingsRepository(
  private val dataStore: DataStore<Preferences>
) {
  private object PreferencesKeys {
    val DARK_THEME = stringPreferencesKey("dark_theme")
    val SENSOR_NOISE_X = floatPreferencesKey("sensor_noise_x")
    val SENSOR_NOISE_Y = floatPreferencesKey("sensor_noise_y")
    val SENSOR_NOISE_Z = floatPreferencesKey("sensor_noise_z")
  }


  private fun Flow<Preferences>.catchIOException(): Flow<Preferences> {
    return this.catch { exception ->
      if (exception is IOException) {
        emit(emptyPreferences())
      } else {
        throw exception
      }
    }
  }

  private fun Preferences.toDarkTheme(): DarkTheme {
    return DarkTheme.valueOf(this[PreferencesKeys.DARK_THEME] ?: DarkTheme.SYSTEM.name)
  }

  private fun Preferences.toSensorNoise(): FloatArray {
    return floatArrayOf(
      this[PreferencesKeys.SENSOR_NOISE_X] ?: 0f,
      this[PreferencesKeys.SENSOR_NOISE_Y] ?: 0f,
      this[PreferencesKeys.SENSOR_NOISE_Z] ?: 0f
    )
  }

  fun darkThemeInitial(): DarkTheme = runBlocking {
    dataStore.data.first().toDarkTheme()
  }

  val darkThemeFlow: Flow<DarkTheme> = dataStore.data
    .catchIOException()
    .map { preferences ->
      preferences.toDarkTheme()
    }

  fun sensorNoiseInitial(): FloatArray = runBlocking {
    dataStore.data.first().toSensorNoise()
  }

  val sensorNoiseFlow: Flow<FloatArray> = dataStore.data
    .catchIOException()
    .map { preferences ->
      preferences.toSensorNoise()
    }


  suspend fun setDarkTheme(theme: DarkTheme) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.DARK_THEME] = theme.name
    }
  }

  suspend fun setSensorNoise(noise: FloatArray) {
    dataStore.edit { preferences ->
      preferences[PreferencesKeys.SENSOR_NOISE_X] = noise[0]
      preferences[PreferencesKeys.SENSOR_NOISE_Y] = noise[1]
      preferences[PreferencesKeys.SENSOR_NOISE_Z] = noise[2]
    }
  }

  suspend fun clearAll() {
    dataStore.edit { preferences ->
      preferences.clear()
    }
  }
}