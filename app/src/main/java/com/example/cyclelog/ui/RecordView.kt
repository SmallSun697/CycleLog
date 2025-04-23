package com.example.cyclelog.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.graphics.BlurMaskFilter
import android.graphics.Path
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults.containerColor
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.cyclelog.R
import com.example.cyclelog.utils.NotifyType
import com.example.cyclelog.utils.estimateCalories
import com.example.cyclelog.utils.estimateWeight
import com.example.cyclelog.utils.formatDistance
import com.example.cyclelog.utils.formatDuration
import com.example.cyclelog.utils.isScreenTall
import java.lang.String.format

enum class Direction {
  LEFT,
  RIGHT
}

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordView() {
  val context = LocalContext.current
  val notification = LocalNotification.current
  val configuration = LocalConfiguration.current
  val bluetoothViewModel = LocalBluetoothViewModel.current
  val locationViewModel = LocalLocationViewModel.current
  val stateViewModel = LocalStateViewModel.current
  val alarmViewModel = LocalAlarmViewModel.current
  val historyViewModel = LocalHistoryViewModel.current
  val recordViewModel = LocalRecordViewModel.current
  val weight by historyViewModel.weight.collectAsState()
  val isPortrait = configuration.orientation == Configuration.ORIENTATION_PORTRAIT
  val left = bluetoothViewModel.left
  val right = bluetoothViewModel.right
  val alarmWeight by animateFloatAsState(
    targetValue = if (alarmViewModel.alarm) 1f else 0.3f,
    animationSpec = tween(600)
  )

  recordViewModel.updateDistance(left, right)

  Scaffold(
    modifier = Modifier
      .navigationBarsPadding(),
    topBar = {
      if (isScreenTall()) {
        CenterAlignedTopAppBar(
          modifier = Modifier
            .fillMaxWidth(),
          title = { Text(Destination.RECORD.title) },
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
      }
    },
    floatingActionButton = {
      val isRecording = stateViewModel.isRecording
      val fabRadius by animateDpAsState(if (isRecording) 16.dp else 32.dp)

      FloatingActionButton(
        modifier = Modifier
          .widthIn(64.dp)
          .height(64.dp),
        onClick = {
          if (isRecording) {
            val duration = stateViewModel.recordedTime
            val distance = locationViewModel.distance
            val calories = estimateCalories(distance, 1f)
            historyViewModel.insertCycleRecord(
              date = stateViewModel.startTime,
              duration = duration,
              distance = distance,
              path = locationViewModel.pathPoints,
              calories = calories,
              weight = estimateWeight(
                weight = weight,
                duration = duration,
                distance = distance,
                calories = calories
              )
            )
            notification.show(
              type = NotifyType.NOTIFY,
              title = "新紀錄",
              message = "新紀錄已存入資料庫",
              navigate = Destination.HISTORY
            )
          } else {
            locationViewModel.initialize()
          }
          stateViewModel.setRecording(!isRecording)
        },
        shape = RoundedCornerShape(fabRadius),
        containerColor = if (isRecording) Color(0xFFBA1A1A) else containerColor,
        contentColor = if (isRecording) Color.White else contentColorFor(containerColor)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          val formattedTime = formatDuration(stateViewModel.recordedTime)
          val fabTextWidth by animateDpAsState(
            targetValue = if (isRecording) 200.dp else 0.dp,
            animationSpec = tween(durationMillis = 400, easing = LinearEasing)
          )

          if (isRecording) {
            Text(
              modifier = Modifier
                .padding(start = 20.dp)
                .widthIn(0.dp, fabTextWidth),
              text = formattedTime,
              fontSize = MaterialTheme.typography.titleMedium.fontSize,
              maxLines = 1
            )
            Icon(
              modifier = Modifier
                .padding(end = 14.dp),
              painter = painterResource(R.drawable.rounded_stop_36),
              contentDescription = "停止紀錄"
            )
          } else {
            Icon(
              painter = painterResource(R.drawable.rounded_fiber_manual_record_36),
              contentDescription = "開始紀錄"
            )
          }
        }
      }
    }
  ) { innerPadding ->
    WarningLight(
      color = when (left) {
        in 0..30 -> Color.Red
        in 31..60 -> Color(0xFFFF6900)
        in 61..90 -> Color.Yellow
        in 91..120 -> Color.Green
        else -> null
      },
      direction = Direction.LEFT,
      modifier = if (isScreenTall()) {
        Modifier
          .padding(innerPadding)
      } else {
        Modifier
      }
    )
    WarningLight(
      color = when (right) {
        in 0..30 -> Color.Red
        in 31..60 -> Color(0xFFFF6900)
        in 61..90 -> Color.Yellow
        in 91..120 -> Color.Green
        else -> null
      },
      direction = Direction.RIGHT,
      modifier = if (isScreenTall()) {
        Modifier
          .padding(innerPadding)
      } else {
        Modifier
      }
    )
    AlertSettingDialog()

    if (isPortrait) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxWidth(),
        ) {
          Speedometer()
        }
        Column(
          modifier = Modifier
            .weight(alarmWeight)
            .fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          AlarmClock()
        }
      }
    } else {
      Row(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .fillMaxHeight(),
        ) {
          Speedometer()
        }
        Column(
          modifier = Modifier
            .weight(alarmWeight)
            .fillMaxSize(),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          AlarmClock()
        }
      }
    }
  }

  DisposableEffect(Unit) {
    onDispose {
      recordViewModel.stopTone()
    }
  }
}

