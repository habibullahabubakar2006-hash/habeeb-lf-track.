package com.example.ui.viewmodel

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication state management with persistent login sessions.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

  private val sessionManager = SessionManager(application)

  // Form State Flows
  private val _name = MutableStateFlow(sessionManager.userName)
  val name: StateFlow<String> = _name.asStateFlow()

  private val _email = MutableStateFlow(sessionManager.userEmail)
  val email: StateFlow<String> = _email.asStateFlow()

  private val _password = MutableStateFlow("")
  val password: StateFlow<String> = _password.asStateFlow()

  private val _confirmPassword = MutableStateFlow("")
  val confirmPassword: StateFlow<String> = _confirmPassword.asStateFlow()

  private val _isSessionActive = MutableStateFlow(sessionManager.isLoggedIn)
  val isSessionActive: StateFlow<Boolean> = _isSessionActive.asStateFlow()

  // Error States
  private val _nameError = MutableStateFlow<String?>(null)
  val nameError: StateFlow<String?> = _nameError.asStateFlow()

  private val _emailError = MutableStateFlow<String?>(null)
  val emailError: StateFlow<String?> = _emailError.asStateFlow()

  private val _passwordError = MutableStateFlow<String?>(null)
  val passwordError: StateFlow<String?> = _passwordError.asStateFlow()

  private val _confirmPasswordError = MutableStateFlow<String?>(null)
  val confirmPasswordError: StateFlow<String?> = _confirmPasswordError.asStateFlow()

  // Loading & Event States
  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _authSuccessMessage = MutableStateFlow<String?>(null)
  val authSuccessMessage: StateFlow<String?> = _authSuccessMessage.asStateFlow()

  private val _resetMessage = MutableStateFlow<String?>(null)
  val resetMessage: StateFlow<String?> = _resetMessage.asStateFlow()

  fun setName(value: String) {
    _name.value = value
    if (_nameError.value != null) validateName()
  }

  fun setEmail(value: String) {
    _email.value = value
    if (_emailError.value != null) validateEmail()
  }

  fun setPassword(value: String) {
    _password.value = value
    if (_passwordError.value != null) validatePassword()
  }

  fun setConfirmPassword(value: String) {
    _confirmPassword.value = value
    if (_confirmPasswordError.value != null) validateConfirmPassword()
  }

  fun clearAll() {
    _password.value = ""
    _confirmPassword.value = ""
    _nameError.value = null
    _emailError.value = null
    _passwordError.value = null
    _confirmPasswordError.value = null
    _isLoading.value = false
    _authSuccessMessage.value = null
    _resetMessage.value = null
  }

  fun signOut() {
    sessionManager.clearSession()
    _name.value = ""
    _email.value = ""
    _password.value = ""
    _confirmPassword.value = ""
    _isSessionActive.value = false
    clearAll()
  }

  fun validateName(): Boolean {
    val currentName = _name.value.trim()
    return if (currentName.isEmpty()) {
      _nameError.value = "Full name is required"
      false
    } else {
      _nameError.value = null
      true
    }
  }

  fun validateEmail(): Boolean {
    val currentEmail = _email.value.trim()
    return when {
      currentEmail.isEmpty() -> {
        _emailError.value = "Email address is required"
        false
      }
      !Patterns.EMAIL_ADDRESS.matcher(currentEmail).matches() -> {
        _emailError.value = "Please enter a valid email address"
        false
      }
      else -> {
        _emailError.value = null
        true
      }
    }
  }

  fun validatePassword(): Boolean {
    val currentPassword = _password.value
    return when {
      currentPassword.isEmpty() -> {
        _passwordError.value = "Password is required"
        false
      }
      currentPassword.length < 6 -> {
        _passwordError.value = "Password must be at least 6 characters"
        false
      }
      else -> {
        _passwordError.value = null
        true
      }
    }
  }

  fun validateConfirmPassword(): Boolean {
    val currentConfirm = _confirmPassword.value
    val currentPass = _password.value
    return when {
      currentConfirm.isEmpty() -> {
        _confirmPasswordError.value = "Please confirm your password"
        false
      }
      currentConfirm != currentPass -> {
        _confirmPasswordError.value = "Passwords do not match"
        false
      }
      else -> {
        _confirmPasswordError.value = null
        true
      }
    }
  }

  fun loginWithEmail(onSuccess: () -> Unit) {
    val isEmailValid = validateEmail()
    val isPasswordValid = validatePassword()

    if (isEmailValid && isPasswordValid) {
      viewModelScope.launch {
        _isLoading.value = true
        _authSuccessMessage.value = null
        delay(1200)
        _isLoading.value = false
        if (_name.value.isEmpty()) {
          val inferred = _email.value.substringBefore("@").replace(".", " ")
            .split(" ")
            .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
          _name.value = if (inferred.isNotBlank()) inferred else "Habibullah"
        }
        sessionManager.saveSession(_name.value, _email.value)
        _isSessionActive.value = true
        _authSuccessMessage.value = "Logged in successfully as ${_email.value}"
        onSuccess()
      }
    }
  }

  fun signUp(onSuccess: () -> Unit) {
    val isNameValid = validateName()
    val isEmailValid = validateEmail()
    val isPasswordValid = validatePassword()
    val isConfirmValid = validateConfirmPassword()

    if (isNameValid && isEmailValid && isPasswordValid && isConfirmValid) {
      viewModelScope.launch {
        _isLoading.value = true
        _authSuccessMessage.value = null
        delay(1200)
        _isLoading.value = false
        sessionManager.saveSession(_name.value, _email.value)
        _isSessionActive.value = true
        _authSuccessMessage.value = "Account created successfully for ${_name.value}"
        onSuccess()
      }
    }
  }

  fun loginWithGoogle(onSuccess: () -> Unit) {
    viewModelScope.launch {
      _isLoading.value = true
      delay(1000)
      _isLoading.value = false
      if (_name.value.isEmpty()) {
        _name.value = "Habibullah Abubakar"
      }
      if (_email.value.isEmpty()) {
        _email.value = "habibullahabubakar2006@gmail.com"
      }
      sessionManager.saveSession(_name.value, _email.value)
      _isSessionActive.value = true
      _authSuccessMessage.value = "Logged in successfully via Google"
      onSuccess()
    }
  }

  fun resetPassword(onSuccess: () -> Unit) {
    if (validateEmail()) {
      viewModelScope.launch {
        _isLoading.value = true
        _resetMessage.value = null
        delay(1200)
        _isLoading.value = false
        _resetMessage.value = "Password reset link has been sent to ${_email.value}"
        onSuccess()
      }
    }
  }
}
