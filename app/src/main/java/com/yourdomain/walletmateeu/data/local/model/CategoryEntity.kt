package com.yourdomain.walletmateeu.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String, // "EXPENSE" or "INCOME" : 필드 추가!
    val icon: String,
    val color: String
)