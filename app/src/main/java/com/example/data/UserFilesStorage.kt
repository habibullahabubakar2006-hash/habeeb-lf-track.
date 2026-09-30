package com.example.data

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.ui.model.FileCategory
import com.example.ui.model.UserFileItem
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Manages persistent storage of user files isolated per user email.
 * Copies uploaded media/photos to app-private storage and maintains metadata in SharedPreferences.
 */
class UserFilesStorage(private val context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("habeeb_files_storage", Context.MODE_PRIVATE)

  private fun getKeyForUser(userEmail: String): String {
    val clean = userEmail.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
    return "files_v1_$clean"
  }

  fun loadFiles(userEmail: String): List<UserFileItem> {
    val key = getKeyForUser(userEmail)
    val jsonString = prefs.getString(key, null)
    if (jsonString.isNullOrBlank()) {
      val initial = getDefaultFiles(userEmail)
      saveFiles(userEmail, initial)
      return initial
    }

    return try {
      val jsonArray = JSONArray(jsonString)
      val list = mutableListOf<UserFileItem>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val catStr = obj.optString("category", "DOCUMENTS")
        val category = try {
          FileCategory.valueOf(catStr)
        } catch (_: Exception) {
          FileCategory.DOCUMENTS
        }

        list.add(
          UserFileItem(
            id = obj.optInt("id", i + 1),
            name = obj.optString("name", "File"),
            folder = obj.optString("folder", "Documents"),
            category = category,
            sizeText = obj.optString("sizeText", "1.0 MB"),
            dateModified = obj.optString("dateModified", "Today"),
            userEmail = userEmail,
            previewText = obj.optString("previewText", ""),
            localUri = obj.optString("localUri").takeIf { it.isNotBlank() }
          )
        )
      }
      list
    } catch (e: Exception) {
      Log.e("UserFilesStorage", "Error parsing files for $userEmail", e)
      getDefaultFiles(userEmail)
    }
  }

  fun saveFiles(userEmail: String, files: List<UserFileItem>) {
    val key = getKeyForUser(userEmail)
    try {
      val jsonArray = JSONArray()
      for (item in files) {
        val obj = JSONObject()
        obj.put("id", item.id)
        obj.put("name", item.name)
        obj.put("folder", item.folder)
        obj.put("category", item.category.name)
        obj.put("sizeText", item.sizeText)
        obj.put("dateModified", item.dateModified)
        obj.put("userEmail", item.userEmail)
        obj.put("previewText", item.previewText)
        obj.put("localUri", item.localUri ?: "")
        jsonArray.put(obj)
      }
      prefs.edit().putString(key, jsonArray.toString()).apply()
    } catch (e: Exception) {
      Log.e("UserFilesStorage", "Error saving files for $userEmail", e)
    }
  }

  /**
   * Copies an external picked Uri (Photo or Document) into app-private storage
   * and creates a persisted UserFileItem tied to the specified userEmail.
   */
  fun importMediaUri(
    uri: Uri,
    userEmail: String,
    targetFolder: String? = null
  ): Result<UserFileItem> {
    return try {
      val contentResolver = context.contentResolver
      var displayName = "Photo_${System.currentTimeMillis()}.jpg"
      var sizeBytes: Long = 0

      // Query display name and size from content provider
      contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
        if (cursor.moveToFirst()) {
          if (nameIndex != -1) {
            val name = cursor.getString(nameIndex)
            if (!name.isNullOrBlank()) displayName = name
          }
          if (sizeIndex != -1) {
            sizeBytes = cursor.getLong(sizeIndex)
          }
        }
      }

      // Check extension & category
      val ext = displayName.substringAfterLast(".", "").lowercase()
      val isPhoto = ext in listOf("jpg", "jpeg", "png", "webp")
      val category = when {
        isPhoto -> FileCategory.PHOTOS
        ext in listOf("mp4", "mkv", "mov", "avi") -> FileCategory.VIDEOS
        ext in listOf("pdf") -> FileCategory.PDFS
        ext in listOf("mp3", "wav", "m4a", "aac") -> FileCategory.AUDIO
        ext in listOf("txt", "md") -> FileCategory.NOTES
        else -> FileCategory.DOCUMENTS
      }

      val folder = targetFolder ?: when (category) {
        FileCategory.PHOTOS -> "Photos"
        FileCategory.VIDEOS -> "Videos"
        FileCategory.PDFS -> "PDFs"
        FileCategory.AUDIO -> "Audio"
        FileCategory.NOTES -> "Notes"
        else -> "Documents"
      }

      // Prepare user-isolated destination file in private storage
      val safeEmail = userEmail.trim().lowercase().replace(Regex("[^a-z0-9]"), "_")
      val userDir = File(context.filesDir, "user_files/$safeEmail")
      if (!userDir.exists()) {
        userDir.mkdirs()
      }

      val cleanFileName = "${System.currentTimeMillis()}_${displayName.replace(Regex("[^a-zA-Z0-9._-]"), "_")}"
      val destFile = File(userDir, cleanFileName)

      contentResolver.openInputStream(uri)?.use { input ->
        FileOutputStream(destFile).use { output ->
          input.copyTo(output)
        }
      } ?: return Result.failure(Exception("Could not open media stream"))

      val actualSize = if (sizeBytes > 0) sizeBytes else destFile.length()
      val sizeFormatted = formatFileSize(actualSize)

      val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
      val todayStr = dateFormat.format(Date())

      val currentFiles = loadFiles(userEmail)
      val nextId = (currentFiles.maxOfOrNull { it.id } ?: 0) + 1

      val newFile = UserFileItem(
        id = nextId,
        name = displayName,
        folder = folder,
        category = category,
        sizeText = sizeFormatted,
        dateModified = "Today, $todayStr",
        userEmail = userEmail,
        previewText = if (isPhoto) "Photo imported from device Gallery/Photo Picker." else "Imported personal document.",
        localUri = destFile.absolutePath
      )

      val updatedList = listOf(newFile) + currentFiles
      saveFiles(userEmail, updatedList)
      Result.success(newFile)
    } catch (e: Exception) {
      Log.e("UserFilesStorage", "Failed to import media URI: $uri", e)
      Result.failure(e)
    }
  }

  fun deleteFile(userEmail: String, fileId: Int) {
    val current = loadFiles(userEmail)
    val toDelete = current.find { it.id == fileId }
    toDelete?.localUri?.let { path ->
      try {
        val f = File(path)
        if (f.exists()) f.delete()
      } catch (e: Exception) {
        Log.w("UserFilesStorage", "Could not delete local file $path", e)
      }
    }
    val updated = current.filter { it.id != fileId }
    saveFiles(userEmail, updated)
  }

  fun renameFile(userEmail: String, fileId: Int, newName: String) {
    val current = loadFiles(userEmail)
    val updated = current.map {
      if (it.id == fileId) it.copy(name = newName) else it
    }
    saveFiles(userEmail, updated)
  }

  private fun formatFileSize(bytes: Long): String {
    return when {
      bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes.toDouble() / (1024 * 1024))
      bytes >= 1024 -> "${bytes / 1024} KB"
      bytes > 0 -> "$bytes B"
      else -> "1.0 MB"
    }
  }

  private fun getDefaultFiles(userEmail: String): List<UserFileItem> {
    return listOf(
      UserFileItem(1, "Fiqh_Transactions_Summary.pdf", "Documents", FileCategory.PDFS, "2.4 MB", "Oct 01", userEmail, "Summary of legal Islamic contracts in business and ethical finance."),
      UserFileItem(2, "Data_Structures_Lecture_Slides.pdf", "Study", FileCategory.PDFS, "4.8 MB", "Sep 28", userEmail, "Trees, heaps, graph traversal BFS/DFS algorithmic analysis."),
      UserFileItem(3, "Surah_AlMulk_Recitation.mp3", "Quran", FileCategory.AUDIO, "8.2 MB", "Sep 25", userEmail, "Mishary Rashid Alafasy complete Ayah recitation audio recording."),
      UserFileItem(4, "Islamic_Campus_Conference_Photo.jpg", "Photos", FileCategory.PHOTOS, "3.1 MB", "Sep 18", userEmail, "High-resolution photo from Islamic Society youth workshop."),
      UserFileItem(5, "Exam_Preparation_Notes.txt", "Notes", FileCategory.NOTES, "14 KB", "Today", userEmail, "Formula sheets, key proofs, and memorization tips."),
      UserFileItem(6, "Hifz_Spaced_Repetition_Table.pdf", "Quran", FileCategory.PDFS, "1.1 MB", "Yesterday", userEmail, "Spaced repetition matrix for retention of Juz 1 through 18."),
      UserFileItem(7, "Algorithms_Problem_Walkthrough.mp4", "Videos", FileCategory.VIDEOS, "45.0 MB", "Sep 12", userEmail, "Video lecture recording on dynamic programming and memoization.")
    )
  }
}
