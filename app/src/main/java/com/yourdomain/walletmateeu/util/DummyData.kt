package com.yourdomain.walletmateeu.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.House
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Train
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Label


object DummyData {
    val categoryIcons = mapOf(
        "ic_default" to Icons.Default.Label,
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
        // 빨, 주, 노, 초, 파, 남, 보
        Color(0xFFE53935), // Red
        Color(0xFFFB8C00), // Orange
        Color(0xFFFFD600), // Yellow
        Color(0xFF43A047), // Green
        Color(0xFF1E88E5), // Blue
        Color(0xFF3949AB), // Indigo
        Color(0xFF8E24AA), // Purple
        // 흰색, 밝은 회색, 어두운 회색, 검은색
        Color(0xFFFFFFFF), // White
        Color(0xFFBDBDBD), // Light Gray
        Color(0xFF616161), // Dark Gray
        Color(0xFF212121), // Black
    )
}