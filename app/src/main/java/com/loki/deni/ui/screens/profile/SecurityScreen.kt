package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LockReset
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.ProfileViewModel
import com.loki.deni.util.BiometricAuth
import kotlinx.coroutines.launch

@Composable
fun SecurityScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val biometricReady = BiometricAuth.isAvailable(context)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val biometricsEnabled by viewModel.isBiometricsOn.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SnackbarHost(hostState = snackbarHostState)
        DeniTopBar(title = "Security", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileHeroCard(
                title = "Security Center",
                subtitle = "Protect your account with biometric access and session controls.",
            )
            ProfileSurfaceCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SecurityPill(value = if (biometricsEnabled) "ON" else "OFF", label = "Fingerprint", modifier = Modifier.weight(1f))
                    SecurityPill(value = "PIN", label = "Access", modifier = Modifier.weight(1f))
                    SecurityPill(value = "SAFE", label = "Device", modifier = Modifier.weight(1f))
                }
            }
            ProfileSurfaceCard {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.linearGradient(
                                        if (isDark) listOf(Color(0xFF0D2C30), Color(0xFF11545A)) else listOf(Color(0xFF014D52), Color(0xFF01696F)),
                                    ),
                                    CircleShape,
                                )
                                .padding(8.dp),
                        ) {
                            Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = Color.White)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Biometric unlock", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                            Text(
                                if (biometricsEnabled) "Enabled and ready on this device" else "Disabled. Enable for faster secure login",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                    ProfilePrimaryButton(
                        text = if (biometricsEnabled) "Disable Fingerprint" else "Enable Fingerprint",
                        onClick = {
                            if (biometricsEnabled) {
                                viewModel.setBiometricsEnabled(false)
                                scope.launch { snackbarHostState.showSnackbar("Fingerprint login disabled.") }
                            } else if (!BiometricAuth.isAvailable(context)) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Biometric authentication is not available on this device.")
                                }
                            } else {
                                BiometricAuth.authenticate(
                                    context = context,
                                    title = "Enable Fingerprint Login",
                                    subtitle = "Confirm your identity to enable biometric sign in.",
                                    onSuccess = {
                                        viewModel.setBiometricsEnabled(true)
                                        scope.launch { snackbarHostState.showSnackbar("Fingerprint login enabled.") }
                                    },
                                    onError = { message ->
                                        scope.launch { snackbarHostState.showSnackbar(message) }
                                    },
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = if (biometricReady) {
                            "Biometric ready on this device."
                        } else {
                            "Biometric not available on this device."
                        },
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            ProfileSurfaceCard {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Session Security", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleSmall)
                    SecurityActionRow(
                        title = "Active sessions",
                        subtitle = "This device is trusted and currently signed in",
                        icon = Icons.Outlined.Shield,
                        onClick = { navController.navigate("login_activity") },
                    )
                    SecurityActionRow(
                        title = "Login Activity",
                        subtitle = "Review sign in attempts and recent access",
                        icon = Icons.Outlined.History,
                        onClick = { navController.navigate("login_activity") },
                    )
                }
            }
            SecurityActionRow(
                title = "Change PIN",
                subtitle = "Reset your account PIN and keep access secure",
                icon = Icons.Outlined.LockReset,
                onClick = { navController.navigate(Routes.AUTH_PHONE) },
            )
        }
    }
}

@Composable
private fun SecurityActionRow(
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
private fun SecurityPill(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
