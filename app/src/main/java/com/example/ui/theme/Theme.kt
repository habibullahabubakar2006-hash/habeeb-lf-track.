package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = EmeraldGreenDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F3E2B),
    onPrimaryContainer = MintGreenAccent,
    secondary = GoldAccentDark,
    onSecondary = Color(0xFF261D04),
    secondaryContainer = Color(0xFF3F3516),
    onSecondaryContainer = GoldAccentDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    error = SoftError,
    outline = CardBorderDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldGreenLight,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD0F1E3),
    onPrimaryContainer = Color(0xFF052B1E),
    secondary = GoldAccentLight,
    onSecondary = Color(0xFF261D04),
    secondaryContainer = Color(0xFFFEF4D5),
    onSecondaryContainer = Color(0xFF534212),
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    error = SoftError,
    outline = CardBorderLight
  )

@Composable
fun HabibullahLifeOSTheme(
  themeMode: String = "system",
  darkTheme: Boolean = when (themeMode) {
    "dark" -> true
    "light" -> false
    else -> isSystemInDarkTheme()
  },
  // Disable dynamic color by default to guarantee our pristine emerald/gold theme branding is preserved
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
