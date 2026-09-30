package com.example.ui.screens.deen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AudioPlaybackState
import com.example.data.QuranAudioService
import com.example.data.QuranRepository
import com.example.data.ReciterOption
import com.example.ui.model.AyahData
import com.example.ui.model.SurahData
import kotlinx.coroutines.launch

@Composable
fun QuranReaderScreen(
  isAudioPlaying: Boolean = false,
  onToggleAudio: () -> Unit = {}
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Initialize QuranAudioService preferences
  LaunchedEffect(Unit) {
    QuranAudioService.init(context)
  }

  // The 7 designated Surahs in exact required sequence
  val surahsList = remember { QuranRepository.SEVEN_SURAHS }

  var selectedSurah by remember { mutableStateOf<SurahData?>(null) }
  var searchQuery by remember { mutableStateOf("") }
  var selectedTab by remember { mutableIntStateOf(0) } // 0: 7 Surahs, 1: Bookmarks
  var isDarkReadingMode by remember { mutableStateOf(false) }
  var showReciterDialog by remember { mutableStateOf(false) }

  // Async state for Ayahs loading
  var ayahsState by remember { mutableStateOf<List<AyahData>?>(null) }
  var isLoadingAyahs by remember { mutableStateOf(false) }
  var ayahsErrorMessage by remember { mutableStateOf<String?>(null) }

  // Real-time audio player state flows
  val audioPlaybackState by QuranAudioService.playbackState.collectAsState()
  val audioSurahNumber by QuranAudioService.currentSurahNumber.collectAsState()
  val selectedReciterId by QuranAudioService.selectedReciterId.collectAsState()
  val audioErrorMessage by QuranAudioService.errorMessage.collectAsState()
  val currentReciter = QuranAudioService.getSelectedReciter()

  var bookmarkedAyahs by remember {
    mutableStateOf(
      listOf(
        Pair("Surah Al-Kahf (18:10)", "رَبَّنَا آتِنَا مِن لَّدُنكَ رَحْمَةً وَهَيِّئْ لَنَا مِنْ أَمْرِنَا رَشَدًا"),
        Pair("Surah Ya-Sin (36:58)", "سَلَامٌ قَوْلًا مِّن رَّبٍّ رَّحِيمٍ"),
        Pair("Surah Al-Mulk (67:1)", "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ وَهُوَ عَلَىٰ كُلِّ شَيْءٍ قَدِيرٌ")
      )
    )
  }

  // Load ayahs whenever a surah is selected
  fun loadAyahsForSurah(surah: SurahData) {
    coroutineScope.launch {
      isLoadingAyahs = true
      ayahsErrorMessage = null
      val result = QuranRepository.getSurahAyahs(context, surah.number)
      isLoadingAyahs = false
      if (result.isSuccess) {
        val list = result.getOrNull().orEmpty()
        val expected = QuranRepository.EXPECTED_AYAH_COUNTS[surah.number] ?: 0
        if (list.size == expected) {
          ayahsState = list
        } else {
          ayahsErrorMessage = "Ayah verification failed: Expected $expected, received ${list.size}."
        }
      } else {
        ayahsErrorMessage = result.exceptionOrNull()?.localizedMessage
          ?: "Network error loading Surah. Tap Retry to reload."
      }
    }
  }

  LaunchedEffect(selectedSurah) {
    if (selectedSurah != null) {
      // Sync audio player to open Surah; stops previous audio first so two never clash
      QuranAudioService.syncWithSurah(selectedSurah!!.number)
      loadAyahsForSurah(selectedSurah!!)
    } else {
      ayahsState = null
      ayahsErrorMessage = null
      isLoadingAyahs = false
    }
  }

  // Handle hardware/gesture back press when in reader mode
  BackHandler(enabled = selectedSurah != null) {
    selectedSurah = null
  }

  // --------------------------------------------------------------------------
  // RECITER SELECTION DIALOG (All 7 Verified Reciters)
  // --------------------------------------------------------------------------
  if (showReciterDialog) {
    AlertDialog(
      onDismissRequest = { showReciterDialog = false },
      icon = {
        Icon(
          Icons.Default.RecordVoiceOver,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(32.dp)
        )
      },
      title = {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "Select Quran Reciter",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Verified 100% full-surah CDN streaming",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      },
      text = {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(QuranAudioService.VERIFIED_RECITERS) { reciter ->
            val isSelected = reciter.id == selectedReciterId
            Surface(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  // Switching reciter stops previous audio first
                  QuranAudioService.setReciter(context, reciter.id)
                  showReciterDialog = false
                  Toast.makeText(context, "Reciter: ${reciter.name}", Toast.LENGTH_SHORT).show()
                },
              shape = RoundedCornerShape(12.dp),
              color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
              border = BorderStroke(
                1.dp,
                if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
              )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  RadioButton(
                    selected = isSelected,
                    onClick = {
                      QuranAudioService.setReciter(context, reciter.id)
                      showReciterDialog = false
                    },
                    colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(
                      text = reciter.name,
                      style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                      )
                    )
                    Text(
                      text = reciter.style,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Text(
                  text = reciter.titleArabic,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                  )
                )
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showReciterDialog = false }) {
          Text("Close")
        }
      }
    )
  }

  // --------------------------------------------------------------------------
  // SURAH READER VIEW
  // --------------------------------------------------------------------------
  if (selectedSurah != null) {
    val surah = selectedSurah!!
    val currentIdx = surahsList.indexOfFirst { it.number == surah.number }
    val prevSurah = if (currentIdx > 0) surahsList[currentIdx - 1] else null
    val nextSurah = if (currentIdx in 0 until surahsList.lastIndex) surahsList[currentIdx + 1] else null

    val isThisSurahAudio = audioSurahNumber == surah.number
    val isPlayingSurah = isThisSurahAudio && audioPlaybackState == AudioPlaybackState.PLAYING
    val isBufferingSurah = isThisSurahAudio && audioPlaybackState == AudioPlaybackState.BUFFERING
    val isPausedSurah = isThisSurahAudio && audioPlaybackState == AudioPlaybackState.PAUSED
    val isErrorSurah = isThisSurahAudio && audioPlaybackState == AudioPlaybackState.ERROR

    val readBg = if (isDarkReadingMode) Color(0xFF111713) else MaterialTheme.colorScheme.surface
    val readTextColor = if (isDarkReadingMode) Color(0xFFE2EBE6) else MaterialTheme.colorScheme.onSurface
    val listState = rememberLazyListState()

    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(readBg)
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // Top Navigation & Action Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = { selectedSurah = null }) {
          Icon(
            Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back to Surah List",
            tint = MaterialTheme.colorScheme.primary
          )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(
            text = "سُورَةُ ${surah.nameArabic}",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
          )
          Text(
            text = "${surah.number}. ${surah.nameEnglish} • ${surah.versesCount} Ayahs",
            style = MaterialTheme.typography.labelSmall,
            color = readTextColor.copy(alpha = 0.7f)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { isDarkReadingMode = !isDarkReadingMode }) {
            Icon(
              imageVector = if (isDarkReadingMode) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Toggle Reading Mode",
              tint = MaterialTheme.colorScheme.secondary
            )
          }

          // Top Bar Audio Control (Driven strictly by real audio events)
          IconButton(
            onClick = {
              QuranAudioService.togglePlayPause(context, surah.number)
            }
          ) {
            when {
              isBufferingSurah -> {
                CircularProgressIndicator(
                  modifier = Modifier.size(20.dp),
                  strokeWidth = 2.dp,
                  color = MaterialTheme.colorScheme.primary
                )
              }
              isPlayingSurah -> {
                Icon(
                  Icons.Default.PauseCircle,
                  contentDescription = "Pause Audio Recitation",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
              else -> {
                Icon(
                  Icons.Default.PlayCircle,
                  contentDescription = "Stream Audio Recitation",
                  tint = MaterialTheme.colorScheme.primary
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Previous / Next Surah Navigation Row (strictly among the 7 designated surahs)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (prevSurah != null) {
          TextButton(
            onClick = {
              // Navigating surahs stops previous audio first so two never play together
              selectedSurah = prevSurah
            },
            contentPadding = PaddingValues(horizontal = 6.dp)
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Prev: ${prevSurah.nameEnglish}", fontSize = 12.sp, maxLines = 1)
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        ) {
          Text(
            text = "${currentIdx + 1} of ${surahsList.size}",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
          )
        }

        if (nextSurah != null) {
          TextButton(
            onClick = {
              // Navigating surahs stops previous audio first
              selectedSurah = nextSurah
            },
            contentPadding = PaddingValues(horizontal = 6.dp)
          ) {
            Text("Next: ${nextSurah.nameEnglish}", fontSize = 12.sp, maxLines = 1)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
          }
        } else {
          Spacer(modifier = Modifier.width(1.dp))
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // ----------------------------------------------------------------------
      // DEDICATED FULL-SURAH AUDIO PLAYER CARD
      // ----------------------------------------------------------------------
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (isPlayingSurah) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
          } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
          }
        ),
        border = BorderStroke(
          1.dp,
          if (isPlayingSurah) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
          else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        )
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Top Row: Reciter info + Change Reciter button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(
                    if (isPlayingSurah) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.primaryContainer
                  ),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = if (isPlayingSurah) Icons.Default.VolumeUp else Icons.Default.Headphones,
                  contentDescription = null,
                  tint = if (isPlayingSurah) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp)
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = currentReciter.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${currentReciter.titleArabic} • ${currentReciter.style}",
                  style = MaterialTheme.typography.labelSmall,
                  color = readTextColor.copy(alpha = 0.7f),
                  maxLines = 1
                )
              }
            }

            // Reciter Selection Trigger
            OutlinedButton(
              onClick = { showReciterDialog = true },
              shape = RoundedCornerShape(8.dp),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.height(32.dp)
            ) {
              Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Reciters", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Real Audio Status Display & Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Status Subtext
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              when {
                isBufferingSurah -> {
                  CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Buffering audio stream...",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                  )
                }
                isPlayingSurah -> {
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(Color(0xFF2E7D32))
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Playing Surah ${surah.number} (CDN Stream)",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF2E7D32)
                  )
                }
                isPausedSurah -> {
                  Text(
                    text = "Recitation Paused",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                  )
                }
                isErrorSurah -> {
                  Text(
                    text = "Stream Connection Error",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                  )
                }
                else -> {
                  Text(
                    text = "Stream full Surah audio on demand",
                    style = MaterialTheme.typography.bodySmall,
                    color = readTextColor.copy(alpha = 0.65f)
                  )
                }
              }
            }

            // Buttons: Stop / Reset & Play / Pause
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              if (isPlayingSurah || isPausedSurah || isBufferingSurah) {
                IconButton(
                  onClick = { QuranAudioService.stop() },
                  modifier = Modifier.size(32.dp)
                ) {
                  Icon(
                    Icons.Default.StopCircle,
                    contentDescription = "Stop recitation",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                  )
                }
              }

              Button(
                onClick = {
                  // Audio starts strictly from user tap (conforming to mobile autoplay rules)
                  QuranAudioService.togglePlayPause(context, surah.number)
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp),
                colors = ButtonDefaults.buttonColors(
                  containerColor = if (isPlayingSurah) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                )
              ) {
                when {
                  isBufferingSurah -> {
                    Text("Buffering...", fontSize = 12.sp)
                  }
                  isPlayingSurah -> {
                    Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pause", fontSize = 12.sp)
                  }
                  isPausedSurah -> {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Resume", fontSize = 12.sp)
                  }
                  isErrorSurah -> {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Retry", fontSize = 12.sp)
                  }
                  else -> {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Stream", fontSize = 12.sp)
                  }
                }
              }
            }
          }

          // Error message banner with Retry button if network fails
          if (isErrorSurah && audioErrorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.weight(1f)
                ) {
                  Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = audioErrorMessage ?: "Audio error",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    maxLines = 2
                  )
                }

                TextButton(
                  onClick = { QuranAudioService.playSurah(context, surah.number) },
                  contentPadding = PaddingValues(horizontal = 6.dp)
                ) {
                  Text("Retry", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Bismillah Header Banner
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
      ) {
        Text(
          text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          ),
          textAlign = TextAlign.Center,
          modifier = Modifier.padding(vertical = 12.dp)
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Main Content Area: Loading, Error, or Verified Ayahs List
      when {
        isLoadingAyahs -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(14.dp))
              Text(
                text = "Loading authentic Uthmani text for Surah ${surah.nameEnglish}...",
                style = MaterialTheme.typography.bodyMedium,
                color = readTextColor.copy(alpha = 0.7f)
              )
              Text(
                text = "Verifying ${surah.versesCount} Ayahs",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        ayahsErrorMessage != null -> {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f))
            ) {
              Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text = "Quran Text Verification Notice",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = ayahsErrorMessage ?: "An error occurred.",
                  style = MaterialTheme.typography.bodySmall,
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                  onClick = { loadAyahsForSurah(surah) },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Retry Authentic Fetch")
                }
              }
            }
          }
        }

        ayahsState != null -> {
          val ayahs = ayahsState!!
          LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
          ) {
            items(ayahs, key = { "${surah.number}_${it.number}" }) { ayah ->
              Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                  containerColor = if (isDarkReadingMode) Color(0xFF1B231F) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                ),
                border = BorderStroke(
                  0.5.dp,
                  if (isDarkReadingMode) Color(0xFF2C3831) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                )
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  // Ayah Header Row (Number badge + Action buttons)
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.primaryContainer,
                      modifier = Modifier.height(28.dp)
                    ) {
                      Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                      ) {
                        Text(
                          text = "Ayah ${ayah.number}",
                          style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                          )
                        )
                      }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                      // Copy Ayah
                      IconButton(
                        onClick = {
                          val clip = ClipData.newPlainText("Ayah", "${ayah.textArabic}\n${ayah.textEnglish}")
                          val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                          clipboard?.setPrimaryClip(clip)
                          Toast.makeText(context, "Copied Ayah ${ayah.number}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(28.dp)
                      ) {
                        Icon(
                          Icons.Default.ContentCopy,
                          contentDescription = "Copy Ayah",
                          tint = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.size(16.dp)
                        )
                      }

                      // Bookmark Ayah
                      IconButton(
                        onClick = {
                          val entry = Pair("Surah ${surah.nameEnglish} (${surah.number}:${ayah.number})", ayah.textArabic)
                          if (!bookmarkedAyahs.contains(entry)) {
                            bookmarkedAyahs = bookmarkedAyahs + entry
                            Toast.makeText(context, "Bookmarked Ayah ${ayah.number}", Toast.LENGTH_SHORT).show()
                          }
                        },
                        modifier = Modifier.size(28.dp)
                      ) {
                        Icon(
                          Icons.Default.BookmarkAdd,
                          contentDescription = "Bookmark",
                          tint = MaterialTheme.colorScheme.secondary,
                          modifier = Modifier.size(18.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  // Authentic Arabic Uthmani Text with RTL alignment
                  Text(
                    text = ayah.textArabic,
                    style = MaterialTheme.typography.headlineSmall.copy(
                      lineHeight = 42.sp,
                      fontWeight = FontWeight.Normal,
                      color = if (isDarkReadingMode) Color(0xFFF0F5F2) else Color(0xFF0F1E16)
                    ),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                  )

                  Spacer(modifier = Modifier.height(10.dp))

                  // English Translation
                  Text(
                    text = ayah.textEnglish,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = readTextColor.copy(alpha = 0.82f)
                  )
                }
              }
            }
          }
        }
      }
    }
    return
  }

  // --------------------------------------------------------------------------
  // 7 SURAHS BROWSE LIST VIEW
  // --------------------------------------------------------------------------
  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Continue Reading Banner (quick jump to Surah Al-Kahf or currently streaming surah)
    Card(
      modifier = Modifier
        .fillMaxWidth()
        .clickable {
          selectedSurah = surahsList.firstOrNull { it.number == audioSurahNumber }
            ?: surahsList.firstOrNull { it.number == 18 }
            ?: surahsList.first()
        },
      shape = RoundedCornerShape(18.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(18.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "FEATURED SURAHS • 7 SELECTED",
            style = MaterialTheme.typography.labelSmall.copy(
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Surah Al-Kahf (الكهف)",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary
            )
          )
          Text(
            text = "Surah 18 • 110 Verified Ayahs • Full Audio Recitation",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
          )
        }

        FilledTonalButton(
          onClick = {
            selectedSurah = surahsList.firstOrNull { it.number == 18 } ?: surahsList.first()
          },
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary
          ),
          shape = RoundedCornerShape(10.dp)
        ) {
          Text("Read")
        }
      }
    }

    // Search Bar
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
      placeholder = { Text("Search by name (e.g. Mulk, Yasin, طه)...") },
      modifier = Modifier.fillMaxWidth(),
      shape = RoundedCornerShape(14.dp),
      singleLine = true
    )

    // Sub Tabs: 7 Surahs | Bookmarks
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = MaterialTheme.colorScheme.surface,
      contentColor = MaterialTheme.colorScheme.primary
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = { Text("7 Surahs") }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = { Text("Bookmarks (${bookmarkedAyahs.size})") }
      )
    }

    when (selectedTab) {
      0 -> {
        val filtered = surahsList.filter {
          searchQuery.isBlank() ||
            it.nameEnglish.contains(searchQuery, ignoreCase = true) ||
            it.nameArabic.contains(searchQuery) ||
            it.number.toString() == searchQuery.trim()
        }

        if (filtered.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No Surahs matched \"$searchQuery\"",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          }
        }

        // Persistent Mini Audio Player Banner when audio is active
        if (audioPlaybackState == AudioPlaybackState.PLAYING ||
            audioPlaybackState == AudioPlaybackState.BUFFERING ||
            audioPlaybackState == AudioPlaybackState.PAUSED
        ) {
          val activeSurah = surahsList.find { it.number == audioSurahNumber } ?: surahsList.first()
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedSurah = activeSurah },
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                if (audioPlaybackState == AudioPlaybackState.BUFFERING) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                  )
                } else {
                  Icon(
                    imageVector = if (audioPlaybackState == AudioPlaybackState.PLAYING) Icons.Default.VolumeUp else Icons.Default.Pause,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                  )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = "${activeSurah.nameEnglish} • ${currentReciter.name.split(" ").lastOrNull() ?: currentReciter.name}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = if (audioPlaybackState == AudioPlaybackState.PLAYING) "Playing Audio Recitation" else if (audioPlaybackState == AudioPlaybackState.BUFFERING) "Buffering..." else "Paused",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                  )
                }
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                  onClick = { QuranAudioService.togglePlayPause(context, activeSurah.number) }
                ) {
                  Icon(
                    imageVector = if (audioPlaybackState == AudioPlaybackState.PLAYING) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                    contentDescription = "Toggle Audio",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                  )
                }
                IconButton(
                  onClick = { QuranAudioService.stop() }
                ) {
                  Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop Audio",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
            }
          }
        }

        filtered.forEachIndexed { index, surah ->
          val isSurahPlaying = audioSurahNumber == surah.number && audioPlaybackState == AudioPlaybackState.PLAYING
          val isSurahBuffering = audioSurahNumber == surah.number && audioPlaybackState == AudioPlaybackState.BUFFERING

          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { selectedSurah = surah },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (isSurahPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
              else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = if (isSurahPlaying) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
              ) {
                Box(
                  modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                      if (isSurahPlaying) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.primaryContainer
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = "${surah.number}",
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (isSurahPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                    )
                  )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                  Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                      text = "${index + 1}. ${surah.nameEnglish}",
                      style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  Text(
                    text = "${surah.revelationType} • ${surah.versesCount} Ayahs",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                  )
                }
              }

              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                // Quick Audio Stream Button for this Surah in the list
                IconButton(
                  onClick = {
                    // Audio starts from explicit user tap
                    QuranAudioService.togglePlayPause(context, surah.number)
                  },
                  modifier = Modifier.size(36.dp)
                ) {
                  when {
                    isSurahBuffering -> {
                      CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.primary
                      )
                    }
                    isSurahPlaying -> {
                      Icon(
                        Icons.Default.PauseCircle,
                        contentDescription = "Pause audio for ${surah.nameEnglish}",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                      )
                    }
                    else -> {
                      Icon(
                        Icons.Default.PlayCircleOutline,
                        contentDescription = "Stream audio for ${surah.nameEnglish}",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                      )
                    }
                  }
                }

                Text(
                  text = surah.nameArabic,
                  style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                )
              }
            }
          }
        }
      }

      1 -> {
        if (bookmarkedAyahs.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = "No bookmarks yet. Tap the bookmark icon on any Ayah to save it here.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
              textAlign = TextAlign.Center
            )
          }
        } else {
          bookmarkedAyahs.forEach { (title, snippet) ->
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(14.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
              Column(modifier = Modifier.padding(14.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                  IconButton(
                    onClick = {
                      bookmarkedAyahs = bookmarkedAyahs.filterNot { it.first == title }
                    },
                    modifier = Modifier.size(24.dp)
                  ) {
                    Icon(
                      Icons.Default.DeleteOutline,
                      contentDescription = "Remove",
                      tint = MaterialTheme.colorScheme.error,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = snippet,
                  style = MaterialTheme.typography.bodySmall.copy(lineHeight = 20.sp),
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                  textAlign = TextAlign.Right,
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }
      }
    }
  }
}
