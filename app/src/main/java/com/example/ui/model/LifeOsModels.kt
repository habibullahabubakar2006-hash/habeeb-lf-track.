package com.example.ui.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Primary 5-Pillar Navigation Sections for HABEEB LF TRACK
 */
enum class MainSection(val title: String, val icon: ImageVector) {
  STUDY("Study", Icons.Default.School),
  DEEN("Deen", Icons.Default.Mosque),
  HABEEB_AI("Habeeb AI", Icons.Default.AutoAwesome),
  WALLET("Wallet", Icons.Default.AccountBalanceWallet),
  PLANNER("Planner", Icons.Default.CalendarMonth)
}

// -------------------------------------------------------------
// 1. DEEN MODELS
// -------------------------------------------------------------
data class PrayerTimeItem(
  val name: String,
  val time: String,
  val isCompleted: Boolean,
  val isCurrent: Boolean = false,
  val isAlarmEnabled: Boolean = true,
  val reminderMinutes: Int = 15
)

data class PrayerAlarmSettings(
  val masterAlarmEnabled: Boolean = true,
  val reminderOptionMinutes: Int = 15, // 5, 10, 15, 30
  val soundType: String = "Makkah Adhan",
  val testTriggered: Boolean = false
)

data class SurahData(
  val number: Int,
  val nameArabic: String,
  val nameEnglish: String,
  val englishMeaning: String,
  val versesCount: Int,
  val revelationType: String, // Makkiyah or Madaniyah
  val lastReadVerse: Int = 1
)

data class AyahData(
  val number: Int,
  val textArabic: String,
  val textEnglish: String,
  val isBookmarked: Boolean = false
)

data class HadithItem(
  val arabic: String,
  val translation: String,
  val narrator: String,
  val source: String
)

data class DuaItem(
  val title: String,
  val arabic: String,
  val transliteration: String,
  val translation: String,
  val category: String
)

data class AdhkarItem(
  val title: String,
  val arabic: String,
  val translation: String,
  val targetCount: Int,
  val currentCount: Int = 0
)

// -------------------------------------------------------------
// 2. STUDY MODELS
// -------------------------------------------------------------
data class StudentTask(
  val id: Int,
  val title: String,
  val subject: String,
  val dueText: String,
  val isDone: Boolean
)

data class CourseItem(
  val id: Int,
  val name: String,
  val code: String,
  val instructor: String,
  val progressPercent: Float,
  val currentTopic: String
)

data class ExamItem(
  val id: Int,
  val title: String,
  val course: String,
  val dateText: String,
  val daysRemaining: Int
)

data class StudyNote(
  val id: Int,
  val title: String,
  val subject: String,
  val content: String,
  val date: String
)

data class PdfDocumentItem(
  val id: Int,
  val title: String,
  val pagesCount: Int,
  val fileSize: String,
  val date: String,
  val category: String = "Academic Notes"
)

// -------------------------------------------------------------
// 3. WALLET MODELS
// -------------------------------------------------------------
enum class TransactionType {
  INCOME,
  EXPENSE,
  SAVINGS
}

data class WalletTransaction(
  val id: Int,
  val title: String,
  val amount: Double,
  val isIncome: Boolean = true,
  val category: String,
  val dateText: String,
  val note: String = "",
  val type: TransactionType = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE
)

// -------------------------------------------------------------
// 4. PLANNER MODELS
// -------------------------------------------------------------
data class PlannerItem(
  val id: Int,
  val title: String,
  val time: String,
  val category: String,
  val isCompleted: Boolean,
  val isDeen: Boolean = false
)

// -------------------------------------------------------------
// 5. MY FILES / PERSONAL STORAGE MODELS
// -------------------------------------------------------------
enum class FileCategory(val label: String, val iconName: String) {
  ALL("All Files", "folder"),
  PHOTOS("Photos", "image"),
  VIDEOS("Videos", "videocam"),
  DOCUMENTS("Documents", "description"),
  PDFS("PDFs", "picture_as_pdf"),
  AUDIO("Audio", "audiotrack"),
  NOTES("Notes", "edit_note"),
  QURAN("Quran", "menu_book"),
  STUDY("Study", "school"),
  PERSONAL("Personal", "lock")
}

data class UserFileItem(
  val id: Int,
  val name: String,
  val folder: String, // "Photos", "Videos", "Documents", "Quran", "Study", "Personal"
  val category: FileCategory,
  val sizeText: String,
  val dateModified: String,
  val userEmail: String,
  val previewText: String = "",
  val localUri: String? = null
)
