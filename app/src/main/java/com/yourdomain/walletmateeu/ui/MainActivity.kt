package com.yourdomain.walletmateeu.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourdomain.walletmateeu.ui.navigation.AppNavigation
import com.yourdomain.walletmateeu.ui.navigation.BottomNavItem
import com.yourdomain.walletmateeu.ui.theme.WalletMateEUTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletMateEUTheme {
                MainScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        AppNavigation(navController = navController, paddingValues = innerPadding)
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        BottomNavItem.Dashboard,
        BottomNavItem.Analytics,
        BottomNavItem.Settings
    )

    NavigationBar(
        // --- 이 부분이 수정되었습니다 ---
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            NavigationBarItem(
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onErrorContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                    selectedTextColor = MaterialTheme.colorScheme.onErrorContainer,
                    unselectedTextColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                ),
                icon = { Icon(imageVector = item.icon, contentDescription = stringResource(id = item.titleResId)) },
                label = { Text(text = stringResource(id = item.titleResId)) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}