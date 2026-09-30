package com.example.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver triggered by Android AlarmManager for prayer times.
 */
class PrayerAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return
    val prayerName = intent.getStringExtra("prayer_name") ?: "Prayer"
    val prayerTime = intent.getStringExtra("prayer_time") ?: ""
    val reminderMinutes = intent.getIntExtra("reminder_minutes", 15)
    val soundOption = intent.getStringExtra("sound_option") ?: "Makkah Adhan"

    Log.i("PrayerAlarmReceiver", "Alarm received for $prayerName at $prayerTime")
    PrayerAlarmScheduler.triggerPrayerAlert(
      context = context,
      prayerName = prayerName,
      prayerTime = prayerTime,
      reminderMinutes = reminderMinutes,
      soundOption = soundOption,
      isTest = false
    )
  }
}
