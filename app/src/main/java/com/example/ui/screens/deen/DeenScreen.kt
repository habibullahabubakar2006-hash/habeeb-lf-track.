package com.example.ui.screens.deen

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.DuaItem
import com.example.ui.model.HadithItem
import com.example.ui.model.PrayerTimeItem

@Composable
fun DeenScreen(
  prayers: List<PrayerTimeItem>,
  onTogglePrayer: (Int) -> Unit,
  onToggleAlarm: (Int) -> Unit,
  isAudioPlaying: Boolean,
  onToggleAudio: () -> Unit
) {
  var selectedSubTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Prayers & Alarm 🚨", "Quran Reader", "Hifz & Muraja'ah", "Adhkar & Tasbih", "Hadith & Du'as")

  // Tasbih Counter State
  var tasbihCount by remember { mutableIntStateOf(33) }
  var selectedDhikrIndex by remember { mutableIntStateOf(0) }
  val dhikrList = listOf("SubhanAllah (سبحان الله)", "Alhamdulillah (الحمد لله)", "Allahu Akbar (الله أكبر)", "Astaghfirullah (أستغفر الله)")

  val hadithOfDay = remember {
    HadithItem(
      arabic = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
      translation = "\"The best among you are those who learn the Quran and teach it.\"",
      narrator = "Narrated by Uthman ibn Affan (RA)",
      source = "Sahih al-Bukhari 5027"
    )
  }

  val dailyDuas = remember {
    listOf(
      DuaItem(
        title = "For Knowledge & Understanding",
        arabic = "رَبِّ زِدْنِي عِلْمًا",
        transliteration = "Rabbi zidnee 'ilmaa",
        translation = "\"My Lord, increase me in knowledge.\"",
        category = "Study & Knowledge"
      ),
      DuaItem(
        title = "For Ease in Tasks & Speech",
        arabic = "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
        transliteration = "Rabbish-rah lee sadree wa yassir lee amree",
        translation = "\"My Lord, expand for me my breast and ease for me my task.\"",
        category = "Exams & Focus"
      ),
      DuaItem(
        title = "For Morning Protection",
        arabic = "بِسْمِ اللَّهِ الَّذِي لَا يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الْأَرْضِ وَلَا فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
        transliteration = "Bismillahil-lathee la yadurru ma'as-mihi shay'un fil-ardi wa la fis-samaa'i wa Huwas-Samee'ul-'Aleem",
        translation = "\"In the name of Allah with whose Name nothing on earth or in heaven can cause harm.\"",
        category = "Morning Protection"
      )
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("deen_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp)
  ) {
    // Header
    item {
      Column {
        Text(
          text = "🕌 Deen & Spiritual OS",
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        )
        Text(
          text = "Quran Reader, Prayer Times, Alarm 🚨, Prayer Ring 🔔, Adhkar & Tasbih",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
      }
    }

    // Sub Navigation Tabs
    item {
      ScrollableTabRow(
        selectedTabIndex = selectedSubTab,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedSubTab == index,
            onClick = { selectedSubTab = index },
            text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedSubTab == index) FontWeight.Bold else FontWeight.Normal) }
          )
        }
      }
    }

    when (selectedSubTab) {
      0 -> {
        // 1. PRAYERS, PRAYER RING 🔔 & PRAYER ALARM 🚨
        item {
          PrayerAlarmRingComponent(
            prayers = prayers,
            onToggleAlarm = onToggleAlarm
          )
        }

        // Daily Checklist
        item {
          Text(
            text = "Daily Prayer Checklist (${prayers.count { it.isCompleted }}/5)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        items(prayers.indices.toList()) { index ->
          val prayer = prayers[index]
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onTogglePrayer(index) },
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (prayer.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
            )
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                  checked = prayer.isCompleted,
                  onCheckedChange = { onTogglePrayer(index) },
                  colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                  Text(
                    text = prayer.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = if (prayer.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  )
                  if (prayer.isCurrent) {
                    Text(
                      text = "Current Prayer Time",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.secondary
                    )
                  }
                }
              }

              Text(
                text = prayer.time,
                style = MaterialTheme.typography.bodyLarge.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )
            }
          }
        }

        // Qibla Compass Card
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                Box(
                  modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = Icons.Default.Explore,
                    contentDescription = "Qibla Direction",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(28.dp)
                  )
                }
                Column {
                  Text(
                    text = "Qibla Direction",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = "118° ESE • Facing the Holy Kaaba, Makkah",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                  )
                }
              }
            }
          }
        }
      }

      1 -> {
        // 2. QURAN READING AREA
        item {
          QuranReaderScreen(
            isAudioPlaying = isAudioPlaying,
            onToggleAudio = onToggleAudio
          )
        }
      }

      2 -> {
        // 3. HIFZ & MURAJA'AH
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = "Hifz Memorization Progress",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(10.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = "18 of 30 Juz Memorized", style = MaterialTheme.typography.bodyMedium)
                Text(text = "60%", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
              }
              Spacer(modifier = Modifier.height(8.dp))
              LinearProgressIndicator(
                progress = { 0.6f },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(8.dp)
                  .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }

        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = "Muraja'ah Spaced Repetition Schedule",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Based on Islamic traditional Hifz retention matrix:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
              )
              Spacer(modifier = Modifier.height(12.dp))

              MurajaahStatusRow("Juz 1 & 2 (Al-Baqarah)", "Reviewed 2 days ago", "Excellent", MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.height(8.dp))
              MurajaahStatusRow("Juz 3 (Al-Imran)", "Target: 20 pages", "Due Today", MaterialTheme.colorScheme.secondary)
              Spacer(modifier = Modifier.height(8.dp))
              MurajaahStatusRow("Juz 29 (Tabarak)", "Surah Al-Mulk to Al-Mursalat", "Tomorrow", MaterialTheme.colorScheme.outline)
            }
          }
        }
      }

      3 -> {
        // 4. ADHKAR & TASBIH
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "DIGITAL TASBIH COUNTER",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.secondary,
                  letterSpacing = 1.sp
                )
              )

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = dhikrList[selectedDhikrIndex],
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
              )

              Spacer(modifier = Modifier.height(16.dp))

              // Big Circular Tap Area
              Box(
                modifier = Modifier
                  .size(140.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.primaryContainer)
                  .clickable { tasbihCount += 1 },
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Text(
                    text = "$tasbihCount",
                    style = MaterialTheme.typography.displayMedium.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                  )
                  Text(
                    text = "Tap to count",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                  onClick = { tasbihCount = 0 },
                  shape = RoundedCornerShape(10.dp)
                ) {
                  Icon(Icons.Default.Refresh, contentDescription = null)
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Reset")
                }

                Button(
                  onClick = {
                    selectedDhikrIndex = (selectedDhikrIndex + 1) % dhikrList.size
                    tasbihCount = 0
                  },
                  shape = RoundedCornerShape(10.dp),
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                  Text("Next Dhikr")
                }
              }
            }
          }
        }

        item {
          Text(text = "Essential Daily Adhkar", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        item {
          AdhkarCard(
            title = "Ayat al-Kursi (Morning & Evening)",
            arabic = "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ الْحَيُّ الْقَيُّومُ",
            translation = "Allah - there is no deity except Him, the Ever-Living, the Sustainer of all existence.",
            target = "1 time (Protection)"
          )
        }

        item {
          AdhkarCard(
            title = "Tasbih Fatimah",
            arabic = "سُبْحَانَ اللَّهِ • الْحَمْدُ لِلَّهِ • اللَّهُ أَكْبَرُ",
            translation = "33x SubhanAllah, 33x Alhamdulillah, 34x Allahu Akbar before sleeping.",
            target = "100 times"
          )
        }
      }

      4 -> {
        // 5. HADITH & DU'AS
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Icon(Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                Text(
                  text = "HADITH OF THE DAY",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                  )
                )
              }
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = hadithOfDay.arabic,
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = hadithOfDay.translation,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "${hadithOfDay.narrator} • ${hadithOfDay.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
              )
            }
          }
        }

        item {
          Text(text = "Curated Du'a Collection", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        items(dailyDuas) { dua ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Text(
                  text = dua.category,
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = dua.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = dua.arabic,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary),
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = dua.transliteration,
                style = MaterialTheme.typography.bodySmall.copy(fontStyle = androidx.compose.ui.text.font.FontStyle.Italic),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = dua.translation, style = MaterialTheme.typography.bodyMedium)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MurajaahStatusRow(title: String, desc: String, tag: String, color: Color) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(12.dp))
      .background(color.copy(alpha = 0.1f))
      .padding(12.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Column {
      Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
      Text(text = desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
    Surface(shape = RoundedCornerShape(8.dp), color = color.copy(alpha = 0.2f)) {
      Text(
        text = tag,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = color),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      )
    }
  }
}

@Composable
private fun AdhkarCard(title: String, arabic: String, translation: String, target: String) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
        Text(text = target, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(text = arabic, style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary), textAlign = TextAlign.Right, modifier = Modifier.fillMaxWidth())
      Spacer(modifier = Modifier.height(4.dp))
      Text(text = translation, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
    }
  }
}
