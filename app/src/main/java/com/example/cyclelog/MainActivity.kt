package com.example.cyclelog

import com.example.cyclelog.ui.MainView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import com.example.cyclelog.ui.LocalMainViewModel
import com.example.cyclelog.ui.theme.CycleLogTheme
import com.example.cyclelog.ui.viewModel.MainViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: MainViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {

      CompositionLocalProvider(
        LocalMainViewModel provides viewModel
      ) {
        CycleLogTheme {
          MainView()
        }
      }
    }
  }
}