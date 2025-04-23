package com.example.cyclelog.data

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.example.cyclelog.ui.viewModel.BluetoothViewModel
import java.io.IOException
import java.util.UUID

class BluetoothConnector(
  private val context: Context,
  private val bluetoothViewModel: BluetoothViewModel
) {
  private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

  fun connectToDevice() {
    if (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_CONNECT
      ) != PackageManager.PERMISSION_GRANTED ||
      ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_SCAN
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      ActivityCompat.requestPermissions(
        context as Activity,
        arrayOf(
          Manifest.permission.BLUETOOTH_CONNECT,
          Manifest.permission.BLUETOOTH_SCAN
        ), 200
      )
    } else {
      val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
      val bluetoothAdapter = bluetoothManager.adapter

      if (!bluetoothAdapter.isEnabled) {
        Handler(Looper.getMainLooper()).post {
          Toast.makeText(context, "藍牙已關閉", Toast.LENGTH_SHORT).show()
        }

        return
      }

      val device = bluetoothAdapter.bondedDevices.firstOrNull { it.name == "HC-01" }

      if (device == null) {
        Handler(Looper.getMainLooper()).post {
          Toast.makeText(context, "裝置連接失敗", Toast.LENGTH_SHORT).show()
        }

        return
      }

      Thread {
        try {
          val socket = device.createRfcommSocketToServiceRecord(uuid)
          socket.connect()

          Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "裝置連接成功", Toast.LENGTH_SHORT).show()
          }

          val inputStream = socket.inputStream
          val buffer = ByteArray(1024)
          var bytes: Int
          var readBufferPosition = 0
          val readBuffer = ByteArray(1024)

          while (true) {
            bytes = inputStream.read(buffer)

            for (i in 0 until bytes) {
              val b = buffer[i]

              if (readBufferPosition >= readBuffer.size) {
                readBufferPosition = 0
              }

              if (b == 13.toByte()) {
                val message = String(readBuffer, 0, readBufferPosition, Charsets.US_ASCII).drop(1)

                if (message.isNotEmpty() && message.first() == 'G') {
                  val distance = message.drop(1).dropLast(1).toIntOrNull()

                  if (distance != null) {
                    when (message.last()) {
                      'R' -> bluetoothViewModel.left = distance
                      'Y' -> bluetoothViewModel.right = distance
                    }
                  }
                  readBufferPosition = 0
                }
              } else {
                readBuffer[readBufferPosition++] = b
              }
            }
          }
        } catch (e: IOException) {
          Handler(Looper.getMainLooper()).post {
            Toast.makeText(context, "連接失敗", Toast.LENGTH_SHORT).show()
          }
        }
      }.start()
    }
  }
}