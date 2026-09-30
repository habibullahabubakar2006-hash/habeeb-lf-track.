package com.example.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistent user session, settings, and profile data for HABEEB LF TRACK.
 */
class SessionManager(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("habeeb_lf_track_session", Context.MODE_PRIVATE)

  companion object {
    private const val KEY_IS_LOGGED_IN = "is_logged_in"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_THEME_MODE = "theme_mode" // "system", "light", "dark"
    private const val KEY_LANGUAGE = "selected_language" // "English", "Arabic", "Hausa"
    private const val KEY_NOTIF_PRAYER = "notif_prayer"
    private const val KEY_NOTIF_STUDY = "notif_study"
    private const val KEY_NOTIF_PLANNER = "notif_planner"
    private const val KEY_NOTIF_GENERAL = "notif_general"
  }

  var isLoggedIn: Boolean
    get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    set(value) = prefs.edit().putBoolean(KEY_IS_LOGGED_IN, value).apply()

  var userName: String
    get() = prefs.getString(KEY_USER_NAME, "") ?: ""
    set(value) = prefs.edit().putString(KEY_USER_NAME, value).apply()

  var userEmail: String
    get() = prefs.getString(KEY_USER_EMAIL, "") ?: ""
    set(value) = prefs.edit().putString(KEY_USER_EMAIL, value).apply()

  var themeMode: String
    get() = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
    set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()

  var selectedLanguage: String
    get() = prefs.getString(KEY_LANGUAGE, "English") ?: "English"
    set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

  var notifyPrayer: Boolean
    get() = prefs.getBoolean(KEY_NOTIF_PRAYER, true)
    set(value) = prefs.edit().putBoolean(KEY_NOTIF_PRAYER, value).apply()

  var notifyStudy: Boolean
    get() = prefs.getBoolean(KEY_NOTIF_STUDY, true)
    set(value) = prefs.edit().putBoolean(KEY_NOTIF_STUDY, value).apply()

  var notifyPlanner: Boolean
    get() = prefs.getBoolean(KEY_NOTIF_PLANNER, true)
    set(value) = prefs.edit().putBoolean(KEY_NOTIF_PLANNER, value).apply()

  var notifyGeneral: Boolean
    get() = prefs.getBoolean(KEY_NOTIF_GENERAL, true)
    set(value) = prefs.edit().putBoolean(KEY_NOTIF_GENERAL, value).apply()

  fun saveSession(name: String, email: String) {
    prefs.edit()
      .putBoolean(KEY_IS_LOGGED_IN, true)
      .putString(KEY_USER_NAME, name)
      .putString(KEY_USER_EMAIL, email)
      .apply()
  }

  fun clearSession() {
    prefs.edit()
      .putBoolean(KEY_IS_LOGGED_IN, false)
      .remove(KEY_USER_NAME)
      .remove(KEY_USER_EMAIL)
      .apply()
  }
}
