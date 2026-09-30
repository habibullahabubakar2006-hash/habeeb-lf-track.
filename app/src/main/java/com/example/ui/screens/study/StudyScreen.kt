package com.example.ui.screens.study

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.example.ui.model.CourseItem
import com.example.ui.model.ExamItem
import com.example.ui.model.StudentTask
import com.example.ui.model.StudyNote

@Composable
fun StudyScreen(
  tasks: List<StudentTask>,
  onToggleTask: (Int) -> Unit,
  onAddTask: (String, String) -> Unit,
  secondsLeft: Int,
  isRunning: Boolean,
  onToggleTimer: () -> Unit,
  onResetTimer: () -> Unit
) {
  // Configurable / Editable Categories & Tools
  var studyTools by remember {
    mutableStateOf(
      listOf(
        "Dashboard",
        "Courses",
        "Assignments & Exams",
        "AI Solver",
        "Calculator",
        "PDF Tools",
        "Study Notes"
      )
    )
  }
  var selectedToolIndex by remember { mutableIntStateOf(0) }

  var courses by remember {
    mutableStateOf(
      listOf(
        CourseItem(1, "Islamic Fiqh & Jurisprudence", "ISL 301", "Sheikh Dr. Tariq", 0.78f, "Transactions (Mu'amalat)"),
        CourseItem(2, "Data Structures & Algorithms", "CSC 210", "Prof. Anderson", 0.65f, "Graph Search & Trees"),
        CourseItem(3, "Classical Arabic Grammar (Nahw)", "ARB 202", "Ustadh Zayd", 0.84f, "I'rab & Nominal Sentences"),
        CourseItem(4, "Calculus & Linear Algebra", "MTH 150", "Dr. Fatima", 0.58f, "Eigenvalues & Matrices")
      )
    )
  }

  var exams by remember {
    mutableStateOf(
      listOf(
        ExamItem(1, "Fiqh Midterm Examination", "ISL 301", "Oct 12 • 09:00 AM", 5),
        ExamItem(2, "Data Structures Quiz #3", "CSC 210", "Oct 18 • 02:00 PM", 11),
        ExamItem(3, "Arabic Grammar Final Portfolio", "ARB 202", "Nov 02 • 11:59 PM", 26)
      )
    )
  }

  var notes by remember {
    mutableStateOf(
      listOf(
        StudyNote(1, "Conditions of Halal Business Contracts", "Islamic Fiqh", "1. Mutual consent. 2. Clear subject matter. 3. Price agreed upon. 4. Free from Riba & Gharar.", "Today"),
        StudyNote(2, "Dijkstra's Algorithm Priority Queue", "Computer Science", "Using binary heap: O((V+E) log V). Maintain distance array and visited set.", "Yesterday")
      )
    )
  }

  var showAddTaskDialog by remember { mutableStateOf(false) }
  var showAddCourseDialog by remember { mutableStateOf(false) }
  var showAddNoteDialog by remember { mutableStateOf(false) }

  val minutes = secondsLeft / 60
  val seconds = secondsLeft % 60
  val formattedTimer = String.format("%02d:%02d", minutes, seconds)

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("study_screen"),
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
            text = "📚 Study & Academic OS",
            style = MaterialTheme.typography.headlineSmall.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
          )
          Text(
            text = "All-in-one student suite: Courses, Calculator, PDF Tools & AI Solver",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
          )
        }
      }
    }

    // Scrollable Tool / Category Switcher (Editable & Flexible)
    item {
      ScrollableTabRow(
        selectedTabIndex = selectedToolIndex,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 0.dp
      ) {
        studyTools.forEachIndexed { index, toolName ->
          Tab(
            selected = selectedToolIndex == index,
            onClick = { selectedToolIndex = index },
            text = { Text(toolName, fontSize = 12.sp, fontWeight = if (selectedToolIndex == index) FontWeight.Bold else FontWeight.Normal) }
          )
        }
      }
    }

    when (selectedToolIndex) {
      0 -> {
        // 1. DASHBOARD & POMODORO TIMER
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(18.dp)) {
              Text(
                text = "Study Progress & Streak",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
              ) {
                StudyMetricItem(value = "7 Days", label = "Study Streak", icon = Icons.Default.LocalFireDepartment, tint = MaterialTheme.colorScheme.secondary)
                StudyMetricItem(value = "3.5 hrs", label = "Studied Today", icon = Icons.Default.Timer, tint = MaterialTheme.colorScheme.primary)
                StudyMetricItem(value = "${tasks.count { it.isDone }}/${tasks.size}", label = "Tasks Done", icon = Icons.Default.CheckCircle, tint = MaterialTheme.colorScheme.primary)
              }
            }
          }
        }

        // Pomodoro Focus Timer
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
          ) {
            Column(
              modifier = Modifier.padding(20.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = "POMODORO FOCUS STUDY TIMER",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  letterSpacing = 1.sp
                )
              )

              Spacer(modifier = Modifier.height(8.dp))

              Text(
                text = formattedTimer,
                style = MaterialTheme.typography.displayLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isRunning) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
              )

              Spacer(modifier = Modifier.height(14.dp))

              Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                  onClick = onToggleTimer,
                  colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                  ),
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(if (isRunning) "Pause" else "Start Focus (25m)")
                }

                OutlinedButton(
                  onClick = onResetTimer,
                  shape = RoundedCornerShape(12.dp)
                ) {
                  Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("Reset")
                }
              }
            }
          }
        }

        // Quick Priority Tasks
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Priority Tasks",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(onClick = { selectedToolIndex = 2 }) {
              Text("View All")
              Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
          }
        }

        items(tasks.take(3)) { task ->
          TaskCard(task = task, onToggle = { onToggleTask(task.id) })
        }
      }

      1 -> {
        // 2. COURSES & SUBJECTS
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Enrolled Courses (${courses.size})",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = { showAddCourseDialog = true }) {
              Icon(Icons.Default.AddCircle, contentDescription = "Add Course", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }

        items(courses) { course ->
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
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = course.code,
                    style = MaterialTheme.typography.labelSmall.copy(
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                Text(
                  text = "${(course.progressPercent * 100).toInt()}% Complete",
                  style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.primary
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              Text(
                text = course.name,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Instructor: ${course.instructor}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
              )
              Text(
                text = "Current Syllabus Topic: ${course.currentTopic}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
              )

              Spacer(modifier = Modifier.height(10.dp))

              LinearProgressIndicator(
                progress = { course.progressPercent },
                modifier = Modifier
                  .fillMaxWidth()
                  .height(6.dp)
                  .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
              )
            }
          }
        }
      }

      2 -> {
        // 3. ASSIGNMENTS & EXAMS
        item {
          Text(
            text = "Upcoming Exam Preparation",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
        }

        items(exams) { exam ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = exam.title,
                  style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                  text = "${exam.course} • ${exam.dateText}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
              }

              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.secondary
              ) {
                Text(
                  text = "in ${exam.daysRemaining} days",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondary
                  ),
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                )
              }
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Academic Assignments (${tasks.size})",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = { showAddTaskDialog = true }) {
              Icon(Icons.Default.AddCircle, contentDescription = "Add Task", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }

        items(tasks) { task ->
          TaskCard(task = task, onToggle = { onToggleTask(task.id) })
        }
      }

      3 -> {
        // 4. AI SOLVER
        item {
          StudyAiSolver()
        }
      }

      4 -> {
        // 5. CALCULATOR
        item {
          StudyCalculator()
        }
      }

      5 -> {
        // 6. PDF TOOLS
        item {
          StudyPdfTools()
        }
      }

      6 -> {
        // 7. STUDY NOTES
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Study Notes & Summaries (${notes.size})",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = { showAddNoteDialog = true }) {
              Icon(Icons.Default.AddCircle, contentDescription = "Add Note", tint = MaterialTheme.colorScheme.primary)
            }
          }
        }

        items(notes) { note ->
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                  Text(
                    text = note.subject,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                  )
                }
                Text(text = note.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = note.title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = note.content, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
            }
          }
        }
      }
    }
  }

  // Dialogs for Adding Task, Course, and Note
  if (showAddTaskDialog) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Computer Science") }

    AlertDialog(
      onDismissRequest = { showAddTaskDialog = false },
      title = { Text("Add Assignment") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Assignment Title") },
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject / Course") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (title.isNotBlank()) {
              onAddTask(title.trim(), subject.trim())
              showAddTaskDialog = false
            }
          }
        ) {
          Text("Add")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
      }
    )
  }

  if (showAddCourseDialog) {
    var name by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var instructor by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showAddCourseDialog = false },
      title = { Text("Add Enrolled Course") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Course Name") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = code, onValueChange = { code = it }, label = { Text("Course Code (e.g. CSC 301)") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = instructor, onValueChange = { instructor = it }, label = { Text("Instructor / Lecturer") }, modifier = Modifier.fillMaxWidth())
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isNotBlank()) {
              val newCourse = CourseItem(
                id = (courses.maxOfOrNull { it.id } ?: 0) + 1,
                name = name.trim(),
                code = if (code.isNotBlank()) code.trim() else "GEN 101",
                instructor = if (instructor.isNotBlank()) instructor.trim() else "Instructor",
                progressPercent = 0.1f,
                currentTopic = "Orientation & Introduction"
              )
              courses = courses + newCourse
              showAddCourseDialog = false
            }
          }
        ) {
          Text("Save Course")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddCourseDialog = false }) { Text("Cancel") }
      }
    )
  }

  if (showAddNoteDialog) {
    var noteTitle by remember { mutableStateOf("") }
    var noteSubject by remember { mutableStateOf("General") }
    var noteContent by remember { mutableStateOf("") }

    AlertDialog(
      onDismissRequest = { showAddNoteDialog = false },
      title = { Text("Create Study Note") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(value = noteTitle, onValueChange = { noteTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = noteSubject, onValueChange = { noteSubject = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
          OutlinedTextField(value = noteContent, onValueChange = { noteContent = it }, label = { Text("Notes / Summary") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
              val newNote = StudyNote(
                id = (notes.maxOfOrNull { it.id } ?: 0) + 1,
                title = noteTitle.trim(),
                subject = noteSubject.trim(),
                content = noteContent.trim(),
                date = "Today"
              )
              notes = listOf(newNote) + notes
              showAddNoteDialog = false
            }
          }
        ) {
          Text("Save Note")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddNoteDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
private fun StudyMetricItem(
  value: String,
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  tint: Color
) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
  }
}

@Composable
private fun TaskCard(
  task: StudentTask,
  onToggle: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable { onToggle() },
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Checkbox(
        checked = task.isDone,
        onCheckedChange = { onToggle() },
        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
      )
      Spacer(modifier = Modifier.width(10.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = task.title,
          style = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.SemiBold,
            color = if (task.isDone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.onSurface
          )
        )
        Text(
          text = "${task.subject} • Due ${task.dueText}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
      }
    }
  }
}
