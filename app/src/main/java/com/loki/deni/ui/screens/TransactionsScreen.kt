package com.loki.deni.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.components.BorrowingBarChart
import com.loki.deni.ui.components.ChipType
import com.loki.deni.ui.components.DeniBottomNav
import com.loki.deni.ui.components.DonutChart
import com.loki.deni.ui.components.DonutSegment
import com.loki.deni.ui.components.OfflineBanner
import com.loki.deni.ui.components.RepaymentStatusChip
import com.loki.deni.ui.components.TimelineItem
import com.loki.deni.ui.components.rememberIsOnline
import com.loki.deni.ui.model.BarEntry
import com.loki.deni.ui.model.PaymentFeedItem
import com.loki.deni.ui.model.ScorePoint
import com.loki.deni.ui.model.UpcomingPaymentItem
import com.loki.deni.ui.viewmodel.AccountDataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
private val Success = Color(0xFF437A22)
private val SuccessSurface = Color(0xFFEAF3E5)
private val InfoBlue = Color(0xFF006494)
private val Error: Color
    @Composable get() = MaterialTheme.colorScheme.error
private val ErrorSurface: Color
    @Composable get() = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun TransactionsScreen(
    navController: NavController,
    focus: String? = null,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val user by viewModel.user.collectAsStateWithLifecycle()
    val compact = LocalConfiguration.current.screenWidthDp < 360
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val all = stringResource(R.string.tx_filter_all)
    val disb = stringResource(R.string.tx_filter_disb)
    val repay = stringResource(R.string.tx_filter_repay)
    var filter by remember { mutableStateOf(all) }
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSearch by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf("All") }
    var refreshing by remember { mutableStateOf(false) }
    val online = rememberIsOnline()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { refreshing = true },
    )

    var show by remember { mutableStateOf(0) }
    val allTransactionsRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(Unit) {
        repeat(8) { i -> delay(90); show = i + 1 }
    }
    LaunchedEffect(focus) {
        if (focus.equals("all", ignoreCase = true)) {
            show = 8
            delay(220)
            allTransactionsRequester.bringIntoView()
        }
    }
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(600)
            refreshing = false
        }
    }

    val txFeed = remember(transactions, loans) { transactions.toPaymentFeed(loans) }
    val txs = remember(filter, query, statusFilter, txFeed) {
        txFeed.filter {
            val typeOk = when (filter) {
                disb -> !it.isRepayment
                repay -> it.isRepayment
                else -> true
            }
            val statusOk = when (statusFilter) {
                "Paid" -> it.statusLabel.equals("Paid", true)
                "Active" -> it.statusLabel.equals("Active", true)
                "Overdue" -> it.statusLabel.equals("Overdue", true)
                else -> true
            }
            val searchOk = query.isBlank() ||
                it.title.contains(query, true) ||
                it.loanId.contains(query, true) ||
                it.date.contains(query, true)
            typeOk && statusOk && searchOk
        }
    }
    val scoreHistory = remember(transactions) { transactions.toScoreHistory(user?.creditScore ?: 500) }
    val borrowingBars = remember(transactions) { transactions.toBorrowingBars() }
    val upcoming = remember(loans) { loans.toUpcomingPayments() }
    val repaymentBreakdown = remember(loans) { loans.toRepaymentBreakdown() }
    val purposeBreakdown = remember(loans) { loans.toPurposeBreakdown() }
    val onTimeRate = remember(loans) {
        if (loans.isEmpty()) 0 else ((loans.count { it.isPaid }.toFloat() / loans.size.toFloat()) * 100f).toInt()
    }
    val totalRepaid = stats.totalRepaid

    Scaffold(
        containerColor = Background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = { DeniBottomNav(navController = navController) },
    ) { p ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(p)
                .background(Background)
                .pullRefresh(pullRefreshState),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                if (!online) {
                    item { OfflineBanner() }
                }
                item {
                    AnimatedVisibility(show >= 1, enter = fadeIn() + slideInVertically { -it / 4 }) {
                        Hero(
                            onFilter = { showFilterSheet = true },
                            onSearch = { showSearch = !showSearch },
                            score = user?.creditScore ?: 500,
                            availableLimit = stats.availableLimit,
                            onTimeRate = onTimeRate,
                        )
                    }
                }
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(28.dp).offset(y = (-8).dp)
                            .background(Background, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    )
                }
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().offset(y = (-14).dp).padding(horizontal = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (showSearch) {
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search by loan id, title or date") },
                                singleLine = true,
                            )
                        }
                        AnimatedVisibility(show >= 2, enter = fadeIn() + scaleIn(initialScale = 0.96f)) { Metrics(compact, onTimeRate, loans.size, totalRepaid) }
                        AnimatedVisibility(show >= 3, enter = fadeIn() + slideInVertically { it / 4 }) { ScoreCard(scoreHistory, user?.creditScore ?: 500) }
                        AnimatedVisibility(show >= 4, enter = fadeIn() + slideInVertically { it / 4 }) { BorrowCard(borrowingBars) }
                        AnimatedVisibility(show >= 5, enter = fadeIn()) { Donuts(onTimeRate, repaymentBreakdown, purposeBreakdown) }
                        AnimatedVisibility(show >= 6, enter = fadeIn() + slideInVertically { it / 4 }) {
                            Box(modifier = Modifier.bringIntoViewRequester(allTransactionsRequester)) {
                                if (txs.isEmpty()) {
                                    val message = when {
                                        query.isNotBlank() -> "No results for \"$query\""
                                        filter == disb -> "No in transactions"
                                        filter == repay -> "No out transactions"
                                        else -> "No transactions yet. Apply for a loan to get started."
                                    }
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Surface),
                                        border = BorderStroke(1.dp, Border),
                                    ) {
                                        Text(
                                            message,
                                            modifier = Modifier.padding(14.dp),
                                            color = TextMuted,
                                        )
                                    }
                                } else {
                                    Feed(
                                        txs = txs,
                                        active = filter,
                                        all = all,
                                        disb = disb,
                                        repay = repay,
                                        onFilter = { filter = it },
                                        onItem = { navController.navigate("receipt/${it.paymentId}") },
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(show >= 7, enter = fadeIn() + slideInVertically { it / 4 }) { Upcoming(navController, upcoming) }
                        AnimatedVisibility(show >= 8, enter = fadeIn()) {
                            Recommendations(
                                compact = compact,
                                onRepay = { navController.navigate("repay/1") },
                                onPricing = { navController.navigate("pricing") },
                                onReminders = { navController.navigate("profile") },
                            )
                        }
                    }
                }
            }
            PullRefreshIndicator(refreshing = refreshing, state = pullRefreshState, modifier = Modifier.align(Alignment.TopCenter))
        }
    }
    if (showFilterSheet) {
        ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Filter transactions", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                Text("Status", color = TextMuted, fontSize = 12.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Paid", "Active", "Overdue").forEach { item ->
                        val selected = statusFilter == item
                        Box(
                            modifier = Modifier
                                .background(if (selected) Primary else Surface, RoundedCornerShape(999.dp))
                                .border(1.dp, if (selected) Primary else Border, RoundedCornerShape(999.dp))
                                .clickable { statusFilter = item }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(item, color = if (selected) Color.White else TextMuted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = { showFilterSheet = false }) { Text("Apply") }
                }
            }
        }
    }
}

