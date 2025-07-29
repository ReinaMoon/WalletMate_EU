package com.yourdomain.walletmateeu.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL // 카테고리 삭제 시 categoryId를 NULL로 설정
        )
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val amount: Double,
    val type: String, // "EXPENSE" or "INCOME"
    val date: Long,
    val categoryId: String?, // <<--- Nullable로 변경
    val lastModified: Long
)