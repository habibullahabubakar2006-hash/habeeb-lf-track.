package com.example.ui.screens.files

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.UserFilesStorage
import com.example.ui.model.FileCategory
import com.example.ui.model.UserFileItem
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFilesScreen(
  userEmail: String,
  onNavigateBack: () -> Unit
) {
  val context = LocalContext.current
  BackHandler(onBack = onNavigateBack)

  val userFilesStorage = remember(userEmail) { UserFilesStorage(context) }

  var folders by remember {
    mutableStateOf(
      listOf("All", "Photos", "Documents", "PDFs", "Study", "Quran", "Videos", "Audio", "Notes", "Personal")
    )
  }
  var selectedFolder by remember { mutableStateOf("All") }
  var searchQuery by remember { mutableStateOf("") }

  // Persistent user files isolated for current user
  var userFiles by remember(userEmail) {
    mutableStateOf(userFilesStorage.loadFiles(userEmail))
  }

  var selectedFileForViewing by remember { mutableStateOf<UserFileItem?>(null) }
  var fileToRename by remember { mutableStateOf<UserFileItem?>(null) }
  var showNewFolderDialog by remember { mutableStateOf(false) }
  var showUploadMenuDialog by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  // 1. Real Android System Photo Picker (Multiple Selection)
  val photoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 15)
  ) { uris: List<Uri> ->
    if (uris.isEmpty()) {
      // User cancelled selection - handle silently without errors or fake stubs
      return@rememberLauncherForActivityResult
    }

    var successCount = 0
    val targetFolder = if (selectedFolder == "All") "Photos" else selectedFolder

    for (uri in uris) {
      val result = userFilesStorage.importMediaUri(
        uri = uri,
        userEmail = userEmail,
        targetFolder = targetFolder
      )
      if (result.isSuccess) {
        successCount++
      }
    }

    if (successCount > 0) {
      userFiles = userFilesStorage.loadFiles(userEmail)
      statusMessage = "✅ Successfully added $successCount photo(s) to '$targetFolder'"
      Toast.makeText(context, "Added $successCount photo(s) to My Files", Toast.LENGTH_SHORT).show()
    } else {
      statusMessage = "❌ Could not import selected photos. Check format (JPG, PNG, WEBP)."
    }
  }

  // 2. Real Android General File Picker (PDFs, Notes, Audio, Videos)
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri == null) {
      // User cancelled selection - handle silently
      return@rememberLauncherForActivityResult
    }

    val result = userFilesStorage.importMediaUri(
      uri = uri,
      userEmail = userEmail,
      targetFolder = if (selectedFolder == "All") null else selectedFolder
    )

    if (result.isSuccess) {
      val imported = result.getOrNull()
      userFiles = userFilesStorage.loadFiles(userEmail)
      statusMessage = "✅ Uploaded ${imported?.name ?: "file"} to ${imported?.folder ?: "My Files"}"
      Toast.makeText(context, "File uploaded successfully", Toast.LENGTH_SHORT).show()
    } else {
      statusMessage = "❌ Could not upload file: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
    }
  }

  val filteredFiles = remember(userFiles, selectedFolder, searchQuery) {
    userFiles.filter { file ->
      val matchesFolder = if (selectedFolder == "All") true else file.folder.equals(selectedFolder, ignoreCase = true)
      val matchesSearch = if (searchQuery.isBlank()) true else file.name.contains(searchQuery, ignoreCase = true)
      matchesFolder && matchesSearch
    }
  }

  val totalStorageUsedMb = remember(userFiles) {
    userFiles.sumOf {
      when {
        it.sizeText.contains("MB") -> it.sizeText.replace(" MB", "").toDoubleOrNull() ?: 1.5
        it.sizeText.contains("KB") -> 0.05
        else -> 1.0
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text(
              text = "🗂️ My Files",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Personal Storage • $userEmail",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(
            onClick = {
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.testTag("add_photo_header_button")
          ) {
            Icon(
              imageVector = Icons.Default.AddPhotoAlternate,
              contentDescription = "Add Photos from Gallery",
              tint = MaterialTheme.colorScheme.primary
            )
          }
          IconButton(onClick = { showNewFolderDialog = true }) {
            Icon(
              imageVector = Icons.Default.CreateNewFolder,
              contentDescription = "New Folder",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    },
    floatingActionButton = {
      Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Quick Action: Add Photo (System Photo Picker)
        FilledTonalButton(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          shape = RoundedCornerShape(16.dp),
          colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
          ),
          modifier = Modifier.testTag("upload_photo_fab")
        ) {
          Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Photo", fontWeight = FontWeight.Bold)
        }

        // Quick Action: Upload Other File (PDF, Docs, Audio)
        ExtendedFloatingActionButton(
          onClick = { showUploadMenuDialog = true },
          icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
          text = { Text("Upload") },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          shape = RoundedCornerShape(16.dp),
          modifier = Modifier.testTag("upload_file_fab")
        )
      }
    }
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
        .padding(horizontal = 16.dp)
        .testTag("my_files_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      contentPadding = PaddingValues(top = 12.dp, bottom = 88.dp)
    ) {
      // Storage Usage Indicator
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Personal Cloud & Device Storage",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "${String.format(java.util.Locale.US, "%.1f", totalStorageUsedMb)} MB / 5.0 GB",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = { (totalStorageUsedMb / 5120.0).toFloat().coerceIn(0.01f, 1f) },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "🔒 Account-isolated storage for $userEmail",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
              Text(
                text = "${userFiles.size} items",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }

      // Status notification banner
      statusMessage?.let { msg ->
        item {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (msg.startsWith("❌")) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(
                text = msg,
                style = MaterialTheme.typography.bodySmall,
                color = if (msg.startsWith("❌")) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f)
              )
              IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // Quick Upload Shortcut Banner
      item {
        Card(
          onClick = {
            photoPickerLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.PhotoLibrary,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.size(20.dp)
              )
            }
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Import Device Photos (Multi-Select)",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Opens your phone's Photo Gallery (JPG, PNG, WEBP)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
              )
            }
            Icon(
              Icons.Default.AddCircle,
              contentDescription = "Pick Photos",
              tint = MaterialTheme.colorScheme.secondary,
              modifier = Modifier.size(24.dp)
            )
          }
        }
      }

      // Search Field
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(Icons.Default.Close, contentDescription = "Clear search")
              }
            }
          },
          placeholder = { Text("Search files by name...") },
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          singleLine = true
        )
      }

      // Folder Chips (Horizontally Scrollable)
      item {
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(folders) { folderName ->
            FilterChip(
              selected = selectedFolder == folderName,
              onClick = { selectedFolder = folderName },
              label = { Text(folderName) },
              leadingIcon = {
                Icon(
                  imageVector = if (selectedFolder == folderName) Icons.Default.FolderOpen else Icons.Default.Folder,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp)
                )
              }
            )
          }
        }
      }

      // File List Header
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "$selectedFolder Files (${filteredFiles.size})",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }
      }

      if (filteredFiles.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.FolderOpen,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.outline
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                "No files in this folder",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
              Text(
                "Tap 'Add Photo' to import from your phone's Gallery",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
              )
            }
          }
        }
      }

      items(filteredFiles, key = { it.id }) { file ->
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedFileForViewing = file }
            .testTag("file_item_${file.id}"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
              horizontalArrangement = Arrangement.spacedBy(12.dp),
              modifier = Modifier.weight(1f)
            ) {
              // File Avatar / Photo Thumbnail
              if (file.category == FileCategory.PHOTOS && !file.localUri.isNullOrBlank() && File(file.localUri).exists()) {
                Box(
                  modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                ) {
                  AsyncImage(
                    model = File(file.localUri),
                    contentDescription = file.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                  )
                }
              } else {
                Box(
                  modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(getFileCategoryBgColor(file.category)),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = getFileCategoryIcon(file.category),
                    contentDescription = null,
                    tint = getFileCategoryTintColor(file.category),
                    modifier = Modifier.size(22.dp)
                  )
                }
              }

              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = file.name,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Text(
                  text = "${file.folder} • ${file.sizeText} • ${file.dateModified}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
              }
            }

            Row {
              IconButton(onClick = { fileToRename = file }, modifier = Modifier.size(32.dp)) {
                Icon(
                  Icons.Default.Edit,
                  contentDescription = "Rename",
                  tint = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.size(16.dp)
                )
              }
              IconButton(
                onClick = {
                  userFilesStorage.deleteFile(userEmail, file.id)
                  userFiles = userFilesStorage.loadFiles(userEmail)
                  statusMessage = "🗑️ Deleted ${file.name}"
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  Icons.Default.DeleteOutline,
                  contentDescription = "Delete",
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(16.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  // Upload Options Menu Dialog
  if (showUploadMenuDialog) {
    AlertDialog(
      onDismissRequest = { showUploadMenuDialog = false },
      title = { Text("Add / Upload to My Files", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          // Photo Gallery Option
          FilledTonalButton(
            onClick = {
              showUploadMenuDialog = false
              photoPickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.Start) {
              Text("Choose Photos (Gallery)", fontWeight = FontWeight.Bold)
              Text("Select one or multiple photos", style = MaterialTheme.typography.labelSmall)
            }
          }

          // Document / File Option
          OutlinedButton(
            onClick = {
              showUploadMenuDialog = false
              filePickerLauncher.launch("*/*")
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          ) {
            Icon(Icons.Default.UploadFile, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.Start) {
              Text("Upload Document or File", fontWeight = FontWeight.Bold)
              Text("PDFs, Notes, Audio, Videos", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showUploadMenuDialog = false }) { Text("Cancel") }
      }
    )
  }

  // View File Details Dialog
  selectedFileForViewing?.let { file ->
    AlertDialog(
      onDismissRequest = { selectedFileForViewing = null },
      icon = {
        Icon(
          getFileCategoryIcon(file.category),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(36.dp)
        )
      },
      title = { Text(file.name, maxLines = 2, overflow = TextOverflow.Ellipsis) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // If it's an imported photo with a local file, show full image preview
          if (file.category == FileCategory.PHOTOS && !file.localUri.isNullOrBlank() && File(file.localUri).exists()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.05f))
            ) {
              AsyncImage(
                model = File(file.localUri),
                contentDescription = file.name,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
          }

          Text("Folder: ${file.folder}")
          Text("File Type: ${file.category.label}")
          Text("Size: ${file.sizeText}")
          Text("Last Modified: ${file.dateModified}")
          Text(
            "Account: ${file.userEmail}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
          )
          Spacer(modifier = Modifier.height(4.dp))
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = file.previewText.ifEmpty { "File stored securely in private account storage container." },
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      },
      confirmButton = {
        Button(onClick = { selectedFileForViewing = null }) {
          Text("Done")
        }
      }
    )
  }

  // Rename File Dialog
  fileToRename?.let { file ->
    var newName by remember { mutableStateOf(file.name) }
    AlertDialog(
      onDismissRequest = { fileToRename = null },
      title = { Text("Rename File") },
      text = {
        OutlinedTextField(
          value = newName,
          onValueChange = { newName = it },
          label = { Text("Filename") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newName.isNotBlank()) {
              userFilesStorage.renameFile(userEmail, file.id, newName.trim())
              userFiles = userFilesStorage.loadFiles(userEmail)
              statusMessage = "Renamed to $newName"
              fileToRename = null
            }
          }
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { fileToRename = null }) { Text("Cancel") }
      }
    )
  }

  // New Folder Dialog
  if (showNewFolderDialog) {
    var newFolderName by remember { mutableStateOf("") }
    AlertDialog(
      onDismissRequest = { showNewFolderDialog = false },
      title = { Text("Create New Folder") },
      text = {
        OutlinedTextField(
          value = newFolderName,
          onValueChange = { newFolderName = it },
          label = { Text("Folder Name") },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newFolderName.isNotBlank() && !folders.contains(newFolderName.trim())) {
              folders = folders + newFolderName.trim()
              selectedFolder = newFolderName.trim()
              showNewFolderDialog = false
              statusMessage = "Created folder: $newFolderName"
            }
          }
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showNewFolderDialog = false }) { Text("Cancel") }
      }
    )
  }
}

private fun getFileCategoryIcon(category: FileCategory): ImageVector = when (category) {
  FileCategory.PHOTOS -> Icons.Default.Image
  FileCategory.VIDEOS -> Icons.Default.Videocam
  FileCategory.PDFS -> Icons.Default.PictureAsPdf
  FileCategory.AUDIO -> Icons.Default.Audiotrack
  FileCategory.NOTES -> Icons.Default.EditNote
  FileCategory.QURAN -> Icons.Default.AutoStories
  FileCategory.STUDY -> Icons.Default.School
  FileCategory.PERSONAL -> Icons.Default.Lock
  else -> Icons.Default.Description
}

@Composable
private fun getFileCategoryBgColor(category: FileCategory): Color = when (category) {
  FileCategory.PHOTOS -> MaterialTheme.colorScheme.secondaryContainer
  FileCategory.VIDEOS -> Color(0xFFFFEBEE)
  FileCategory.PDFS -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
  FileCategory.AUDIO -> MaterialTheme.colorScheme.primaryContainer
  FileCategory.NOTES -> MaterialTheme.colorScheme.surfaceVariant
  FileCategory.QURAN -> MaterialTheme.colorScheme.primaryContainer
  else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
}

@Composable
private fun getFileCategoryTintColor(category: FileCategory): Color = when (category) {
  FileCategory.PHOTOS -> MaterialTheme.colorScheme.secondary
  FileCategory.VIDEOS -> Color(0xFFC62828)
  FileCategory.PDFS -> MaterialTheme.colorScheme.error
  FileCategory.AUDIO -> MaterialTheme.colorScheme.primary
  FileCategory.NOTES -> MaterialTheme.colorScheme.onSurfaceVariant
  FileCategory.QURAN -> MaterialTheme.colorScheme.primary
  else -> MaterialTheme.colorScheme.onSurfaceVariant
}