@Composable
private fun Hero(
    onFilter: () -> Unit,
    onSearch: () -> Unit,
    score: Int,
    availableLimit: Int,
    onTimeRate: Int,
) {
    val isDark = isSystemInDarkTheme()
    Box(
        modifier = Modifier.fillMaxWidth().background(
            Brush.linearGradient(
                if (isDark) listOf(Color(0xFF091C1F), Color(0xFF0E2A2E), PrimaryDark) else listOf(PrimaryDeep, Color(0xFF014D52), Primary),
                start = Offset.Zero,
                end = Offset(1000f, 360f),
            ),
        ),
    ) {
        DotGrid(0.05f)
        Ring(Modifier.align(Alignment.TopEnd).offset(x = 80.dp, y = (-118).dp), 300.dp)
        Ring(Modifier.align(Alignment.BottomStart).offset(x = (-52).dp, y = 68.dp), 180.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.tx_title), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp, color = Color.White)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroButton(Icons.Default.FilterList, onFilter)
                    HeroButton(Icons.Default.Search, onSearch)
                }
            }
            HeroScoreCard(score = score, availableLimit = availableLimit, onTimeRate = onTimeRate)
            CreditBand(score = score)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun HeroButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier.size(36.dp).background(Color.White.copy(alpha = 0.10f), CircleShape)
            .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
}

