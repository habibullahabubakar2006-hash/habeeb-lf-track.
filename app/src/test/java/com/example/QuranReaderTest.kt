package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.QuranRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuranReaderTest {

  private lateinit var context: Context

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
  }

  @Test
  fun testSevenSurahsSpecification() {
    val surahs = QuranRepository.SEVEN_SURAHS
    assertEquals(7, surahs.size)

    val expectedNumbers = listOf(18, 20, 36, 44, 55, 56, 67)
    val actualNumbers = surahs.map { it.number }
    assertEquals(expectedNumbers, actualNumbers)

    val expectedNames = listOf("Al-Kahf", "Ta-Ha", "Ya-Sin", "Ad-Dukhan", "Ar-Rahman", "Al-Waqi'ah", "Al-Mulk")
    val actualNames = surahs.map { it.nameEnglish }
    assertEquals(expectedNames, actualNames)
  }

  @Test
  fun testAyahCountsVerificationForAllSevenSurahs() = runBlocking {
    val expectedCounts = QuranRepository.EXPECTED_AYAH_COUNTS

    for ((surahNumber, expectedCount) in expectedCounts) {
      val result = QuranRepository.getSurahAyahs(context, surahNumber)
      assertTrue("Fetch must succeed for Surah $surahNumber", result.isSuccess)

      val ayahs = result.getOrNull()
      assertNotNull("Ayahs list must not be null for Surah $surahNumber", ayahs)
      assertEquals("Ayah count must match exactly for Surah $surahNumber", expectedCount, ayahs!!.size)

      // Verify consecutive Ayah numbering starting at 1
      for (i in 0 until expectedCount) {
        val ayah = ayahs[i]
        assertEquals("Ayah number must be ${i + 1}", i + 1, ayah.number)
        assertTrue("Arabic text must not be empty", ayah.textArabic.isNotBlank())
        assertTrue("English translation must not be empty", ayah.textEnglish.isNotBlank())
      }
    }
  }
}
