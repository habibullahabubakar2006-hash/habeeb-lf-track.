package com.example.data

import android.content.Context
import android.content.SharedPreferences
import com.example.ui.model.TransactionType
import com.example.ui.model.WalletTransaction
import org.json.JSONArray
import org.json.JSONObject

/**
 * Manages persistent storage of wallet transactions per logged-in user.
 */
class WalletStorage(context: Context) {
  private val prefs: SharedPreferences =
    context.getSharedPreferences("habeeb_wallet_data", Context.MODE_PRIVATE)

  fun loadTransactions(userEmail: String): List<WalletTransaction> {
    val key = getKeyForUser(userEmail)
    val jsonString = prefs.getString(key, null)
    if (jsonString.isNullOrBlank()) {
      val initial = getDefaultTransactions()
      saveTransactions(userEmail, initial)
      return initial
    }

    return try {
      val jsonArray = JSONArray(jsonString)
      val list = mutableListOf<WalletTransaction>()
      for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)
        val typeStr = obj.optString("type", "")
        val isIncome = obj.optBoolean("isIncome", false)
        val type = when {
          typeStr.equals("INCOME", ignoreCase = true) -> TransactionType.INCOME
          typeStr.equals("SAVINGS", ignoreCase = true) -> TransactionType.SAVINGS
          typeStr.equals("EXPENSE", ignoreCase = true) -> TransactionType.EXPENSE
          isIncome -> TransactionType.INCOME
          else -> TransactionType.EXPENSE
        }
        list.add(
          WalletTransaction(
            id = obj.optInt("id", i + 1),
            title = obj.optString("title", "Transaction"),
            amount = obj.optDouble("amount", 0.0),
            isIncome = (type == TransactionType.INCOME),
            category = obj.optString("category", "General"),
            dateText = obj.optString("dateText", "Today"),
            note = obj.optString("note", ""),
            type = type
          )
        )
      }
      list
    } catch (e: Exception) {
      getDefaultTransactions()
    }
  }

  fun saveTransactions(userEmail: String, transactions: List<WalletTransaction>) {
    val key = getKeyForUser(userEmail)
    val jsonArray = JSONArray()
    for (tx in transactions) {
      val obj = JSONObject().apply {
        put("id", tx.id)
        put("title", tx.title)
        put("amount", tx.amount)
        put("isIncome", tx.type == TransactionType.INCOME)
        put("category", tx.category)
        put("dateText", tx.dateText)
        put("note", tx.note)
        put("type", tx.type.name)
      }
      jsonArray.put(obj)
    }
    prefs.edit().putString(key, jsonArray.toString()).apply()
  }

  private fun getKeyForUser(email: String): String {
    val clean = if (email.isBlank()) "guest_user" else email.trim().lowercase().replace("@", "_").replace(".", "_")
    return "wallet_txs_$clean"
  }

  private fun getDefaultTransactions(): List<WalletTransaction> {
    return listOf(
      WalletTransaction(
        id = 1,
        title = "Monthly Student Stipend",
        amount = 350.00,
        isIncome = true,
        category = "Allowance",
        dateText = "Oct 01",
        note = "Campus educational grant",
        type = TransactionType.INCOME
      ),
      WalletTransaction(
        id = 2,
        title = "Academic Textbooks & Notes",
        amount = 45.00,
        isIncome = false,
        category = "Education",
        dateText = "Today",
        note = "Fiqh & Computer Science manuals",
        type = TransactionType.EXPENSE
      ),
      WalletTransaction(
        id = 3,
        title = "Emergency & Hifz Graduation Fund",
        amount = 200.00,
        isIncome = false,
        category = "Savings",
        dateText = "Yesterday",
        note = "Dedicated spiritual & academic milestones savings",
        type = TransactionType.SAVINGS
      ),
      WalletTransaction(
        id = 4,
        title = "Campus Halal Lunch",
        amount = 12.50,
        isIncome = false,
        category = "Halal Food",
        dateText = "Yesterday",
        note = "Cafeteria meal",
        type = TransactionType.EXPENSE
      ),
      WalletTransaction(
        id = 5,
        title = "Friday Sadaqah Donation",
        amount = 15.00,
        isIncome = false,
        category = "Sadaqah / Charity",
        dateText = "Last Friday",
        note = "Jumu'ah mosque charity box",
        type = TransactionType.EXPENSE
      )
    )
  }
}
