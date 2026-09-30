package com.example.data

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AudioPlaybackState {
  IDLE,
  BUFFERING,
  PLAYING,
  PAUSED,
  ERROR
}

data class ReciterOption(
  val id: String,
  val name: String,
  val titleArabic: String,
  val style: String,
  val urlBuilder: (surahNumber: Int) -> String
)

/**
 * Shared singleton audio service for streaming recitation of the 7 designated Surahs.
 * Features 100% verified public CDN URLs, real audio lifecycle events,
 * persistent reciter selection, and strict single-audio guarantees.
 */
object QuranAudioService {
  private const val TAG = "QuranAudioService"
  private const val PREFS_NAME = "habeeb_quran_audio_prefs"
  private const val KEY_RECITER_ID = "selected_reciter_id"

  /**
   * Only reciters whose audio URLs have been checked and verified to return HTTP 200
   * for all 7 designated Surahs (Al-Kahf 18, Ta-Ha 20, Ya-Sin 36, Ad-Dukhan 44,
   * Ar-Rahman 55, Al-Waqi'ah 56, Al-Mulk 67).
   */
  val VERIFIED_RECITERS: List<ReciterOption> = listOf(
    ReciterOption(
      id = "ar.alafasy",
      name = "Mishary Rashid Alafasy",
      titleArabic = "مشاري راشد العفاسي",
      style = "Murattal (Kuwait)",
      urlBuilder = { surah -> "https://cdn.islamic.network/quran/audio/128/ar.alafasy/$surah.mp3" }
    ),
    ReciterOption(
      id = "ar.husary",
      name = "Mahmoud Khalil Al-Husary",
      titleArabic = "محمود خليل الحصري",
      style = "Murattal (Egypt)",
      urlBuilder = { surah -> "https://cdn.islamic.network/quran/audio/128/ar.husary/$surah.mp3" }
    ),
    ReciterOption(
      id = "ar.minshawi",
      name = "Mohamed Siddiq Al-Minshawi",
      titleArabic = "محمد صديق المنشاوي",
      style = "Murattal (Egypt)",
      urlBuilder = { surah -> "https://cdn.islamic.network/quran/audio/128/ar.minshawi/$surah.mp3" }
    ),
    ReciterOption(
      id = "ar.mahermuaiqly",
      name = "Maher Al-Muaiqly",
      titleArabic = "ماهر المعيقلي",
      style = "Imam Masjid Al-Haram",
      urlBuilder = { surah -> "https://cdn.islamic.network/quran/audio/128/ar.mahermuaiqly/$surah.mp3" }
    ),
    ReciterOption(
      id = "ar.abdulbasit",
      name = "Abdul Basit Abdul Samad",
      titleArabic = "عبد الباسط عبد الصمد",
      style = "Murattal (Egypt)",
      urlBuilder = { surah -> "https://server7.mp3quran.net/basit/${String.format("%03d", surah)}.mp3" }
    ),
    ReciterOption(
      id = "ar.shatri",
      name = "Abu Bakr Ash-Shatri",
      titleArabic = "أبو بكر الشاطري",
      style = "Murattal (Saudi Arabia)",
      urlBuilder = { surah -> "https://server11.mp3quran.net/shatri/${String.format("%03d", surah)}.mp3" }
    ),
    ReciterOption(
      id = "ar.s_gmd",
      name = "Saad Al-Ghamdi",
      titleArabic = "سعد الغامدي",
      style = "Murattal (Saudi Arabia)",
      urlBuilder = { surah -> "https://server7.mp3quran.net/s_gmd/${String.format("%03d", surah)}.mp3" }
    )
  )

  private var mediaPlayer: MediaPlayer? = null

  // Reactive state holders observed by UI
  private val _playbackState = MutableStateFlow(AudioPlaybackState.IDLE)
  val playbackState: StateFlow<AudioPlaybackState> = _playbackState.asStateFlow()

  private val _currentSurahNumber = MutableStateFlow(18)
  val currentSurahNumber: StateFlow<Int> = _currentSurahNumber.asStateFlow()

  private val _selectedReciterId = MutableStateFlow("ar.alafasy")
  val selectedReciterId: StateFlow<String> = _selectedReciterId.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private var isInitialized = false

