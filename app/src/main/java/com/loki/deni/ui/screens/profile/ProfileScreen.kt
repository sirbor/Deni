package com.loki.deni.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.loki.deni.BuildConfig
import com.loki.deni.R
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.AuthViewModel
import com.loki.deni.ui.viewmodel.ProfileUiState
import com.loki.deni.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.delay

private val Primary: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val PrimaryDark: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
private val PrimaryDeep = Color(0xFF012E31)
private val PrimaryLight: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
private val Background: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val Surface: Color
    @Composable get() = MaterialTheme.colorScheme.surface
private val TextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val TextMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
private val TextFaint: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
private val Border: Color
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
private val Gold = Color(0xFFF5A623)
private val GoldSurface = Color(0xFFFFF8EC)
private val GoldLabel = Color(0xFFF6C060)
private val Success = Color(0xFF437A22)
private val SuccessSurface = Color(0xFFEAF3E5)
private val InfoBlue = Color(0xFF006494)
private val InfoBlueSurface = Color(0xFFE8F2F8)
private val Error: Color
    @Composable get() = MaterialTheme.colorScheme.error
private val ErrorSurface: Color
    @Composable get() = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)

@Composable
fun ProfileScreen(
    navController: NavController,
    onThemeToggle: (Boolean) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val compact = LocalConfiguration.current.screenWidthDp < 360
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val remindersEnabled by viewModel.isNotificationsOn.collectAsStateWithLifecycle()
    val darkModeEnabled by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val debugDiagnostics by viewModel.debugDiagnostics.collectAsStateWithLifecycle()
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) {
            viewModel.toggleRemindersPersist(true)
        }
    }
    var showStep by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        repeat(8) { i -> delay(80); showStep = i + 1 }
    }

    when (val state = uiState) {
        ProfileUiState.Loading -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.loading), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        is ProfileUiState.Error -> Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Text(state.message, color = MaterialTheme.colorScheme.error)
        }
        is ProfileUiState.Success -> {
            val profile = state.profile
            val completion = 92
            val completionProgress by animateFloatAsState(
                targetValue = if (showStep > 0) completion / 100f else 0f,
                animationSpec = tween(700, easing = FastOutSlowInEasing),
                label = "completion",
            )
            val tierProgress by animateFloatAsState(
                targetValue = if (showStep > 1) 0.72f else 0f,
                animationSpec = tween(900, easing = FastOutSlowInEasing),
                label = "tierProgress",
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    AnimatedVisibility(visible = showStep >= 1, enter = fadeIn() + slideInVertically { -it / 4 }) {
                        Hero(
                            name = profile.fullName,
                            maskedPhone = viewModel.maskPhone(profile.phone),
                            completion = completion,
                            completionProgress = completionProgress,
                            compact = compact,
                            onEdit = { navController.navigate(Routes.EDIT_PROFILE) },
                            initials = profile.fullName
                                .split(" ")
                                .filter { it.isNotBlank() }
                                .take(2)
                                .joinToString("") { it.first().uppercase() }
                                .ifBlank { "U" },
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .offset(y = (-8).dp)
                            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(y = (-12).dp)
                            .padding(horizontal = 18.dp)
                            .padding(top = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        AnimatedVisibility(visible = showStep >= 2, enter = fadeIn() + slideInVertically { it / 5 }) {
                            QuickPrefsRow(
                                darkModeEnabled = darkModeEnabled,
                                remindersEnabled = remindersEnabled,
                                onDarkModeToggle = { viewModel.toggleDarkMode() },
                                onRemindersToggle = {
                                    if (!remindersEnabled && Build.VERSION.SDK_INT >= 33) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        viewModel.toggleRemindersPersist(!remindersEnabled)
                                    }
                                },
                                onSettings = { navController.navigate(Routes.SETTINGS) },
                                onPricing = { navController.navigate("pricing") },
                            )
                        }
                        AnimatedVisibility(visible = showStep >= 3, enter = fadeIn() + slideInVertically { it / 5 }) {
                            TierProgressCard(tierProgress = tierProgress)
                        }
                        AnimatedVisibility(visible = showStep >= 4, enter = fadeIn() + scaleIn(initialScale = 0.97f)) {
                            StatsRow(
                                compact = compact,
                                score = profile.creditScore,
                                creditLimitLabel = viewModel.formatCurrency(profile.creditLimit),
                                repayRateLabel = "${profile.repayRate}%",
                                onScore = { navController.navigate(Routes.TRANSACTIONS) },
                                onLimit = { navController.navigate(Routes.LOANS) },
                                onRate = { navController.navigate(Routes.TRANSACTIONS) },
                            )
                        }
                        AnimatedVisibility(visible = showStep >= 5, enter = fadeIn()) {
                            AccountSection(navController)
                        }
                        AnimatedVisibility(visible = showStep >= 6, enter = fadeIn()) {
                            SupportSection(navController)
                        }
                        AnimatedVisibility(visible = showStep >= 7, enter = fadeIn()) {
                            SignOutButton { showLogoutDialog = true }
                        }
                        if (BuildConfig.DEBUG) {
                            AnimatedVisibility(visible = showStep >= 7, enter = fadeIn()) {
                                DebugDiagnosticsCard(debugDiagnostics)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(stringResource(R.string.profile_logout_title)) },
            text = { Text(stringResource(R.string.profile_logout_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutDialog = false
                        authViewModel.signOut()
                        navController.navigate(Routes.WELCOME) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    },
                ) { Text(stringResource(R.string.profile_sign_out), color = Error) }
            },
            dismissButton = { TextButton(onClick = { showLogoutDialog = false }) { Text(stringResource(R.string.profile_cancel)) } },
        )
    }
}


@Composable
private fun DebugDiagnosticsCard(diagnostics: com.loki.deni.ui.viewmodel.DebugProfileDiagnostics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, Border),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text("DEBUG DIAGNOSTICS", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
            Text("userId: ${diagnostics.currentUserId.ifBlank { "<none>" }}", fontSize = 11.sp, color = TextPrimary)
            Text("sessionTokenPresent: ${diagnostics.hasSessionToken}", fontSize = 11.sp, color = TextPrimary)
            Text("savedPhone: ${diagnostics.savedPhone.ifBlank { "<none>" }}", fontSize = 11.sp, color = TextPrimary)
            Text("email: ${diagnostics.email.ifBlank { "<none>" }}", fontSize = 11.sp, color = TextPrimary)
            Text("contactsPermissionGranted: ${diagnostics.contactsPermissionGranted}", fontSize = 11.sp, color = TextPrimary)
            Text("smsPermissionGranted: ${diagnostics.smsPermissionGranted}", fontSize = 11.sp, color = TextPrimary)
            Text("contactsCount: ${diagnostics.contactsCount}", fontSize = 11.sp, color = TextPrimary)
            Text("financialSmsCount: ${diagnostics.financialSmsCount}", fontSize = 11.sp, color = TextPrimary)
        }
    }
}

