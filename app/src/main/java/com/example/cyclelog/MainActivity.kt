package com.example.cyclelog

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import com.example.cyclelog.ui.MainView
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.cyclelog.data.AppDatabase
import com.example.cyclelog.data.BluetoothConnector
import com.example.cyclelog.data.dataStore
import com.example.cyclelog.ui.Destination
import com.example.cyclelog.ui.LocalAlarmViewModel
import com.example.cyclelog.ui.LocalBluetoothViewModel
import com.example.cyclelog.ui.LocalHistoryViewModel
import com.example.cyclelog.ui.LocalStateViewModel
import com.example.cyclelog.ui.LocalLocationViewModel
import com.example.cyclelog.ui.LocalMapViewModel
import com.example.cyclelog.ui.LocalNotification
import com.example.cyclelog.ui.LocalRecordViewModel
import com.example.cyclelog.ui.LocalSettingsViewModel
import com.example.cyclelog.ui.theme.CycleLogTheme
import com.example.cyclelog.ui.viewModel.AlarmViewModel
import com.example.cyclelog.ui.viewModel.BluetoothViewModel
import com.example.cyclelog.ui.viewModel.HistoryViewModel
import com.example.cyclelog.ui.viewModel.CycleHistoryViewModelFactory
import com.example.cyclelog.ui.viewModel.StateViewModel
import com.example.cyclelog.ui.viewModel.LocationViewModel
import com.example.cyclelog.ui.viewModel.MapViewModel
import com.example.cyclelog.ui.viewModel.MapViewModelFactory
import com.example.cyclelog.ui.viewModel.RecordViewModel
import com.example.cyclelog.ui.viewModel.RecordViewModelFactory
import com.example.cyclelog.ui.viewModel.SettingsViewModel
import com.example.cyclelog.ui.viewModel.SettingsViewModelFactory
import com.example.cyclelog.utils.Notification

class MainActivity : ComponentActivity() {
  private val bluetoothViewModel: BluetoothViewModel by viewModels()
  private val locationViewModel: LocationViewModel by viewModels()
  private val stateViewModel: StateViewModel by viewModels()
  private val alarmViewModel: AlarmViewModel by viewModels()
  private lateinit var notification: Notification
  private lateinit var settingsViewModel: SettingsViewModel
  private lateinit var recordViewModel: RecordViewModel
  private lateinit var mapViewModel: MapViewModel
  private lateinit var historyViewModel: HistoryViewModel
  private val requestBluetoothPermissionsLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
      if (!permissions.all { it.value }) {
        Toast.makeText(this, "未允許藍牙權限", Toast.LENGTH_SHORT).show()
      } else {
        BluetoothConnector(this, bluetoothViewModel).connectToDevice()
      }
      initiateLocation()
    }
  private val requestLocationPermissionsLauncher =
    registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
      if (!permissions.all { it.value }) {
        Toast.makeText(this, "未允許定位權限", Toast.LENGTH_SHORT).show()
      }
      initiateNotification()
    }
  private val requestNotificationPermissionLauncher =
    registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
      if (!isGranted) {
        Toast.makeText(this, "未允許通知權限", Toast.LENGTH_SHORT).show()
      }
    }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    initiateBluetooth()
    initiateLocation()

    val cycleHistoryDao = AppDatabase.getDatabase(applicationContext).cycleHistoryDao()
    val cycleHistoryViewModelFactory = CycleHistoryViewModelFactory(cycleHistoryDao)
    notification = Notification(this)
    settingsViewModel =
      ViewModelProvider(
        this,
        SettingsViewModelFactory(this.dataStore)
      )[SettingsViewModel::class.java]
    recordViewModel =
      ViewModelProvider(
        this,
        RecordViewModelFactory(this, this.dataStore)
      )[RecordViewModel::class.java]
    mapViewModel =
      ViewModelProvider(this, MapViewModelFactory(this))[MapViewModel::class.java]
    historyViewModel =
      ViewModelProvider(this, cycleHistoryViewModelFactory)[HistoryViewModel::class.java]

    enableEdgeToEdge()
    setContent {
      CompositionLocalProvider(
        LocalNotification provides notification,
        LocalSettingsViewModel provides settingsViewModel,
        LocalBluetoothViewModel provides bluetoothViewModel,
        LocalLocationViewModel provides locationViewModel,
        LocalStateViewModel provides stateViewModel,
        LocalAlarmViewModel provides alarmViewModel,
        LocalRecordViewModel provides recordViewModel,
        LocalMapViewModel provides mapViewModel,
        LocalHistoryViewModel provides historyViewModel
      ) {
        CycleLogTheme {
          MainView()
        }
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)

    intent.getStringExtra("navigate")?.let {
      stateViewModel.currentDestination = Destination.valueOf(it)
    }
  }

  private fun initiateBluetooth() {
    val permissions = arrayOf(
      Manifest.permission.BLUETOOTH_CONNECT,
      Manifest.permission.BLUETOOTH_SCAN
    )
    val permissionsToRequest = permissions.filter {
      ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }

    if (permissionsToRequest.isNotEmpty()) {
      requestBluetoothPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
    } else {
      BluetoothConnector(this, bluetoothViewModel).connectToDevice()
    }
  }

  private fun initiateLocation() {
    val permissions = arrayOf(
      Manifest.permission.ACCESS_COARSE_LOCATION,
      Manifest.permission.ACCESS_FINE_LOCATION
    )
    val permissionsToRequest = permissions.filter {
      ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
    }

    if (permissionsToRequest.isNotEmpty()) {
      requestLocationPermissionsLauncher.launch(permissionsToRequest.toTypedArray())
    }
  }

  private fun initiateNotification() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
      != PackageManager.PERMISSION_GRANTED
    ) {
      requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
  }
}