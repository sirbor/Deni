package com.loki.deni.presentation

import android.os.Bundle
import android.graphics.Color
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.loki.deni.presentation.ui.theme.DeniTheme
import com.loki.deni.ui.screens.AppLockScreen
import com.loki.deni.ui.navigation.DeniNavGraph
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.components.DeniBottomNav
import com.loki.deni.ui.viewmodel.AuthViewModel
import com.loki.deni.ui.viewmodel.NotificationsViewModel
import com.loki.deni.ui.viewmodel.ThemeViewModel
import androidx.core.view.WindowCompat
import com.loki.deni.util.AuthEvent
import com.loki.deni.util.AuthEvents
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import androidx.fragment.app.FragmentActivity

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = Color.TRANSPARENT
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        setContent {
            var isLocked by remember { mutableStateOf(false) }
            val themeViewModel: ThemeViewModel = hiltViewModel()
            val authViewModel: AuthViewModel = hiltViewModel()
            val notificationsViewModel: NotificationsViewModel = hiltViewModel()
            val isDarkTheme by themeViewModel.darkModeEnabled.collectAsStateWithLifecycle()
            val unreadCount by notificationsViewModel.unreadCount.collectAsStateWithLifecycle()
            DeniTheme(darkTheme = isDarkTheme) {
                val navController = rememberNavController()
                var showExitDialog by remember { mutableStateOf(false) }
                Box(modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surface)
                    .fillMaxSize()) {
                    if (isLocked) {
                        AppLockScreen(onUnlock = { isLocked = false })
                    } else {
                        val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route.orEmpty()
                        val bottomNavRoutePrefixes = listOf(
                            Routes.HOME,
                            Routes.LOANS,
                            Routes.TRANSACTIONS,
                            Routes.NOTIFICATIONS,
                            Routes.PROFILE,
                            Routes.EDIT_PROFILE,
                            Routes.PERSONAL_INFO,
                            Routes.PAYMENT_METHODS,
                            Routes.KYC,
                            Routes.PRICING,
                            Routes.SETTINGS,
                            Routes.SECURITY,
                            Routes.SUPPORT,
                            Routes.CLOSE_ACCOUNT,
                            "support_chat",
                            "support_ticket",
                            "login_activity",
                            "change_pin",
                            "linked_accounts",
                            "referral",
                            "apply",
                            "borrow_amount",
                            "borrow_tenure",
                            "borrow_summary",
                            "borrow_review",
                            "borrow_success",
                            "summary",
                            "success",
                            "repay",
                            "repay_success",
                            "loan_detail",
                            "loan_schedule",
                            "loan_topup",
                        )
                        val showBottomNav = bottomNavRoutePrefixes.any { prefix ->
                            currentRoute == prefix || currentRoute.startsWith("$prefix/")
                        }
                        BackHandler(enabled = showBottomNav && (currentRoute == Routes.HOME || currentRoute == Routes.LOANS || currentRoute == Routes.TRANSACTIONS || currentRoute == Routes.PROFILE)) {
                            showExitDialog = true
                        }
                        Scaffold(
                            containerColor = ComposeColor.Transparent,
                            contentWindowInsets = WindowInsets(0, 0, 0, 0),
                            bottomBar = {
                                if (showBottomNav) {
                                    DeniBottomNav(navController = navController, unreadCount = unreadCount)
                                }
                            },
                        ) { paddingValues ->
                            LaunchedEffect(Unit) {
                                navController.navigate("splash") {
                                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                }
                            }
                            LaunchedEffect(Unit) {
                                AuthEvents.events.collectLatest { event ->
                                    if (event == AuthEvent.SessionExpired) {
                                        navController.navigate(Routes.WELCOME) {
                                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                                        }
                                    }
                                }
                            }
                            DeniNavGraph(
                                isDarkTheme = isDarkTheme,
                                navController = navController,
                                onThemeToggle = { themeViewModel.toggleDarkMode() },
                                startDestination = "splash",
                                modifier = Modifier.padding(paddingValues),
                            )
                        }
                        if (showExitDialog) {
                            AlertDialog(
                                onDismissRequest = { showExitDialog = false },
                                title = { Text("Exit app?") },
                                text = { Text("Press Exit to close Deni.") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showExitDialog = false
                                        finish()
                                    }) { Text("Exit") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showExitDialog = false }) { Text("Cancel") }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
