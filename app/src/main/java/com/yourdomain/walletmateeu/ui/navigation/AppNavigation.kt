package com.yourdomain.walletmateeu.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yourdomain.walletmateeu.ui.feature_analytics.AnalyticsScreen
import com.yourdomain.walletmateeu.ui.feature_dashboard.DashboardScreen
import com.yourdomain.walletmateeu.ui.feature_settings.*
import com.yourdomain.walletmateeu.ui.feature_timeline.AddEditTransactionScreen

object Routes {
    const val DASHBOARD = "dashboard"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val ADD_EDIT_TRANSACTION = "add_edit_transaction"
    const val CATEGORY_SETTINGS = "category_settings"
    const val TAG_SETTINGS = "tag_settings"
    const val ICON_PICKER = "icon_picker"
}

@Composable
fun AppNavigation(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToAddTransaction = { navController.navigate(Routes.ADD_EDIT_TRANSACTION) }
            )
        }
        composable(Routes.ANALYTICS) {
            AnalyticsScreen()
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToCategorySettings = { navController.navigate(Routes.CATEGORY_SETTINGS) },
                onNavigateToTagSettings = { navController.navigate(Routes.TAG_SETTINGS) }
            )
        }
        composable(
            route = "${Routes.ADD_EDIT_TRANSACTION}?transactionId={transactionId}",
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType; nullable = true })
        ) {
            AddEditTransactionScreen(onSaveCompleted = { navController.popBackStack() })
        }
        composable(Routes.CATEGORY_SETTINGS) {
            CategorySettingsScreen(
                onNavigateToIconPicker = { navController.navigate(Routes.ICON_PICKER) },
                navController = navController,
                // --- 이 부분이 수정되었습니다 ---
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.TAG_SETTINGS) {
            TagSettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ICON_PICKER) {
            IconPickerScreen(
                onIconSelected = { iconName ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("selected_icon", iconName)
                    navController.popBackStack()
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}