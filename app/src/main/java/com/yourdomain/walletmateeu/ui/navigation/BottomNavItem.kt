package com.yourdomain.walletmateeu.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.yourdomain.walletmateeu.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleResId: Int,
    val icon: ImageVector
) {
    object Dashboard : BottomNavItem("dashboard", R.string.bottom_nav_dashboard, Icons.Default.Dashboard)
    object Analytics : BottomNavItem("analytics", R.string.bottom_nav_analytics, Icons.Default.Analytics)
    object Settings : BottomNavItem("settings", R.string.bottom_nav_settings, Icons.Default.Settings)
}