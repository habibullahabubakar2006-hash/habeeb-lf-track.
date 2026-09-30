package com.example.ui.screens.wallet

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.localization.AppStrings
import com.example.ui.model.TransactionType
import com.example.ui.model.WalletTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WalletScreen(
  transactions: List<WalletTransaction>,
  onAddTransaction: (String, Double, Boolean, String) -> Unit = { _, _, _, _ -> },
  onAddDetailedTransaction: (String, Double, TransactionType, String, String, String) -> Unit = { _, _, _, _, _, _ -> },
  onEditTransaction: (WalletTransaction) -> Unit = {},
  onDeleteTransaction: (Int) -> Unit = {},
  currentLanguage: String = "English"
) {
  val strings = AppStrings.get(currentLanguage)

  // Dialog States
  var showAddEditDialog by remember { mutableStateOf(false) }
  var editingTransaction by remember { mutableStateOf<WalletTransaction?>(null) }
  var pendingDeleteTransaction by remember { mutableStateOf<WalletTransaction?>(null) }

  // Pre-selected type when opening add dialog via dedicated buttons
  var defaultTypeForAdd by remember { mutableStateOf(TransactionType.INCOME) }

  // Filter: 0: All, 1: Income, 2: Expenses, 3: Savings, 4: Sadaqah
  var selectedFilter by remember { mutableIntStateOf(0) }

  // Calculations
  val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
  val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
  val totalSavings = transactions.filter { it.type == TransactionType.SAVINGS }.sumOf { it.amount }

  // Base capital balance $1000 + income - expenses
  val currentBalance = 1000.00 + totalIncome - totalExpenses

  val totalSadaqah = transactions.filter {
    it.category.contains("Sadaqah", ignoreCase = true) ||
    it.category.contains("Charity", ignoreCase = true) ||
    it.title.contains("Sadaqah", ignoreCase = true)
  }.sumOf { it.amount }

  val filteredTransactions = remember(transactions, selectedFilter) {
    when (selectedFilter) {
      1 -> transactions.filter { it.type == TransactionType.INCOME }
      2 -> transactions.filter { it.type == TransactionType.EXPENSE }
      3 -> transactions.filter { it.type == TransactionType.SAVINGS }
      4 -> transactions.filter {
        it.category.contains("Sadaqah", ignoreCase = true) ||
        it.category.contains("Charity", ignoreCase = true) ||
        it.title.contains("Sadaqah", ignoreCase = true)
      }
      else -> transactions
    }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("wallet_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 28.dp)
  ) {
    // 1. Header
    item {
      Column {
        Text(
          text = "💰 " + strings.tabWallet,
          style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
          )
        )
        Text(
          text = "Halal financial tracking: Income, Expenses, Savings & Sadaqah",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f)
        )
      }
    }

    // 2. Action Buttons Row: Add Income | Add Expense | Add Savings
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Add Income Button
        Button(
          onClick = {
            editingTransaction = null
            defaultTypeForAdd = TransactionType.INCOME
            showAddEditDialog = true
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(strings.addIncome, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Add Expense Button
        Button(
          onClick = {
            editingTransaction = null
            defaultTypeForAdd = TransactionType.EXPENSE
            showAddEditDialog = true
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.ArrowUpward, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(strings.addExpense, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        // Add Savings Button
        Button(
          onClick = {
            editingTransaction = null
            defaultTypeForAdd = TransactionType.SAVINGS
            showAddEditDialog = true
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Savings, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSecondary)
          Spacer(modifier = Modifier.width(4.dp))
          Text(strings.addSavings, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSecondary)
        }
      }
    }

    // 3. Hero Total Balance Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
      ) {
        Column(modifier = Modifier.padding(22.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = strings.availableBalance,
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                letterSpacing = 1.sp
              )
            )

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
            ) {
              Text(
                text = strings.halalBudget,
                style = MaterialTheme.typography.labelSmall.copy(
                  color = MaterialTheme.colorScheme.onPrimary,
                  fontWeight = FontWeight.SemiBold
                ),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          Text(
            text = "$${String.format(Locale.US, "%.2f", currentBalance)}",
            style = MaterialTheme.typography.displayMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimary
            )
          )

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider(color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f))
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(
                text = strings.monthlyIncome,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
              )
              Text(
                text = "+$${String.format(Locale.US, "%.2f", totalIncome)}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimary
                )
              )
            }

            Column {
              Text(
                text = strings.monthlyExpenses,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
              )
              Text(
                text = "-$${String.format(Locale.US, "%.2f", totalExpenses)}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimary
                )
              )
            }

            Column {
              Text(
                text = strings.totalSavings,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
              )
              Text(
                text = "💎 $${String.format(Locale.US, "%.2f", totalSavings)}",
                style = MaterialTheme.typography.titleSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimary
                )
              )
            }
          }
        }
      }
    }

    // 4. Sadaqah Tracker Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
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
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Column {
              Text(
                text = strings.sadaqahGiven,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "\"Charity extinguishes sin as water extinguishes fire.\"",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.secondary
          ) {
            Text(
              text = "$${String.format(Locale.US, "%.2f", totalSadaqah)}",
              style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondary
              ),
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
            )
          }
        }
      }
    }

    // 5. Filter Chips
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        FilterChip(
          selected = selectedFilter == 0,
          onClick = { selectedFilter = 0 },
          label = { Text("${strings.filterAll} (${transactions.size})", fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedFilter == 1,
          onClick = { selectedFilter = 1 },
          label = { Text(strings.filterIncome, fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedFilter == 2,
          onClick = { selectedFilter = 2 },
          label = { Text(strings.filterExpenses, fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedFilter == 3,
          onClick = { selectedFilter = 3 },
          label = { Text(strings.filterSavings, fontSize = 11.sp) }
        )
        FilterChip(
          selected = selectedFilter == 4,
          onClick = { selectedFilter = 4 },
          label = { Text(strings.filterSadaqah, fontSize = 11.sp) }
        )
      }
    }

    // 6. Transaction List
    if (filteredTransactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("No transactions in this category", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
            Text("Use + Income, - Expense, or 💎 Savings above to record a transaction.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
          }
        }
      }
    }

    items(filteredTransactions, key = { it.id }) { tx ->
      Card(
        modifier = Modifier.fillMaxWidth(),
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
                .background(
                  when (tx.type) {
                    TransactionType.INCOME -> MaterialTheme.colorScheme.primaryContainer
                    TransactionType.EXPENSE -> MaterialTheme.colorScheme.errorContainer
                    TransactionType.SAVINGS -> MaterialTheme.colorScheme.secondaryContainer
                  }
                ),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = when (tx.type) {
                  TransactionType.INCOME -> Icons.Default.ArrowDownward
                  TransactionType.EXPENSE -> Icons.Default.ArrowUpward
                  TransactionType.SAVINGS -> Icons.Default.Savings
                },
                contentDescription = null,
                tint = when (tx.type) {
                  TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                  TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                  TransactionType.SAVINGS -> MaterialTheme.colorScheme.secondary
                },
                modifier = Modifier.size(20.dp)
              )
            }

            Column {
              Text(
                text = tx.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                  text = "${tx.category} • ${tx.dateText}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
              }
              if (tx.note.isNotBlank()) {
                Text(
                  text = "Note: ${tx.note}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                  fontSize = 10.sp
                )
              }
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Text(
              text = when (tx.type) {
                TransactionType.INCOME -> "+$" + String.format(Locale.US, "%.2f", tx.amount)
                TransactionType.EXPENSE -> "-$" + String.format(Locale.US, "%.2f", tx.amount)
                TransactionType.SAVINGS -> "💎 $" + String.format(Locale.US, "%.2f", tx.amount)
              },
              style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = when (tx.type) {
                  TransactionType.INCOME -> MaterialTheme.colorScheme.primary
                  TransactionType.EXPENSE -> MaterialTheme.colorScheme.error
                  TransactionType.SAVINGS -> MaterialTheme.colorScheme.secondary
                }
              )
            )

            // Edit Action
            IconButton(
              onClick = {
                editingTransaction = tx
                showAddEditDialog = true
              },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            }

            // Delete Action with confirmation dialog trigger
            IconButton(
              onClick = { pendingDeleteTransaction = tx },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }

  // -------------------------------------------------------------
  // Add / Edit Transaction Dialog
  // -------------------------------------------------------------
  if (showAddEditDialog) {
    val isEditing = editingTransaction != null
    val todayFormatted = remember {
      SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
    }

    var title by remember { mutableStateOf(editingTransaction?.title ?: "") }
    var amountText by remember { mutableStateOf(editingTransaction?.amount?.toString() ?: "") }
    var selectedType by remember { mutableStateOf(editingTransaction?.type ?: defaultTypeForAdd) }
    var category by remember {
      mutableStateOf(
        editingTransaction?.category ?: when (defaultTypeForAdd) {
          TransactionType.INCOME -> "Allowance"
          TransactionType.EXPENSE -> "Halal Food"
          TransactionType.SAVINGS -> "Savings"
        }
      )
    }
    var dateText by remember { mutableStateOf(editingTransaction?.dateText ?: todayFormatted) }
    var note by remember { mutableStateOf(editingTransaction?.note ?: "") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var amountError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showAddEditDialog = false },
      title = { Text(if (isEditing) strings.editTransaction else strings.add + " Transaction") },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Type Selector: Income | Expense | Savings
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            FilterChip(
              selected = selectedType == TransactionType.INCOME,
              onClick = {
                selectedType = TransactionType.INCOME
                if (!isEditing && category == "Halal Food" || category == "Savings") category = "Allowance"
              },
              label = { Text("Income", fontSize = 11.sp) },
              modifier = Modifier.weight(1f)
            )
            FilterChip(
              selected = selectedType == TransactionType.EXPENSE,
              onClick = {
                selectedType = TransactionType.EXPENSE
                if (!isEditing && category == "Allowance" || category == "Savings") category = "Halal Food"
              },
              label = { Text("Expense", fontSize = 11.sp) },
              modifier = Modifier.weight(1f)
            )
            FilterChip(
              selected = selectedType == TransactionType.SAVINGS,
              onClick = {
                selectedType = TransactionType.SAVINGS
                if (!isEditing) category = "Savings"
              },
              label = { Text("Savings", fontSize = 11.sp) },
              modifier = Modifier.weight(1f)
            )
          }

          // Amount Field with validation
          OutlinedTextField(
            value = amountText,
            onValueChange = {
              amountText = it
              val parsed = it.toDoubleOrNull()
              amountError = if (parsed == null || parsed <= 0.0) strings.invalidAmountError else null
            },
            label = { Text(strings.amount) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            isError = amountError != null,
            supportingText = amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
          )

          // Description / Title Field with validation
          OutlinedTextField(
            value = title,
            onValueChange = {
              title = it
              titleError = if (it.isBlank()) strings.titleRequiredError else null
            },
            label = { Text(strings.description) },
            isError = titleError != null,
            supportingText = titleError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
            modifier = Modifier.fillMaxWidth()
          )

          // Category Field
          OutlinedTextField(
            value = category,
            onValueChange = { category = it },
            label = { Text(strings.category) },
            modifier = Modifier.fillMaxWidth()
          )

          // Date Field
          OutlinedTextField(
            value = dateText,
            onValueChange = { dateText = it },
            label = { Text(strings.date) },
            modifier = Modifier.fillMaxWidth()
          )

          // Optional Note
          OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(strings.optionalNote) },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val validAmount = amountText.toDoubleOrNull()
            var hasErr = false
            if (validAmount == null || validAmount <= 0.0) {
              amountError = strings.invalidAmountError
              hasErr = true
            }
            if (title.isBlank()) {
              titleError = strings.titleRequiredError
              hasErr = true
            }

            if (!hasErr && validAmount != null) {
              if (isEditing && editingTransaction != null) {
                onEditTransaction(
                  editingTransaction!!.copy(
                    title = title.trim(),
                    amount = validAmount,
                    type = selectedType,
                    isIncome = (selectedType == TransactionType.INCOME),
                    category = category.trim().ifBlank { "General" },
                    dateText = dateText.trim().ifBlank { todayFormatted },
                    note = note.trim()
                  )
                )
              } else {
                onAddDetailedTransaction(
                  title.trim(),
                  validAmount,
                  selectedType,
                  category.trim().ifBlank { "General" },
                  dateText.trim().ifBlank { todayFormatted },
                  note.trim()
                )
                // Also trigger legacy callback for backwards compatibility
                onAddTransaction(
                  title.trim(),
                  validAmount,
                  selectedType == TransactionType.INCOME,
                  category.trim().ifBlank { "General" }
                )
              }
              showAddEditDialog = false
            }
          }
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddEditDialog = false }) {
          Text(strings.cancel)
        }
      }
    )
  }

  // -------------------------------------------------------------
  // Delete Confirmation Dialog
  // -------------------------------------------------------------
  pendingDeleteTransaction?.let { tx ->
    AlertDialog(
      onDismissRequest = { pendingDeleteTransaction = null },
      title = { Text(strings.deleteConfirmTitle) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(strings.deleteConfirmMessage)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(tx.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
              Text("${tx.category} • $${String.format(Locale.US, "%.2f", tx.amount)} (${tx.type})", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteTransaction(tx.id)
            pendingDeleteTransaction = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text(strings.delete)
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingDeleteTransaction = null }) {
          Text(strings.cancel)
        }
      }
    )
  }
}
