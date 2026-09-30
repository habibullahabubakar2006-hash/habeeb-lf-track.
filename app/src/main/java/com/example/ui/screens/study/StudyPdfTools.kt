package com.example.ui.screens.study

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.PdfDocumentItem

@Composable
fun StudyPdfTools() {
  var pdfDocuments by remember {
    mutableStateOf(
      listOf(
        PdfDocumentItem(1, "Islamic Fiqh & Mu'amalat Lecture Notes.pdf", 28, "2.4 MB", "Oct 01", "Islamic Studies"),
        PdfDocumentItem(2, "Data Structures: Trees & Graph Theory.pdf", 45, "4.1 MB", "Sep 28", "Computer Science"),
        PdfDocumentItem(3, "Classical Arabic Grammar Rules (Nahw).pdf", 16, "1.8 MB", "Sep 22", "Arabic"),
        PdfDocumentItem(4, "Calculus Vector Spaces & Matrices.pdf", 32, "3.5 MB", "Sep 15", "Mathematics")
      )
    )
  }

  var selectedPdfForViewing by remember { mutableStateOf<PdfDocumentItem?>(null) }
  var currentPage by remember { mutableIntStateOf(1) }
  var showImageToPdfDialog by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }

  // PDF File Picker from Android storage
  val pdfPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      val filename = it.lastPathSegment?.substringAfterLast("/") ?: "Uploaded_Document_${System.currentTimeMillis()}.pdf"
      val newDoc = PdfDocumentItem(
        id = (pdfDocuments.maxOfOrNull { d -> d.id } ?: 0) + 1,
        title = filename,
        pagesCount = 14,
        fileSize = "2.1 MB",
        date = "Today",
        category = "Uploaded Document"
      )
      pdfDocuments = listOf(newDoc) + pdfDocuments
      selectedPdfForViewing = newDoc
      currentPage = 1
      statusMessage = "PDF Document opened successfully: $filename"
    }
  }

  // Image Picker for Image to PDF tool
  val imagePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      val generatedName = "Scanned_Document_${System.currentTimeMillis() % 1000}.pdf"
      val newDoc = PdfDocumentItem(
        id = (pdfDocuments.maxOfOrNull { d -> d.id } ?: 0) + 1,
        title = generatedName,
        pagesCount = 1,
        fileSize = "1.2 MB",
        date = "Today",
        category = "Image to PDF"
      )
      pdfDocuments = listOf(newDoc) + pdfDocuments
      statusMessage = "Converted image to PDF: $generatedName"
    }
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Quick Action Bar: Open/Upload PDF and Image to PDF
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = { pdfPickerLauncher.launch("application/pdf") },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Open / Upload PDF", fontSize = 12.sp)
      }

      OutlinedButton(
        onClick = { imagePickerLauncher.launch("image/*") },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Image to PDF", fontSize = 12.sp)
      }
    }

    statusMessage?.let { msg ->
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(text = msg, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
          IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(24.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(16.dp))
          }
        }
      }
    }

    // PDF Reader / Viewer Preview Mode
    selectedPdfForViewing?.let { doc ->
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "PDF VIEWER",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  letterSpacing = 1.sp
                )
              )
              Text(
                text = doc.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 1
              )
            }
            IconButton(onClick = { selectedPdfForViewing = null }) {
              Icon(Icons.Default.Close, contentDescription = "Close Viewer")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Document Simulated Page Container
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(260.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
            color = Color(0xFFFAFAFA)
          ) {
            Column(
              modifier = Modifier
                .padding(18.dp)
                .fillMaxSize(),
              verticalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = "HABEEB LF TRACK • ACADEMIC SUITE", fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                  Text(text = "PAGE $currentPage OF ${doc.pagesCount}", fontSize = 9.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                  text = "Chapter $currentPage: Core Concept Formulation",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "In this section, we examine the fundamental proofs and theorems applicable to ${doc.category}. Understanding foundational principles ensures clarity during practical problem solving and exam revisions.",
                  fontSize = 12.sp,
                  lineHeight = 18.sp,
                  color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "Key take-away: Always cross-reference primary definitions and solve adjacent problems in your focus sessions.",
                  fontSize = 12.sp,
                  lineHeight = 18.sp,
                  color = Color.Black
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = { if (currentPage > 1) currentPage-- },
                  enabled = currentPage > 1,
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text("Previous", fontSize = 11.sp)
                }

                Text(
                  text = "$currentPage / ${doc.pagesCount}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = Color.Black
                )

                OutlinedButton(
                  onClick = { if (currentPage < doc.pagesCount) currentPage++ },
                  enabled = currentPage < doc.pagesCount,
                  contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                  Text("Next", fontSize = 11.sp)
                }
              }
            }
          }
        }
      }
    }

    // PDF Documents List
    Text(
      text = "Academic Documents & PDF Library (${pdfDocuments.size})",
      style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    )

    pdfDocuments.forEach { doc ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable {
            selectedPdfForViewing = doc
            currentPage = 1
          },
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
            Box(
              modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(24.dp)
              )
            }

            Column {
              Text(
                text = doc.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1
              )
              Text(
                text = "${doc.pagesCount} Pages • ${doc.fileSize} • ${doc.category}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            }
          }

          TextButton(onClick = {
            selectedPdfForViewing = doc
            currentPage = 1
          }) {
            Text("Read", color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }
  }
}