@Composable
private fun Hero(
    name: String,
    maskedPhone: String,
    completion: Int,
    completionProgress: Float,
    compact: Boolean,
    onEdit: () -> Unit,
    initials: String,
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    if (isDark) listOf(Color(0xFF091C1F), Color(0xFF0E2A2E), PrimaryDark) else listOf(PrimaryDeep, Color(0xFF014D52), Primary),
                    start = Offset.Zero,
                    end = Offset(1000f, 360f),
                ),
            ),
    ) {
        DotGridOverlay(alpha = 0.05f)
        RingDecoration(Modifier.align(Alignment.TopEnd).offset(x = 80.dp, y = (-118).dp), 300.dp)
        RingDecoration(Modifier.align(Alignment.BottomStart).offset(x = (-52).dp, y = 68.dp), 180.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
                Text(
                    stringResource(R.string.profile_title),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = ProfileTokens.HeroTopActionsTopPadding + 8.dp, bottom = 14.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    color = Color.White,
                )

            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Avatar(initials)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            name,
                            fontSize = if (compact) 19.sp else 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = (-0.4).sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(maskedPhone, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.58f), modifier = Modifier.padding(top = 2.dp))
                        Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Badge(stringResource(R.string.profile_verified), Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.78f))
                            Badge(stringResource(R.string.profile_silver_tier), if (isDark) Gold.copy(alpha = 0.20f) else Color(0x2EF5A623), if (isDark) Gold.copy(alpha = 0.34f) else Color(0x47F5A623), GoldLabel)
                        }
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier.size(40.dp).background(Color.White, RoundedCornerShape(14.dp)).clickable(onClick = onEdit),
                            contentAlignment = Alignment.Center,
                        ) { Icon(Icons.Default.Edit, null, tint = Primary, modifier = Modifier.size(18.dp)) }
                        Box(modifier = Modifier.width(40.dp).height(4.dp).background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(completionProgress)
                                    .height(4.dp)
                                    .background(Brush.horizontalGradient(listOf(Gold, Color(0xFF6DAA45))), RoundedCornerShape(999.dp)),
                            )
                        }
                        Text(stringResource(R.string.profile_completion, completion), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = 0.66f), textAlign = TextAlign.Center)
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
        }
    }
}

@Composable
private fun Avatar(initials: String) {
    Box(
        modifier = Modifier
            .size(72.dp)
                .background(Brush.linearGradient(listOf(Color.White, if (isSystemInDarkTheme()) Color(0xFFBED0D2) else Color(0xFFD4EAEA))), CircleShape)
            .padding(3.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(if (isSystemInDarkTheme()) listOf(Color(0xFF115F64), Color(0xFF0B2427)) else listOf(Color(0xFF0D7379), PrimaryDeep)), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(initials, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }
}

@Composable
private fun Badge(text: String, bg: Color, stroke: Color, fg: Color) {
    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .border(1.dp, stroke, RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 4.dp),
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = fg)
    }
}

