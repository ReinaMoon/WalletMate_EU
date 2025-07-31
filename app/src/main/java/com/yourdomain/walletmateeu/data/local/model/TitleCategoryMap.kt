package com.yourdomain.walletmateeu.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "title_category_map")
data class TitleCategoryMap(
    @PrimaryKey val title: String,
    val categoryId: String
)