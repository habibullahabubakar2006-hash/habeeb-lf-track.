package com.example.data

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.Calendar
import kotlin.math.sin

/**
 * Real Android Alarm and Notification Scheduler for HABEEB LF TRACK Prayer Times.
 * Produces real audible alarm sounds (synthesized chime audio + tone generator + adhan stream),
 * manages exact AlarmManager alarms, and enforces honest scheduling states.
 */
object PrayerAlarmScheduler {
  private const val TAG = "PrayerAlarmScheduler"
  const val CHANNEL_ID = "habeeb_prayer_alarm_channel"
  const val CHANNEL_NAME = "Prayer Times & Adhan Alarms"

  private var activeRingtone: Ringtone? = null
  private var activeToneGenerator: ToneGenerator? = null
  private var activeMediaPlayer: android.media.MediaPlayer? = null
  private var activeAudioTrack: AudioTrack? = null
  private var audioThread: Thread? = null

  @Volatile
  var isSoundPlaying: Boolean = false
    private set

  private val mainHandler = Handler(Looper.getMainLooper())
  private var autoStopRunnable: Runnable? = null

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

      val alarmSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_ALARM)
        .build()

      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "High-priority notifications and alerts for Daily Prayers and Adhan"
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 500, 200, 500, 200, 800)
        setSound(alarmSoundUri, audioAttributes)
        setShowBadge(true)
      }
      notificationManager.createNotificationChannel(channel)
    }
  }

  /**
   * Immediately plays an audible alert and posts a real Android notification.
   */
  fun triggerPrayerAlert(
    context: Context,
    prayerName: String,
    prayerTime: String,
    reminderMinutes: Int,
    soundOption: String,
    isTest: Boolean = false
  ) {
    createNotificationChannel(context)

    // 1. Play real audible sound
    playAlertSound(context)

    // 2. Build and post real system notification
    val notificationManager =
      context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

    val title = if (isTest) {
      "🚨 Prayer Alarm: $prayerName"
    } else {
      "🕌 Prayer Time: $prayerName ($prayerTime)"
    }

    val content = if (reminderMinutes > 0) {
      "$prayerName prayer approaches in $reminderMinutes mins ($prayerTime). Audio alert: $soundOption."
    } else {
      "It is now time for $prayerName prayer ($prayerTime). Audio alert: $soundOption."
    }

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
      .setContentTitle(title)
      .setContentText(content)
      .setStyle(NotificationCompat.BigTextStyle().bigText(content))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_ALARM)
      .setAutoCancel(true)
      .setVibrate(longArrayOf(0, 500, 200, 500, 200, 800))

    val notifId = prayerName.hashCode() + if (isTest) 9999 else 0
    try {
      notificationManager.notify(notifId, builder.build())
    } catch (e: SecurityException) {
      Log.w(TAG, "Notification permission missing: ${e.message}")
    }
  }

  /**
   * Plays a guaranteed audible alarm sound through the device speaker.
   * Generates PCM harmonic chime audio, triggers ToneGenerator, and attempts Adhan playback.
   */
  fun playAlertSound(context: Context) {
    try {
      stopAlertSound()
      isSoundPlaying = true

      // 1. High-reliability PCM synthesized melodic chime stream on STREAM_MUSIC
      val thread = Thread {
        try {
          val sampleRate = 22050
          val minBufferSize = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
          )
          val audioTrack = AudioTrack(
            AudioManager.STREAM_MUSIC,
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize.coerceAtLeast(sampleRate * 2),
            AudioTrack.MODE_STREAM
          )
          activeAudioTrack = audioTrack
          audioTrack.play()

          // Adhan musical notes sequence (Frequencies: A4 440Hz, C#5 554Hz, E5 659Hz, A5 880Hz)
          val notes = doubleArrayOf(440.0, 554.37, 659.25, 880.0, 659.25, 880.0)
          val noteDuration = 0.35 // seconds
          val numSamplesPerNote = (sampleRate * noteDuration).toInt()

          var loopCount = 0
          while (!Thread.currentThread().isInterrupted && isSoundPlaying && loopCount < 8) {
            for (freq in notes) {
              if (Thread.currentThread().isInterrupted || !isSoundPlaying) break
              val samples = ShortArray(numSamplesPerNote)
              for (i in 0 until numSamplesPerNote) {
                val t = i.toDouble() / sampleRate
                // Harmonic sine wave with natural envelope decay
                val envelope = 1.0 - (i.toDouble() / numSamplesPerNote) * 0.4
                val wave = (sin(2.0 * Math.PI * freq * t) + 0.3 * sin(4.0 * Math.PI * freq * t)) * envelope
                samples[i] = (wave * Short.MAX_VALUE * 0.85).toInt().toShort()
              }
              audioTrack.write(samples, 0, samples.size)
            }
            Thread.sleep(600)
            loopCount++
          }
        } catch (_: InterruptedException) {
        } catch (e: Exception) {
          Log.w(TAG, "AudioTrack chime generator error: ${e.message}")
        }
      }
      audioThread = thread
      thread.start()

      // 2. Play audible ToneGenerator sequence
      try {
        val tg = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
        activeToneGenerator = tg
        tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 1000)
      } catch (e: Exception) {
        Log.w(TAG, "ToneGenerator fallback: ${e.message}")
      }

      // 3. Play online Adhan audio stream via MediaPlayer if network is available
      try {
        val mp = android.media.MediaPlayer().apply {
          setAudioAttributes(
            AudioAttributes.Builder()
              .setUsage(AudioAttributes.USAGE_ALARM)
              .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
              .build()
          )
          setDataSource("https://cdn.islamic.network/adhan/makkah.mp3")
          setOnPreparedListener {
            if (isSoundPlaying) start()
          }
          setOnErrorListener { _, _, _ -> true }
        }
        activeMediaPlayer = mp
        mp.prepareAsync()
      } catch (e: Exception) {
        Log.w(TAG, "Adhan stream network fallback: ${e.message}")
      }

      // 4. System Ringtone as secondary alert
      try {
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
          ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (soundUri != null) {
          val ringtone = RingtoneManager.getRingtone(context, soundUri)
          if (ringtone != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
              ringtone.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            }
            activeRingtone = ringtone
            ringtone.play()
          }
        }
      } catch (e: Exception) {
        Log.w(TAG, "Ringtone playback fallback", e)
      }

      // Automatically shut off sound after 30 seconds
      autoStopRunnable?.let { mainHandler.removeCallbacks(it) }
      autoStopRunnable = Runnable {
        stopAlertSound()
      }
      mainHandler.postDelayed(autoStopRunnable!!, 30_000)
    } catch (e: Exception) {
      Log.e(TAG, "Error playing alert sound", e)
    }
  }

  /**
   * Stops currently playing alarm sound across all audio engines immediately.
   */
  fun stopAlertSound() {
    isSoundPlaying = false
    autoStopRunnable?.let {
      mainHandler.removeCallbacks(it)
      autoStopRunnable = null
    }

    try {
      audioThread?.interrupt()
      audioThread = null

      activeAudioTrack?.let {
        try {
          it.stop()
          it.release()
        } catch (_: Exception) {}
      }
      activeAudioTrack = null

      activeToneGenerator?.release()
      activeToneGenerator = null

      activeMediaPlayer?.let {
        try {
          if (it.isPlaying) it.stop()
          it.release()
        } catch (_: Exception) {}
      }
      activeMediaPlayer = null

      activeRingtone?.let {
        try {
          if (it.isPlaying) it.stop()
        } catch (_: Exception) {}
      }
      activeRingtone = null
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping alert sound", e)
    }
  }

  /**
   * Schedules an exact alarm with the Android AlarmManager.
   * Returns true if scheduling was successfully registered with AlarmManager.
   */
  fun schedulePrayerAlarm(
    context: Context,
    prayerIndex: Int,
    prayerName: String,
    prayerTime: String,
    reminderMinutes: Int,
    soundOption: String
  ): Boolean {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false

    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = "com.example.ACTION_PRAYER_ALARM"
      putExtra("prayer_name", prayerName)
      putExtra("prayer_time", prayerTime)
      putExtra("reminder_minutes", reminderMinutes)
      putExtra("sound_option", soundOption)
      putExtra("prayer_index", prayerIndex)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      1000 + prayerIndex,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTime = computeTriggerTimeMillis(prayerTime, reminderMinutes)

    return try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      } else {
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
      }
      recordScheduledState(context, prayerIndex, true)
      Log.i(TAG, "Registered exact alarm for $prayerName at $triggerTime")
      true
    } catch (e: SecurityException) {
      Log.w(TAG, "SCHEDULE_EXACT_ALARM permission not granted: ${e.message}")
      recordScheduledState(context, prayerIndex, false)
      false
    } catch (e: Exception) {
      Log.e(TAG, "Failed to schedule alarm: ${e.message}")
      recordScheduledState(context, prayerIndex, false)
      false
    }
  }

  /**
   * Cancels a scheduled prayer alarm.
   */
  fun cancelPrayerAlarm(context: Context, prayerIndex: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
    val intent = Intent(context, PrayerAlarmReceiver::class.java).apply {
      action = "com.example.ACTION_PRAYER_ALARM"
    }
    val pendingIntent = PendingIntent.getBroadcast(
      context,
      1000 + prayerIndex,
      intent,
      PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
    )
    if (pendingIntent != null) {
      alarmManager.cancel(pendingIntent)
    }
    recordScheduledState(context, prayerIndex, false)
  }

  private fun recordScheduledState(context: Context, prayerIndex: Int, isScheduled: Boolean) {
    val prefs = context.getSharedPreferences("habeeb_alarm_status", Context.MODE_PRIVATE)
    prefs.edit().putBoolean("prayer_alarm_scheduled_$prayerIndex", isScheduled).apply()
  }

  fun isAnyAlarmScheduled(context: Context): Boolean {
    val prefs = context.getSharedPreferences("habeeb_alarm_status", Context.MODE_PRIVATE)
    for (i in 0 until 5) {
      if (prefs.getBoolean("prayer_alarm_scheduled_$i", false)) return true
    }
    return false
  }

  private fun computeTriggerTimeMillis(timeStr: String, reminderMinutes: Int): Long {
    val cal = Calendar.getInstance()
    try {
      val parts = timeStr.trim().split(" ")
      val timeParts = parts[0].split(":")
      var hour = timeParts[0].toInt()
      val minute = timeParts[1].toInt()
      val amPm = if (parts.size > 1) parts[1].uppercase() else "AM"

      if (amPm == "PM" && hour < 12) hour += 12
      if (amPm == "AM" && hour == 12) hour = 0

      cal.set(Calendar.HOUR_OF_DAY, hour)
      cal.set(Calendar.MINUTE, minute)
      cal.set(Calendar.SECOND, 0)
      cal.set(Calendar.MILLISECOND, 0)
      cal.add(Calendar.MINUTE, -reminderMinutes)

      // If already passed today, schedule for tomorrow
      if (cal.timeInMillis <= System.currentTimeMillis()) {
        cal.add(Calendar.DAY_OF_YEAR, 1)
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing prayer time: $timeStr", e)
      cal.timeInMillis = System.currentTimeMillis() + (10 * 60 * 1000)
    }
    return cal.timeInMillis
  }
}
