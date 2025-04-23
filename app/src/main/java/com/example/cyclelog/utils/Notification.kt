package com.example.cyclelog.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioAttributes
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.cyclelog.MainActivity
import com.example.cyclelog.R
import com.example.cyclelog.ui.Destination

enum class NotifyType {
  NOTIFY,
  ALARM
}

class Notification(
  private val context: Context
) {
  private val alarmChannelId = "alarm_channel"
  private val notifyChannelId = "notify_channel"

  init {
    val manager = context.getSystemService(NotificationManager::class.java)

    listOf(
      Triple(
        notifyChannelId,
        "CycleLog 通知",
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) to AudioAttributes.USAGE_NOTIFICATION
      ),
      Triple(
        alarmChannelId,
        "CycleLog 鬧鐘",
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) to AudioAttributes.USAGE_ALARM
      )
    ).forEach { (id, name, pair) ->
      val (soundUri, usage) = pair
      val attributes = AudioAttributes.Builder()
        .setUsage(usage)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()
      val channel = NotificationChannel(id, name, NotificationManager.IMPORTANCE_HIGH).apply {
        setSound(soundUri, attributes)
        description = "CycleLog $name"
      }

      manager.createNotificationChannel(channel)
    }
  }

  fun show(
    type: NotifyType,
    title: String,
    message: String,
    navigate: Destination = Destination.HISTORY
  ) {
    val intent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
      putExtra("navigate", navigate.name)
    }
    val channelId = when (type) {
      NotifyType.ALARM -> alarmChannelId
      NotifyType.NOTIFY -> notifyChannelId
    }
    val pendingIntent = PendingIntent.getActivity(
      context, 0, intent, PendingIntent.FLAG_IMMUTABLE
    )
    val builder = NotificationCompat.Builder(context, channelId)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle(title)
      .setContentText(message)
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setAutoCancel(true)
      .setFullScreenIntent(pendingIntent, true)

    if (type == NotifyType.ALARM) {
      builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
    }
    if (ActivityCompat.checkSelfPermission(
        context, android.Manifest.permission.POST_NOTIFICATIONS
      ) == PackageManager.PERMISSION_GRANTED
    ) {
      NotificationManagerCompat.from(context).notify(1, builder.build())
    }
  }
}