package com.example.cyclelog.ui

import androidx.compose.runtime.compositionLocalOf
import com.example.cyclelog.ui.viewModel.AlarmViewModel
import com.example.cyclelog.ui.viewModel.BluetoothViewModel
import com.example.cyclelog.ui.viewModel.HistoryViewModel
import com.example.cyclelog.ui.viewModel.StateViewModel
import com.example.cyclelog.ui.viewModel.LocationViewModel
import com.example.cyclelog.ui.viewModel.MapViewModel
import com.example.cyclelog.ui.viewModel.RecordViewModel
import com.example.cyclelog.ui.viewModel.SettingsViewModel
import com.example.cyclelog.utils.Notification

val LocalNotification = compositionLocalOf<Notification> {
  error("No Notification provided")
}
val LocalSettingsViewModel = compositionLocalOf<SettingsViewModel> {
  error("No SettingsViewModel provided")
}
val LocalBluetoothViewModel = compositionLocalOf<BluetoothViewModel> {
  error("No BluetoothViewModel provided")
}
val LocalLocationViewModel = compositionLocalOf<LocationViewModel> {
  error("No LocationViewModel provided")
}
val LocalStateViewModel = compositionLocalOf<StateViewModel> {
  error("No StateViewModel provided")
}
val LocalAlarmViewModel = compositionLocalOf<AlarmViewModel> {
  error("No AlarmViewModel provided")
}
val LocalHistoryViewModel = compositionLocalOf<HistoryViewModel> {
  error("No HistoryViewModel provided")
}
val LocalRecordViewModel = compositionLocalOf<RecordViewModel> {
  error("No RecordViewModel provided")
}
val LocalMapViewModel = compositionLocalOf<MapViewModel> {
  error("No MapViewModel provided")
}