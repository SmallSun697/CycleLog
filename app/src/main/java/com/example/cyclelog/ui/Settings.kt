package com.example.cyclelog.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import com.example.cyclelog.R
import com.example.cyclelog.data.AppDatabase
import com.example.cyclelog.data.DarkTheme
import com.example.cyclelog.data.dataStore
import com.example.cyclelog.ui.theme.CycleLogTheme
import com.example.cyclelog.ui.viewModel.AlarmViewModel
import com.example.cyclelog.ui.viewModel.BluetoothViewModel
import com.example.cyclelog.ui.viewModel.HistoryViewModel
import com.example.cyclelog.ui.viewModel.CycleHistoryViewModelFactory
import com.example.cyclelog.ui.viewModel.LocationViewModel
import com.example.cyclelog.ui.viewModel.MapViewModel
import com.example.cyclelog.ui.viewModel.MapViewModelFactory
import com.example.cyclelog.ui.viewModel.RecordViewModel
import com.example.cyclelog.ui.viewModel.RecordViewModelFactory
import com.example.cyclelog.ui.viewModel.SettingsViewModel
import com.example.cyclelog.ui.viewModel.SettingsViewModelFactory
import com.example.cyclelog.ui.viewModel.StateViewModel

enum class OptionType {
  RADIO,
  CHECKBOX
}

class SettingsActivity : ComponentActivity() {
  private lateinit var settingsViewModel: SettingsViewModel
  private val bluetoothViewModel: BluetoothViewModel by viewModels()
  private val locationViewModel: LocationViewModel by viewModels()
  private val stateViewModel: StateViewModel by viewModels()
  private val alarmViewModel: AlarmViewModel by viewModels()
  private lateinit var recordViewModel: RecordViewModel
  private lateinit var mapViewModel: MapViewModel
  private lateinit var historyViewModel: HistoryViewModel

