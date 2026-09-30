package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.AudioPlaybackState
import com.example.data.QuranAudioService
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuranAudioServiceTest {

  private lateinit var context: Context
  private val designatedSurahs = listOf(18, 20, 36, 44, 55, 56, 67)

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    QuranAudioService.init(context)
    QuranAudioService.stop()
  }

  @Test
  fun testVerifiedRecitersAudioUrlsForAllSevenSurahs() {
    val reciters = QuranAudioService.VERIFIED_RECITERS
    assertTrue("At least 5 verified reciters must be available", reciters.size >= 5)

    for (reciter in reciters) {
      assertFalse("Reciter ID must not be blank", reciter.id.isBlank())
      assertFalse("Reciter name must not be blank", reciter.name.isBlank())
      assertFalse("Reciter Arabic title must not be blank", reciter.titleArabic.isBlank())

      for (surah in designatedSurahs) {
        val url = reciter.urlBuilder(surah)
        assertTrue("URL must start with https:// for ${reciter.name} Surah $surah", url.startsWith("https://"))
        assertTrue("URL must end with .mp3 for ${reciter.name} Surah $surah", url.endsWith(".mp3"))
        assertFalse("URL must not contain empty segments", url.contains("//surah"))
      }
    }
  }

  @Test
  fun testReciterSelectionAndPersistence() {
    // Select Mahmoud Khalil Al-Husary
    QuranAudioService.setReciter(context, "ar.husary")
    assertEquals("ar.husary", QuranAudioService.selectedReciterId.value)
    assertEquals("Mahmoud Khalil Al-Husary", QuranAudioService.getSelectedReciter().name)

    // Select Mohamed Siddiq Al-Minshawi
    QuranAudioService.setReciter(context, "ar.minshawi")
    assertEquals("ar.minshawi", QuranAudioService.selectedReciterId.value)
    assertEquals("Mohamed Siddiq Al-Minshawi", QuranAudioService.getSelectedReciter().name)

    // Reset back to Alafasy
    QuranAudioService.setReciter(context, "ar.alafasy")
    assertEquals("ar.alafasy", QuranAudioService.selectedReciterId.value)
  }

  @Test
  fun testSwitchingReciterStopsPreviousAudio() {
    // Change reciter
    QuranAudioService.setReciter(context, "ar.mahermuaiqly")
    // Player should be IDLE, no conflicting audio running
    assertEquals(AudioPlaybackState.IDLE, QuranAudioService.playbackState.value)
    assertFalse(QuranAudioService.isPlaying())
  }

  @Test
  fun testSurahSyncDoesNotAutoplay() {
    // When reader opens Surah 20 (Ta-Ha)
    QuranAudioService.syncWithSurah(20)
    assertEquals(20, QuranAudioService.currentSurahNumber.value)

    // Mobile autoplay rules: Must remain IDLE, not playing
    assertEquals(AudioPlaybackState.IDLE, QuranAudioService.playbackState.value)
    assertFalse(QuranAudioService.isPlaying())

    // When reader moves to Surah 36 (Ya-Sin)
    QuranAudioService.syncWithSurah(36)
    assertEquals(36, QuranAudioService.currentSurahNumber.value)
    assertEquals(AudioPlaybackState.IDLE, QuranAudioService.playbackState.value)
  }

  @Test
  fun testStopAndResetFunctions() {
    QuranAudioService.syncWithSurah(55)
    assertEquals(55, QuranAudioService.currentSurahNumber.value)

    QuranAudioService.stop()
    assertEquals(AudioPlaybackState.IDLE, QuranAudioService.playbackState.value)
    assertNull(QuranAudioService.errorMessage.value)

    QuranAudioService.reset()
    assertEquals(AudioPlaybackState.IDLE, QuranAudioService.playbackState.value)
    assertNull(QuranAudioService.errorMessage.value)
  }
}
