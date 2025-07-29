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

/**
 * 하단 네비게이션 바 UI를 생성하는 Composable입니다.
 */
@Composable
fun BottomNavigationBar(navController: NavController) {
    // 표시할 탭 아이템 목록
    val items = listOf(
        BottomNavItem.Timeline,
        BottomNavItem.Analytics,
        BottomNavItem.Settings
    )

    NavigationBar {
        // 현재 화면의 경로를 실시간으로 추적합니다.
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // 각 아이템을 NavigationBar에 추가합니다.
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(imageVector = item.icon, contentDescription = item.title) },
                label = { Text(text = item.title) },
                // 현재 경로와 아이템의 경로가 일치하는지 확인하여 선택 상태를 결정합니다.
                selected = currentRoute == item.route,
                onClick = {
                    // 아이템 클릭 시 해당 경로로 이동합니다.
                    navController.navigate(item.route) {
                        // 백 스택 관리를 위한 옵션들
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