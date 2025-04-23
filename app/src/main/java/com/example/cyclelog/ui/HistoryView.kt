package com.example.cyclelog.ui

import android.content.Intent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cyclelog.R
import com.example.cyclelog.utils.formatDistance
import com.example.cyclelog.utils.formatDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryView() {
  val context = LocalContext.current
  val historyViewModel = LocalHistoryViewModel.current
  val cycleHistory by historyViewModel.cycleHistoryList.collectAsState()

  Scaffold(
    modifier = Modifier
      .navigationBarsPadding(),
    topBar = {
      CenterAlignedTopAppBar(
        modifier = Modifier
          .fillMaxWidth(),
        title = { Text(Destination.HISTORY.title) },
        actions = {
          IconButton(
            onClick = {
              val intent = Intent(context, SettingsActivity::class.java)
              context.startActivity(intent)
            }
          ) {
            Icon(
              painter = painterResource(R.drawable.rounded_settings_24),
              contentDescription = "setting"
            )
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = {
          historyViewModel.dialogShow()
        }
      ) {
        Icon(
          modifier = Modifier
            .size(36.dp),
          painter = painterResource(R.drawable.rounded_monitor_weight_24),
          contentDescription = "設定體重"
        )
      }
      with(historyViewModel) {
        if (weightDialogVisible) {
          AlertDialog(
            onDismissRequest = { dialogClose() },
            title = { Text("設定體重") },
            dismissButton = {
              TextButton(
                onClick = { dialogClose() }
              ) {
                Text("取消")
              }
            },
            confirmButton = {
              TextButton(
                onClick = {
                  updateWeight(textFieldValue.toFloat())

                  dialogClose()
                },
                enabled = historyViewModel.textFieldValue.isNotBlank()
              ) {
                Text("設定")
              }
            },
            text = {
              OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                  if (it.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                    textFieldValue = it
                  }
                },
                label = { Text("體重") },
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                  keyboardType = KeyboardType.Number
                )
              )
            }
          )
        }
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      items(
        items = cycleHistory,
        key = { it.id }
      ) {
        Column(
          modifier = Modifier
            .padding(16.dp)
        ) {
          Text("${it.year}/${it.month}/${it.day} ${it.dateTime}")
          Text(formatDuration(it.duration))
          Text(formatDistance(it.distance))
          Text("${it.calories} cal")
          Text("${it.weight} kg")
        }
      }
    }
  }
}