@Composable
private fun HeroScoreCard(score: Int, availableLimit: Int, onTimeRate: Int) {
    val isDark = isSystemInDarkTheme()
    val anim = remember { Animatable(0f) }
    LaunchedEffect(score) { anim.animateTo(score.toFloat().coerceIn(0f, 850f) / 850f, tween(900, easing = FastOutSlowInEasing)) }
    Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)), border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(78.dp), contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(78.dp)) {
                    drawArc(Color.White.copy(alpha = 0.10f), -90f, 360f, false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(Brush.horizontalGradient(listOf(Gold, if (isDark) Color(0xFF92C26B) else Color(0xFF6DAA45))), -90f, anim.value * 360f, false, style = Stroke(8.dp.toPx(), cap = StrokeCap.Round))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(score.toString(), fontSize = 17.sp, lineHeight = 17.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("SCORE", fontSize = 7.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp, color = Color.White.copy(alpha = 0.44f))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.tx_score_title), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                Text(stringResource(R.string.tx_score_sub, "KES %,d".format(availableLimit)), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.55f), modifier = Modifier.padding(top = 2.dp))
                Box(modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth().height(4.dp).background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(999.dp))) {
                    Box(modifier = Modifier.fillMaxWidth(0.84f).height(4.dp).background(Brush.horizontalGradient(listOf(Gold, if (isDark) Color(0xFF92C26B) else Color(0xFF6DAA45))), RoundedCornerShape(999.dp)))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    HeroChip("Silver Tier", if (isDark) Gold.copy(alpha = 0.20f) else Color(0x2EF5A623), if (isDark) Gold.copy(alpha = 0.34f) else Color(0x47F5A623), Color(0xFFF6C060))
                    HeroChip("$onTimeRate% on time", Color.White.copy(alpha = 0.11f), Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.76f))
                }
            }
        }
    }
}

@Composable
private fun HeroChip(text: String, bg: Color, stroke: Color, textColor: Color) {
    Box(modifier = Modifier.background(bg, RoundedCornerShape(999.dp)).border(1.dp, stroke, RoundedCornerShape(999.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = textColor)
    }
}

@Composable
private fun CreditBand(score: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Band(Color(0xFFE05050), stringResource(R.string.tx_band_poor), score < 600, Modifier.weight(1f))
        Band(Color(0xFFE08040), stringResource(R.string.tx_band_fair), score in 600..679, Modifier.weight(1f))
        Band(Gold, stringResource(R.string.tx_band_good), score in 680..749, Modifier.weight(1f))
        Band(Color(0xFF6DAA45), stringResource(R.string.tx_band_excellent), score >= 750, Modifier.weight(1f))
    }
}

@Composable
private fun Band(color: Color, label: String, active: Boolean, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.fillMaxWidth().height(5.dp).background(color, RoundedCornerShape(3.dp)).border(if (active) 1.dp else 0.dp, if (active) color.copy(alpha = 0.5f) else Color.Transparent, RoundedCornerShape(3.dp)))
        Text(label, fontSize = 9.sp, fontWeight = if (active) FontWeight.Bold else FontWeight.Normal, color = if (active) Color(0xFFF6C060) else Color.White.copy(alpha = 0.35f), modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun Metrics(compact: Boolean, onTimeRate: Int, loanCount: Int, totalRepaid: Int) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Metric("$onTimeRate%", stringResource(R.string.tx_ontime), stringResource(R.string.tx_top_five), 18.sp, Modifier.weight(1f))
        Metric(loanCount.toString(), stringResource(R.string.tx_loans_taken), stringResource(R.string.tx_since), 18.sp, Modifier.weight(1f))
        Metric("KES %,d".format(totalRepaid), stringResource(R.string.tx_total_repaid), stringResource(R.string.tx_all_time), if (compact) 13.sp else 15.sp, Modifier.weight(1f))
    }
}

