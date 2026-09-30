package com.example.data

import android.content.Context
import android.util.Log
import com.example.ui.model.PrayerTimeItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.*

/**
 * Calculates prayer times dynamically based on the device's real time zone, calendar date,
 * and astronomical solar positions.
 */
object PrayerTimeCalculator {
  private const val TAG = "PrayerTimeCalculator"

  data class PrayerScheduleResult(
    val prayers: List<PrayerTimeItem>,
    val nextPrayerName: String,
    val nextPrayerTime: String,
    val timeRemainingText: String,
    val progressFraction: Float,
    val timeZoneDisplayName: String
  )

  /**
   * Computes today's prayer times using the device's default TimeZone.
   */
  fun calculateTodayPrayers(
    context: Context,
    existingAlarms: Map<String, Boolean> = emptyMap()
  ): PrayerScheduleResult {
    val timeZone = TimeZone.getDefault()
    val calendar = Calendar.getInstance(timeZone)
    val now = calendar.time

    // Estimate coordinates from TimeZone offset (or typical central latitude ~25.0 N)
    val rawOffsetHours = timeZone.rawOffset / (1000.0 * 60 * 60)
    // Rough longitude from timezone (15 deg per hour offset)
    val estimatedLng = rawOffsetHours * 15.0
    // Moderate latitude default (e.g., 20.0 to 30.0 depending on region)
    val estimatedLat = when {
      timeZone.id.contains("Africa") -> 10.0
      timeZone.id.contains("Riyadh") || timeZone.id.contains("Asia/Dubai") -> 24.7
      timeZone.id.contains("London") || timeZone.id.contains("Europe") -> 51.5
      timeZone.id.contains("America") -> 38.0
      else -> 21.4 // Mecca latitude as baseline
    }

    val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)
    val prayerTimesMap = computeSolarPrayerTimes(dayOfYear, estimatedLat, estimatedLng, rawOffsetHours)

    val timeFormatter = SimpleDateFormat("hh:mm a", Locale.US).apply {
      this.timeZone = timeZone
    }

    // Build the 5 daily prayers
    val prayerKeys = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
    val prayerDateTimes = mutableListOf<Pair<String, Date>>()

