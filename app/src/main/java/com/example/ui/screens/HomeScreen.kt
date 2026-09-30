package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.AudioPlaybackState
import com.example.data.PrayerTimeCalculator
import com.example.data.QuranAudioService
import com.example.data.SessionManager
import com.example.data.WalletStorage
import com.example.ui.components.HabeebLogo
import com.example.ui.localization.AppStrings
import com.example.ui.model.*
import java.util.Locale
import com.example.ui.screens.ai.HabeebAiScreen
import com.example.ui.screens.deen.DeenScreen
import com.example.ui.screens.files.MyFilesScreen
import com.example.ui.screens.planner.PlannerScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.study.StudyScreen
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

enum class AppSubView { NONE, SETTINGS, MY_FILES }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  authViewModel: AuthViewModel,
  onThemeModeChanged: (String) -> Unit = {},
  onSignOut: () -> Unit
) {
  val context = LocalContext.current
  val sessionManager = remember { SessionManager(context) }

  val userName by authViewModel.name.collectAsState()
  val userEmail by authViewModel.email.collectAsState()

  val activeUserEmail = if (userEmail.isNotBlank()) userEmail else "habibullahabubakar2006@gmail.com"
  val activeUserName = if (userName.isNotBlank()) userName else "Habibullah"

  val walletStorage = remember { WalletStorage(context) }
  var currentLanguage by remember { mutableStateOf(sessionManager.selectedLanguage) }
  val strings = AppStrings.get(currentLanguage)

  // Sub-views state (Settings, My Files, or Main OS)
  var currentSubView by remember { mutableStateOf(AppSubView.NONE) }

  // State: whether showing Master Overview or one of the 5 pillars
  var isOverviewActive by remember { mutableStateOf(true) }
  var currentSection by remember { mutableStateOf(MainSection.STUDY) }
  var showSignOutDialog by remember { mutableStateOf(false) }

  // Back button handling: return from SubViews or specific sections
  BackHandler(enabled = currentSubView != AppSubView.NONE) {
    if (currentSubView == AppSubView.MY_FILES) {
      currentSubView = AppSubView.SETTINGS
    } else {
      currentSubView = AppSubView.NONE
    }
  }

  BackHandler(enabled = currentSubView == AppSubView.NONE && !isOverviewActive) {
    isOverviewActive = true
  }

  // 1. Deen State (Prayers & Audio) - Keyed by user for data isolation
  var prayers by remember(activeUserEmail) {
    mutableStateOf(
      PrayerTimeCalculator.calculateTodayPrayers(context).prayers
    )
  }
  val audioPlaybackState by QuranAudioService.playbackState.collectAsState()
  val isAudioPlaying = audioPlaybackState == AudioPlaybackState.PLAYING

  // 2. Study State (Tasks & Pomodoro) - Keyed by user for data isolation
  var tasks by remember(activeUserEmail) {
    mutableStateOf(
      listOf(
        StudentTask(1, "Review Islamic Fiqh & Jurisprudence Notes", "Islamic Studies", "Today, 5:00 PM", true),
        StudentTask(2, "Data Structures: Graph Traversal Algorithms", "Computer Science", "Tomorrow, 11:59 PM", false),
        StudentTask(3, "Muraja'ah: Surah Al-Kahf verses 1-50", "Hifz Revision", "Wednesday", false)
      )
    )
  }
  var pomodoroSecondsLeft by remember { mutableIntStateOf(25 * 60) }
  var isPomodoroRunning by remember { mutableStateOf(false) }

  LaunchedEffect(isPomodoroRunning) {
    while (isPomodoroRunning && pomodoroSecondsLeft > 0) {
      delay(1000L)
      pomodoroSecondsLeft -= 1
    }
    if (pomodoroSecondsLeft <= 0) {
      isPomodoroRunning = false
    }
  }

  // 3. Habeeb AI State
  var aiQueryText by remember { mutableStateOf("") }
  var aiMessages by remember(activeUserEmail) {
    mutableStateOf(
      listOf(
        "Habeeb AI" to "As-salamu alaykum, $activeUserName! I am your Habeeb AI companion in HABEEB LF TRACK. How can I assist you with your academic studies, Quran memorization, or daily goals today?"
      )
    )
  }
  var isAiThinking by remember { mutableStateOf(false) }

  LaunchedEffect(isAiThinking) {
    if (isAiThinking) {
      delay(1200L)
      val lastUserMsg = aiMessages.lastOrNull { it.first == "You" }?.second ?: ""
      val aiReply = generateAiResponse(lastUserMsg)
      aiMessages = aiMessages + ("Habeeb AI" to aiReply)
      isAiThinking = false
    }
  }

  // 4. Wallet State - Keyed by user for data isolation and persisted locally
  var transactions by remember(activeUserEmail) {
    mutableStateOf(walletStorage.loadTransactions(activeUserEmail))
  }

  // 5. Planner State - Keyed by user for data isolation
  var scheduleItems by remember(activeUserEmail) {
    mutableStateOf(
      listOf(
        PlannerItem(1, "Fajr Prayer + Morning Hifz", "05:15 AM - 06:15 AM", "Salah & Quran", true, isDeen = true),
        PlannerItem(2, "Data Structures Lecture (CSC 210)", "09:00 AM - 10:30 AM", "University Lecture", true, isDeen = false),
        PlannerItem(3, "Dhuhr Prayer in Congregation", "12:45 PM - 01:15 PM", "Jama'ah Salah", true, isDeen = true),
        PlannerItem(4, "Islamic Jurisprudence (Fiqh) Class", "02:00 PM - 03:30 PM", "Islamic Studies", false, isDeen = true),
        PlannerItem(5, "Pomodoro Focus: Algorithms Problem Set", "04:30 PM - 06:00 PM", "Study Block", false, isDeen = false),
        PlannerItem(6, "Maghrib Prayer + Juz 3 Muraja'ah", "06:45 PM - 07:30 PM", "Spaced Review", false, isDeen = true)
      )
    )
  }

  // If sub-view is active, render it directly
  if (currentSubView == AppSubView.SETTINGS) {
    SettingsScreen(
      userName = activeUserName,
      userEmail = activeUserEmail,
      sessionManager = sessionManager,
      onNavigateBack = { currentSubView = AppSubView.NONE },
      onNavigateToMyFiles = { currentSubView = AppSubView.MY_FILES },
      onThemeModeChanged = onThemeModeChanged,
      onLanguageChanged = { newLang ->
        currentLanguage = newLang
        sessionManager.selectedLanguage = newLang
      },
      onSignOut = {
        currentSubView = AppSubView.NONE
        authViewModel.signOut()
        onSignOut()
      }
    )
    return
  }

  if (currentSubView == AppSubView.MY_FILES) {
    MyFilesScreen(
      userEmail = activeUserEmail,
      onNavigateBack = { currentSubView = AppSubView.SETTINGS }
    )
    return
  }

  Scaffold(
    modifier = Modifier
      .fillMaxSize()
      .testTag("home_screen"),
    topBar = {
      TopAppBar(
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            HabeebLogo(
              size = 36.dp,
              shapeRadius = 8.dp,
              showBorder = true,
              modifier = Modifier.clickable { isOverviewActive = true }
            )
            Column {
              Text(
                text = "HABEEB LF TRACK",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = if (isOverviewActive) strings.dashboardOverview else when (currentSection) {
                  MainSection.STUDY -> strings.tabStudy
                  MainSection.DEEN -> strings.tabDeen
                  MainSection.HABEEB_AI -> strings.tabAi
                  MainSection.WALLET -> strings.tabWallet
                  MainSection.PLANNER -> strings.tabPlanner
                },
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
              )
            }
          }
        },
        actions = {
          // Quick Dashboard Overview Action
          FilledTonalButton(
            onClick = { isOverviewActive = !isOverviewActive },
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = if (isOverviewActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.testTag("btn_toggle_overview")
          ) {
            Icon(
              imageVector = Icons.Default.Dashboard,
              contentDescription = "Overview",
              modifier = Modifier.size(16.dp),
              tint = if (isOverviewActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (isOverviewActive) "Overview" else "Home",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (isOverviewActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Settings Action Button
          IconButton(
            onClick = { currentSubView = AppSubView.SETTINGS },
            modifier = Modifier.testTag("btn_top_settings")
          ) {
            Icon(
              imageVector = Icons.Default.Settings,
              contentDescription = "Settings & Profile",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          IconButton(
            onClick = { showSignOutDialog = true },
            modifier = Modifier.testTag("btn_top_sign_out")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ExitToApp,
              contentDescription = "Sign Out",
              tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      // Primary 5-Pillar Navigation: 📚 Study | 🕌 Deen | 🤖 Habeeb AI | 💰 Wallet | 📅 Planner
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
      ) {
        MainSection.entries.forEach { section ->
          val isSelected = !isOverviewActive && currentSection == section
          NavigationBarItem(
            selected = isSelected,
            onClick = {
              currentSection = section
              isOverviewActive = false
            },
            icon = {
              Icon(
                imageVector = section.icon,
                contentDescription = section.title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            },
            label = {
              val sectionLabel = when (section) {
                MainSection.STUDY -> strings.tabStudy
                MainSection.DEEN -> strings.tabDeen
                MainSection.HABEEB_AI -> strings.tabAi
                MainSection.WALLET -> strings.tabWallet
                MainSection.PLANNER -> strings.tabPlanner
              }
              Text(
                text = sectionLabel,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            },
            colors = NavigationBarItemDefaults.colors(
              indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
          )
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      AnimatedContent(
        targetState = if (isOverviewActive) null else currentSection,
        transitionSpec = {
          fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
        },
        label = "LifeOsSectionTransition"
      ) { section ->
        if (section == null) {
          // Master Home Dashboard Overview
          MasterDashboardOverview(
            userName = activeUserName,
            userEmail = activeUserEmail,
            prayers = prayers,
            onTogglePrayer = { index ->
              prayers = prayers.mapIndexed { i, p ->
                if (i == index) p.copy(isCompleted = !p.isCompleted) else p
              }
            },
            tasks = tasks,
            onToggleTask = { taskId ->
              tasks = tasks.map {
                if (it.id == taskId) it.copy(isDone = !it.isDone) else it
              }
            },
            onNavigateToSection = { targetSection ->
              currentSection = targetSection
              isOverviewActive = false
            },
            onNavigateToMyFiles = { currentSubView = AppSubView.MY_FILES },
            onNavigateToSettings = { currentSubView = AppSubView.SETTINGS },
            transactions = transactions,
            currentLanguage = currentLanguage
          )
        } else {
          when (section) {
            MainSection.STUDY -> {
              StudyScreen(
                tasks = tasks,
                onToggleTask = { taskId ->
                  tasks = tasks.map {
                    if (it.id == taskId) it.copy(isDone = !it.isDone) else it
                  }
                },
                onAddTask = { title, subject ->
                  val nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                  tasks = tasks + StudentTask(nextId, title, subject, "Upcoming", false)
                },
                secondsLeft = pomodoroSecondsLeft,
                isRunning = isPomodoroRunning,
                onToggleTimer = { isPomodoroRunning = !isPomodoroRunning },
                onResetTimer = {
                  isPomodoroRunning = false
                  pomodoroSecondsLeft = 25 * 60
                }
              )
            }
            MainSection.DEEN -> {
              DeenScreen(
                prayers = prayers,
                onTogglePrayer = { index ->
                  prayers = prayers.mapIndexed { i, p ->
                    if (i == index) p.copy(isCompleted = !p.isCompleted) else p
                  }
                },
                onToggleAlarm = { index ->
                  prayers = prayers.mapIndexed { i, p ->
                    if (i == index) p.copy(isAlarmEnabled = !p.isAlarmEnabled) else p
                  }
                },
                isAudioPlaying = isAudioPlaying,
                onToggleAudio = {
                  QuranAudioService.togglePlayPause(context, QuranAudioService.currentSurahNumber.value)
                }
              )
            }
            MainSection.HABEEB_AI -> {
              HabeebAiScreen(
                messages = aiMessages,
                queryText = aiQueryText,
                onQueryTextChange = { aiQueryText = it },
                isThinking = isAiThinking,
                onSendQuery = { query ->
                  if (query.isNotBlank()) {
                    val userMsg = query.trim()
                    aiMessages = aiMessages + ("You" to userMsg)
                    aiQueryText = ""
                    isAiThinking = true
                  }
                },
                onClearChat = {
                  aiMessages = listOf("Habeeb AI" to "Chat history cleared. How can I assist you now, $activeUserName?")
                }
              )
            }
            MainSection.WALLET -> {
              WalletScreen(
                transactions = transactions,
                onAddDetailedTransaction = { title, amount, type, category, dateText, note ->
                  val nextId = (transactions.maxOfOrNull { it.id } ?: 0) + 1
                  val updated = listOf(
                    WalletTransaction(nextId, title, amount, type == TransactionType.INCOME, category, dateText, note, type)
                  ) + transactions
                  transactions = updated
                  walletStorage.saveTransactions(activeUserEmail, updated)
                },
                onAddTransaction = { title, amount, isIncome, category ->
                  val nextId = (transactions.maxOfOrNull { it.id } ?: 0) + 1
                  val type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
                  val updated = listOf(
                    WalletTransaction(nextId, title, amount, isIncome, category, "Today", "", type)
                  ) + transactions
                  transactions = updated
                  walletStorage.saveTransactions(activeUserEmail, updated)
                },
                onEditTransaction = { editedTx ->
                  val updated = transactions.map { if (it.id == editedTx.id) editedTx else it }
                  transactions = updated
                  walletStorage.saveTransactions(activeUserEmail, updated)
                },
                onDeleteTransaction = { txId ->
                  val updated = transactions.filter { it.id != txId }
                  transactions = updated
                  walletStorage.saveTransactions(activeUserEmail, updated)
                },
                currentLanguage = currentLanguage
              )
            }
            MainSection.PLANNER -> {
              PlannerScreen(
                scheduleItems = scheduleItems,
                onToggleScheduleItem = { itemId ->
                  scheduleItems = scheduleItems.map {
                    if (it.id == itemId) it.copy(isCompleted = !it.isCompleted) else it
                  }
                },
                onAddScheduleItem = { title, time, category, isDeen ->
                  val nextId = (scheduleItems.maxOfOrNull { it.id } ?: 0) + 1
                  scheduleItems = scheduleItems + PlannerItem(nextId, title, time, category, false, isDeen)
                },
                onDeleteScheduleItem = { itemId ->
                  scheduleItems = scheduleItems.filter { it.id != itemId }
                }
              )
            }
          }
        }
      }
    }
  }

  // Sign Out Confirmation Dialog
  if (showSignOutDialog) {
    AlertDialog(
      onDismissRequest = { showSignOutDialog = false },
      title = { Text(text = "Sign Out") },
      text = { Text(text = "Are you sure you want to sign out of HABEEB LF TRACK?") },
      confirmButton = {
        Button(
          onClick = {
            showSignOutDialog = false
            authViewModel.signOut()
            onSignOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.testTag("btn_confirm_sign_out")
        ) {
          Text("Sign Out")
        }
      },
      dismissButton = {
        TextButton(onClick = { showSignOutDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

// -------------------------------------------------------------
// MASTER DASHBOARD OVERVIEW: 5 PILLARS COMPREHENSIVE VIEW
// -------------------------------------------------------------
@Composable
private fun MasterDashboardOverview(
  userName: String,
  userEmail: String,
  prayers: List<PrayerTimeItem>,
  onTogglePrayer: (Int) -> Unit,
  tasks: List<StudentTask>,
  onToggleTask: (Int) -> Unit,
  onNavigateToSection: (MainSection) -> Unit,
  onNavigateToMyFiles: () -> Unit,
  onNavigateToSettings: () -> Unit,
  transactions: List<WalletTransaction> = emptyList(),
  currentLanguage: String = "English"
) {
  val strings = AppStrings.get(currentLanguage)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp)
  ) {
    // 1. Personalized Greeting Card
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .testTag("card_user_greeting"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(20.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${strings.welcomePrefix}, $userName 👋",
                style = MaterialTheme.typography.headlineSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
              Text(
                text = "HABEEB LF TRACK • Faith & Academics in Harmony",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
              )
            }
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.secondaryContainer,
              modifier = Modifier.padding(start = 8.dp)
            ) {
              Text(
                text = "14 Rabi' al-Awwal 1448 AH",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          // Daily inspirational Quran verse
          Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(
              imageVector = Icons.Default.FormatQuote,
              contentDescription = "Daily Verse",
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(24.dp)
            )
            Column {
              Text(
                text = "\"And He found you lost and guided you.\"",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.onSurface
                )
              )
              Text(
                text = "Surah Ad-Duha (93:7)",
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Medium
                )
              )
            }
          }
        }
      }
    }

    // 2. Quick Navigation Shortcut Hub (5 Pillars + My Files + Settings)
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        item {
          PillarShortcutChip(title = "Study", icon = Icons.Default.School, onClick = { onNavigateToSection(MainSection.STUDY) })
        }
        item {
          PillarShortcutChip(title = "Deen", icon = Icons.Default.Mosque, onClick = { onNavigateToSection(MainSection.DEEN) })
        }
        item {
          PillarShortcutChip(title = "AI", icon = Icons.Default.AutoAwesome, onClick = { onNavigateToSection(MainSection.HABEEB_AI) })
        }
        item {
          PillarShortcutChip(title = "Wallet", icon = Icons.Default.AccountBalanceWallet, onClick = { onNavigateToSection(MainSection.WALLET) })
        }
        item {
          PillarShortcutChip(title = "Planner", icon = Icons.Default.CalendarMonth, onClick = { onNavigateToSection(MainSection.PLANNER) })
        }
        item {
          PillarShortcutChip(title = "My Files", icon = Icons.Default.FolderShared, onClick = onNavigateToMyFiles)
        }
        item {
          PillarShortcutChip(title = "Settings", icon = Icons.Default.Settings, onClick = onNavigateToSettings)
        }
      }
    }

    // 3. 🕌 DEEN & PRAYER RING 🔔 SNAPSHOT
    item {
      val cardContext = LocalContext.current
      val dashboardPrayerSchedule = remember(prayers) {
        PrayerTimeCalculator.calculateTodayPrayers(
          context = cardContext,
          existingAlarms = prayers.associate { it.name to it.isAlarmEnabled }
        )
      }
      val hasActiveAlarm = remember(prayers) {
        prayers.any { it.isAlarmEnabled }
      }

      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              modifier = Modifier.weight(1f),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(20.dp)
              )
              Text(
                text = "Next: ${dashboardPrayerSchedule.nextPrayerName} (${dashboardPrayerSchedule.nextPrayerTime})",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
              onClick = { onNavigateToSection(MainSection.DEEN) },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
              modifier = Modifier.wrapContentSize(Alignment.CenterEnd)
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
              ) {
                Text(
                  text = "Open Deen",
                  maxLines = 1,
                  softWrap = false,
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Prayer Ring Indicator Banner
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                CircularProgressIndicator(
                  progress = { dashboardPrayerSchedule.progressFraction },
                  modifier = Modifier.size(36.dp),
                  color = MaterialTheme.colorScheme.primary,
                  trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                  strokeWidth = 4.dp
                )
                Column {
                  Text(
                    text = "Prayer Ring: ${dashboardPrayerSchedule.timeRemainingText} remaining",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                  )
                  Text(
                    text = if (hasActiveAlarm) "Exact alarm active • Makkah Adhan" else "All prayer alarms silent",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                  )
                }
              }

              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (hasActiveAlarm) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
              ) {
                Text(
                  text = if (hasActiveAlarm) "Alarm Active 🚨" else "Silent Mode 🔕",
                  style = MaterialTheme.typography.labelSmall.copy(
                    color = if (hasActiveAlarm) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                  ),
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 5 Daily Prayer mini chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            prayers.forEachIndexed { idx, p ->
              PrayerChipMini(prayer = p, onToggle = { onTogglePrayer(idx) })
            }
          }
        }
      }
    }

    // 4. 📚 STUDY PROGRESS & TASKS SNAPSHOT
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Text(
                text = "📚 Study & Assignments",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
            }
            TextButton(onClick = { onNavigateToSection(MainSection.STUDY) }) {
              Text("Open Study")
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          tasks.take(2).forEach { task ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleTask(task.id) }
                .padding(vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Checkbox(
                checked = task.isDone,
                onCheckedChange = { onToggleTask(task.id) },
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = task.title,
                  style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (task.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface
                  )
                )
                Text(
                  text = "${task.subject} • ${task.dueText}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                )
              }
            }
          }
        }
      }
    }

    // 5. 🤖 HABEEB AI QUICK ACCESS BANNER
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onNavigateToSection(MainSection.HABEEB_AI) },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(18.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          Box(
            modifier = Modifier
              .size(46.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
          }

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Ask Habeeb AI Assistant",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Academic solver, Quran & Hifz retention, and daily productivity.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
          }

          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
      }
    }

    // 6. 💰 WALLET & 📅 PLANNER ROW
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Wallet Card
        val overviewIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val overviewExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val overviewBalance = 1000.0 + overviewIncome - overviewExpenses

        Card(
          modifier = Modifier
            .weight(1f)
            .clickable { onNavigateToSection(MainSection.WALLET) },
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Text(strings.tabWallet, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "$${String.format(Locale.US, "%.2f", overviewBalance)}",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            )
            Text(
              text = "Halal Balance & Sadaqah",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          }
        }

        // Planner Card
        Card(
          modifier = Modifier
            .weight(1f)
            .clickable { onNavigateToSection(MainSection.PLANNER) },
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
              Text("Planner", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "6 Blocks",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Today's Schedule & Goals",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PillarShortcutChip(
  title: String,
  icon: ImageVector,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
    modifier = modifier.clickable { onClick() }
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Icon(imageVector = icon, contentDescription = title, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
        fontSize = 11.sp,
        maxLines = 1
      )
    }
  }
}

@Composable
private fun PrayerChipMini(
  prayer: PrayerTimeItem,
  onToggle: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (prayer.isCompleted) {
      MaterialTheme.colorScheme.primaryContainer
    } else if (prayer.isCurrent) {
      MaterialTheme.colorScheme.secondaryContainer
    } else {
      MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    },
    modifier = Modifier
      .clickable { onToggle() }
      .width(58.dp)
  ) {
    Column(
      modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = prayer.name,
        style = MaterialTheme.typography.labelSmall.copy(
          fontWeight = FontWeight.Bold,
          color = if (prayer.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
      )
      Spacer(modifier = Modifier.height(4.dp))
      if (prayer.isCompleted) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = "Completed",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(16.dp)
        )
      } else {
        Text(
          text = prayer.time.substringBefore(" "),
          fontSize = 10.sp,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
      }
    }
  }
}

private fun generateAiResponse(query: String): String {
  val q = query.lowercase()
  return when {
    q.contains("kahf") || q.contains("memoriz") || q.contains("hifz") -> {
      "Masha'Allah! When memorizing or revising (Muraja'ah):\n1. Link verses conceptually using meaning (Tafsir).\n2. Recite the new portion 20-30 times in the morning after Fajr prayer when the mind is clearest.\n3. Recite the memorized verses in your Sunnah & Nawafil prayers.\n4. Spaced repetition: Always revise older Juz before starting new pages."
    }
    q.contains("schedule") || q.contains("balance") || q.contains("time") -> {
      "Here is an optimal HABEEB LF TRACK routine:\n• 05:00 - Fajr + 30m Quran Memorization\n• 08:30 - University / Academic Classes\n• 13:00 - Dhuhr + Healthy Meal\n• 16:30 - Asr + 45m Pomodoro Study Session\n• 19:00 - Maghrib + Muraja'ah Revision\n• 20:30 - Isha + Light reading & Sleep early"
    }
    q.contains("muraja") -> {
      "Muraja'ah (مراجعة) is the Islamic practice of consistent review of memorized Quran. The Prophet ﷺ likened the Quran in memory to camels tied with ropes—if neglected, it slips away. Aim to revise at least 1 Juz every day so your entire Hifz is reviewed every 30 days."
    }
    q.contains("congregation") || q.contains("jama'ah") || q.contains("prayer") -> {
      "Praying in congregation (Jama'ah) carries 27 times more reward than praying alone (Sahih al-Bukhari). The Prophet ﷺ emphasized that if people knew the reward of the Isha and Fajr prayers in congregation, they would attend them even if they had to crawl."
    }
    q.contains("big-o") || q.contains("asymptotic") || q.contains("complexity") -> {
      "Big-O notation describes the upper bound of an algorithm's execution time or memory space as input size n grows. For example:\n• O(1): Constant time (array index lookup)\n• O(log n): Logarithmic (Binary search)\n• O(n): Linear (Single loop)\n• O(n log n): Efficient sorting (Merge sort)\n• O(n²): Nested loops (Bubble sort)"
    }
    else -> {
      "Barakallahu feek for your question! In HABEEB LF TRACK, combining continuous remembrance of Allah (Dhikr & Salah) with disciplined academic focus (Pomodoro & structured notes) leads to Barakah in both worlds."
    }
  }
}
