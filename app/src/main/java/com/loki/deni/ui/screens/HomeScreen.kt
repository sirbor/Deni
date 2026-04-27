package com.loki.deni.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color.TRANSPARENT
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.data.local.entity.LoanEntity
import com.loki.deni.data.local.entity.TransactionEntity
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.components.RepaymentStatusChip
import com.loki.deni.ui.viewmodel.AccountDataViewModel
import java.time.LocalTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private val HomePrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val HomePrimaryDark: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
private val HomePrimaryDeep = Color(0xFF012E31)
private val HomePrimaryLight: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
private val HomeBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val HomeSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface
private val HomeTextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val HomeTextMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
private val HomeTextFaint: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
private val HomeBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
private val HomeGold = Color(0xFFF5A623)
private val HomeGoldSurface = Color(0xFFFFF8EC)
private val HomeSuccess = Color(0xFF437A22)
private val HomeSuccessSurface = Color(0xFFEAF3E5)
private val HomeInfoBlue = Color(0xFF006494)
private val HomeInfoBlueSurface = Color(0xFFE8F2F8)
private val HomeError: Color
    @Composable get() = MaterialTheme.colorScheme.error

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    val compact = LocalConfiguration.current.screenWidthDp < 360
    val view = LocalView.current
    DisposableEffect(view) {
        view.context.findActivity()?.window?.let { window ->
            window.statusBarColor = TRANSPARENT
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = false
        }
        onDispose { }
    }

    LaunchedEffect(Unit) { viewModel.load() }
    val user by viewModel.user.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val activeLoan = loans.firstOrNull { !it.isPaid && outstandingForLoan(it) > 1.0 }
    val activeLoanId = activeLoan?.loanId ?: 1
    val unreadCount = transactions.count { it.status.equals("pending", true) }
    val greeting = when (LocalTime.now().hour) {
        in 0..11 -> stringResource(R.string.home_greeting_morning)
        in 12..17 -> stringResource(R.string.home_greeting_afternoon)
        else -> stringResource(R.string.home_greeting_evening)
    }

    var showGreeting by remember { mutableStateOf(false) }
    var showHero by remember { mutableStateOf(false) }
    var showLoan by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var showSnapshot by remember { mutableStateOf(false) }
    var showInsights by remember { mutableStateOf(false) }
    var showActivity by remember { mutableStateOf(false) }
    var showBanner by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showGreeting = true
        delay(70)
        showHero = true
        delay(80)
        showLoan = true
        delay(80)
        showActions = true
        delay(80)
        showSnapshot = true
        delay(80)
        showInsights = true
        delay(80)
        showActivity = true
        delay(80)
        showBanner = true
    }

    Scaffold(
        containerColor = HomeBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HomeBackground),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item {
                HeroHeader(
                    greeting = greeting,
                    userName = user?.name ?: "User",
                    creditScore = stats.creditScore,
                    availableLimit = stats.availableLimit,
                    approvedLimit = stats.approvedLimit,
                    inUseLimit = stats.outstanding,
                    unreadCount = unreadCount,
                    showGreeting = showGreeting,
                    showHero = showHero,
                    compact = compact,
                    onApply = { navController.navigate(DeniRoutes.Loans.route) },
                    onViewScore = { navController.navigate(DeniRoutes.Transactions.route) },
                    onTransactions = { navController.navigate(DeniRoutes.TransactionsFeed.route) },
                    onNotifications = { navController.navigate(DeniRoutes.Notifications.route) },
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .background(HomeBackground, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .offset(y = (-8).dp),
                )
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-20).dp)
                        .padding(horizontal = 18.dp)
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    AnimatedVisibility(visible = showLoan, enter = fadeIn() + slideInVertically { it / 4 } + scaleIn(initialScale = 0.98f)) {
                        if (activeLoan != null) {
                            ActiveLoanCard(
                                loan = activeLoan,
                                onRepay = { navController.navigate(DeniRoutes.RepayByLoan.createRoute(activeLoanId)) },
                                onTopUp = { navController.navigate(DeniRoutes.LoanTopup.createRoute(activeLoanId)) },
                            )
                        } else {
                            EmptyLoanBanner(onApply = { navController.navigate(DeniRoutes.Loans.route) })
                        }
                    }
                    AnimatedVisibility(visible = showActions, enter = fadeIn() + slideInVertically { it / 4 }) {
                        QuickActionsSection(
                            onApply = { navController.navigate(DeniRoutes.Loans.route) },
                            // Keep quick actions inside bottom-nav destinations for easier tab switching.
                            onRepay = { navController.navigate(DeniRoutes.Loans.route) },
                            onSchedule = { navController.navigate(DeniRoutes.Loans.route) },
                            onHistory = { navController.navigate(DeniRoutes.Transactions.route) },
                        )
                    }
                    AnimatedVisibility(visible = showSnapshot, enter = fadeIn()) {
                        SnapshotSection(
                            compact = compact,
                            stats = stats,
                            onOpen = { navController.navigate(DeniRoutes.Transactions.route) },
                        )
                    }
                    AnimatedVisibility(visible = showInsights, enter = fadeIn() + slideInVertically { it / 4 }) {
                        InsightMiniCards(stats = stats, onTap = { navController.navigate(DeniRoutes.Transactions.route) })
                    }
                    AnimatedVisibility(visible = showActivity, enter = fadeIn() + slideInVertically { it / 4 }) {
                        RecentActivitySection(
                            transactions = transactions,
                            onOpen = { navController.navigate(DeniRoutes.TransactionsFeed.route) },
                            onTransactionClick = { txId -> navController.navigate("receipt/$txId") },
                        )
                    }
                    AnimatedVisibility(visible = showBanner, enter = fadeIn()) {
                        GrowthBanner(compact = compact, onOpen = { navController.navigate("pricing") })
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroHeader(
    greeting: String,
    userName: String,
    creditScore: Int,
    availableLimit: Int,
    approvedLimit: Int,
    inUseLimit: Int,
    unreadCount: Int,
    showGreeting: Boolean,
    showHero: Boolean,
    compact: Boolean,
    onApply: () -> Unit,
    onViewScore: () -> Unit,
    onTransactions: () -> Unit,
    onNotifications: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = if (isDark) {
                        listOf(Color(0xFF091C1F), Color(0xFF0E2A2E), HomePrimaryDark)
                    } else {
                        listOf(HomePrimaryDeep, Color(0xFF014D52), HomePrimary)
                    },
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 360f),
                ),
            ),
    ) {
        DotPatternGrid(alpha = 0.05f, spacing = 22.dp)
        RingDecoration(Modifier.align(Alignment.TopEnd).offset(x = 80.dp, y = (-118).dp), 300.dp)
        RingDecoration(Modifier.align(Alignment.BottomStart).offset(x = (-52).dp, y = 68.dp), 180.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            AnimatedVisibility(visible = showGreeting, enter = fadeIn()) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(greeting, fontSize = 13.sp, color = Color.White.copy(alpha = 0.55f), fontWeight = FontWeight.SemiBold)
                        Text(userName, fontSize = if (compact) 18.sp else 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp, color = Color.White, modifier = Modifier.padding(top = 2.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = if (isDark) 0.14f else 0.12f))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                            .clickable(onClick = onNotifications),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp),
                        )
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 2.dp, y = (-2).dp)
                                    .size(16.dp)
                                    .background(HomeGold, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = unreadCount.coerceAtMost(9).toString(),
                                    color = Color.Black,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                )
                            }
                        }
                    }
                }
            }
            AnimatedVisibility(visible = showHero, enter = fadeIn() + slideInVertically { -it / 4 }) {
                CreditHeroCard(
                    creditScore = creditScore,
                    availableLimit = availableLimit,
                    approvedLimit = approvedLimit,
                    inUseLimit = inUseLimit,
                    onApply = onApply,
                    onViewScore = onViewScore,
                    onTransactions = onTransactions,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun CreditHeroCard(
    creditScore: Int,
    availableLimit: Int,
    approvedLimit: Int,
    inUseLimit: Int,
    onApply: () -> Unit,
    onViewScore: () -> Unit,
    onTransactions: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) MaterialTheme.colorScheme.surface.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.home_available_limit).uppercase(), fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f), letterSpacing = 0.5.sp, fontWeight = FontWeight.Bold)
                Text(formatKes(availableLimit), fontSize = 24.sp, lineHeight = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.6).sp, color = Color.White, modifier = Modifier.padding(top = 3.dp))
                Text(
                    stringResource(R.string.home_limit_in_use, formatKes(inUseLimit), formatKes(approvedLimit)),
                    color = Color.White.copy(alpha = 0.46f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 6.dp),
                )
                Box(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.12f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (approvedLimit <= 0) 0f else (inUseLimit.toFloat() / approvedLimit.toFloat()).coerceIn(0f, 1f))
                            .height(5.dp)
                            .background(Brush.horizontalGradient(listOf(HomeGold, if (isDark) Color(0xFFFFDE9A) else Color.White))),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                ) {
                    SmallHeroButton(
                        icon = Icons.Default.AddCircle,
                        contentDescription = stringResource(R.string.home_apply_now),
                        filled = true,
                        onClick = onApply,
                        modifier = Modifier.weight(1f),
                    )
                    SmallHeroButton(
                        icon = Icons.Default.TrendingUp,
                        contentDescription = stringResource(R.string.home_view_score),
                        filled = false,
                        onClick = onViewScore,
                        modifier = Modifier.weight(1f),
                    )
                    SmallHeroButton(
                        icon = Icons.Default.History,
                        contentDescription = stringResource(R.string.transactions_tab),
                        filled = false,
                        onClick = onTransactions,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .width(70.dp)
                    .padding(top = 2.dp)
                    .clickable(onClick = onViewScore),
            ) {
                ScoreRing72(score = creditScore, size = 58.dp)
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                ) {
                    Text(stringResource(R.string.home_status_good), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = 0.78f))
                }
            }
        }
        }
    }
}