@Composable
private fun Metric(value: String, label: String, delta: String, valueSize: androidx.compose.ui.unit.TextUnit, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface), border = BorderStroke(1.dp, Border), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp)) {
            Text(value, fontSize = valueSize, fontWeight = FontWeight.ExtraBold, color = Primary)
            Text(label, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.35.sp, color = TextMuted, modifier = Modifier.padding(top = 5.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(delta, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Success, modifier = Modifier.padding(top = 6.dp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun ScoreCard(scoreHistory: List<ScorePoint>, currentScore: Int) {
    val scoreBars = scoreHistory.mapIndexed { index, point ->
        BarEntry(
            month = point.month,
            amount = point.score,
            isCurrent = index == scoreHistory.lastIndex,
        )
    }
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Surface), border = BorderStroke(1.dp, Border), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.tx_score_history), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                    Text("Last 12 months", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextFaint, modifier = Modifier.padding(top = 2.dp))
                }
                Text(currentScore.toString(), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Primary)
            }
            BorrowingBarChart(data = scoreBars, modifier = Modifier.padding(top = 4.dp))
        }
    }
}

@Composable
private fun BorrowCard(data: List<BarEntry>) {
    Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = Surface), border = BorderStroke(1.dp, Border), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(stringResource(R.string.tx_borrow_activity), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
            Text(stringResource(R.string.tx_monthly_amounts), fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextFaint)
            BorrowingBarChart(data = data)
        }
    }
}

@Composable
private fun Donuts(
    onTimeRate: Int,
    repayment: RepaymentBreakdown,
    purposes: List<PurposeSlice>,
) {
    val donutCardHeight = 288.dp
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            modifier = Modifier.weight(1f).height(donutCardHeight),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Border),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.tx_repay_status), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.padding(bottom = 10.dp))
                DonutChart(
                    segments = listOf(
                        DonutSegment("Track", 100f, SuccessSurface),
                        DonutSegment("Paid", repayment.paidPct.toFloat(), Success),
                        DonutSegment("Active", repayment.activePct.toFloat(), Gold.copy(alpha = 0.85f)),
                        DonutSegment("Overdue", repayment.overduePct.toFloat(), Error.copy(alpha = 0.86f)),
                    ),
                    centerText = "${repayment.paidPct}%",
                )
                Legend("Paid (${repayment.paidPct}%)", Success)
                Legend("Active (${repayment.activePct}%)", Gold)
                Legend("Overdue (${repayment.overduePct}%)", Error)
            }
        }
        Card(
            modifier = Modifier.weight(1f).height(donutCardHeight),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Surface),
            border = BorderStroke(1.dp, Border),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.tx_loan_purpose), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.padding(bottom = 10.dp))
                val palette = listOf(Primary, Gold, InfoBlue, Success, Color(0xFF7A5AF8))
                DonutChart(
                    segments = buildList {
                        add(DonutSegment("Track", 100f, PrimaryLight))
                        purposes.forEachIndexed { index, p ->
                            add(DonutSegment(p.label, p.percent.toFloat(), palette[index % palette.size].copy(alpha = 0.88f)))
                        }
                    },
                    centerText = "${purposes.size} types",
                )
                purposes.forEachIndexed { index, p ->
                    Legend("${p.label} (${p.percent}%)", palette[index % palette.size])
                }
            }
        }
    }
}

@Composable
private fun Legend(text: String, dot: Color) {
    Row(modifier = Modifier.padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(dot, CircleShape))
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = TextMuted)
    }
}

