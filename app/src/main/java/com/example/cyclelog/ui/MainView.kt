package com.example.cyclelog.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.cyclelog.R

enum class Destination(val label: String, val icon: Int) {
  HISTORY("資料", R.drawable.rounded_monitoring_24),
  RECORD("紀錄", R.drawable.rounded_screen_record_24),
  MAP("軌跡", R.drawable.rounded_map_24)
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainView() {
  val mainViewModel = LocalMainViewModel.current
  var currentDestination by remember { mutableStateOf(mainViewModel.currentDestination) }

  Scaffold(
    topBar = {
      CenterAlignedTopAppBar(
        modifier = Modifier
          .fillMaxWidth(),
        title = { Text("CycleLog") },
        actions = {
          IconButton(
            onClick = {}
          ) {
            Icon(
              painter = painterResource(R.drawable.rounded_settings_24),
              contentDescription = "setting"
            )
          }
        }
      )
    },
    bottomBar = {
      NavigationBar(
        modifier = Modifier
          .fillMaxWidth()
      ) {
        Destination.entries.forEach { destination ->
          NavigationBarItem(
            icon = {
              Icon(
                painter = painterResource(destination.icon),
                contentDescription = destination.label
              )
            },
            label = { Text(destination.label) },
            selected = currentDestination == destination,
            onClick = {
              currentDestination = destination
              mainViewModel.currentDestination = destination
            }
          )
        }
      }
    }
  ) { innerPadding ->
    when (currentDestination) {
      Destination.HISTORY -> HistoryView(innerPadding)
      Destination.MAP -> MapView(innerPadding)
      Destination.RECORD -> RecordView(innerPadding)
    }
  }
}