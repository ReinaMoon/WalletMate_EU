package com.yourdomain.walletmateeu.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

object DummyData {
    val categoryIcons = mapOf(
        "ic_category" to Icons.Default.Category, // <<--- 기본 아이콘 추가
        "ic_fastfood" to Icons.Default.Fastfood,
        "ic_shopping_cart" to Icons.Default.ShoppingCart,
        "ic_train" to Icons.Default.Train,
        "ic_house" to Icons.Default.House,
        "ic_hospital" to Icons.Default.LocalHospital,
        "ic_school" to Icons.Default.School,
        "ic_giftcard" to Icons.Default.CardGiftcard,
        "ic_account_balance" to Icons.Default.AccountBalance
    )

    val categoryColors = listOf(
        Color(0xFFE53935), Color(0xFFFB8C00), Color(0xFFFFD600), Color(0xFF43A047),
        Color(0xFF1E88E5), Color(0xFF3949AB), Color(0xFF8E24AA), Color(0xFFFFFFFF),
        Color(0xFFBDBDBD), Color(0xFF616161), Color(0xFF212121)
    )
}