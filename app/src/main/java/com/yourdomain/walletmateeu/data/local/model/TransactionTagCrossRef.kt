package com.yourdomain.walletmateeu.data.local.model

import androidx.room.Entity

@Entity(tableName = "transaction_tag_cross_ref", primaryKeys = ["transactionId", "tagId"])
data class TransactionTagCrossRef(
    val transactionId: String,
    val tagId: String
)