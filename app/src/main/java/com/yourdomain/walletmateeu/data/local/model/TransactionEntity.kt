package com.yourdomain.walletmateeu.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val date: Long,
    val categoryId: String,
    val lastModified: Long
)