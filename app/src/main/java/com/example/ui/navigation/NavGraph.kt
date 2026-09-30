package com.example.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ui.components.HabibullahButton
import com.example.ui.screens.*
import com.example.ui.viewmodel.AuthViewModel

// Route Identifiers
object Routes {
  const val SPLASH = "splash"
  const val WELCOME = "welcome"
  const val LOGIN = "login"
  const val SIGN_UP = "signup"
  const val FORGOT_PASSWORD = "forgot_password"
  const val AUTH_SUCCESS = "auth_success"
  const val HOME = "home"
}

@Composable
fun HabibullahNavGraph(
  navController: NavHostController = rememberNavController(),
  authViewModel: AuthViewModel = viewModel(),
  onThemeModeChanged: (String) -> Unit = {}
) {
  val isSessionActive by authViewModel.isSessionActive.collectAsState()

  NavHost(
    navController = navController,
    startDestination = Routes.SPLASH,
    modifier = Modifier.fillMaxSize()
  ) {
    // 1. Splash Screen Destination - Checks persistent session
    composable(Routes.SPLASH) {
      SplashScreen(
        onNavigateToWelcome = {
          if (isSessionActive) {
            navController.navigate(Routes.HOME) {
              popUpTo(Routes.SPLASH) { inclusive = true }
            }
          } else {
            navController.navigate(Routes.WELCOME) {
              popUpTo(Routes.SPLASH) { inclusive = true }
            }
          }
        }
      )
    }

    // 2. Welcome Onboarding Destination
    composable(Routes.WELCOME) {
      WelcomeScreen(
        onNavigateToSignUp = {
          authViewModel.clearAll()
          navController.navigate(Routes.SIGN_UP)
        },
        onNavigateToLogin = {
          authViewModel.clearAll()
          navController.navigate(Routes.LOGIN)
        }
      )
    }

    // 3. Login Destination - Automatically routes to Home Dashboard on success
    composable(Routes.LOGIN) {
      LoginScreen(
        viewModel = authViewModel,
        onNavigateToSignUp = {
          authViewModel.clearAll()
          navController.navigate(Routes.SIGN_UP) {
            popUpTo(Routes.WELCOME)
          }
        },
        onNavigateToForgotPassword = {
          authViewModel.clearAll()
          navController.navigate(Routes.FORGOT_PASSWORD)
        },
        onLoginSuccess = {
          navController.navigate(Routes.HOME) {
            popUpTo(Routes.WELCOME) { inclusive = true }
          }
        }
      )
    }

    // 4. Sign Up Destination - Automatically routes to Home Dashboard on success
    composable(Routes.SIGN_UP) {
      SignUpScreen(
        viewModel = authViewModel,
        onNavigateToLogin = {
          authViewModel.clearAll()
          navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.WELCOME)
          }
        },
        onSignUpSuccess = {
          navController.navigate(Routes.HOME) {
            popUpTo(Routes.WELCOME) { inclusive = true }
          }
        }
      )
    }

    // 5. Forgot Password Destination
    composable(Routes.FORGOT_PASSWORD) {
      ForgotPasswordScreen(
        viewModel = authViewModel,
        onNavigateBackToLogin = {
          authViewModel.clearAll()
          navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.LOGIN) { inclusive = true }
          }
        },
        onResetSentSuccess = {
          navController.navigate(Routes.LOGIN) {
            popUpTo(Routes.LOGIN) { inclusive = true }
          }
        }
      )
    }

    // 6. Main Home Dashboard Destination
    composable(Routes.HOME) {
      HomeScreen(
        authViewModel = authViewModel,
        onThemeModeChanged = onThemeModeChanged,
        onSignOut = {
          authViewModel.signOut()
          navController.navigate(Routes.WELCOME) {
            popUpTo(Routes.HOME) { inclusive = true }
          }
        }
      )
    }

    // 7. Auth Success Fallback (Automatically forwards to Home Dashboard)
    composable(Routes.AUTH_SUCCESS) {
      LaunchedEffect(Unit) {
        navController.navigate(Routes.HOME) {
          popUpTo(Routes.AUTH_SUCCESS) { inclusive = true }
        }
      }
      AuthSuccessPlaceholderScreen(
        authViewModel = authViewModel,
        onNavigateToHome = {
          navController.navigate(Routes.HOME) {
            popUpTo(Routes.AUTH_SUCCESS) { inclusive = true }
          }
        },
        onSignOut = {
          authViewModel.signOut()
          navController.navigate(Routes.WELCOME) {
            popUpTo(Routes.AUTH_SUCCESS) { inclusive = true }
          }
        }
      )
    }
  }
}

@Composable
fun AuthSuccessPlaceholderScreen(
  authViewModel: AuthViewModel,
  onNavigateToHome: () -> Unit,
  onSignOut: () -> Unit
) {
  val email by authViewModel.email.collectAsState()
  val name by authViewModel.name.collectAsState()

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp)
      .statusBarsPadding()
      .navigationBarsPadding()
      .testTag("auth_success_screen"),
    contentAlignment = Alignment.Center
  ) {
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(),
      shape = RoundedCornerShape(24.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(32.dp)
          .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Box(
          modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success Icon",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(48.dp)
          )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
          text = "Authentication Success!",
          style = MaterialTheme.typography.displayMedium,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )

        val displayName = if (name.isNotEmpty()) name else "Habibullah"
        val displayEmail = if (email.isNotEmpty()) email else "habibullahabubakar2006@gmail.com"

        Text(
          text = "You are welcome, $displayName 👋\n($displayEmail)",
          style = MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          ),
          textAlign = TextAlign.Center
        )

        Text(
          text = "Redirecting to your Home Dashboard...",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        HabibullahButton(
          text = "Go to Dashboard",
          onClick = onNavigateToHome,
          modifier = Modifier.fillMaxWidth(),
          testTag = "btn_auth_success_go_home"
        )
      }
    }
  }
}