@Composable
fun WarningLight(
  color: Color? = null,
  direction: Direction,
  modifier: Modifier = Modifier
) {
  if (color != null) {
    val animatedColor by animateColorAsState(
      targetValue = color,
      animationSpec = tween(600)
    )

    Box(
      modifier = modifier
        .fillMaxSize()
        .drawBehind {
          val paint = Paint().apply {
            this.color = animatedColor.copy(alpha = 0.8f)
            strokeWidth = 48.dp.toPx()
            style = PaintingStyle.Stroke
            isAntiAlias = true
          }

          val frameworkPaint = paint.asFrameworkPaint().apply {
            maskFilter = BlurMaskFilter(120f, BlurMaskFilter.Blur.SOLID)
          }

          drawIntoCanvas { canvas ->
            val center = size.width / 2
            val path = Path().apply {
              when (direction) {
                Direction.LEFT -> {
                  moveTo(center, 0f)
                  lineTo(0f, 0f)
                  lineTo(0f, size.height)
                  lineTo(center, size.height)
                }

                Direction.RIGHT -> {
                  moveTo(center, 0f)
                  lineTo(size.width, 0f)
                  lineTo(size.width, size.height)
                  lineTo(center, size.height)
                }
              }
            }

            canvas.nativeCanvas.drawPath(path, frameworkPaint)
          }
        }
    )
  }
}

@SuppressLint("DefaultLocale")
@Composable
fun Speedometer() {
  val stateViewModel = LocalStateViewModel.current
  val locationViewModel = LocalLocationViewModel.current
  val speedKphGps = locationViewModel.speedKph
  val progressMax = 60f
  val progress by animateFloatAsState(
    targetValue = ((minOf(speedKphGps, progressMax) / progressMax * 0.75f)),
    animationSpec = tween(800)
  )
  BoxWithConstraints(
    contentAlignment = Alignment.Center
  ) {
    val size = min(maxWidth, maxHeight)

    CircularProgressIndicator(
      modifier = Modifier
        .padding(48.dp)
        .size(size)
        .aspectRatio(1f)
        .graphicsLayer {
          rotationZ = -135f
        },
      progress = { progress },
      strokeWidth = 36.dp,
      trackColor = Color.Transparent
    )
    Column(
      modifier = Modifier
        .fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = format("%.1f km/h", speedKphGps),
        fontSize = 40.sp
      )
      AnimatedVisibility(stateViewModel.isRecording) {
        Text(
          text = formatDistance(locationViewModel.distance),
          fontSize = 36.sp
        )
      }
    }
  }
}

@Composable
fun AlarmClock() {
  val alarmViewModel = LocalAlarmViewModel.current
  val recordViewModel = LocalRecordViewModel.current
  val isAlarmOn = alarmViewModel.alarm
  val alarmButtonSize by animateDpAsState(
    targetValue = if (alarmViewModel.alarm) 24.dp else 36.dp,
    animationSpec = tween(600)
  )

  AnimatedVisibility(isAlarmOn) {
    Text(
      modifier = Modifier
        .padding(bottom = 32.dp),
      text = formatDuration(alarmViewModel.timeLeft / 1000),
      fontSize = 40.sp
    )
  }
  Button(
    onClick = {
      if (isAlarmOn) {
        alarmViewModel.clearAlarm()
      } else {
        recordViewModel.dialogShow()
      }
    },
    contentPadding = PaddingValues(20.dp)
  ) {
    Icon(
      modifier = Modifier
        .size(alarmButtonSize),
      painter = painterResource(
        if (isAlarmOn) {
          R.drawable.rounded_alarm_off_24
        } else {
          R.drawable.rounded_alarm_24
        }
      ),
      contentDescription = "定時通知"
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertSettingDialog() {
  val recordViewModel = LocalRecordViewModel.current
  val alarmViewModel = LocalAlarmViewModel.current

  with(recordViewModel) {
    if (alarmDialogVisible) {
      val density = LocalDensity.current
      var columnWidth by remember { mutableIntStateOf(0) }
      val columnWidthDp = with(density) { columnWidth.toDp() }
      val timePickerState = rememberTimePickerState(is24Hour = true)

      BasicAlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = { dialogClose() }
      ) {
        Surface(
          shape = AlertDialogDefaults.shape,
          color = AlertDialogDefaults.containerColor,
          tonalElevation = AlertDialogDefaults.TonalElevation
        ) {
          Column(
            modifier = Modifier
              .padding(vertical = 8.dp, horizontal = 24.dp)
              .onGloballyPositioned { coordinates ->
                columnWidth = coordinates.size.width
              },
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              modifier = Modifier
                .padding(vertical = 8.dp),
              text = "設定提醒間隔",
              style = MaterialTheme.typography.titleLarge
            )
            TimePicker(
              state = timePickerState,
            )
            Row(
              modifier = Modifier
                .width(columnWidthDp),
              horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              TextButton(
                onClick = { dialogClose() }
              ) {
                Text("取消")
              }
              TextButton(
                onClick = {
                  alarmViewModel.startAlarm(
                    timePickerState.hour,
                    timePickerState.minute
                  )
                  dialogClose()
                },
                enabled = timePickerState.hour + timePickerState.minute > 0
              ) {
                Text("設定")
              }
            }
          }
        }
      }
    }
  }
}