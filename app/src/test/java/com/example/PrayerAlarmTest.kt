package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.PrayerAlarmScheduler
import com.example.data.PrayerTimeCalculator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PrayerAlarmTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun testPrayerTimeCalculationWithDeviceTimezone() {
    val tz = TimeZone.getDefault()
    assertNotNull(tz)

    val schedule = PrayerTimeCalculator.calculateTodayPrayers(context)
    assertEquals(5, schedule.prayers.size)

    val prayerNames = schedule.prayers.map { it.name }
    assertTrue(prayerNames.contains("Fajr"))
    assertTrue(prayerNames.contains("Dhuhr"))
    assertTrue(prayerNames.contains("Asr"))
    assertTrue(prayerNames.contains("Maghrib"))
    assertTrue(prayerNames.contains("Isha"))

    assertNotNull(schedule.nextPrayerName)
    assertTrue(schedule.nextPrayerName.isNotBlank())
    assertTrue(schedule.nextPrayerTime.isNotBlank())
    assertTrue(schedule.timeRemainingText.startsWith("in "))
    assertTrue(schedule.progressFraction in 0.0f..1.0f)
  }

  @Test
  fun testAlarmSchedulingAndCancellation() {
    // Schedule Fajr
    val scheduled = PrayerAlarmScheduler.schedulePrayerAlarm(
      context = context,
      prayerIndex = 0,
      prayerName = "Fajr",
      prayerTime = "05:15 AM",
      reminderMinutes = 15,
      soundOption = "Makkah Adhan"
    )
    assertTrue("Alarm should be registered", scheduled)

    // Cancel
    PrayerAlarmScheduler.cancelPrayerAlarm(context, 0)
    assertFalse(
      "Alarm 0 should be cancelled",
      context.getSharedPreferences("habeeb_alarm_status", Context.MODE_PRIVATE)
        .getBoolean("prayer_alarm_scheduled_0", true)
    )
  }
}
