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
import androidx.compose.material.icons.filled.Person
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
fun SignUpScreen(
  viewModel: AuthViewModel,
  onNavigateToLogin: () -> Unit,
  onSignUpSuccess: () -> Unit
) {
  val name by viewModel.name.collectAsState()
  val email by viewModel.email.collectAsState()
  val password by viewModel.password.collectAsState()
  val confirmPassword by viewModel.confirmPassword.collectAsState()

  val nameError by viewModel.nameError.collectAsState()
  val emailError by viewModel.emailError.collectAsState()
  val passwordError by viewModel.passwordError.collectAsState()
  val confirmPasswordError by viewModel.confirmPasswordError.collectAsState()
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
      .testTag("signup_screen")
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
        Spacer(modifier = Modifier.height(8.dp))

        // Small App Logo Header
        AppBrandingHeader(showSubTitle = false)

        Spacer(modifier = Modifier.height(4.dp))

        // Title and description
        Text(
          text = "Create Account",
          style = MaterialTheme.typography.displayMedium,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onBackground
        )

        Text(
          text = "Join Habibullah Life OS to elevate your studies and daily prayer journey.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Full Name Field
        HabibullahTextField(
          value = name,
          onValueChange = { viewModel.setName(it) },
          label = "Full Name",
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Person,
              contentDescription = "User Icon",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          isError = nameError != null,
          errorText = nameError,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next
          ),
          testTag = "input_signup_name"
        )

        // Email Field
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
          testTag = "input_signup_email"
        )

        // Password Field
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
            imeAction = ImeAction.Next
          ),
          testTag = "input_signup_password"
        )

        // Confirm Password Field
        HabibullahTextField(
          value = confirmPassword,
          onValueChange = { viewModel.setConfirmPassword(it) },
          label = "Confirm Password",
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = "Lock Icon",
              tint = MaterialTheme.colorScheme.primary
            )
          },
          isError = confirmPasswordError != null,
          errorText = confirmPasswordError,
          isPassword = true,
          keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done
          ),
          testTag = "input_signup_confirm_password"
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Primary Submit Button
        HabibullahButton(
          text = if (isLoading) "Creating account..." else "Sign Up",
          onClick = {
            viewModel.signUp {
              onSignUpSuccess()
            }
          },
          enabled = !isLoading,
          modifier = Modifier.fillMaxWidth(),
          testTag = "btn_submit_signup"
        )

        Spacer(modifier = Modifier.weight(1f))

        // Navigate to Login Footer
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Already have an account? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
          )
          Text(
            text = "Sign In",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
              .clickable { onNavigateToLogin() }
              .padding(4.dp)
              .testTag("link_go_to_login")
          )
        }
      }

      // Blocking Full-Screen Loading Overlay
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
                  text = "Creating account...",
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
