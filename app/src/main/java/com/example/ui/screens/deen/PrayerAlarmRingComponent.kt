package com.example.ui.screens.deen

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PrayerAlarmScheduler
import com.example.data.PrayerTimeCalculator
import com.example.ui.model.PrayerTimeItem

@Composable
fun PrayerAlarmRingComponent(
  prayers: List<PrayerTimeItem>,
  onToggleAlarm: (Int) -> Unit
) {
  val context = LocalContext.current
  var masterAlarmEnabled by remember { mutableStateOf(true) }
  var reminderMinutes by remember { mutableIntStateOf(15) }
  val reminderOptions = listOf(5, 10, 15, 30)

  var soundOption by remember { mutableStateOf("Makkah Adhan") }
  val soundOptions = listOf("Makkah Adhan", "Madinah Adhan", "Al-Aqsa Adhan", "Gentle Chime")

  var showTestAlarmAlert by remember { mutableStateOf(false) }
  var isSoundPlaying by remember { mutableStateOf(false) }
  var hasNotificationPermission by remember { mutableStateOf(true) }

  // Dynamically calculate prayer times based on device's real timezone and clock
  val prayerSchedule = remember(prayers, masterAlarmEnabled) {
    PrayerTimeCalculator.calculateTodayPrayers(
      context = context,
      existingAlarms = prayers.associate { it.name to (it.isAlarmEnabled && masterAlarmEnabled) }
    )
  }

  // Active alarm check: ONLY true if master is ON and at least one prayer alarm is enabled
  val isAnyAlarmActive = remember(masterAlarmEnabled, prayers) {
    masterAlarmEnabled && prayers.any { it.isAlarmEnabled }
  }

  // Permission Launcher for POST_NOTIFICATIONS on Android 13+
  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
  ) { isGranted ->
    hasNotificationPermission = isGranted
  }

  // Ensure notification channel is initialized
  LaunchedEffect(Unit) {
    PrayerAlarmScheduler.createNotificationChannel(context)
  }

  // Schedule real alarms when settings or prayer items change
  LaunchedEffect(masterAlarmEnabled, reminderMinutes, soundOption, prayers) {
    if (masterAlarmEnabled) {
      prayers.forEachIndexed { idx, p ->
        if (p.isAlarmEnabled) {
          PrayerAlarmScheduler.schedulePrayerAlarm(
            context = context,
            prayerIndex = idx,
            prayerName = p.name,
            prayerTime = p.time,
            reminderMinutes = reminderMinutes,
            soundOption = soundOption
          )
        } else {
          PrayerAlarmScheduler.cancelPrayerAlarm(context, idx)
        }
      }
    } else {
      prayers.forEachIndexed { idx, _ ->
        PrayerAlarmScheduler.cancelPrayerAlarm(context, idx)
      }
    }
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // -------------------------------------------------------------
    // 1. VISUAL PRAYER RING 🔔
    // -------------------------------------------------------------
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(22.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
      elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "PRAYER RING 🔔",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          )

          // "Alarm Active" badge shows ONLY when alarms are actually configured & active
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isAnyAlarmActive) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
          ) {
            Text(
              text = if (isAnyAlarmActive) "Alarm Active 🚨" else "Silent Mode 🔕",
              style = MaterialTheme.typography.labelSmall.copy(
                color = if (isAnyAlarmActive) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Circular Ring with Progress Indicator
        Box(
          modifier = Modifier.size(170.dp),
          contentAlignment = Alignment.Center
        ) {
          CircularProgressIndicator(
            progress = { prayerSchedule.progressFraction },
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.secondary,
            trackColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
            strokeWidth = 10.dp
          )

          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(16.dp)
          ) {
            Icon(
              imageVector = if (isAnyAlarmActive) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = prayerSchedule.nextPrayerName,
              style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
              )
            )
            Text(
              text = prayerSchedule.timeRemainingText,
              style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
              )
            )
            Text(
              text = prayerSchedule.nextPrayerTime,
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
          text = if (isAnyAlarmActive) {
            "Reminder: Next prayer alert set $reminderMinutes mins prior with $soundOption"
          } else {
            "All prayer alarms currently muted. Toggle switches below to activate."
          },
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
          textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        // TimeZone and Real Location indicator
        Text(
          text = "📍 Timezone: ${prayerSchedule.timeZoneDisplayName}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.65f),
          textAlign = TextAlign.Center
        )
      }
    }

    // -------------------------------------------------------------
    // 2. PRAYER ALARM CONTROL 🚨
    // -------------------------------------------------------------
    Card(
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
      Column(modifier = Modifier.padding(18.dp)) {
        // Master Alarm Toggle
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = if (masterAlarmEnabled) Icons.Default.Alarm else Icons.Default.AlarmOff,
              contentDescription = null,
              tint = if (masterAlarmEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
            Column {
              Text(
                text = "Master Prayer Alarm 🚨",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = if (masterAlarmEnabled) "Exact system alarm & audio alerts active" else "Alarms disabled (Silent Mode)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            }
          }

          Switch(
            checked = masterAlarmEnabled,
            onCheckedChange = {
              masterAlarmEnabled = it
              if (it && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
            },
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
          )
        }

        Spacer(modifier = Modifier.height(12.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))

        // Reminder Options: 5, 10, 15, 30 Minutes
        Text(
          text = "Reminder Timing (Minutes before Adhan)",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          reminderOptions.forEach { opt ->
            FilterChip(
              selected = reminderMinutes == opt,
              onClick = { reminderMinutes = opt },
              label = { Text("$opt mins") },
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Adhan Sound Selection
        Text(
          text = "Adhan Audio / Ring Tone",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          soundOptions.take(2).forEach { snd ->
            FilterChip(
              selected = soundOption == snd,
              onClick = { soundOption = snd },
              label = { Text(snd, fontSize = 11.sp) },
              modifier = Modifier.weight(1f)
            )
          }
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          soundOptions.drop(2).forEach { snd ->
            FilterChip(
              selected = soundOption == snd,
              onClick = { soundOption = snd },
              label = { Text(snd, fontSize = 11.sp) },
              modifier = Modifier.weight(1f)
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Test Alarm Button & Stop Button Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
              }
              PrayerAlarmScheduler.triggerPrayerAlert(
                context = context,
                prayerName = prayerSchedule.nextPrayerName,
                prayerTime = prayerSchedule.nextPrayerTime,
                reminderMinutes = reminderMinutes,
                soundOption = soundOption,
                isTest = true
              )
              isSoundPlaying = true
              showTestAlarmAlert = true
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Test Adhan Alarm 🚨", color = MaterialTheme.colorScheme.onSecondary, fontWeight = FontWeight.Bold)
          }

          if (isSoundPlaying) {
            FilledTonalButton(
              onClick = {
                PrayerAlarmScheduler.stopAlertSound()
                isSoundPlaying = false
              },
              shape = RoundedCornerShape(12.dp),
              colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
              Icon(Icons.Default.Stop, contentDescription = "Stop", tint = MaterialTheme.colorScheme.error)
              Spacer(modifier = Modifier.width(4.dp))
              Text("Stop ⏹️", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Individual Prayer Alarm Switches
    Text(
      text = "Individual Prayer Alarms",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )

    prayers.forEachIndexed { index, prayer ->
      val isThisAlarmActive = prayer.isAlarmEnabled && masterAlarmEnabled
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = if (isThisAlarmActive) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
              contentDescription = null,
              tint = if (isThisAlarmActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
            Column {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                  text = prayer.name,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (prayer.isCurrent) {
                  Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                  ) {
                    Text(
                      text = "NOW",
                      style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                      color = MaterialTheme.colorScheme.onSecondaryContainer,
                      modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                  }
                }
              }
              Text(
                text = "${prayer.time} • ${if (isThisAlarmActive) "Alarm set ($soundOption)" else "Silent"}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            }
          }

          Switch(
            checked = isThisAlarmActive,
            onCheckedChange = { onToggleAlarm(index) },
            enabled = masterAlarmEnabled,
            colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary)
          )
        }
      }
    }
  }

  // Test Alarm Dialog
  if (showTestAlarmAlert) {
    AlertDialog(
      onDismissRequest = {
        PrayerAlarmScheduler.stopAlertSound()
        isSoundPlaying = false
        showTestAlarmAlert = false
      },
      icon = {
        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(40.dp))
      },
      title = { Text("🚨 Adhan Alarm Sound Active") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Audio tone: \"$soundOption\" for upcoming ${prayerSchedule.nextPrayerName} prayer.")
          Text("Configured reminder: $reminderMinutes minutes prior to prayer time.")
          Text("🔊 Real audio playing through device speaker.")
          Text("✓ High-priority notification posted to system notification drawer.")
          Text(
            "Auto-silences in 30 seconds or tap 'Stop Alarm Sound' below.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            PrayerAlarmScheduler.stopAlertSound()
            isSoundPlaying = false
            showTestAlarmAlert = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Stop Alarm Sound ⏹️")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showTestAlarmAlert = false
          }
        ) {
          Text("Keep Playing in Background")
        }
      }
    )
  }
}
