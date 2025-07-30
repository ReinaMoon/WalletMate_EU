package com.yourdomain.walletmateeu.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String, // stringResource(id = R.string.xxx) 를 사용하는 것이 이상적
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem( // <<--- 이름 변경
        route = Routes.DASHBOARD, // <<--- 경로 이름 변경
        title = "Dashboard",    // <<--- 탭 제목 변경
        icon = Icons.Default.Dashboard // <<--- 아이콘 변경 (Dashboard 아이콘 임포트 필요)
    )
    object Analytics : BottomNavItem(
        route = Routes.ANALYTICS, // 새 경로
        title = "Analytics",
        icon = Icons.Default.Analytics
    )
    object Settings : BottomNavItem(
        route = Routes.SETTINGS, // 새 경로
        title = "Settings",
        icon = Icons.Default.Settings
    )
}