@Composable
private fun TierProgressCard(tierProgress: Float) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Color(0x2401696F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.profile_tier_progress), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Box(
                    modifier = Modifier.background(GoldSurface, RoundedCornerShape(999.dp)).border(1.dp, Color(0x38F5A623), RoundedCornerShape(999.dp)).padding(horizontal = 13.dp, vertical = 5.dp),
                ) { Text(stringResource(R.string.profile_silver), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Gold) }
            }
            Text(
                stringResource(R.string.profile_tier_sub),
                fontSize = 12.sp,
                lineHeight = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier.padding(top = 6.dp, bottom = 14.dp),
            )
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).background(PrimaryLight, RoundedCornerShape(999.dp))) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(tierProgress)
                        .height(8.dp)
                        .background(Brush.horizontalGradient(listOf(Gold, Primary)), RoundedCornerShape(999.dp)),
                )
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(stringResource(R.string.profile_silver), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextFaint)
                Text(stringResource(R.string.profile_to_gold, 72), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniTier("KES 100K", stringResource(R.string.profile_gold_limit), Modifier.weight(1f))
                MiniTier("2 loans", stringResource(R.string.profile_to_unlock), Modifier.weight(1f))
                MiniTier("0.5%", stringResource(R.string.profile_rate_drop), Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MiniTier(value: String, label: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Background),
        border = BorderStroke(1.dp, Border),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Primary, textAlign = TextAlign.Center)
            Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, letterSpacing = 0.3.sp, modifier = Modifier.padding(top = 2.dp), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun StatsRow(
    compact: Boolean,
    score: Int,
    creditLimitLabel: String,
    repayRateLabel: String,
    onScore: () -> Unit,
    onLimit: () -> Unit,
    onRate: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        StatCard(score.toString(), stringResource(R.string.profile_score_label), stringResource(R.string.profile_good_standing), 20.sp, Success, Modifier.weight(1f), onScore)
        StatCard(creditLimitLabel, stringResource(R.string.profile_limit_label), stringResource(R.string.profile_limit_active, creditLimitLabel), if (compact) 14.sp else 16.sp, Success, Modifier.weight(1f), onLimit)
        StatCard(repayRateLabel, stringResource(R.string.profile_rate_label), stringResource(R.string.profile_trusted), 20.sp, Success, Modifier.weight(1f), onRate)
    }
}

@Composable
private fun StatCard(
    value: String,
    label: String,
    sub: String,
    valueSize: TextUnit,
    subColor: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(value, fontSize = valueSize, fontWeight = FontWeight.ExtraBold, color = Primary, lineHeight = valueSize)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = TextMuted, letterSpacing = 0.35.sp, modifier = Modifier.padding(top = 6.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(sub, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = subColor, modifier = Modifier.padding(top = 5.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun AccountSection(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(stringResource(R.string.profile_account), stringResource(R.string.profile_manage))
        MenuItem(Icons.Outlined.Person, PrimaryLight, Primary, stringResource(R.string.profile_personal_info), stringResource(R.string.profile_personal_info_sub)) {
            navController.navigate(Routes.PERSONAL_INFO)
        }
        MenuItem(Icons.Default.CreditCard, GoldSurface, Color(0xFFB8790A), stringResource(R.string.profile_payment_methods), stringResource(R.string.profile_payment_methods_sub)) {
            navController.navigate(Routes.PAYMENT_METHODS)
        }
        MenuItem(Icons.Outlined.VerifiedUser, SuccessSurface, Success, stringResource(R.string.profile_kyc), stringResource(R.string.profile_kyc_sub)) {
            navController.navigate(Routes.KYC)
        }
        MenuItem(Icons.Outlined.Badge, InfoBlueSurface, InfoBlue, stringResource(R.string.profile_limits_pricing), stringResource(R.string.profile_limits_pricing_sub)) {
            navController.navigate(Routes.PRICING)
        }
    }
}

@Composable
private fun RemindersCard(enabled: Boolean, compact: Boolean, onToggle: (Boolean) -> Unit) {
    val isDark = isSystemInDarkTheme()
    val knobOffset by animateDpAsState(targetValue = if (enabled) 22.dp else 2.dp, label = "toggleKnob")
    val shellColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (enabled) Color.White.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.12f),
        label = "toggleBg",
    )
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF0E2A2E), PrimaryDark) else listOf(Primary, PrimaryDark),
                    ),
                    RoundedCornerShape(18.dp),
                )
                .padding(18.dp),
        ) {
            DotGridOverlay(0.07f)
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.profile_reminders_title), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Box(
                        modifier = Modifier
                            .width(46.dp)
                            .height(28.dp)
                            .background(shellColor, RoundedCornerShape(999.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                            .clickable { onToggle(!enabled) },
                    ) {
                        Box(modifier = Modifier.offset(x = knobOffset, y = 3.dp).size(22.dp).background(Color.White, CircleShape))
                    }
                }
                Text(
                    stringResource(R.string.profile_reminders_sub),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.62f),
                    modifier = Modifier.width(if (compact) 200.dp else 230.dp).padding(top = 8.dp, bottom = 12.dp),
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Tag(stringResource(R.string.profile_push))
                    Tag(stringResource(R.string.profile_sms))
                    Tag(stringResource(R.string.profile_alerts))
                }
            }
        }
    }
}

