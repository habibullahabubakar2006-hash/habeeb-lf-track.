package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.ui.model.AyahData
import com.example.ui.model.SurahData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

/**
 * Authentic repository for the 7 designated Surahs in HABEEB LF TRACK.
 * Fetches verified Uthmani Arabic text from AlQuran Cloud API / Quran.com API v4,
 * verifies exact ayah counts, and provides robust local caching.
 */
object QuranRepository {
  private const val TAG = "QuranRepository"

  /**
   * The 7 designated Surahs in required sequence.
   */
  val SEVEN_SURAHS: List<SurahData> = listOf(
    SurahData(18, "الكهف", "Al-Kahf", "The Cave", 110, "Makkiyah", 1),
    SurahData(20, "طه", "Ta-Ha", "Ta-Ha", 135, "Makkiyah", 1),
    SurahData(36, "يس", "Ya-Sin", "Ya-Sin", 83, "Makkiyah", 1),
    SurahData(44, "الدخان", "Ad-Dukhan", "The Smoke", 59, "Makkiyah", 1),
    SurahData(55, "الرحمن", "Ar-Rahman", "The Beneficent", 78, "Madaniyah", 1),
    SurahData(56, "الواقعة", "Al-Waqi'ah", "The Inevitable", 96, "Makkiyah", 1),
    SurahData(67, "الملك", "Al-Mulk", "The Sovereignty", 30, "Makkiyah", 1)
  )

  /**
   * Canonical Ayah counts required for validation.
   */
  val EXPECTED_AYAH_COUNTS: Map<Int, Int> = mapOf(
    18 to 110, // Al-Kahf
    20 to 135, // Ta-Ha
    36 to 83,  // Ya-Sin
    44 to 59,  // Ad-Dukhan
    55 to 78,  // Ar-Rahman
    56 to 96,  // Al-Waqi'ah
    67 to 30   // Al-Mulk
  )

  /**
   * Loads authentic Ayahs for one of the 7 designated Surahs.
   * Checks local disk cache -> tries API fetch -> falls back to bundled asset.
   * Enforces exact Ayah count verification.
   */
  suspend fun getSurahAyahs(
    context: Context,
    surahNumber: Int
  ): Result<List<AyahData>> = withContext(Dispatchers.IO) {
    val expectedCount = EXPECTED_AYAH_COUNTS[surahNumber]
      ?: return@withContext Result.failure(IllegalArgumentException("Surah $surahNumber is not one of the 7 designated Surahs"))

    // 1. Try local cache in SharedPreferences
    val cached = getFromCache(context, surahNumber)
    if (cached != null && cached.size == expectedCount) {
      Log.i(TAG, "Loaded Surah $surahNumber from cache (${cached.size} Ayahs verified)")
      return@withContext Result.success(cached)
    }

    // 2. Try live fetch from AlQuran Cloud API (Uthmani edition + Sahih English)
    try {
      val liveAyahs = fetchFromApi(surahNumber)
      if (liveAyahs.size == expectedCount) {
        saveToCache(context, surahNumber, liveAyahs)
        Log.i(TAG, "Fetched Surah $surahNumber live from API (${liveAyahs.size} Ayahs verified)")
        return@withContext Result.success(liveAyahs)
      } else {
        val errorMsg = "Ayah count mismatch from API for Surah $surahNumber: expected $expectedCount, got ${liveAyahs.size}"
        Log.e(TAG, errorMsg)
      }
    } catch (e: Exception) {
      Log.w(TAG, "API fetch for Surah $surahNumber failed: ${e.message}")
    }

    // 3. Fallback to bundled authentic Uthmani dataset from app assets
    try {
      val assetAyahs = loadFromAssets(context, surahNumber)
      if (assetAyahs != null && assetAyahs.size == expectedCount) {
        saveToCache(context, surahNumber, assetAyahs)
        Log.i(TAG, "Loaded Surah $surahNumber from verified app asset (${assetAyahs.size} Ayahs verified)")
        return@withContext Result.success(assetAyahs)
      } else {
        val assetCount = assetAyahs?.size ?: 0
        return@withContext Result.failure(
          IllegalStateException("Ayah count mismatch for Surah $surahNumber: expected $expectedCount, received $assetCount")
        )
      }
    } catch (e: Exception) {
      Log.e(TAG, "Failed to load Surah $surahNumber from assets", e)
      return@withContext Result.failure(e)
    }
  }

