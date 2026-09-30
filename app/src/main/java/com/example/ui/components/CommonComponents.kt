package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R

/**
 * Modern, branded button for Habibullah Life OS.
 */
@Composable
fun HabibullahButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  testTag: String = ""
) {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
  val buttonBg = if (isDark) Color(0xFF14B47C) else MaterialTheme.colorScheme.primary

  Button(
    onClick = onClick,
    enabled = enabled,
    colors = ButtonDefaults.buttonColors(
      containerColor = buttonBg,
      contentColor = Color.White,
      disabledContainerColor = buttonBg.copy(alpha = 0.5f),
      disabledContentColor = Color.White.copy(alpha = 0.6f)
    ),
    shape = RoundedCornerShape(16.dp),
    modifier = modifier
      .heightIn(min = 52.dp)
      .minimumInteractiveComponentSize()
      .testTag(testTag),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
  ) {
    Text(
      text = text,
      color = Color.White,
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    )
  }
}

/**
 * Modern secondary outlined button with custom Gold / Emerald styling.
 */
@Composable
fun HabibullahOutlineButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
  testTag: String = ""
) {
  val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
  val outlineColor = if (isDark) Color(0xFF55D8A3) else MaterialTheme.colorScheme.primary
  val textColor = if (isDark) Color.White else MaterialTheme.colorScheme.primary

  OutlinedButton(
    onClick = onClick,
    enabled = enabled,
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = if (isDark) Color(0xFF16201C) else Color.Transparent,
      contentColor = textColor
    ),
    border = BorderStroke(1.5.dp, outlineColor),
    shape = RoundedCornerShape(16.dp),
    modifier = modifier
      .heightIn(min = 52.dp)
      .minimumInteractiveComponentSize()
      .testTag(testTag),
    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp)
  ) {
    Text(
      text = text,
      color = textColor,
      style = MaterialTheme.typography.labelLarge.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
      )
    )
  }
}

/**
 * Styled text field supporting customized styling, validation feedback, and trailing icons.
 */
@Composable
fun HabibullahTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  modifier: Modifier = Modifier,
  leadingIcon: @Composable (() -> Unit)? = null,
  isError: Boolean = false,
  errorText: String? = null,
  isPassword: Boolean = false,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
  testTag: String = ""
) {
  var passwordVisible by remember { mutableStateOf(!isPassword) }

  Column(modifier = modifier.fillMaxWidth()) {
    OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      leadingIcon = leadingIcon,
      trailingIcon = if (isPassword) {
        {
          val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
          IconButton(onClick = { passwordVisible = !passwordVisible }) {
            Icon(
              imageVector = icon,
              contentDescription = if (passwordVisible) "Hide password" else "Show password",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      } else null,
      isError = isError,
      visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
      keyboardOptions = keyboardOptions,
      shape = RoundedCornerShape(12.dp),
      colors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = MaterialTheme.colorScheme.primary,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        errorBorderColor = MaterialTheme.colorScheme.error,
        errorLabelColor = MaterialTheme.colorScheme.error
      ),
      modifier = Modifier
        .fillMaxWidth()
        .heightIn(min = 56.dp)
        .testTag(testTag)
    )

    if (isError && !errorText.isNullOrEmpty()) {
      Text(
        text = errorText,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.labelSmall,
        modifier = Modifier
          .padding(start = 8.dp, top = 4.dp)
          .testTag("${testTag}_error")
      )
    }
  }
}

/**
 * Modern, branded social sign-in button for Google.
 */
@Composable
fun GoogleSignInButton(
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  text: String = "Continue with Google",
  testTag: String = ""
) {
  OutlinedButton(
    onClick = onClick,
    shape = RoundedCornerShape(16.dp),
    colors = ButtonDefaults.outlinedButtonColors(
      containerColor = Color.Transparent,
      contentColor = MaterialTheme.colorScheme.onBackground
    ),
    border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
      width = 1.dp
    ),
    modifier = modifier
      .fillMaxWidth()
      .heightIn(min = 52.dp)
      .minimumInteractiveComponentSize()
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      // Re-create simple Google G logo shape with canvas / colors,
      // or show a neat placeholder icon so it builds immediately
      Icon(
        painter = painterResource(id = R.drawable.ic_launcher_foreground), // fallback safely
        contentDescription = "Google Icon",
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier
          .size(24.dp)
          .padding(end = 8.dp)
      )
      Text(
        text = text,
        style = MaterialTheme.typography.labelLarge.copy(
          fontWeight = FontWeight.SemiBold
        )
      )
    }
  }
}

/**
 * Reusable branding header displaying application title and official slogan.
 */
@Composable
fun AppBrandingHeader(
  modifier: Modifier = Modifier,
  showSubTitle: Boolean = true
) {
  HabeebBrandingHeader(
    modifier = modifier,
    logoSize = 88.dp,
    showSlogan = showSubTitle,
    isCentered = true
  )
}