@Composable
private fun Tag(text: String) {
    Box(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
            .padding(horizontal = 11.dp, vertical = 4.dp),
    ) { Text(text, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color.White) }
}

@Composable
private fun SupportSection(navController: NavController) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(stringResource(R.string.profile_support), "")
        MenuItem(Icons.Default.SupportAgent, PrimaryLight, Primary, stringResource(R.string.profile_help), stringResource(R.string.profile_help_sub)) {
            navController.navigate(Routes.SUPPORT)
        }
        MenuItem(Icons.Default.Security, SuccessSurface, Success, stringResource(R.string.profile_security), stringResource(R.string.profile_security_sub)) {
            navController.navigate(Routes.SECURITY)
        }
        MenuItem(Icons.AutoMirrored.Filled.Logout, ErrorSurface, Error, stringResource(R.string.profile_close), stringResource(R.string.profile_close_sub)) {
            navController.navigate(Routes.CLOSE_ACCOUNT)
        }
    }
}

@Composable
private fun SecurityPreferencesCard(
    remindersEnabled: Boolean,
    onRemindersToggle: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF091C1F), Color(0xFF0D2C30)) else listOf(PrimaryDeep, PrimaryDark),
                    ),
                    RoundedCornerShape(18.dp),
                )
                .padding(16.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Security & Preferences", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                PreferenceRow("Notifications", remindersEnabled, onRemindersToggle)
            }
        }
    }
}

@Composable
private fun QuickPrefsRow(
    darkModeEnabled: Boolean,
    remindersEnabled: Boolean,
    onDarkModeToggle: () -> Unit,
    onRemindersToggle: () -> Unit,
    onSettings: () -> Unit,
    onPricing: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PrefPill("Dark mode", Icons.Default.DarkMode, darkModeEnabled, Modifier.weight(1f), onDarkModeToggle)
        PrefPill("Alerts", Icons.Default.NotificationsNone, remindersEnabled, Modifier.weight(1f), onRemindersToggle)
        PrefPill("Pricing", Icons.Default.MonetizationOn, false, Modifier.weight(1f), onPricing)
    }
}

@Composable
private fun PrefPill(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val bg = if (enabled) PrimaryLight else Surface
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        border = BorderStroke(1.dp, Border),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(icon, contentDescription = null, tint = if (enabled) Primary else TextMuted, modifier = Modifier.size(16.dp))
            Text(label, color = TextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
private fun PreferenceRow(label: String, enabled: Boolean, onToggle: () -> Unit) {
    val knobOffset by animateDpAsState(targetValue = if (enabled) 20.dp else 2.dp, label = "prefKnob")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, color = Color.White.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(26.dp)
                .background(Color.White.copy(alpha = if (enabled) 0.22f else 0.12f), RoundedCornerShape(99.dp))
                .clickable(onClick = onToggle),
        ) {
            Box(
                modifier = Modifier
                    .offset(x = knobOffset, y = 3.dp)
                    .size(20.dp)
                    .background(Color.White, CircleShape),
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, action: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
        if (action.isNotBlank()) {
            Text(action, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Primary)
        }
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.dp, Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(44.dp).background(iconBg, RoundedCornerShape(14.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextMuted, modifier = Modifier.padding(top = 2.dp))
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = TextFaint, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SignOutButton(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = BorderStroke(1.5.dp, Color(0x29B00020)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Error, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(stringResource(R.string.profile_sign_out), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Error)
        }
    }
}

@Composable
private fun DotGridOverlay(alpha: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val tile = 22.dp.toPx()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(color = Color.White.copy(alpha = alpha), radius = 1.1.dp.toPx(), center = Offset(x + tile / 2f, y + tile / 2f))
                x += tile
            }
            y += tile
        }
    }
}

@Composable
private fun RingDecoration(modifier: Modifier, size: androidx.compose.ui.unit.Dp) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.White.copy(alpha = 0.06f), style = androidx.compose.ui.graphics.drawscope.Stroke(1.dp.toPx()))
        }
    }
}
