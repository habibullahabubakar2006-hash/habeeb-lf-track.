package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
  viewModel: AuthViewModel,
  onNavigateToSignUp: () -> Unit,
  onNavigateToForgotPassword: () -> Unit,
  onLoginSuccess: () -> Unit
) {
  val email by viewModel.email.collectAsState()
  val password by viewModel.password.collectAsState()
  
  val emailError by viewModel.emailError.collectAsState()
  val passwordError by viewModel.passwordError.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val authSuccessMessage by viewModel.authSuccessMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(authSuccessMessage) {
    authSuccessMessage?.let {
      snackbarHostState.showSnackbar(it)
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = Modifier
      .fillMaxSize()
      .testTag("login_screen")
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .statusBarsPadding()
        .navigationBarsPadding()
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(24.dp)
          .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Spacer(modifier = Modifier.height(12.dp))

        // App Logo & Header
        AppBrandingHeader(showSubTitle = false)

        Spacer(modifier = Modifier.height(12.dp))

        // Title text
        Text(
          text = "Welcome Back",
          style = MaterialTheme.typography.displayMedium,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onBackground
        )

        Text(
          text = "Sign in to access your Quran tracker, studies, and AI mentor.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Form Input
        HabibullahTextField(
          value = email,
          onValueChange = { viewModel.setEmail(it) },
          label = "Email Address",
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Email,
              contentDescription = "Email Icon",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          isError = emailError != null,
          errorText = emailError,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
          ),
          testTag = "input_login_email"
        )

        // Password Form Input
        HabibullahTextField(
          value = password,
          onValueChange = { viewModel.setPassword(it) },
          label = "Password",
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Lock Icon",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          isError = passwordError != null,
          errorText = passwordError,
          isPassword = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          testTag = "input_login_password"
        )

        // Forgot Password link align right
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          Text(
            text = "Forgot Password?",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelLarge.copy(
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
              .clickable { onNavigateToForgotPassword() }
              .padding(vertical = 4.dp, horizontal = 8.dp)
              .testTag("link_forgot_password")
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Primary Sign-In button
        HabibullahButton(
          text = if (isLoading) "Signing in..." else "Sign In",
          onClick = {
            viewModel.loginWithEmail {
              onLoginSuccess()
            }
          },
          enabled = !isLoading,
          modifier = Modifier.fillMaxWidth(),
          testTag = "btn_submit_login"
        )

        // "Or continue with" custom divider
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
          )
          Text(
            text = "OR CONTINUE WITH",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(horizontal = 16.dp)
          )
          HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
          )
        }

        // Google Sign-In Option
        GoogleSignInButton(
          onClick = {
            viewModel.loginWithGoogle {
              onLoginSuccess()
            }
          },
          testTag = "btn_google_login"
        )

        Spacer(modifier = Modifier.weight(1f))

        // Don't have an account footer navigation
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Don't have an account? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
          )
          Text(
            text = "Sign Up",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
              .clickable { onNavigateToSignUp() }
              .padding(4.dp)
              .testTag("link_go_to_signup")
          )
        }
      }

      // Elegant loading circular overlay blocking user interaction during mock calls
      if (isLoading) {
        Surface(
          color = MaterialTheme.colorScheme.background.copy(alpha = 0.7f),
          modifier = Modifier.fillMaxSize()
        ) {
          Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
          ) {
            Card(
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
              shape = RoundedCornerShape(16.dp),
              modifier = Modifier.padding(24.dp)
            ) {
              Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                CircularProgressIndicator(
                  color = MaterialTheme.colorScheme.primary,
                  strokeWidth = 3.dp,
                  modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = "Signing in...",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }
    }
  }
}
