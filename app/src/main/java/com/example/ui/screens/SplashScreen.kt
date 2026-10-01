package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.HabeebLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onNavigateToWelcome: () -> Unit
) {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

  // Animation states for smooth reveal
  val animateAlpha = remember { Animatable(0f) }
  val animateScale = remember { Animatable(0.85f) }

  LaunchedEffect(Unit) {
    animateAlpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(Unit) {
    animateScale.animateTo(
      targetValue = 1f,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMediumLow
      )
    )
  }

  // Remove splash screen as soon as loading completes
  LaunchedEffect(Unit) {
    delay(1200)
    onNavigateToWelcome()
  }

  // Theme-adaptive colors for crystal clear readability in both light & dark mode
  val backgroundColor = if (isDark) Color(0xFF0A120E) else Color(0xFFF4F9F6)
  val titleColor = if (isDark) Color.White else Color(0xFF0F8A5F)
  val sloganColor = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
  val subtextColor = if (isDark) Color(0xFF9CA3AF) else Color(0xFF4B5563)
  val indicatorColor = if (isDark) Color(0xFF10B981) else Color(0xFF0F8A5F)

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        if (isDark) {
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFF0F261C),
              Color(0xFF06140E)
            )
          )
        } else {
          Brush.verticalGradient(
            colors = listOf(
              Color(0xFFFFFFFF),
              Color(0xFFEDF7F2)
            )
          )
        }
      )
      .systemBarsPadding()
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Centered Approved Square Logo
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .alpha(animateAlpha.value)
          .scale(animateScale.value)
      ) {
        HabeebLogo(
          size = 110.dp,
          shapeRadius = 22.dp,
          showBorder = true,
          contentDescription = "HABEEB LF TRACK App Logo",
          modifier = Modifier.testTag("splash_logo")
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Text "HABEEB LF TRACK"
        Text(
          text = "HABEEB LF TRACK",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          ),
          color = titleColor,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Slogan "LEARN • FAITH • TRACK • SUCCEED"
        Text(
          text = "LEARN • FAITH • TRACK • SUCCEED",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.2.sp
          ),
          color = sloganColor,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Circular progress indicator while loading
        CircularProgressIndicator(
          color = indicatorColor,
          strokeWidth = 3.dp,
          modifier = Modifier
            .size(32.dp)
            .testTag("splash_loading")
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Loading your workspace...",
          style = MaterialTheme.typography.labelSmall,
          color = subtextColor,
          letterSpacing = 0.5.sp
        )
      }
    }
  }
}
