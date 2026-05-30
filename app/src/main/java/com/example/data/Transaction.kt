package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: String, // "income" or "expense"
    val category: String, // "Salary", "Invest", "Gift", "Other" (Income) or "Food", "Rent", "Fun", "Shop", "Travel", "Health", "Edu", "Utility", "Sent"
    val timestamp: Long, // epoch millis
    val note: String? = null,
    val status: String = "COMPLETED" // "COMPLETED" or "PENDING"
) : Serializable
