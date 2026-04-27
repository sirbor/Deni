package com.loki.deni.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.loki.deni.R
import com.loki.deni.ui.navigation.Routes

@Composable
fun DeniBottomNav(
    navController: NavController,
    unreadCount: Int = 0,
) {
    val isDark = isSystemInDarkTheme()
    val currentEntry = navController.currentBackStackEntryAsState().value
    val currentRoute = currentEntry?.destination?.route.orEmpty()
    val items = listOf(
        BottomNavItem(Routes.HOME, stringResource(R.string.home_tab), Icons.Outlined.Home),
        BottomNavItem(Routes.LOANS, stringResource(R.string.loans_tab), Icons.Outlined.CreditCard),
        BottomNavItem(Routes.TRANSACTIONS, stringResource(R.string.transactions_tab), Icons.AutoMirrored.Outlined.CompareArrows),
        BottomNavItem(Routes.PROFILE, stringResource(R.string.profile_tab), Icons.Outlined.Person),
    )

    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .height(74.dp)
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                        )
                    } else {
                        listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                        )
                    },
                ),
            )
            .border(
                1.dp,
                if (isDark) MaterialTheme.colorScheme.outline.copy(alpha = 0.28f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
                RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            ),
    ) {
        NavigationBar(
            modifier = Modifier.height(74.dp),
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            tonalElevation = 0.dp,
        ) {
            items.forEach { item ->
                val selected = currentRoute == item.route || currentRoute.startsWith("${item.route}/")
                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = false
                            }
                            launchSingleTop = true
                            restoreState = false
                        }
                    },
                    icon = {
                        Box {
                            Icon(imageVector = item.icon, contentDescription = item.label)
                            if (item.route == Routes.HOME && unreadCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(MaterialTheme.colorScheme.error, CircleShape)
                                        .align(Alignment.TopEnd),
                                )
                            }
                        }
                    },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = item.label, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            if (selected) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                                )
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.22f else 0.12f),
                    ),
                )
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
