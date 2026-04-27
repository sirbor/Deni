package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.Policy
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.AuthViewModel
import com.loki.deni.ui.viewmodel.ProfileViewModel

@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val notifications by viewModel.isNotificationsOn.collectAsStateWithLifecycle()
    val darkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = "Settings", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ProfileHeroCard(
                title = "Security & Preferences",
                subtitle = "Manage alerts, privacy, account access and session controls.",
            )
            ProfileSurfaceCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SettingsPill(
                        value = if (notifications) "ON" else "OFF",
                        label = "Alerts",
                        modifier = Modifier.weight(1f),
                    )
                    SettingsPill(
                        value = if (darkMode) "DARK" else "LIGHT",
                        label = "Theme",
                        modifier = Modifier.weight(1f),
                    )
                    SettingsPill(
                        value = "SAFE",
                        label = "Account",
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            ProfileSurfaceCard {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingToggleRow(
                        title = "Notifications",
                        subtitle = if (notifications) "Alerts are enabled" else "Alerts are muted",
                        icon = Icons.Outlined.NotificationsActive,
                        enabled = notifications,
                        onToggle = { viewModel.setNotificationsEnabled(!notifications) },
                    )
                    SettingToggleRow(
                        title = "Dark Mode",
                        subtitle = if (darkMode) "Using dark theme" else "Using light theme",
                        icon = Icons.Outlined.DarkMode,
                        enabled = darkMode,
                        onToggle = { viewModel.setDarkModeEnabled(!darkMode) },
                    )
                    SettingsActionRow(
                        title = "Manage Biometrics",
                        subtitle = "Configure fingerprint and device authentication",
                        icon = Icons.Outlined.AdminPanelSettings,
                        onClick = { navController.navigate(Routes.SECURITY) },
                    )
                    SettingsActionRow(
                        title = "Change PIN",
                        subtitle = "Update your login PIN securely",
                        icon = Icons.Outlined.LockReset,
                        onClick = { navController.navigate(Routes.AUTH_PHONE) },
                    )
                    SettingsActionRow(
                        title = "Login Activity",
                        subtitle = "Review devices and account sessions",
                        icon = Icons.Outlined.ManageAccounts,
                        onClick = { navController.navigate("login_activity") },
                    )
                }
            }
            ProfileSurfaceCard {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SettingsActionRow(
                        title = "Terms & Privacy",
                        subtitle = "Legal, policies and support resources",
                        icon = Icons.Outlined.Policy,
                        onClick = { navController.navigate(Routes.SUPPORT) },
                    )
                    SettingsActionRow(
                        title = "Logout",
                        subtitle = "Sign out from this device",
                        icon = Icons.Outlined.Logout,
                        onClick = {
                            authViewModel.signOut()
                            navController.navigate(Routes.WELCOME) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            }
                        },
                    )
                }
            }
            ProfileSurfaceCard {
                Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f), CircleShape)
                                .padding(8.dp),
                        ) {
                            Icon(Icons.Outlined.WarningAmber, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Danger Zone", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                            Text(
                                "Deleting your account removes profile and loan history permanently.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                    ProfilePrimaryButton(
                        text = "Delete Account",
                        onClick = { navController.navigate(Routes.CLOSE_ACCOUNT) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    ProfileSurfaceCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f), RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                        .padding(9.dp),
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column {
                    Text(title, style = MaterialTheme.typography.titleSmall)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
            }
            CompactToggleChip(
                enabled = enabled,
                onClick = onToggle,
            )
        }
    }
}

@Composable
private fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                    .padding(8.dp),
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SettingsPill(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun CompactToggleChip(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(86.dp)
            .height(34.dp)
            .background(
                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(11.dp),
            )
            .border(
                1.dp,
                if (enabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.32f),
                RoundedCornerShape(11.dp),
            )
            .padding(horizontal = 8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = if (enabled) "ON" else "OFF",
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
