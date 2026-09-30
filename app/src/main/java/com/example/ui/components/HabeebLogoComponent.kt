package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Reusable, central HABEEB LF TRACK logo component.
 * Uses the exact approved square logo image (R.drawable.img_habeeb_lf_track_logo_1790681527347).
 * Avoids circular clipping or white boxes so it remains crisp and seamless in both light and dark mode.
 */
@Composable
fun HabeebLogo(
  modifier: Modifier = Modifier,
  size: Dp = 48.dp,
  shapeRadius: Dp = (size.value * 0.20f).dp,
  showBorder: Boolean = true,
  contentDescription: String = "HABEEB LF TRACK Logo"
) {
  Surface(
    modifier = modifier
      .size(size)
      .testTag("habeeb_app_logo"),
    shape = RoundedCornerShape(shapeRadius),
    color = Color(0xFF043428), // Matches the exact dark emerald background (#043428) of the logo asset; never shows a white box
    border = if (showBorder) {
      BorderStroke(
        width = 1.dp,
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
      )
    } else null
  ) {
    Image(
      painter = painterResource(id = R.drawable.img_habeeb_lf_track_logo_1790681527347),
      contentDescription = contentDescription,
      contentScale = ContentScale.Fit,
      modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(shapeRadius))
    )
  }
}

/**
 * Reusable branding header displaying application title and official slogan.
 * "HABEEB LF TRACK"
 * "LEARN • FAITH • TRACK • SUCCEED"
 */
@Composable
fun HabeebBrandingHeader(
  modifier: Modifier = Modifier,
  logoSize: Dp = 80.dp,
  showSlogan: Boolean = true,
  isCentered: Boolean = true
) {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
  val alignment = if (isCentered) Alignment.CenterHorizontally else Alignment.Start
  val textAlign = if (isCentered) TextAlign.Center else TextAlign.Start
  val titleColor = if (isDark) Color(0xFF6EE7B7) else MaterialTheme.colorScheme.primary
  val sloganColor = if (isDark) Color(0xFFFBBF24) else MaterialTheme.colorScheme.secondary

  Column(
    modifier = modifier.fillMaxWidth(),
    horizontalAlignment = alignment
  ) {
    HabeebLogo(
      size = logoSize,
      shapeRadius = (logoSize.value * 0.20f).dp,
      showBorder = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
      text = "HABEEB LF TRACK",
      style = MaterialTheme.typography.headlineMedium.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      ),
      color = titleColor,
      textAlign = textAlign
    )

    if (showSlogan) {
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "LEARN • FAITH • TRACK • SUCCEED",
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          letterSpacing = 1.2.sp
        ),
        color = sloganColor,
        textAlign = textAlign
      )
    }
  }
}
