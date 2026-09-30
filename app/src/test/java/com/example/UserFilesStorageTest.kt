package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.UserFilesStorage
import com.example.ui.model.FileCategory
import com.example.ui.model.UserFileItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserFilesStorageTest {

  private lateinit var context: Context
  private lateinit var storage: UserFilesStorage

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    storage = UserFilesStorage(context)
  }

  @Test
  fun testUserFilesLoadingAndIsolation() {
    val user1 = "testuser1@gmail.com"
    val user2 = "testuser2@gmail.com"

    val files1 = storage.loadFiles(user1)
    assertTrue("User 1 files should not be empty", files1.isNotEmpty())

    // Add a custom file for user 1
    val customItem = UserFileItem(
      id = 999,
      name = "My_Special_Photo.jpg",
      folder = "Photos",
      category = FileCategory.PHOTOS,
      sizeText = "3.2 MB",
      dateModified = "Today",
      userEmail = user1,
      previewText = "Imported test photo"
    )
    storage.saveFiles(user1, listOf(customItem) + files1)

    val updatedFiles1 = storage.loadFiles(user1)
    assertEquals(files1.size + 1, updatedFiles1.size)
    assertEquals("My_Special_Photo.jpg", updatedFiles1[0].name)

    // User 2 must NOT have user 1's custom file
    val files2 = storage.loadFiles(user2)
    val foundInUser2 = files2.any { it.name == "My_Special_Photo.jpg" }
    assertEquals("User 2 should not have User 1's private files", false, foundInUser2)
  }

  @Test
  fun testRenameAndDeleteFile() {
    val user = "isolated_user@example.com"
    val initial = storage.loadFiles(user)
    val targetId = initial[0].id

    // Rename
    storage.renameFile(user, targetId, "Renamed_Academic_Paper.pdf")
    val renamedList = storage.loadFiles(user)
    val renamed = renamedList.find { it.id == targetId }
    assertNotNull(renamed)
    assertEquals("Renamed_Academic_Paper.pdf", renamed?.name)

    // Delete
    storage.deleteFile(user, targetId)
    val afterDelete = storage.loadFiles(user)
    assertEquals(initial.size - 1, afterDelete.size)
  }
}