  private fun fetchFromApi(surahNumber: Int): List<AyahData> {
    val url = URL("https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,en.sahih")
    val conn = (url.openConnection() as HttpURLConnection).apply {
      requestMethod = "GET"
      connectTimeout = 5000
      readTimeout = 5000
      setRequestProperty("User-Agent", "HABEEB-LF-TRACK/1.0")
    }

    if (conn.responseCode != 200) {
      throw IllegalStateException("API error HTTP ${conn.responseCode}")
    }

    val jsonText = conn.inputStream.bufferedReader().use { it.readText() }
    val root = JSONObject(jsonText)
    if (root.optInt("code") != 200) {
      throw IllegalStateException("API response returned error code ${root.optInt("code")}")
    }

    val dataArray = root.getJSONArray("data")
    val arabicEdition = dataArray.getJSONObject(0).getJSONArray("ayahs")
    val englishEdition = dataArray.getJSONObject(1).getJSONArray("ayahs")

    val result = mutableListOf<AyahData>()
    for (i in 0 until arabicEdition.length()) {
      val arAyah = arabicEdition.getJSONObject(i)
      val enAyah = englishEdition.getJSONObject(i)
      val numInSurah = arAyah.getInt("numberInSurah")
      val textAr = arAyah.getString("text")
      val textEn = enAyah.getString("text")
      result.add(
        AyahData(
          number = numInSurah,
          textArabic = textAr,
          textEnglish = textEn
        )
      )
    }
    return result
  }

  private fun loadFromAssets(context: Context, surahNumber: Int): List<AyahData>? {
    return try {
      context.assets.open("quran_7_surahs.json").use { stream ->
        val jsonText = InputStreamReader(stream).use { it.readText() }
        val root = JSONObject(jsonText)
        val key = surahNumber.toString()
        if (!root.has(key)) return null

        val ayahsArray = root.getJSONArray(key)
        val list = mutableListOf<AyahData>()
        for (i in 0 until ayahsArray.length()) {
          val obj = ayahsArray.getJSONObject(i)
          list.add(
            AyahData(
              number = obj.getInt("num"),
              textArabic = obj.getString("ar"),
              textEnglish = obj.getString("en")
            )
          )
        }
        list
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error reading quran_7_surahs.json for Surah $surahNumber", e)
      null
    }
  }

  private fun getFromCache(context: Context, surahNumber: Int): List<AyahData>? {
    val prefs = getPrefs(context)
    val raw = prefs.getString("surah_cache_$surahNumber", null) ?: return null
    return try {
      val array = JSONArray(raw)
      val list = mutableListOf<AyahData>()
      for (i in 0 until array.length()) {
        val obj = array.getJSONObject(i)
        list.add(
          AyahData(
            number = obj.getInt("num"),
            textArabic = obj.getString("ar"),
            textEnglish = obj.getString("en")
          )
        )
      }
      list
    } catch (_: Exception) {
      null
    }
  }

  private fun saveToCache(context: Context, surahNumber: Int, ayahs: List<AyahData>) {
    try {
      val array = JSONArray()
      for (ayah in ayahs) {
        val obj = JSONObject()
        obj.put("num", ayah.number)
        obj.put("ar", ayah.textArabic)
        obj.put("en", ayah.textEnglish)
        array.put(obj)
      }
      getPrefs(context).edit().putString("surah_cache_$surahNumber", array.toString()).apply()
    } catch (e: Exception) {
      Log.w(TAG, "Failed to cache Surah $surahNumber", e)
    }
  }

  private fun getPrefs(context: Context): SharedPreferences {
    return context.getSharedPreferences("habeeb_quran_cache", Context.MODE_PRIVATE)
  }
}
