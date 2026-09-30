package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.SessionManager
import com.example.ui.navigation.HabibullahNavGraph
import com.example.ui.theme.HabibullahLifeOSTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val sessionManager = remember { SessionManager(this) }
      var currentThemeMode by remember { mutableStateOf(sessionManager.themeMode) }

      HabibullahLifeOSTheme(themeMode = currentThemeMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          HabibullahNavGraph(
            onThemeModeChanged = { newMode ->
              currentThemeMode = newMode
              sessionManager.themeMode = newMode
            }
          )
        }
      }
    }
  }
}
