package com.yourdomain.walletmateeu.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.yourdomain.walletmateeu.ui.navigation.AppNavigation
import com.yourdomain.walletmateeu.ui.navigation.BottomNavItem
import com.yourdomain.walletmateeu.ui.theme.WalletMateEUTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState


/**
 * 앱의 유일한 액티비티이자, 모든 UI의 진입점입니다.
 * @AndroidEntryPoint 어노테이션은 Hilt가 이 액티비티에 의존성을 주입할 수 있도록 합니다.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WalletMateEUTheme {
                // MainScreen Composable을 호출하여 앱의 전체 UI를 구성합니다.
                MainScreen()
            }
        }
    }
}

/**
 * 앱의 메인 레이아웃을 정의하는 Composable입니다.
 * Scaffold를 사용하여 상단 바, 하단 바 등 기본적인 Material Design 구조를 만듭니다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    // 네비게이션의 상태를 기억하는 NavController를 생성합니다.
    val navController = rememberNavController()
    Scaffold(
        // 하단 네비게이션 바를 Scaffold에 연결합니다.
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { innerPadding ->
        // 메인 컨텐츠 영역입니다.
        // AppNavigation Composable을 호출하여 화면 전환을 처리합니다.
        // innerPadding을 전달하여 하단 바에 의해 컨텐츠가 가려지는 것을 방지합니다.
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

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination // 현재 목적지를 가져옵니다.

        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(imageVector = screen.icon, contentDescription = screen.title) },
                label = { Text(text = screen.title) },

                // --- 이 부분이 수정되었습니다 ---
                // 현재 경로가 각 탭의 라우트 계층에 포함되는지 확인합니다.
                // 이렇게 하면 'settings' 탭에 있을 때 'category_settings'로 이동해도
                // 'settings' 탭이 여전히 선택된 상태로 유지됩니다.
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                // --- 여기까지 수정 ---

                onClick = {
                    navController.navigate(screen.route) {
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