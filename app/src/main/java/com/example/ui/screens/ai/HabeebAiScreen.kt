package com.example.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.study.StudyAiSolver

@Composable
fun HabeebAiScreen(
  messages: List<Pair<String, String>>,
  queryText: String,
  onQueryTextChange: (String) -> Unit,
  isThinking: Boolean,
  onSendQuery: (String) -> Unit,
  onClearChat: () -> Unit
) {
  var activeMode by remember { mutableIntStateOf(0) } // 0: Chat Assistant, 1: AI Problem Solver
  var showClearChatDialog by remember { mutableStateOf(false) }

  val quickPrompts = listOf(
    "Tips for memorizing Surah Al-Kahf",
    "How to balance Islamic studies & exams?",
    "Explain Muraja'ah spaced repetition",
    "4-Hour daily student study timetable",
    "What is the ruling on praying in congregation?",
    "Explain Big-O asymptotic notation simply"
  )

  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(16.dp)
      .testTag("habeeb_ai_screen")
  ) {
    // Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondaryContainer),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(22.dp)
          )
        }
        Column {
          Text(
            text = "🤖 Habeeb AI Assistant",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
          )
          Text(
            text = "Quran, Hifz, Academics & Islamic Guidance",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
          )
        }
      }

      if (activeMode == 0 && messages.size > 1) {
        IconButton(onClick = { showClearChatDialog = true }) {
          Icon(Icons.Default.DeleteSweep, contentDescription = "Clear Chat", tint = MaterialTheme.colorScheme.error)
        }
      }
    }

    Spacer(modifier = Modifier.height(10.dp))

    // Mode Selector: Chat vs AI Solver
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      FilterChip(
        selected = activeMode == 0,
        onClick = { activeMode = 0 },
        label = { Text("💬 Chat Assistant") },
        modifier = Modifier.weight(1f)
      )
      FilterChip(
        selected = activeMode == 1,
        onClick = { activeMode = 1 },
        label = { Text("📐 AI Problem Solver") },
        modifier = Modifier.weight(1f)
      )
    }

    Spacer(modifier = Modifier.height(10.dp))

    if (activeMode == 1) {
      // Integrated AI Problem Solver
      Box(modifier = Modifier.weight(1f)) {
        StudyAiSolver()
      }
      return
    }

    // Quick prompt suggestion chips
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.padding(bottom = 10.dp)
    ) {
      items(quickPrompts) { chip ->
        SuggestionChip(
          onClick = { onSendQuery(chip) },
          label = { Text(chip, fontSize = 12.sp) }
        )
      }
    }

    // Message list
    LazyColumn(
      modifier = Modifier
        .weight(1f)
        .fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      contentPadding = PaddingValues(vertical = 4.dp)
    ) {
      items(messages) { (sender, text) ->
        val isUser = sender == "You"
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
          Card(
            shape = RoundedCornerShape(
              topStart = 16.dp,
              topEnd = 16.dp,
              bottomStart = if (isUser) 16.dp else 4.dp,
              bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
              containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.widthIn(max = 310.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              if (!isUser) {
                Text(
                  text = "Habeeb AI",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                  )
                )
                Spacer(modifier = Modifier.height(4.dp))
              }
              Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }

      if (isThinking) {
        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(8.dp)
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(18.dp),
              strokeWidth = 2.dp,
              color = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "Habeeb AI is thinking...",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
          }
        }
      }
    }

    // Input Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      OutlinedTextField(
        value = queryText,
        onValueChange = onQueryTextChange,
        placeholder = { Text("Ask Habeeb AI anything...") },
        modifier = Modifier
          .weight(1f)
          .testTag("input_ai_query"),
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedBorderColor = MaterialTheme.colorScheme.primary
        ),
        maxLines = 3
      )

      FilledIconButton(
        onClick = { onSendQuery(queryText) },
        enabled = queryText.isNotBlank() && !isThinking,
        colors = IconButtonDefaults.filledIconButtonColors(
          containerColor = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
          .size(52.dp)
          .testTag("btn_send_ai_query")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.Send,
          contentDescription = "Send AI Query",
          tint = MaterialTheme.colorScheme.onPrimary
        )
      }
    }
  }

  // Clear Chat Dialog
  if (showClearChatDialog) {
    AlertDialog(
      onDismissRequest = { showClearChatDialog = false },
      title = { Text("Clear Chat History") },
      text = { Text("Are you sure you want to clear your conversation with Habeeb AI?") },
      confirmButton = {
        Button(
          onClick = {
            showClearChatDialog = false
            onClearChat()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Clear")
        }
      },
      dismissButton = {
        TextButton(onClick = { showClearChatDialog = false }) { Text("Cancel") }
      }
    )
  }
}
