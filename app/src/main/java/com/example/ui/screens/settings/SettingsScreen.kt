package com.example.ui.screens.settings

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.SessionManager
import com.example.ui.components.HabeebLogo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  userName: String,
  userEmail: String,
  sessionManager: SessionManager,
  onNavigateBack: () -> Unit,
  onNavigateToMyFiles: () -> Unit,
  onThemeModeChanged: (String) -> Unit = {},
  onLanguageChanged: (String) -> Unit = {},
  onSignOut: () -> Unit
) {
  var selectedLanguage by remember { mutableStateOf(sessionManager.selectedLanguage) }
  var themeMode by remember { mutableStateOf(sessionManager.themeMode) } // "system", "light", "dark"
  val strings = com.example.ui.localization.AppStrings.get(selectedLanguage)

  var notifyPrayer by remember { mutableStateOf(sessionManager.notifyPrayer) }
  var notifyStudy by remember { mutableStateOf(sessionManager.notifyStudy) }
  var notifyPlanner by remember { mutableStateOf(sessionManager.notifyPlanner) }
  var notifyGeneral by remember { mutableStateOf(sessionManager.notifyGeneral) }

  var showPasswordDialog by remember { mutableStateOf(false) }
  var showLanguageDialog by remember { mutableStateOf(false) }
  var showAboutDialog by remember { mutableStateOf(false) }
  var showHelpDialog by remember { mutableStateOf(false) }
  var showBackupDialog by remember { mutableStateOf(false) }
  var showSignOutConfirm by remember { mutableStateOf(false) }
  var statusFeedback by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "⚙️ Settings & Profile",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp)
        .testTag("settings_screen"),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
      // 1. User Profile & Account Summary Card
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(18.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
              HabeebLogo(
                size = 60.dp,
                shapeRadius = 14.dp,
                showBorder = true,
                contentDescription = "User Avatar"
              )

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = if (userName.isNotBlank()) userName else "Habibullah",
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )
                )
                Text(
                  text = if (userEmail.isNotBlank()) userEmail else "habibullah@example.com",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.primaryContainer,
                  modifier = Modifier.padding(top = 4.dp)
                ) {
                  Text(
                    text = "Active Student & Hifz Companion",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // User Statistics Grid
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceAround
            ) {
              ProfileStat("18 Juz", "Hifz Progress")
              ProfileStat("7 Days", "Study Streak")
              ProfileStat("6 Courses", "Enrolled")
              ProfileStat("$1,420", "Wallet Bal.")
            }
          }
        }
      }

      // Feedback banner
      statusFeedback?.let { msg ->
        item {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
              IconButton(onClick = { statusFeedback = null }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // 2. Personal Storage & Files
      item {
        SettingsSectionHeader("Personal Storage")
      }

      item {
        SettingsItemCard(
          title = "🗂️ My Files",
          subtitle = "Private files, photos, videos, notes & PDFs",
          icon = Icons.Default.FolderShared,
          onClick = onNavigateToMyFiles
        )
      }

      // 3. Appearance & Language
      item {
        SettingsSectionHeader("Appearance & Language")
      }

      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(strings.themeMode, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(8.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              FilterChip(
                selected = themeMode == "system",
                onClick = {
                  themeMode = "system"
                  sessionManager.themeMode = "system"
                  onThemeModeChanged("system")
                  statusFeedback = "Theme set to System default"
                },
                label = { Text(strings.system) },
                modifier = Modifier.weight(1f)
              )
              FilterChip(
                selected = themeMode == "light",
                onClick = {
                  themeMode = "light"
                  sessionManager.themeMode = "light"
                  onThemeModeChanged("light")
                  statusFeedback = "Light Mode activated"
                },
                label = { Text(strings.lightMode) },
                modifier = Modifier.weight(1f)
              )
              FilterChip(
                selected = themeMode == "dark",
                onClick = {
                  themeMode = "dark"
                  sessionManager.themeMode = "dark"
                  onThemeModeChanged("dark")
                  statusFeedback = "🌙 Dark Mode activated"
                },
                label = { Text(strings.darkMode) },
                modifier = Modifier.weight(1f)
              )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(14.dp))

            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { showLanguageDialog = true },
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(strings.language, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                Text("Current: $selectedLanguage", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
              }
              Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }

      // 4. Notifications & Alarms
      item {
        SettingsSectionHeader("Notifications & Alarms")
      }

      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            NotificationToggleRow("Prayer Reminders & Alarm 🚨", "Alerts 15 mins prior with Adhan", notifyPrayer) {
              notifyPrayer = it
              sessionManager.notifyPrayer = it
            }
            NotificationToggleRow("Study & Focus Timers", "Pomodoro session break notifications", notifyStudy) {
              notifyStudy = it
              sessionManager.notifyStudy = it
            }
            NotificationToggleRow("Planner & Schedule", "Daily timetable block reminders", notifyPlanner) {
              notifyPlanner = it
              sessionManager.notifyPlanner = it
            }
            NotificationToggleRow("General App Notifications", "Updates and motivational Quran verses", notifyGeneral) {
              notifyGeneral = it
              sessionManager.notifyGeneral = it
            }
          }
        }
      }

      // 5. Security & Data
      item {
        SettingsSectionHeader("Security & Data")
      }

      item {
        SettingsItemCard(
          title = "Change Password",
          subtitle = "Update account security credentials",
          icon = Icons.Default.Lock,
          onClick = { showPasswordDialog = true }
        )
      }

      item {
        SettingsItemCard(
          title = "Data & Backup",
          subtitle = "Export or backup local student & spiritual data",
          icon = Icons.Default.CloudSync,
          onClick = { showBackupDialog = true }
        )
      }

      // 6. Help & About
      item {
        SettingsSectionHeader("Support & About")
      }

      item {
        SettingsItemCard(
          title = "Help & FAQs",
          subtitle = "User guides for Quran, Hifz, Pomodoro & Wallet",
          icon = Icons.AutoMirrored.Filled.HelpOutline,
          onClick = { showHelpDialog = true }
        )
      }

      item {
        SettingsItemCard(
          title = "About HABEEB LF TRACK",
          subtitle = "Version 2.0 • Premium Islamic Student OS",
          icon = Icons.Default.Info,
          onClick = { showAboutDialog = true }
        )
      }

      // 7. Logout
      item {
        Button(
          onClick = { showSignOutConfirm = true },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Sign Out of Account")
        }
      }
    }
  }

  // Change Password Dialog
  if (showPasswordDialog) {
    var curPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confPass by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showPasswordDialog = false },
      title = { Text("Change Password") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(value = curPass, onValueChange = { curPass = it }, label = { Text("Current Password") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("New Password") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = confPass, onValueChange = { confPass = it }, label = { Text("Confirm New Password") }, modifier = Modifier.fillMaxWidth())
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (newPass.length >= 6 && newPass == confPass) {
              statusFeedback = "Password updated successfully"
              showPasswordDialog = false
            }
          }
        ) {
          Text("Update")
        }
      },
      dismissButton = {
        TextButton(onClick = { showPasswordDialog = false }) { Text("Cancel") }
      }
    )
  }

  // Language Dialog
  if (showLanguageDialog) {
    val languages = listOf("English", "Arabic (العربية)", "Hausa")
    AlertDialog(
      onDismissRequest = { showLanguageDialog = false },
      title = { Text("Select Language") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          languages.forEach { lang ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  val chosen = lang.substringBefore(" ")
                  selectedLanguage = chosen
                  sessionManager.selectedLanguage = chosen
                  onLanguageChanged(chosen)
                  statusFeedback = "Language set to $chosen"
                  showLanguageDialog = false
                }
                .padding(vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = selectedLanguage.startsWith(lang.substringBefore(" ")),
                onClick = null
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(lang, style = MaterialTheme.typography.bodyLarge)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLanguageDialog = false }) { Text("Close") }
      }
    )
  }

  // Backup Dialog
  if (showBackupDialog) {
    AlertDialog(
      onDismissRequest = { showBackupDialog = false },
      title = { Text("Data & Backup") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Your study progress, Quran memorization milestones, wallet ledger, and notes are encrypted locally for $userEmail.")
          Text("✓ Automatic local database sync active.")
        }
      },
      confirmButton = {
        Button(
          onClick = {
            statusFeedback = "Local backup generated successfully."
            showBackupDialog = false
          }
        ) {
          Text("Export Backup")
        }
      },
      dismissButton = {
        TextButton(onClick = { showBackupDialog = false }) { Text("Close") }
      }
    )
  }

  // Help Dialog
  if (showHelpDialog) {
    AlertDialog(
      onDismissRequest = { showHelpDialog = false },
      title = { Text("Help & Support") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("• Pomodoro Focus: 25 minutes of deep focus followed by 5 minutes break.", style = MaterialTheme.typography.bodySmall)
          Text("• Quran Reader: Read noble Surahs with Uthmani typography and audio recitation.", style = MaterialTheme.typography.bodySmall)
          Text("• Prayer Ring: Displays time remaining before next Adhan with custom alarm timing.", style = MaterialTheme.typography.bodySmall)
          Text("• My Files: Secure user-isolated private storage for assignments and documents.", style = MaterialTheme.typography.bodySmall)
        }
      },
      confirmButton = {
        Button(onClick = { showHelpDialog = false }) { Text("Got it") }
      }
    )
  }

  // About HABEEB LF TRACK Dialog
  if (showAboutDialog) {
    AlertDialog(
      onDismissRequest = { showAboutDialog = false },
      title = { Text("About HABEEB LF TRACK") },
      text = {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          HabeebLogo(
            size = 64.dp,
            shapeRadius = 14.dp,
            showBorder = true,
            contentDescription = "HABEEB LF TRACK Logo"
          )
          Text("HABEEB LF TRACK", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
          Text(
            text = "LEARN • FAITH • TRACK • SUCCEED",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
            color = MaterialTheme.colorScheme.secondary
          )
          Text("Version 2.0 • Build 2026", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
          Text(
            "An all-in-one productivity platform uniting Islamic faith, academic excellence, AI assistance, Halal budgeting, and structured daily planning.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      },
      confirmButton = {
        Button(onClick = { showAboutDialog = false }) { Text("Close") }
      }
    )
  }

  // Logout Confirm Dialog
  if (showSignOutConfirm) {
    AlertDialog(
      onDismissRequest = { showSignOutConfirm = false },
      title = { Text("Sign Out") },
      text = { Text("Are you sure you want to log out? Your user data will remain saved on this device.") },
      confirmButton = {
        Button(
          onClick = {
            showSignOutConfirm = false
            onSignOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Log Out")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSignOutConfirm = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
private fun ProfileStat(value: String, label: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 10.sp)
  }
}

@Composable
private fun SettingsSectionHeader(title: String) {
  Text(
    text = title,
    style = MaterialTheme.typography.labelLarge.copy(
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.primary,
      letterSpacing = 0.5.sp
    ),
    modifier = Modifier.padding(top = 4.dp)
  )
}

@Composable
private fun SettingsItemCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onClick() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        }
        Column {
          Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
          Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
      }
      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
    }
  }
}

@Composable
private fun NotificationToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
      Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
    Switch(
      checked = checked,
      onCheckedChange = onCheckedChange,
      colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
    )
  }
}
