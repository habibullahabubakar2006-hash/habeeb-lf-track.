package com.example.ui.screens.study

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*

@Composable
fun StudyCalculator() {
  var displayExpr by remember { mutableStateOf("") }
  var resultText by remember { mutableStateOf("0") }
  var isScientificMode by remember { mutableStateOf(false) }

  fun evaluate(expression: String): String {
    try {
      if (expression.isBlank()) return "0"
      val sanitized = expression.replace("×", "*").replace("÷", "/")
      // Simple parser for basic math operations
      val tokens = mutableListOf<String>()
      var currentNumber = StringBuilder()

      var i = 0
      while (i < sanitized.length) {
        val c = sanitized[i]
        if (c.isDigit() || c == '.') {
          currentNumber.append(c)
        } else if (c in listOf('+', '-', '*', '/', '%')) {
          if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
            currentNumber = StringBuilder()
          }
          tokens.add(c.toString())
        }
        i++
      }
      if (currentNumber.isNotEmpty()) {
        tokens.add(currentNumber.toString())
      }

      if (tokens.isEmpty()) return "0"

      // First pass: *, /, %
      var idx = 0
      val intermediate = mutableListOf<String>()
      while (idx < tokens.size) {
        val t = tokens[idx]
        if (t == "*" || t == "/" || t == "%") {
          val left = intermediate.removeAt(intermediate.lastIndex).toDoubleOrNull() ?: 0.0
          val right = tokens.getOrNull(idx + 1)?.toDoubleOrNull() ?: 1.0
          val res = when (t) {
            "*" -> left * right
            "/" -> if (right != 0.0) left / right else Double.NaN
            "%" -> left % right
            else -> 0.0
          }
          intermediate.add(res.toString())
          idx += 2
        } else {
          intermediate.add(t)
          idx++
        }
      }

      // Second pass: +, -
      var result = intermediate.firstOrNull()?.toDoubleOrNull() ?: 0.0
      var j = 1
      while (j < intermediate.size) {
        val op = intermediate[j]
        val nextVal = intermediate.getOrNull(j + 1)?.toDoubleOrNull() ?: 0.0
        if (op == "+") {
          result += nextVal
        } else if (op == "-") {
          result -= nextVal
        }
        j += 2
      }

      return if (result.isNaN()) "Error" else if (result % 1.0 == 0.0) result.toLong().toString() else String.format("%.4f", result).trimEnd('0').trimEnd('.')
    } catch (e: Exception) {
      return "Error"
    }
  }

  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      // Header with mode toggle
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = if (isScientificMode) "Scientific Calculator" else "Standard Calculator",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        TextButton(onClick = { isScientificMode = !isScientificMode }) {
          Text(if (isScientificMode) "Standard" else "Scientific", color = MaterialTheme.colorScheme.primary)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Display Screen
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.End
        ) {
          Text(
            text = if (displayExpr.isEmpty()) "0" else displayExpr,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            maxLines = 1,
            textAlign = TextAlign.End
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = resultText,
            style = MaterialTheme.typography.headlineMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            ),
            maxLines = 1,
            textAlign = TextAlign.End
          )
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // Scientific Extra Row (if enabled)
      if (isScientificMode) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          CalcButton("√", Modifier.weight(1f), isSpecial = true) {
            val v = resultText.toDoubleOrNull() ?: 0.0
            val res = sqrt(v)
            resultText = if (res.isNaN()) "Error" else if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.4f", res)
            displayExpr = "√($v)"
          }
          CalcButton("sin", Modifier.weight(1f), isSpecial = true) {
            val v = resultText.toDoubleOrNull() ?: 0.0
            val res = sin(Math.toRadians(v))
            resultText = String.format("%.4f", res)
            displayExpr = "sin($v°)"
          }
          CalcButton("cos", Modifier.weight(1f), isSpecial = true) {
            val v = resultText.toDoubleOrNull() ?: 0.0
            val res = cos(Math.toRadians(v))
            resultText = String.format("%.4f", res)
            displayExpr = "cos($v°)"
          }
          CalcButton("x²", Modifier.weight(1f), isSpecial = true) {
            val v = resultText.toDoubleOrNull() ?: 0.0
            val res = v * v
            resultText = if (res % 1.0 == 0.0) res.toLong().toString() else String.format("%.4f", res)
            displayExpr = "($v)²"
          }
        }
        Spacer(modifier = Modifier.height(8.dp))
      }

      // Main Button Grid
      val rows = listOf(
        listOf("C", "⌫", "%", "÷"),
        listOf("7", "8", "9", "×"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "+"),
        listOf("0", "00", ".", "=")
      )

      rows.forEach { row ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          row.forEach { btnText ->
            val isOp = btnText in listOf("÷", "×", "-", "+", "=")
            val isAction = btnText in listOf("C", "⌫", "%")
            CalcButton(
              text = btnText,
              modifier = Modifier.weight(1f),
              isOperator = isOp,
              isSpecial = isAction
            ) {
              when (btnText) {
                "C" -> {
                  displayExpr = ""
                  resultText = "0"
                }
                "⌫" -> {
                  if (displayExpr.isNotEmpty()) {
                    displayExpr = displayExpr.dropLast(1)
                    resultText = evaluate(displayExpr)
                  }
                }
                "=" -> {
                  resultText = evaluate(displayExpr)
                }
                else -> {
                  displayExpr += btnText
                  resultText = evaluate(displayExpr)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CalcButton(
  text: String,
  modifier: Modifier = Modifier,
  isOperator: Boolean = false,
  isSpecial: Boolean = false,
  onClick: () -> Unit
) {
  val bgColor = when {
    text == "=" -> MaterialTheme.colorScheme.primary
    isOperator -> MaterialTheme.colorScheme.primaryContainer
    isSpecial -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
  }

  val textColor = when {
    text == "=" -> MaterialTheme.colorScheme.onPrimary
    isOperator -> MaterialTheme.colorScheme.primary
    isSpecial -> MaterialTheme.colorScheme.onSecondaryContainer
    else -> MaterialTheme.colorScheme.onSurface
  }

  Box(
    modifier = modifier
      .height(48.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .clickable { onClick() },
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Bold,
        fontSize = if (text.length > 2) 14.sp else 18.sp
      ),
      color = textColor
    )
  }
}