    for (name in prayerKeys) {
      val (hour, min) = prayerTimesMap[name] ?: Pair(12, 0)
      val pCal = Calendar.getInstance(timeZone).apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, min)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
      prayerDateTimes.add(name to pCal.time)
    }

    // Determine current & next prayer based on current device time
    var nextPrayerIndex = -1
    for (i in prayerDateTimes.indices) {
      if (prayerDateTimes[i].second.after(now)) {
        nextPrayerIndex = i
        break
      }
    }

    // If all prayers today have passed, Fajr tomorrow is next
    val isTomorrowFajr = (nextPrayerIndex == -1)
    if (isTomorrowFajr) {
      nextPrayerIndex = 0
    }

    val currentPrayerIndex = if (isTomorrowFajr) {
      prayerDateTimes.size - 1 // Isha was the most recent
    } else if (nextPrayerIndex > 0) {
      nextPrayerIndex - 1
    } else {
      prayerDateTimes.size - 1 // Before Fajr, previous prayer was yesterday's Isha
    }

    val nextPrayer = prayerDateTimes[nextPrayerIndex]
    val nextPrayerName = nextPrayer.first
    val nextPrayerTimeFormatted = timeFormatter.format(nextPrayer.second)

    // Calculate time remaining to next prayer
    val nextPrayerCal = Calendar.getInstance(timeZone).apply {
      time = nextPrayer.second
      if (isTomorrowFajr) {
        add(Calendar.DAY_OF_YEAR, 1)
      }
    }

    val diffMillis = (nextPrayerCal.timeInMillis - calendar.timeInMillis).coerceAtLeast(0)
    val diffMinutes = (diffMillis / (1000 * 60)).toInt()
    val hoursRemaining = diffMinutes / 60
    val minsRemaining = diffMinutes % 60

    val timeRemainingText = if (hoursRemaining > 0) {
      "in ${hoursRemaining}h ${minsRemaining}m"
    } else {
      "in ${minsRemaining}m"
    }

    // Progress in current prayer window
    val progressFraction = if (diffMinutes > 0) {
      val totalWindowMinutes = 180f // Approx 3 hours typical window
      ((totalWindowMinutes - diffMinutes.coerceAtMost(180)) / totalWindowMinutes).coerceIn(0.15f, 0.95f)
    } else {
      0.95f
    }

    val prayerItems = prayerDateTimes.mapIndexed { index, (name, date) ->
      val formatted = timeFormatter.format(date)
      val isPassed = date.before(now)
      val isCurrent = (index == currentPrayerIndex)
      val isAlarmEnabled = existingAlarms[name] ?: true

      PrayerTimeItem(
        name = name,
        time = formatted,
        isCompleted = isPassed && !isCurrent,
        isCurrent = isCurrent,
        isAlarmEnabled = isAlarmEnabled
      )
    }

    val tzName = "${timeZone.displayName} (${timeZone.id})"

    return PrayerScheduleResult(
      prayers = prayerItems,
      nextPrayerName = nextPrayerName,
      nextPrayerTime = nextPrayerTimeFormatted,
      timeRemainingText = timeRemainingText,
      progressFraction = progressFraction,
      timeZoneDisplayName = tzName
    )
  }

  /**
   * Astronomical calculation for solar prayer times.
   */
  private fun computeSolarPrayerTimes(
    dayOfYear: Int,
    lat: Double,
    lng: Double,
    timeZoneOffset: Double
  ): Map<String, Pair<Int, Int>> {
    // Solar declination approx
    val b = 2.0 * Math.PI * (dayOfYear - 81) / 365.0
    val declination = 23.45 * sin(b) // in degrees
    val decRad = Math.toRadians(declination)
    val latRad = Math.toRadians(lat)

    // Equation of time in minutes
    val eot = 9.87 * sin(2 * b) - 7.53 * cos(b) - 1.5 * sin(b)

    // Solar noon (Dhuhr) in local time
    val solarNoonMinutes = 720.0 - (4.0 * lng) - eot + (60.0 * timeZoneOffset)
    val dhuhrHourDecimal = solarNoonMinutes / 60.0

    // Hour angle for Fajr (-18 deg) and Isha (-17 deg)
    fun hourAngleForAngle(angleDeg: Double): Double {
      val angleRad = Math.toRadians(-angleDeg)
      val cosHA = (sin(angleRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
      val clamped = cosHA.coerceIn(-1.0, 1.0)
      return Math.toDegrees(acos(clamped)) / 15.0
    }

    // Sunset / Sunrise hour angle (center of sun at -0.833 deg)
    val sunsetHA = hourAngleForAngle(0.833)
    val fajrHA = hourAngleForAngle(18.0)
    val ishaHA = hourAngleForAngle(17.5)

    // Asr hour angle (Shafi'i: shadow = 1 + shadow at noon)
    val noonZenith = abs(lat - declination)
    val asrAngle = Math.toDegrees(atan(1.0 + tan(Math.toRadians(noonZenith))))
    val asrHA = hourAngleForAngle(90.0 - asrAngle)

    val fajrDecimal = dhuhrHourDecimal - fajrHA
    val maghribDecimal = dhuhrHourDecimal + sunsetHA
    val asrDecimal = dhuhrHourDecimal + asrHA
    val ishaDecimal = dhuhrHourDecimal + ishaHA

    fun toHourMin(decimal: Double): Pair<Int, Int> {
      var norm = decimal
      while (norm < 0) norm += 24.0
      while (norm >= 24) norm -= 24.0
      val h = norm.toInt()
      val m = ((norm - h) * 60).roundToInt().coerceIn(0, 59)
      return Pair(h, m)
    }

    return mapOf(
      "Fajr" to toHourMin(fajrDecimal),
      "Dhuhr" to toHourMin(dhuhrHourDecimal + 0.1), // standard 5 min after zenith
      "Asr" to toHourMin(asrDecimal),
      "Maghrib" to toHourMin(maghribDecimal + 0.05), // approx 3 mins after sunset
      "Isha" to toHourMin(ishaDecimal)
    )
  }
}
