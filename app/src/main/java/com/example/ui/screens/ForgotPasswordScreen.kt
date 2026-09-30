package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
  viewModel: AuthViewModel,
  onNavigateBackToLogin: () -> Unit,
  onResetSentSuccess: () -> Unit
) {
  val email by viewModel.email.collectAsState()
  val emailError by viewModel.emailError.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()
  val resetMessage by viewModel.resetMessage.collectAsState()

  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(resetMessage) {
    resetMessage?.let {
      snackbarHostState.showSnackbar(it)
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(snackbarHostState) },
    modifier = Modifier
      .fillMaxSize()
      .testTag("forgot_password_screen"),
    topBar = {
      TopAppBar(
        title = { Text("Reset Password") },
        navigationIcon = {
          IconButton(
            onClick = onNavigateBackToLogin,
            modifier = Modifier.testTag("btn_back_to_login_topbar")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back icon"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background,
          titleContentColor = MaterialTheme.colorScheme.onBackground,
          navigationIconContentColor = MaterialTheme.colorScheme.primary
        )
      )
    }
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
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Large App Logo Branding
        AppBrandingHeader(showSubTitle = false)

        Spacer(modifier = Modifier.height(8.dp))

        // Title and instruction text
        Text(
          text = "Recover Password",
          style = MaterialTheme.typography.displayMedium,
          textAlign = TextAlign.Center,
          color = MaterialTheme.colorScheme.onBackground
        )

        Text(
          text = "Enter your email address below. We'll send you a password recovery link shortly.",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
          textAlign = TextAlign.Center,
          lineHeight = 24.sp,
          modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email address form input
        HabibullahTextField(
          value = email,
          onValueChange = { viewModel.setEmail(it) },
          label = "Registered Email",
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
            imeAction = ImeAction.Done
          ),
          testTag = "input_forgot_password_email"
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Primary Recovery Action button
        HabibullahButton(
          text = if (isLoading) "Sending instructions..." else "Send Recovery Link",
          onClick = {
            viewModel.resetPassword {
              onResetSentSuccess()
            }
          },
          enabled = !isLoading,
          modifier = Modifier.fillMaxWidth(),
          testTag = "btn_submit_forgot_password"
        )

        Spacer(modifier = Modifier.weight(1f))

        // Footer return link
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Suddenly remembered? ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
          )
          Text(
            text = "Back to Sign In",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(
              fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
              .clickable { onNavigateBackToLogin() }
              .padding(4.dp)
              .testTag("link_back_to_login_footer")
          )
        }
      }

      // Elegant visual block loading screen
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
                  text = "Sending instructions...",
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