@Composable
private fun ActiveLoanCard(loan: LoanEntity, onRepay: () -> Unit, onTopUp: () -> Unit) {
    val totalRepayment = loan.amount + (loan.amount * loan.interestRate)
    val remaining = (totalRepayment - loan.repaidAmount).coerceAtLeast(0.0)
    val repaidPercent = ((loan.repaidAmount / totalRepayment).toFloat()).coerceIn(0f, 1f)
    val daysLeft = ((loan.dueDate - System.currentTimeMillis()) / (24L * 60L * 60L * 1000L)).toInt().coerceAtLeast(0)
    Card(
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, HomePrimary.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = HomeSurface),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(listOf(HomePrimary, HomePrimaryDark)))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.home_active_loan).uppercase(), fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold)
                Box(modifier = Modifier.background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 5.dp)) {
                        Text("LN-${loan.loanId.toString().padStart(8, '0')}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text(formatKes(loan.amount.toInt()), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = HomeTextPrimary)
                        Text(stringResource(R.string.home_remaining, formatKes(remaining.toInt())), color = HomeTextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Box(
                        modifier = Modifier
                            .background(HomeGoldSurface, RoundedCornerShape(999.dp))
                            .border(1.dp, HomeGold.copy(alpha = 0.22f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                    ) {
                        Text(stringResource(R.string.home_due, formatDate(loan.dueDate)), color = HomeGold, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(HomeBackground),
                ) {
                    Box(modifier = Modifier.fillMaxWidth(repaidPercent).height(6.dp).background(Brush.horizontalGradient(listOf(HomePrimary, HomePrimaryDark))))
                }
                Row(modifier = Modifier.fillMaxWidth().padding(top = 1.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.home_repaid, (repaidPercent * 100f).toInt()), fontSize = 11.sp, color = HomeTextFaint, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.home_days_left, daysLeft), fontSize = 11.sp, color = HomeTextFaint, fontWeight = FontWeight.Bold)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    GradientMiniButton(text = stringResource(R.string.home_repay_now), onClick = onRepay, modifier = Modifier.weight(1f))
                    OutlinedMiniButton(text = stringResource(R.string.top_up), onClick = onTopUp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun EmptyLoanBanner(onApply: () -> Unit) {
    Card(shape = RoundedCornerShape(18.dp), border = androidx.compose.foundation.BorderStroke(1.dp, HomeBorder), colors = CardDefaults.cardColors(containerColor = HomeSurface)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("You have room to borrow", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                Text("Apply now and access up to KES 50,000", color = HomeTextMuted, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(Brush.linearGradient(listOf(HomePrimary, HomePrimaryDark)))
                    .clickable(onClick = onApply)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            ) {
                Text(stringResource(R.string.home_start_application), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun QuickActionsSection(
    onApply: () -> Unit,
    onRepay: () -> Unit,
    onSchedule: () -> Unit,
    onHistory: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionHeader(title = stringResource(R.string.home_quick_actions), action = "", onAction = {})
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            QuickActionCard(Icons.Default.AddCircle, stringResource(R.string.home_action_apply), HomePrimaryLight, HomePrimary, Modifier.weight(1f), onApply)
            QuickActionCard(Icons.Default.CreditCard, stringResource(R.string.home_action_repay), Color(0xFFFFF8EC), Color(0xFFB07A15), Modifier.weight(1f), onRepay)
            QuickActionCard(Icons.Default.CalendarToday, stringResource(R.string.home_action_schedule), Color(0xFFEAF3E5), HomeSuccess, Modifier.weight(1f), onSchedule)
            QuickActionCard(Icons.Default.History, stringResource(R.string.home_action_history), HomeInfoBlueSurface, HomeInfoBlue, Modifier.weight(1f), onHistory)
        }
    }
}

@Composable
private fun SnapshotSection(compact: Boolean, stats: com.loki.deni.ui.viewmodel.DashboardStats, onOpen: () -> Unit) {
    val scoreTitle = stringResource(R.string.home_credit_score)
    val scoreBand = stats.scoreBand
    val onTimeRate = stringResource(R.string.home_on_time_rate)
    val topFive = stringResource(R.string.home_top_five)
    val nextDue = stringResource(R.string.home_next_due)
    val totalRepaid = stringResource(R.string.home_total_repaid)
    val totalLoans = stringResource(R.string.home_total_loans, 7)
    val utilization = if (stats.approvedLimit > 0) (((stats.approvedLimit - stats.availableLimit) * 100f) / stats.approvedLimit).toInt() else 0
    val snapshotItems = listOf(
        Triple(stats.creditScore.toString(), scoreTitle, scoreBand),
        Triple("$utilization%", onTimeRate, topFive),
        Triple("${stats.outstanding.coerceAtLeast(0)}", nextDue, "Active dues"),
        Triple(formatKes(stats.totalRepaid), totalRepaid, stringResource(R.string.home_total_loans, stats.totalBorrowed.coerceAtLeast(0) / 10000)),
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(stringResource(R.string.home_snapshot), stringResource(R.string.home_full_report), onOpen)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(snapshotItems) { item ->
                Card(
                    modifier = Modifier
                        .width(if (compact) 122.dp else 132.dp)
                        .clickable(onClick = onOpen),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, HomeBorder),
                    colors = CardDefaults.cardColors(containerColor = HomeSurface),
                ) {
                    Column(
                        modifier = Modifier
                            .padding(14.dp)
                            .height(92.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        val valueColor = if (item.second == nextDue) HomeGold else if (item.second == totalRepaid) HomeSuccess else HomePrimary
                        val deltaColor = if (item.second == nextDue) HomeGold else HomeSuccess
                        Text(item.first, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = valueColor)
                        Text(
                            item.second,
                            fontSize = 11.sp,
                            color = HomeTextMuted,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            item.third,
                            fontSize = 11.sp,
                            color = deltaColor,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightMiniCards(stats: com.loki.deni.ui.viewmodel.DashboardStats, onTap: () -> Unit = {}) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        MiniInsightCard(
            title = stringResource(R.string.home_eligibility),
            value = formatKes(stats.availableLimit),
            body = stringResource(R.string.home_eligibility_sub),
            tag = stringResource(R.string.home_stable_profile),
            tagBg = HomeSuccessSurface,
            tagColor = HomeSuccess,
            modifier = Modifier.weight(1f),
            onClick = onTap,
        )
        MiniInsightCard(
            title = stringResource(R.string.home_repayment_health),
            value = "${if (stats.approvedLimit > 0) ((stats.availableLimit * 100f) / stats.approvedLimit).toInt() else 0}%",
            body = stringResource(R.string.home_repayment_health_sub),
            tag = stringResource(R.string.home_due_soon_count, 1),
            tagBg = HomeGoldSurface,
            tagColor = HomeGold,
            modifier = Modifier.weight(1f),
            onClick = onTap,
        )
    }
}

@Composable
private fun RecentActivitySection(
    transactions: List<TransactionEntity>,
    onOpen: () -> Unit,
    onTransactionClick: (Int) -> Unit,
) {
    val rows = transactions.take(3)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(stringResource(R.string.home_recent_activity), stringResource(R.string.home_see_all), onOpen)
        rows.forEach { tx ->
            val isRepayment = tx.type.equals("debit", true)
            val iconBg = if (isRepayment) Color(0xFFEAF3E5) else Color(0xFFE6F2F2)
            val iconTint = if (isRepayment) HomeSuccess else HomePrimary
            val amountColor = if (isRepayment) HomeSuccess else HomeError
            val amountPrefix = if (isRepayment) "+" else "-"
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onTransactionClick(tx.transId) },
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, HomeBorder),
                colors = CardDefaults.cardColors(containerColor = HomeSurface),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(modifier = Modifier.size(40.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isRepayment) Icons.Default.CheckCircle else Icons.Default.SouthEast,
                                contentDescription = null,
                                tint = iconTint,
                            )
                        }
                        Column {
                            val title = tx.title.ifBlank { if (isRepayment) stringResource(R.string.home_repayment_received) else stringResource(R.string.home_loan_disbursed) }
                            Text(title, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = HomeTextPrimary)
                            Text("TX-${tx.transId.toString().padStart(6, '0')} · ${formatDate(tx.timestamp)}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = HomeTextFaint)
                        }
                    }
                    Column(
                        modifier = Modifier.padding(start = 10.dp),
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text("$amountPrefix${formatKes(tx.amount.toInt())}", color = amountColor, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        RepaymentStatusChip(
                            status = when {
                                tx.status.equals("paid", true) -> "Paid"
                                tx.status.equals("overdue", true) -> "Overdue"
                                else -> "Active"
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GrowthBanner(compact: Boolean, onOpen: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.padding(top = 8.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF0E2A2E), HomePrimaryDark) else listOf(HomePrimary, HomePrimaryDark),
                    ),
                )
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            DotPatternGrid(alpha = 0.07f, spacing = 20.dp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.home_growth_title),
                    modifier = Modifier.width(if (compact) 180.dp else 230.dp),
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp,
                    lineHeight = 14.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(10.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpen)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(stringResource(R.string.home_learn_more), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                }
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun SectionHeader(title: String, action: String, onAction: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = HomeTextPrimary)
        if (action.isNotBlank()) {
            Text(action, color = HomePrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.clickable(onClick = onAction))
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    bgColor: Color,
    iconColor: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HomeBorder),
        colors = CardDefaults.cardColors(containerColor = HomeSurface),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(42.dp).background(bgColor, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconColor)
            }
            Spacer(Modifier.height(6.dp))
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = HomeTextPrimary)
        }
    }
}

@Composable
private fun ScoreRing72(score: Int, size: Dp) {
    val progress = (score.toFloat() / 850f).coerceIn(0f, 1f)
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(size)) {
        Canvas(modifier = Modifier.size(size)) {
            drawArc(Color.White.copy(alpha = 0.10f), -90f, 360f, false, style = Stroke(7.dp.toPx()))
            drawArc(
                brush = Brush.horizontalGradient(listOf(HomeGold, HomeSuccess)),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(7.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(score.toString(), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
            Text("SCORE", color = Color.White.copy(alpha = 0.45f), fontSize = 7.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DotPatternGrid(alpha: Float = 0.05f, spacing: androidx.compose.ui.unit.Dp = 22.dp) {
    Canvas(modifier = Modifier.fillMaxWidth().height(280.dp)) {
        val step = spacing.toPx()
        var y = 0f
        while (y <= size.height) {
            var x = 0f
            while (x <= size.width) {
                drawCircle(color = Color.White.copy(alpha = alpha), radius = 1.5.dp.toPx(), center = Offset(x, y))
                x += step
            }
            y += step
        }
    }
}

@Composable
private fun RingDecoration(modifier: Modifier, size: androidx.compose.ui.unit.Dp) {
    Box(modifier = modifier.size(size).border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape))
}


@Composable
private fun SmallHeroButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(30.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (filled) Color.White else Color.White.copy(alpha = 0.10f))
            .border(
                1.dp,
                if (filled) Color.Transparent else Color.White.copy(alpha = 0.18f),
                RoundedCornerShape(11.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (filled) HomePrimary else Color.White,
            modifier = Modifier.size(16.dp),
        )
    }
}

@Composable
private fun GradientMiniButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Brush.linearGradient(listOf(HomePrimary, HomePrimaryDark)))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun OutlinedMiniButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, HomeBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, color = HomePrimary, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun MiniInsightCard(
    title: String,
    value: String,
    body: String,
    tag: String,
    tagBg: Color,
    tagColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = HomeSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, HomeBorder),
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(15.dp)) {
            Text(title.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = HomeTextMuted)
            Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = HomeTextPrimary, modifier = Modifier.padding(top = 4.dp))
            Text(body, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HomeTextMuted, modifier = Modifier.padding(top = 4.dp))
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .background(tagBg, RoundedCornerShape(999.dp))
                    .padding(horizontal = 9.dp, vertical = 4.dp),
            ) {
                Text(tag, color = tagColor, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

private fun formatKes(amount: Int): String = "KES %,d".format(amount)

private fun outstandingForLoan(loan: LoanEntity): Double {
    val totalRepayment = loan.amount + (loan.amount * loan.interestRate)
    return (totalRepayment - loan.repaidAmount).coerceAtLeast(0.0)
}

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(timestamp))
