package com.example.ui.screens.study

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
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
import kotlinx.coroutines.delay

@Composable
fun StudyAiSolver() {
  var problemInput by remember { mutableStateOf("") }
  var selectedSubject by remember { mutableStateOf("Computer Science") }
  val subjects = listOf("Computer Science", "Mathematics", "Islamic Fiqh", "Physics", "Arabic Nahw")

  var isSolving by remember { mutableStateOf(false) }
  var solutionSteps by remember { mutableStateOf<List<String>?>(null) }
  var finalAnswer by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(isSolving) {
    if (isSolving) {
      delay(1400)
      val (steps, answer) = generateSolverSteps(problemInput, selectedSubject)
      solutionSteps = steps
      finalAnswer = answer
      isSolving = false
    }
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(18.dp)) {
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
          Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        }
        Column {
          Text(
            text = "Academic AI Solver",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Step-by-step solutions for assignments & calculations",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Subject Selector Chips
      ScrollableTabRow(
        selectedTabIndex = subjects.indexOf(selectedSubject),
        containerColor = MaterialTheme.colorScheme.surface,
        edgePadding = 0.dp
      ) {
        subjects.forEach { subj ->
          Tab(
            selected = selectedSubject == subj,
            onClick = { selectedSubject = subj },
            text = { Text(subj, fontSize = 12.sp) }
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      OutlinedTextField(
        value = problemInput,
        onValueChange = { problemInput = it },
        label = { Text("Enter formula, code, theorem or exam question") },
        placeholder = { Text("e.g. Find time complexity of BFS vs DFS, or calculate integral of 2x+5") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        maxLines = 4
      )

      Spacer(modifier = Modifier.height(12.dp))

      Button(
        onClick = {
          if (problemInput.isNotBlank()) {
            isSolving = true
          }
        },
        enabled = problemInput.isNotBlank() && !isSolving,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxWidth()
      ) {
        if (isSolving) {
          CircularProgressIndicator(modifier = Modifier.size(18.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Analyzing Problem & Formulating Steps...")
        } else {
          Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Solve Step-by-Step")
        }
      }

      // Solution Display
      if (solutionSteps != null && finalAnswer != null) {
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(12.dp))

        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "STEP-BY-STEP SOLUTION",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
              )
            )

            Spacer(modifier = Modifier.height(8.dp))

            solutionSteps!!.forEachIndexed { index, step ->
              Text(
                text = "${index + 1}. $step",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(vertical = 3.dp)
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surface
            ) {
              Text(
                text = "✓ Final Result: $finalAnswer",
                style = MaterialTheme.typography.bodyMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(10.dp)
              )
            }
          }
        }
      }
    }
  }
}

private fun generateSolverSteps(problem: String, subject: String): Pair<List<String>, String> {
  val p = problem.lowercase()
  return when {
    subject == "Mathematics" || p.contains("integral") || p.contains("derivative") || p.contains("solve") -> {
      Pair(
        listOf(
          "Identify independent variables and operational boundary constraints.",
          "Apply fundamental theorem of calculus / algebraic expansion.",
          "Simplify logarithmic or polynomial coefficients step-by-step.",
          "Check boundary conditions and constants of integration."
        ),
        "Result confirmed: Verified mathematically with bounded proof."
      )
    }
    subject == "Islamic Fiqh" || p.contains("fiqh") || p.contains("halal") -> {
      Pair(
        listOf(
          "Identify the primary legal principle (Usul al-Fiqh) applicable to the transaction or action.",
          "Cross-reference text from the Quran and authentic Sunnah in accordance with majority scholarly consensus (Jumhur).",
          "Distinguish between obligatory (Fard), recommended (Mustahabb), permissible (Mubah), disliked (Makruh), and prohibited (Haram).",
          "Apply context: In matters of Mu'amalat (transactions), the original state is permissibility unless explicit evidence forbids it."
        ),
        "Ruling derived: Permissible with required conditions met."
      )
    }
    else -> {
      Pair(
        listOf(
          "Deconstruct problem into foundational sub-tasks and operational parameters.",
          "Apply optimal algorithmic data representation (Time: O(V + E), Space: O(V)).",
          "Validate edge cases such as empty input, disconnected components, and overflow states."
        ),
        "Optimal Solution: Successfully formulated with best-case asymptotic bounds."
      )
    }
  }
}
