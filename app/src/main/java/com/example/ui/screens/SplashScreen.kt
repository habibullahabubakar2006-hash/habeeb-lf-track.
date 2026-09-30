package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.HabeebLogo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onNavigateToWelcome: () -> Unit
) {
  // Animation state for fading in logo and text
  val animateAlpha = remember { Animatable(0f) }
  val animateScale = remember { Animatable(0.8f) }

  LaunchedEffect(key1 = true) {
    // Staggered reveal animations
    animateAlpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
    )
  }

  LaunchedEffect(key1 = true) {
    animateScale.animateTo(
      targetValue = 1f,
      animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
      )
    )
  }

  // 3-second delay then navigate to welcome screen
  LaunchedEffect(key1 = true) {
    delay(3000)
    onNavigateToWelcome()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .testTag("splash_screen")
  ) {
    // 1. Beautiful Islamic geometric background
    Image(
      painter = painterResource(id = R.drawable.img_splash_bg_1784062882034),
      contentDescription = "Islamic Geometric Background",
      modifier = Modifier.fillMaxSize(),
      contentScale = ContentScale.Crop
    )

    // Dark-green aesthetic overlay gradient for premium atmospheric depth & text legibility
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color(0xAA0A1E15),
              Color(0xDD040E0A)
            )
          )
        )
    )

    // 2. Centered Logo and Text Content
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
        .navigationBarsPadding()
        .statusBarsPadding(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      Spacer(modifier = Modifier.height(20.dp))

      // Middle Block: Logo and Branding
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .weight(1f)
          .wrapContentHeight(Alignment.CenterVertically)
          .alpha(animateAlpha.value)
          .scale(animateScale.value)
      ) {
        HabeebLogo(
          size = 140.dp,
          shapeRadius = 24.dp,
          showBorder = true,
          contentDescription = "HABEEB LF TRACK App Logo",
          modifier = Modifier.testTag("splash_logo")
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "HABEEB LF TRACK",
          style = MaterialTheme.typography.displayLarge,
          color = Color.White,
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "LEARN • FAITH • TRACK • SUCCEED",
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
          ),
          color = MaterialTheme.colorScheme.secondary,
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }

      // Bottom Block: Custom modern loading spinner & small footer
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
          .padding(bottom = 32.dp)
          .alpha(animateAlpha.value)
      ) {
        CircularProgressIndicator(
          color = MaterialTheme.colorScheme.secondary,
          strokeWidth = 3.dp,
          modifier = Modifier
            .size(36.dp)
            .testTag("splash_loading")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
          text = "Elevating Productivity & Faith",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.5f),
          letterSpacing = 1.5.sp
        )
      }
    }
  }
}
