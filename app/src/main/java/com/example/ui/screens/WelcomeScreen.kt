package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.components.HabeebLogo
import com.example.ui.components.HabibullahButton
import com.example.ui.components.HabibullahOutlineButton

@Composable
fun WelcomeScreen(
  onNavigateToSignUp: () -> Unit,
  onNavigateToLogin: () -> Unit
) {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

  Column(
    modifier = Modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding()
      .padding(24.dp)
      .verticalScroll(rememberScrollState())
      .testTag("welcome_screen"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // 1. App Header/Branding using central reusable HabeebLogo
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      HabeebLogo(
        size = 38.dp,
        shapeRadius = 8.dp,
        showBorder = true
      )
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = "HABEEB LF TRACK",
          style = MaterialTheme.typography.titleLarge.copy(
            color = if (isDark) Color(0xFF55D8A3) else MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
          )
        )
        Text(
          text = "LEARN • FAITH • TRACK • SUCCEED",
          style = MaterialTheme.typography.labelSmall.copy(
            color = if (isDark) Color(0xFFE1BE4E) else MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp
          )
        )
      }
    }

    // 2. High-Fidelity Modern Illustration at the Top/Center
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
        .padding(vertical = 12.dp)
        .testTag("welcome_illustration_card"),
      shape = RoundedCornerShape(24.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    ) {
      Image(
        painter = painterResource(id = R.drawable.img_welcome_illus_1784062896351),
        contentDescription = "Faith and Study Illustration",
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
      )
    }

    // 3. Welcome title & core application description with high-contrast text in dark & light mode
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
    ) {
      Text(
        text = "Welcome to HABEEB LF TRACK",
        style = MaterialTheme.typography.displayMedium,
        textAlign = TextAlign.Center,
        color = if (isDark) Color(0xFFFFFFFF) else Color(0xFF101614)
      )

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "One app for Quran, Hifz, Muraja'ah, Prayer Times, Student Tools and AI Assistance.",
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = if (isDark) Color(0xFFE2EBE6) else Color(0xFF4A5550),
        lineHeight = 24.sp,
        modifier = Modifier.padding(horizontal = 8.dp)
      )
    }

    // 4. Large Action Buttons with high contrast in both dark and light modes
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      HabibullahButton(
        text = "Get Started",
        onClick = onNavigateToSignUp,
        modifier = Modifier.fillMaxWidth(),
        testTag = "btn_get_started"
      )

      HabibullahOutlineButton(
        text = "Sign In",
        onClick = onNavigateToLogin,
        modifier = Modifier.fillMaxWidth(),
        testTag = "btn_sign_in"
      )
    }
  }
}
