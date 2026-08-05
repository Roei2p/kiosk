package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.KioskMainScreen
import com.example.ui.theme.MedicalKioskTheme
import com.example.ui.viewmodel.KioskViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MedicalKioskTheme {
        val kioskViewModel: KioskViewModel = viewModel()
        KioskMainScreen(viewModel = kioskViewModel)
      }
    }
  }
}
