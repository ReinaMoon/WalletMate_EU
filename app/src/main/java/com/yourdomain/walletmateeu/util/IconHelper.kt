package com.yourdomain.walletmateeu.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconHelper {

    // 모든 아이콘 이름 목록 (검색용)
    // 참고: 실제로는 수천 개에 달하므로, 여기서는 일부만 예시로 작성합니다.
    // 실제 프로젝트에서는 빌드 스크립트 등으로 자동 생성하는 것을 권장합니다.
    val allIconNames = listOf(
        "Home", "AccountBalanceWallet", "ShoppingCart", "Receipt", "LocalPizza", "Fastfood",
        "Restaurant", "LocalCafe", "LocalBar", "Flight", "DirectionsCar", "Train",
        "Subway", "LocalHospital", "Movie", "FitnessCenter", "School", "Pets",
        "CardGiftcard", "Checkroom", "Computer", "PhoneAndroid", "Savings", "CreditCard"
        // ... 계속해서 아이콘 이름을 추가할 수 있습니다.
    )

    // 아이콘 이름을 실제 ImageVector로 변환
    fun getIcon(iconName: String): ImageVector {
        return when (iconName) {
            "Home" -> Icons.Filled.Home
            "AccountBalanceWallet" -> Icons.Filled.AccountBalanceWallet
            "ShoppingCart" -> Icons.Filled.ShoppingCart
            "Receipt" -> Icons.Filled.Receipt
            "LocalPizza" -> Icons.Filled.LocalPizza
            "Fastfood" -> Icons.Filled.Fastfood
            "Restaurant" -> Icons.Filled.Restaurant
            "LocalCafe" -> Icons.Filled.LocalCafe
            "LocalBar" -> Icons.Filled.LocalBar
            "Flight" -> Icons.Filled.Flight
            "DirectionsCar" -> Icons.Filled.DirectionsCar
            "Train" -> Icons.Filled.Train
            "Subway" -> Icons.Filled.Subway
            "LocalHospital" -> Icons.Filled.LocalHospital
            "Movie" -> Icons.Filled.Movie
            "FitnessCenter" -> Icons.Filled.FitnessCenter
            "School" -> Icons.Filled.School
            "Pets" -> Icons.Filled.Pets
            "CardGiftcard" -> Icons.Filled.CardGiftcard
            "Checkroom" -> Icons.Filled.Checkroom
            "Computer" -> Icons.Filled.Computer
            "PhoneAndroid" -> Icons.Filled.PhoneAndroid
            "Savings" -> Icons.Filled.Savings
            "CreditCard" -> Icons.Filled.CreditCard
            else -> Icons.Filled.Category // 기본값
        }
    }
}