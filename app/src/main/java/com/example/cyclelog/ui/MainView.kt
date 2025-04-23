package com.example.cyclelog.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import com.example.cyclelog.R
import com.example.cyclelog.utils.KeepScreenOnEffect
import com.example.cyclelog.utils.NotifyType

enum class Destination(val title: String, val label: String, val icon: Int) {
  HISTORY("資料與分析", "資料", R.drawable.rounded_monitoring_24),
  RECORD("紀錄與警示", "紀錄", R.drawable.rounded_screen_record_24),
  MAP("地圖與軌跡", "軌跡", R.drawable.rounded_map_24)
}

@Composable
fun MainView() {
  val notification = LocalNotification.current
  val stateViewModel = LocalStateViewModel.current
  val alarmViewModel = LocalAlarmViewModel.current
  val isRecording = stateViewModel.isRecording
  val currentDestination = stateViewModel.currentDestination
  val adaptiveInfo = currentWindowAdaptiveInfo()
  val customNavSuiteType = with(adaptiveInfo) {
    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
      NavigationSuiteType.NavigationRail
    } else {
      NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(adaptiveInfo)
    }
  }

  if (alarmViewModel.timeUp) {
    notification.show(
      type = NotifyType.ALARM,
      title = "定時提醒",
      message = "設定的提醒間隔已到"
    )
    alarmViewModel.resetTimeUp()
  }

  NavigationSuiteScaffold(
    navigationSuiteItems = {
      Destination.entries.forEach { destination ->
        item(
          modifier = if (customNavSuiteType == NavigationSuiteType.NavigationRail) {
            Modifier.padding(vertical = 8.dp)
          } else {
            Modifier
          },
          icon = {
            Icon(
              painter = painterResource(destination.icon),
              contentDescription = destination.label
            )
          },
          label = { Text(destination.label) },
          selected = currentDestination == destination,
          onClick = {
            stateViewModel.currentDestination = destination
          }
        )
      }
    },
    layoutType = customNavSuiteType
  ) {
    KeepScreenOnEffect(isRecording)

    when (currentDestination) {
      Destination.HISTORY -> HistoryView()
      Destination.RECORD -> RecordView()
      Destination.MAP -> MapView()
    }
  }
}