@Composable
private fun Feed(
    txs: List<PaymentFeedItem>,
    active: String,
    all: String,
    disb: String,
    repay: String,
    onFilter: (String) -> Unit,
    onItem: (PaymentFeedItem) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Header(stringResource(R.string.tx_all_tx))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 4.dp)) {
            items(listOf(all, disb, repay)) { label ->
                val selected = label == active
                Box(modifier = Modifier.background(if (selected) Primary else Surface, RoundedCornerShape(999.dp)).border(1.5.dp, if (selected) Primary else Border, RoundedCornerShape(999.dp)).clickable { onFilter(label) }.padding(horizontal = 16.dp, vertical = 7.dp)) {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (selected) Color.White else TextMuted)
                }
            }
        }
        txs.take(6).forEach { payment ->
            Card(modifier = Modifier.fillMaxWidth().clickable { onItem(payment) }, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Surface), border = BorderStroke(1.dp, Border), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp), verticalAlignment = Alignment.Top) {
                    val iconBg = if (payment.isRepayment) SuccessSurface else PrimaryLight
                    val iconTint = if (payment.isRepayment) Success else Primary
                    val icon = if (payment.isRepayment) Icons.Default.Check else Icons.Default.SouthEast
                    Box(modifier = Modifier.size(40.dp).background(iconBg, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(icon, null, tint = iconTint, modifier = Modifier.size(18.dp))
                    }
                    Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                        val title = when {
                            payment.statusLabel.equals("Paid", true) -> stringResource(R.string.tx_loan_repaid)
                            payment.isRepayment -> stringResource(R.string.tx_repay_received)
                            else -> stringResource(R.string.tx_loan_disbursed)
                        }
                        Text(title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary)
                        Text(payment.loanId, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextFaint, modifier = Modifier.padding(top = 1.dp))
                        Text(payment.date, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = TextFaint)
                    }
                    val amtColor = if (payment.isRepayment) Success else Error
                    val sign = if (payment.isRepayment) "+" else "-"
                    Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("$sign KES ${payment.amountKes}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = amtColor)
                        RepaymentStatusChip(status = payment.statusLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun Upcoming(navController: NavController, upcomingPayments: List<UpcomingPaymentItem>) {
    Column {
        Header(stringResource(R.string.tx_upcoming))
        Spacer(modifier = Modifier.height(4.dp))
        upcomingPayments.forEachIndexed { index, item ->
            val near = item.daysAway in 0..7
            val overdue = item.daysAway < 0
            val chip = when {
                overdue -> stringResource(R.string.tx_overdue)
                near -> stringResource(R.string.tx_due_soon)
                else -> null
            }
            val chipType = when {
                overdue -> ChipType.OVERDUE
                near -> ChipType.DUE_SOON
                else -> null
            }
            val dot = when {
                overdue -> Error
                near -> Gold
                else -> Border
            }
            val dayLabel = if (item.daysAway > 0 && near) " \u00b7 ${stringResource(R.string.tx_days_away, item.daysAway)}" else ""
            TimelineItem(
                date = item.date + dayLabel,
                title = if (index == upcomingPayments.lastIndex) stringResource(R.string.tx_loan_closes) else stringResource(R.string.tx_installment),
                id = item.loanId,
                amount = "KES ${item.amountKes}",
                chipLabel = chip,
                chipType = chipType,
                dotColor = dot,
                isLast = index == upcomingPayments.lastIndex,
                onClick = { navController.navigate("loan_schedule/1") },
            )
        }
    }
}

private fun List<com.loki.deni.data.local.entity.TransactionEntity>.toPaymentFeed(
    loans: List<com.loki.deni.data.local.entity.LoanEntity>,
): List<PaymentFeedItem> {
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    val now = System.currentTimeMillis()
    val loanById = loans.associateBy { it.loanId }
    return this.sortedByDescending { it.timestamp }.map {
        val loanStatus = it.loanId?.let { id -> loanById[id] }?.let { loan ->
            when {
                loan.isPaid -> "Paid"
                loan.dueDate < now -> "Overdue"
                else -> "Active"
            }
        }
        PaymentFeedItem(
            paymentId = it.transId,
            loanId = "LN-${(it.loanId ?: 0).toString().padStart(8, '0')}",
            title = it.title,
            date = fmt.format(Date(it.timestamp)),
            amountKes = it.amount.toInt(),
            isRepayment = it.type.equals("debit", true),
            statusLabel = loanStatus ?: when {
                it.status.equals("paid", true) -> "Paid"
                it.status.equals("overdue", true) -> "Overdue"
                else -> "Active"
            },
        )
    }
}

private fun List<com.loki.deni.data.local.entity.TransactionEntity>.toScoreHistory(base: Int): List<ScorePoint> {
    val calendar = java.util.Calendar.getInstance()
    val months = mutableListOf<String>()
    repeat(12) {
        months.add(
            0,
            SimpleDateFormat("MMM", Locale.ENGLISH).format(calendar.time),
        )
        calendar.add(java.util.Calendar.MONTH, -1)
    }
    return months.mapIndexed { index, month ->
        ScorePoint(month = month, score = (base - 22 + (index * 3)).coerceIn(300, 850))
    }
}

private fun List<com.loki.deni.data.local.entity.TransactionEntity>.toBorrowingBars(): List<BarEntry> {
    val fmt = SimpleDateFormat("MMM", Locale.ENGLISH)
    val grouped = this.filter { it.type.equals("credit", true) }
        .groupBy { fmt.format(Date(it.timestamp)) }
        .mapValues { (_, values) -> values.sumOf { it.amount }.toInt() }
    val labels = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return labels.mapIndexed { index, month ->
        BarEntry(month = month, amount = grouped[month] ?: 0, isCurrent = index == labels.lastIndex)
    }
}

private fun List<com.loki.deni.data.local.entity.LoanEntity>.toUpcomingPayments(): List<UpcomingPaymentItem> {
    val now = System.currentTimeMillis()
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    return this.filter { !it.isPaid }
        .sortedBy { it.dueDate }
        .take(4)
        .mapIndexed { idx, loan ->
            UpcomingPaymentItem(
                id = idx + 1,
                date = fmt.format(Date(loan.dueDate)),
                daysAway = ((loan.dueDate - now) / (24L * 60L * 60L * 1000L)).toInt(),
                title = loan.type,
                loanId = "LN-${loan.loanId.toString().padStart(8, '0')}",
                amountKes = ((loan.amount + (loan.amount * loan.interestRate) - loan.repaidAmount).coerceAtLeast(0.0)).toInt(),
            )
        }
}

private data class RepaymentBreakdown(
    val paidPct: Int = 0,
    val activePct: Int = 0,
    val overduePct: Int = 0,
)

private data class PurposeSlice(
    val label: String,
    val percent: Int,
)

private fun List<com.loki.deni.data.local.entity.LoanEntity>.toRepaymentBreakdown(): RepaymentBreakdown {
    if (isEmpty()) return RepaymentBreakdown()
    val total = size.toFloat()
    val paid = count { it.isPaid }
    val overdue = count { !it.isPaid && it.dueDate < System.currentTimeMillis() }
    val active = (size - paid - overdue).coerceAtLeast(0)
    return RepaymentBreakdown(
        paidPct = ((paid / total) * 100f).toInt(),
        activePct = ((active / total) * 100f).toInt(),
        overduePct = ((overdue / total) * 100f).toInt(),
    )
}

private fun List<com.loki.deni.data.local.entity.LoanEntity>.toPurposeBreakdown(): List<PurposeSlice> {
    if (isEmpty()) return listOf(PurposeSlice("No data", 0))
    val grouped = groupBy { loan -> normalizeLoanPurposeLabel(loan.type) }
    val total = size.toFloat()
    return grouped
        .map { (label, items) ->
            PurposeSlice(label = label, percent = ((items.size / total) * 100f).toInt())
        }
        .sortedByDescending { it.percent }
        .take(4)
}

private fun normalizeLoanPurposeLabel(raw: String): String {
    val normalized = raw
        .replace("loan", "", ignoreCase = true)
        .trim()
        .lowercase(Locale.getDefault())
    return when {
        normalized.contains("business") -> "Business"
        normalized.contains("emergency") -> "Emergency"
        else -> "Personal"
    }
}

@Composable
private fun Recommendations(compact: Boolean, onRepay: () -> Unit, onPricing: () -> Unit, onReminders: () -> Unit) {
    Column {
        Header(stringResource(R.string.tx_for_you))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 4.dp)) {
            item { RecoCard(stringResource(R.string.tx_reco_1), stringResource(R.string.tx_pay_now), listOf(Primary, PrimaryDark), compact, onRepay) }
            item { RecoCard(stringResource(R.string.tx_reco_2), stringResource(R.string.tx_learn_more), listOf(Color(0xFF8A6200), Color(0xFFB38400)), compact, onPricing) }
            item { RecoCard(stringResource(R.string.tx_reco_3), stringResource(R.string.tx_set_now), listOf(Color(0xFF2A5818), Color(0xFF3A7820)), compact, onReminders) }
        }
    }
}

@Composable
private fun RecoCard(title: String, action: String, colors: List<Color>, compact: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier.width(if (compact) 146.dp else 162.dp).background(Brush.linearGradient(colors), RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(15.dp),
    ) {
        DotGrid(0.07f)
        Column {
            Text(title, fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("$action \u2192", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = 0.58f), modifier = Modifier.padding(top = 10.dp))
        }
    }
}

@Composable
private fun Header(text: String) {
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.padding(bottom = 8.dp))
}

@Composable
private fun DotGrid(alpha: Float) {
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
private fun Ring(modifier: Modifier, size: androidx.compose.ui.unit.Dp) {
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(color = Color.White.copy(alpha = 0.06f), style = Stroke(1.dp.toPx()))
        }
    }
}
