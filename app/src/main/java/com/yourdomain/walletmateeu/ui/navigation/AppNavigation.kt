package com.yourdomain.walletmateeu.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.yourdomain.walletmateeu.ui.feature_analytics.AnalyticsScreen
import com.yourdomain.walletmateeu.ui.feature_settings.CategorySettingsScreen
import com.yourdomain.walletmateeu.ui.feature_settings.SettingsScreen
import com.yourdomain.walletmateeu.ui.feature_timeline.AddEditTransactionScreen
import com.yourdomain.walletmateeu.ui.feature_timeline.TimelineScreen

object Routes {
    const val TIMELINE = "timeline"
    const val ANALYTICS = "analytics"
    const val SETTINGS = "settings"
    const val ADD_EDIT_TRANSACTION = "add_edit_transaction"
    const val CATEGORY_SETTINGS = "category_settings"
}

@Composable
fun AppNavigation(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = Routes.TIMELINE,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(Routes.TIMELINE) {
            TimelineScreen(
                onNavigateToAddTransaction = {
                    navController.navigate(Routes.ADD_EDIT_TRANSACTION) {
                        // 타임라인 상태는 저장하고 복원
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable(Routes.ANALYTICS) { AnalyticsScreen() }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateToCategorySettings = {
                    navController.navigate(Routes.CATEGORY_SETTINGS)
                }
            )
        }
        composable(Routes.ADD_EDIT_TRANSACTION) {
            AddEditTransactionScreen(
                onSaveCompleted = { navController.popBackStack() }
            )
        }
        composable(Routes.CATEGORY_SETTINGS) {
            CategorySettingsScreen()
        }
    }
}