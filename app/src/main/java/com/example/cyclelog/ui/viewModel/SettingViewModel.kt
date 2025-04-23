package com.example.cyclelog.ui.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.cyclelog.data.DarkTheme
import com.example.cyclelog.data.SettingsRepository
import kotlinx.coroutines.launch

class SettingsViewModel(
  dataStore: DataStore<Preferences>
) : ViewModel() {
  private val repository = SettingsRepository(dataStore)
  private var _darkTheme by mutableStateOf(repository.darkThemeInitial())
  val darkTheme get() = _darkTheme

  init {
    viewModelScope.launch {
      repository.darkThemeFlow.collect { theme ->
        _darkTheme = theme
      }
    }
  }

  fun setDarkTheme(theme: DarkTheme) {
    viewModelScope.launch {
      repository.setDarkTheme(theme)
    }
  }

  fun clearPreferences() {
    viewModelScope.launch {
      repository.clearAll()
    }
  }
}

@Suppress("UNCHECKED_CAST")
class SettingsViewModelFactory(
  private val dataStore: DataStore<Preferences>
) : ViewModelProvider.Factory {
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
      return SettingsViewModel(dataStore) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}