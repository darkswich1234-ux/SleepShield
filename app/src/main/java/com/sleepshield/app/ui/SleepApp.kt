package com.sleepshield.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.sleepshield.app.ui.screens.DashboardScreen
import com.sleepshield.app.ui.screens.OnboardingScreen
import com.sleepshield.app.ui.screens.ScheduleScreen
import com.sleepshield.app.ui.screens.SettingsScreen
import com.sleepshield.app.ui.screens.ShieldScreen
import com.sleepshield.app.ui.theme.SleepShieldTheme

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Onboarding : Screen("onboarding", "Setup", Icons.Default.Settings)
    object Dashboard : Screen("dashboard", "Overview", Icons.Default.Home)
    object Shield : Screen("shield", "Shield", Icons.Default.Shield)
    object Schedule : Screen("schedule", "Schedule", Icons.Default.Event)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
}

@Composable
fun SleepApp(viewModel: SleepViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()
    val items = listOf(Screen.Dashboard, Screen.Shield, Screen.Schedule)

    SleepShieldTheme(isAmoled = uiState.isAmoled) {
        val startDestination = if (uiState.onboardingComplete) Screen.Dashboard.route else Screen.Onboarding.route

        Scaffold(
            bottomBar = {
                if (uiState.onboardingComplete) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.primary
                    ) {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = null) },
                                label = { Text(screen.label) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Onboarding.route) {
                    OnboardingScreen(
                        viewModel = viewModel,
                        onFinish = {
                            navController.navigate(Screen.Dashboard.route) {
                                popUpTo(Screen.Onboarding.route) { inclusive = true }
                            }
                        }
                    )
                }
                composable(Screen.Dashboard.route) {
                    DashboardScreen(
                        viewModel = viewModel,
                        onNavigateToShield = { navController.navigate(Screen.Shield.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }
                composable(Screen.Shield.route) {
                    ShieldScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.Schedule.route) {
                    ScheduleScreen(viewModel = viewModel)
                }
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