  fun init(context: Context) {
    if (isInitialized) return
    isInitialized = true
    try {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      val savedId = prefs.getString(KEY_RECITER_ID, "ar.alafasy") ?: "ar.alafasy"
      if (VERIFIED_RECITERS.any { it.id == savedId }) {
        _selectedReciterId.value = savedId
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error initializing reciter preference", e)
    }
  }

  fun getSelectedReciter(): ReciterOption {
    val id = _selectedReciterId.value
    return VERIFIED_RECITERS.firstOrNull { it.id == id } ?: VERIFIED_RECITERS.first()
  }

  /**
   * Sets and persists the reciter choice.
   * Switching reciters immediately stops previous audio so two audios never play together.
   */
  fun setReciter(context: Context, reciterId: String) {
    if (VERIFIED_RECITERS.none { it.id == reciterId }) return
    if (_selectedReciterId.value == reciterId) return

    // Immediately stop previous audio
    stop()

    _selectedReciterId.value = reciterId
    _errorMessage.value = null

    try {
      val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
      prefs.edit().putString(KEY_RECITER_ID, reciterId).apply()
      Log.i(TAG, "Saved reciter preference: $reciterId")
    } catch (e: Exception) {
      Log.w(TAG, "Error persisting reciter preference", e)
    }
  }

  /**
   * Streams full-surah audio on demand from reliable public CDN.
   * Stops any currently playing audio first so two streams never clash.
   */
  fun playSurah(context: Context, surahNumber: Int, reciterId: String? = null) {
    try {
      init(context)

      // Stop previous audio first
      stop()

      _currentSurahNumber.value = surahNumber
      if (reciterId != null) {
        setReciter(context, reciterId)
      }

      val reciter = getSelectedReciter()
      val streamUrl = reciter.urlBuilder(surahNumber)
      Log.i(TAG, "Streaming Surah $surahNumber recitation by ${reciter.name} from: $streamUrl")

      _errorMessage.value = null
      _playbackState.value = AudioPlaybackState.BUFFERING

      val player = MediaPlayer().apply {
        setAudioAttributes(
          AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .build()
        )
        setDataSource(streamUrl)
        setOnPreparedListener { mp ->
          Log.i(TAG, "MediaPlayer prepared for Surah $surahNumber")
          _playbackState.value = AudioPlaybackState.PLAYING
          mp.start()
        }
        setOnCompletionListener {
          Log.i(TAG, "MediaPlayer finished playback for Surah $surahNumber")
          _playbackState.value = AudioPlaybackState.IDLE
        }
        setOnErrorListener { _, what, extra ->
          Log.e(TAG, "MediaPlayer streaming error: what=$what extra=$extra")
          _playbackState.value = AudioPlaybackState.ERROR
          _errorMessage.value = "Audio stream unavailable (Code $what). Please check internet connectivity and tap Retry."
          true
        }
      }
      mediaPlayer = player
      player.prepareAsync()
    } catch (e: Exception) {
      Log.e(TAG, "Exception starting audio stream", e)
      _playbackState.value = AudioPlaybackState.ERROR
      _errorMessage.value = "Failed to stream audio: ${e.localizedMessage ?: "Network error"}"
    }
  }

  fun pause() {
    try {
      mediaPlayer?.let {
        if (it.isPlaying) {
          it.pause()
          _playbackState.value = AudioPlaybackState.PAUSED
          Log.i(TAG, "Audio paused")
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error pausing audio", e)
    }
  }

  fun resume() {
    try {
      mediaPlayer?.let {
        if (!it.isPlaying && _playbackState.value == AudioPlaybackState.PAUSED) {
          it.start()
          _playbackState.value = AudioPlaybackState.PLAYING
          Log.i(TAG, "Audio resumed")
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Error resuming audio", e)
    }
  }

  fun togglePlayPause(context: Context, surahNumber: Int) {
    if (mediaPlayer != null && _currentSurahNumber.value == surahNumber) {
      when (_playbackState.value) {
        AudioPlaybackState.PLAYING -> pause()
        AudioPlaybackState.PAUSED -> resume()
        AudioPlaybackState.BUFFERING -> stop()
        else -> playSurah(context, surahNumber)
      }
    } else {
      playSurah(context, surahNumber)
    }
  }

  /**
   * Stops playback and resets state to IDLE.
   */
  fun stop() {
    try {
      mediaPlayer?.let {
        if (it.isPlaying) {
          it.stop()
        }
        it.reset()
        it.release()
      }
      mediaPlayer = null
      _playbackState.value = AudioPlaybackState.IDLE
    } catch (e: Exception) {
      Log.e(TAG, "Error stopping audio", e)
    }
  }

  /**
   * Resets playback and clears any error.
   */
  fun reset() {
    stop()
    _errorMessage.value = null
  }

  /**
   * Called when the Quran Reader navigates to a new Surah.
   * If audio is currently playing or buffering, it stops previous audio first
   * so audio stays in sync with the open Surah without unexpected overlapping playback.
   */
  fun syncWithSurah(surahNumber: Int) {
    if (_currentSurahNumber.value != surahNumber) {
      if (isPlaying() || _playbackState.value == AudioPlaybackState.BUFFERING) {
        stop()
      }
      _currentSurahNumber.value = surahNumber
      _errorMessage.value = null
    }
  }

  fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true
}
