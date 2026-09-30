package com.example.ui.screens.planner

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.model.PlannerItem

@Composable
fun PlannerScreen(
  scheduleItems: List<PlannerItem>,
  onToggleScheduleItem: (Int) -> Unit,
  onAddScheduleItem: (String, String, String, Boolean) -> Unit,
  onDeleteScheduleItem: (Int) -> Unit
) {
  var selectedDayIndex by remember { mutableIntStateOf(2) } // Wednesday
  val daysOfWeek = listOf("Mon 12", "Tue 13", "Wed 14", "Thu 15", "Fri 16", "Sat 17", "Sun 18")

  var scheduleFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Deen & Quran, 2: Study
  var statusFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Pending, 2: Completed
  var showAddDialog by remember { mutableStateOf(false) }

  val filteredItems = remember(scheduleItems, scheduleFilter, statusFilter) {
    scheduleItems.filter { item ->
      val matchesType = when (scheduleFilter) {
        1 -> item.isDeen
        2 -> !item.isDeen
        else -> true
      }
      val matchesStatus = when (statusFilter) {
        1 -> !item.isCompleted
        2 -> item.isCompleted
        else -> true
      }
      matchesType && matchesStatus
    }
  }

  val goals = remember {
    listOf(
      "Complete 5 daily prayers in congregation" to 0.8f,
      "Revise Surah Al-Baqarah Juz 1-2 (Muraja'ah)" to 1.0f,
      "Study 4 hours for Fiqh Midterm" to 0.75f,
      "Submit Algorithms Programming Lab" to 0.5f
    )
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("planner_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp)
  ) {
    // Header
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "📅 Planner & Timetable",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
          )
          Text(
            text = "Harmonize Deen schedules, Quran revision & study goals",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
          )
        }

        IconButton(onClick = { showAddDialog = true }) {
          Icon(Icons.Default.AddCircle, contentDescription = "Add Schedule Item", tint = MaterialTheme.colorScheme.primary)
        }
      }
    }

    // Days of Week Selector
    item {
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(daysOfWeek.indices.toList()) { index ->
          val isSelected = selectedDayIndex == index
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null,
            modifier = Modifier
              .clickable { selectedDayIndex = index }
              .width(64.dp)
          ) {
            Column(
              modifier = Modifier.padding(vertical = 12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              val parts = daysOfWeek[index].split(" ")
              Text(
                text = parts[0],
                style = MaterialTheme.typography.labelSmall.copy(
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = parts[1],
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
              )
            }
          }
        }
      }
    }

    // Weekly Goals Overview
    item {
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
            Text(
              text = "🎯 Active Goals & Milestones",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
              Text(
                text = "Week 3",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSecondaryContainer),
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          goals.forEach { (goalTitle, progress) ->
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(text = goalTitle, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                Text(text = "${(progress * 100).toInt()}%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
              }
              Spacer(modifier = Modifier.height(4.dp))
              LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary
              )
            }
          }
        }
      }
    }

    // Filter Chips: Combined / Deen / Study and Status
    item {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = scheduleFilter == 0,
            onClick = { scheduleFilter = 0 },
            label = { Text("Combined (${scheduleItems.size})") }
          )
          FilterChip(
            selected = scheduleFilter == 1,
            onClick = { scheduleFilter = 1 },
            label = { Text("🕌 Deen & Quran") }
          )
          FilterChip(
            selected = scheduleFilter == 2,
            onClick = { scheduleFilter = 2 },
            label = { Text("📚 Study") }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          FilterChip(
            selected = statusFilter == 0,
            onClick = { statusFilter = 0 },
            label = { Text("All Status") }
          )
          FilterChip(
            selected = statusFilter == 1,
            onClick = { statusFilter = 1 },
            label = { Text("Pending") }
          )
          FilterChip(
            selected = statusFilter == 2,
            onClick = { statusFilter = 2 },
            label = { Text("Completed") }
          )
        }
      }
    }

    // Timetable Timeline
    items(filteredItems) { item ->
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { onToggleScheduleItem(item.id) },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (item.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = item.isCompleted,
            onCheckedChange = { onToggleScheduleItem(item.id) },
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
          )

          Spacer(modifier = Modifier.width(10.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = item.title,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = if (item.isCompleted) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f) else MaterialTheme.colorScheme.onSurface
              )
            )
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Text(
                text = item.time,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
              )
              Text(
                text = "• ${item.category}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (item.isDeen) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant
          ) {
            Text(
              text = if (item.isDeen) "Deen" else "Study",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = if (item.isDeen) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }

          IconButton(
            onClick = { onDeleteScheduleItem(item.id) },
            modifier = Modifier.size(28.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Delete", tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(16.dp))
          }
        }
      }
    }
  }

  // Add Schedule Item Dialog
  if (showAddDialog) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("04:00 PM") }
    var category by remember { mutableStateOf("Study Block") }
    var isDeen by remember { mutableStateOf(false) }

    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Add Timetable Block") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = isDeen,
              onClick = { isDeen = true },
              label = { Text("🕌 Deen / Quran") }
            )
            FilterChip(
              selected = !isDeen,
              onClick = { isDeen = false },
              label = { Text("📚 Study") }
            )
          }

          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event / Task Title") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Time (e.g. 02:30 PM - 04:00 PM)") },
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text("Category (e.g. Salah, Lecture, Focus Block)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              onAddScheduleItem(title.trim(), time.trim(), category.trim(), isDeen)
              showAddDialog = false
            }
          }
        ) {
          Text("Add")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}
