package com.example.cyclelog.utils

import android.content.Context
import android.media.Ringtone
import android.media.RingtoneManager

enum class SoundType {
  ALARM,
  NOTIF
}

class SoundPlayer(
  private val context: Context
) {
  private val ringtoneMap = mutableMapOf<SoundType, Ringtone>()

  fun play(type: SoundType) {
    val uri = when (type) {
      SoundType.ALARM -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      SoundType.NOTIF -> RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    }

    stop(type)

    val ringtone = RingtoneManager.getRingtone(context, uri)
    ringtone?.play()
    ringtoneMap[type] = ringtone
  }

  fun stop(type: SoundType) {
    ringtoneMap[type]?.stop()
    ringtoneMap.remove(type)
  }

  fun stopAll() {
    ringtoneMap.values.forEach { it.stop() }
    ringtoneMap.clear()
  }
}