  @SuppressLint("CoroutineCreationDuringComposition")
  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val context = this
    val cycleHistoryDao = AppDatabase.getDatabase(applicationContext).cycleHistoryDao()
    val cycleHistoryViewModelFactory = CycleHistoryViewModelFactory(cycleHistoryDao)
    settingsViewModel =
      ViewModelProvider(
        context,
        SettingsViewModelFactory(context.dataStore)
      )[SettingsViewModel::class.java]
    recordViewModel =
      ViewModelProvider(
        context,
        RecordViewModelFactory(context, context.dataStore)
      )[RecordViewModel::class.java]
    mapViewModel =
      ViewModelProvider(context, MapViewModelFactory(context))[MapViewModel::class.java]
    historyViewModel =
      ViewModelProvider(context, cycleHistoryViewModelFactory)[HistoryViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      CompositionLocalProvider(
        LocalSettingsViewModel provides settingsViewModel,
        LocalBluetoothViewModel provides bluetoothViewModel,
        LocalLocationViewModel provides locationViewModel,
        LocalStateViewModel provides stateViewModel,
        LocalAlarmViewModel provides alarmViewModel,
        LocalRecordViewModel provides recordViewModel,
        LocalMapViewModel provides mapViewModel,
        LocalHistoryViewModel provides historyViewModel
      ) {
        val settingsViewModel = LocalSettingsViewModel.current
        val historyViewModel = LocalHistoryViewModel.current
        val darkTheme = settingsViewModel.darkTheme

        CycleLogTheme {
          Scaffold(
            topBar = {
              TopAppBar(
                modifier = Modifier
                  .fillMaxWidth(),
                title = { Text("設定") },
                navigationIcon = {
                  IconButton(
                    onClick = { finish() }
                  ) {
                    Icon(
                      painter = painterResource(id = R.drawable.rounded_arrow_back_24),
                      contentDescription = "返回"
                    )
                  }
                }
              )
            }
          ) { innerPadding ->
            Column(
              modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
            ) {
              SettingCategory("主題")

              SettingItemWithDialog(
                title = "深色主題",
                description = "啟用深色主題可減少螢幕亮度、減輕眼睛疲勞，並在低光環境中提供更好的可讀性。",
                dialogTitle = "深色主題"
              ) { closeDialog ->
                DarkTheme.entries.forEach { theme ->
                  SettingItemOption(
                    type = OptionType.RADIO,
                    isChecked = darkTheme == theme,
                    onClick = {
                      settingsViewModel.setDarkTheme(theme)

                      closeDialog()
                    },
                    text = theme.text
                  )
                }
              }

              SettingCategory("清除資料")

              SettingItemWithDialog(
                title = "清除資料",
                description = "警告：所選擇的數據（使用者設定、筆記內容、語音轉錄稿）將被清除或重置為預設狀態，且無法恢復。",
                dialogTitle = "清除資料",
              ) { closeDialog ->
                var isSettingsCheck by remember { mutableStateOf(false) }
                var isCycleHistoryCheck by remember { mutableStateOf(false) }
                val isAnyCheck =
                  isSettingsCheck || isCycleHistoryCheck

                SettingItemOption(
                  type = OptionType.CHECKBOX,
                  isChecked = isSettingsCheck,
                  onClick = { isSettingsCheck = !isSettingsCheck },
                  text = "使用者設定"
                )
                SettingItemOption(
                  type = OptionType.CHECKBOX,
                  isChecked = isCycleHistoryCheck,
                  onClick = { isCycleHistoryCheck = !isCycleHistoryCheck },
                  text = "歷史資料"
                )
                Row(
                  horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                  TextButton(
                    onClick = { closeDialog() }
                  ) {
                    Text("取消")
                  }
                  Button(
                    onClick = {
                      if (isSettingsCheck) settingsViewModel.clearPreferences()
                      if (isCycleHistoryCheck) historyViewModel.deleteAllHistory()

                      closeDialog()
                    },
                    enabled = isAnyCheck,
                    colors = ButtonDefaults.buttonColors(
                      containerColor = MaterialTheme.colorScheme.error,
                      contentColor = MaterialTheme.colorScheme.onError
                    )
                  ) {
                    Text("清除")
                  }
                }
              }

              SettingCategory("關於")

              SettingItem(
                title = "關於 CycleLog",
                description = "點擊以前往 CycleLog 的 Github 頁面（使用外部瀏覽器開啟）。",
                onClick = {
                  val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/SmallSun697/CycleLog")
                  )
                  context.startActivity(intent)
                }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun SettingCategory(text: String) {
  Text(
    modifier = Modifier
      .padding(horizontal = 24.dp, vertical = 8.dp),
    style = MaterialTheme.typography.labelLarge,
    color = MaterialTheme.colorScheme.primary,
    text = text
  )
}

@Composable
fun SettingItem(
  title: String,
  description: String,
  onClick: () -> Unit
) {
  Box(
    contentAlignment = Alignment.CenterStart,
    modifier = Modifier
      .fillMaxWidth()
      .clickable {
        onClick()
      }
  ) {
    Column(
      modifier = Modifier
        .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
      Text(
        text = title,
        style = MaterialTheme.typography.bodyLarge
      )
      Text(
        text = description,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
inline fun SettingItemWithDialog(
  title: String,
  description: String,
  dialogTitle: String,
  crossinline dialogContent: @Composable (closeDialog: () -> Unit) -> Unit,
) {
  var isDialogDisplay by remember { mutableStateOf(false) }

  SettingItem(
    title = title,
    description = description,
    onClick = { isDialogDisplay = true }
  )

  if (isDialogDisplay) {
    BasicAlertDialog({
      isDialogDisplay = false
    }) {
      Surface(
        shape = AlertDialogDefaults.shape,
        color = AlertDialogDefaults.containerColor,
        tonalElevation = AlertDialogDefaults.TonalElevation
      ) {
        Column(
          horizontalAlignment = Alignment.Start,
          modifier = Modifier
            .padding(vertical = 8.dp)
        ) {
          Text(
            text = dialogTitle,
            modifier = Modifier
              .padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
          )
          dialogContent { isDialogDisplay = false }
        }
      }
    }
  }
}

@Composable
inline fun SettingItemOption(
  type: OptionType,
  isChecked: Boolean,
  crossinline onClick: () -> Unit,
  text: String,
  moreContent: @Composable (modifier: Modifier) -> Unit = {}
) {
  Column(
    modifier = Modifier
      .clickable { onClick() }
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
    ) {
      when (type) {
        OptionType.RADIO -> {
          RadioButton(
            selected = isChecked,
            onClick = null,
            modifier = Modifier
              .padding(12.dp)
              .padding(start = 8.dp)
          )
        }

        OptionType.CHECKBOX -> {
          Checkbox(
            checked = isChecked,
            onCheckedChange = null,
            modifier = Modifier
              .padding(12.dp)
              .padding(start = 8.dp)
          )
        }
      }
      Text(
        text = text,
        modifier = Modifier.padding(start = 8.dp)
      )
    }
    moreContent(
      Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .padding(bottom = 8.dp)
    )
  }
}