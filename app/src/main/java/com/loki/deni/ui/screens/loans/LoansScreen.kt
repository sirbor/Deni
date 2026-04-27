package com.loki.deni.ui.screens

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.view.WindowCompat
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.ui.components.OfflineBanner
import com.loki.deni.ui.components.RepaymentStatusChip
import com.loki.deni.ui.components.rememberIsOnline
import com.loki.deni.ui.model.LoanStatus
import com.loki.deni.ui.model.LoanTransaction
import com.loki.deni.ui.viewmodel.AccountDataViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay

private enum class LoansTab { APPLY, MY_LOANS }

private val LoansPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val LoansPrimaryDark: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
private val LoansPrimaryDeep = Color(0xFF012E31)
private val LoansPrimaryLight: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
private val LoansBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background
private val LoansSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface
private val LoansTextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val LoansTextMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
private val LoansTextFaint: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
private val LoansBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
private val LoansGold = Color(0xFFF5A623)
private val LoansGoldSurface = Color(0xFFFFF8EC)
private val LoansSuccess = Color(0xFF437A22)
private val LoansSuccessSurface = Color(0xFFEAF3E5)
private val LoansError: Color
    @Composable get() = MaterialTheme.colorScheme.error
private val LoansErrorSurface: Color
    @Composable get() = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun LoansScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val user by viewModel.user.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val blockingLoan = loans.firstOrNull { !it.isPaid && outstandingForLoan(it) > 1.0 }
    val blockingLoanUi = blockingLoan?.toUiLoan()
    val compact = LocalConfiguration.current.screenWidthDp < 360
    val view = LocalView.current
    DisposableEffect(view) {
        view.context.findActivity()?.window?.let { window ->
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
        onDispose { }
    }

    var selectedTab by remember { mutableStateOf(LoansTab.APPLY) }
    var refreshing by remember { mutableStateOf(false) }
    val online = rememberIsOnline()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = {
            refreshing = true
        },
    )
    LaunchedEffect(refreshing) {
        if (refreshing) {
            delay(600)
            refreshing = false
        }
    }

    Scaffold(
        containerColor = LoansBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(LoansBackground)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullRefreshState),
            ) {
                if (!online) {
                    item { OfflineBanner() }
                }
                item {
                    LoansHero(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        availableLimit = stats.availableLimit,
                        inUse = stats.outstanding,
                        score = user?.creditScore ?: 500,
                    )
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(28.dp)
                            .offset(y = (-8).dp)
                            .background(LoansBackground, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
                    )
                }
                item {
                    Crossfade(
                        targetState = selectedTab,
                        animationSpec = tween(280),
                        label = "loans-tab-crossfade",
                    ) { tab ->
                        when (tab) {
                            LoansTab.APPLY -> LoansApplyContent(
                                navController = navController,
                                compact = compact,
                                availableLimit = stats.availableLimit.coerceAtLeast(500),
                                approvedLimit = stats.approvedLimit.coerceAtLeast(500),
                                activeOrOverdueLoanId = blockingLoan?.loanId,
                                activeOrOverdueLoan = blockingLoanUi,
                            )
                            LoansTab.MY_LOANS -> MyLoansContent(navController, loans = loans.map { it.toUiLoan() })
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing = refreshing,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

@Composable
private fun LoansHero(
    selectedTab: LoansTab,
    onTabSelected: (LoansTab) -> Unit,
    availableLimit: Int,
    inUse: Int,
    score: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(LoansPrimaryDeep, Color(0xFF014D52), LoansPrimary),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 360f),
                ),
            ),
    ) {
        DotGridOverlay()
        RingDecoration(Modifier.align(Alignment.TopEnd).offset(x = 80.dp, y = (-118).dp), 300.dp)
        RingDecoration(Modifier.align(Alignment.BottomStart).offset(x = (-52).dp, y = 68.dp), 180.dp)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.loans_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.3).sp,
                    color = Color.White,
                )
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                        .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape)
                        .clickable { },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
            }
            AvailableLimitStrip(availableLimit = availableLimit, inUse = inUse, score = score)
            Spacer(modifier = Modifier.height(14.dp))
            SegmentedTopTabs(selectedTab = selectedTab, onSelect = onTabSelected)
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AvailableLimitStrip(availableLimit: Int, inUse: Int, score: Int) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.12f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    stringResource(R.string.loans_available_limit).uppercase(Locale.ENGLISH),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.56f),
                    letterSpacing = 0.5.sp,
                )
                Text(
                    "KES %,d".format(availableLimit),
                    fontSize = 26.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.8).sp,
                    color = Color.White,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    stringResource(R.string.loans_in_use, "KES %,d".format(inUse)),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.46f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                ) {
                    Text(
                        stringResource(R.string.loans_score_pill, score),
                        color = Color.White.copy(alpha = 0.82f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                    )
                }
                Text(
                    stringResource(
                        R.string.loans_utilised,
                        if (availableLimit + inUse == 0) 0 else ((inUse.toFloat() / (availableLimit + inUse).toFloat()) * 100f).toInt(),
                    ),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.46f),
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LoansApplyContent(
    navController: NavController,
    compact: Boolean,
    availableLimit: Int,
    approvedLimit: Int,
    activeOrOverdueLoanId: Int?,
    activeOrOverdueLoan: LoanTransaction?,
) {
    val minAmount = 500
    val maxAmount = availableLimit
    var amount by remember(availableLimit) {
        mutableFloatStateOf(((maxAmount * 0.5f).coerceAtLeast(minAmount.toFloat())).coerceAtMost(maxAmount.toFloat()))
    }
    val tenureChoices = remember(amount) {
        LoanCalculator.allowedTenureDaysFor(
            userLimit = approvedLimit,
            principal = amount.toInt(),
        )
    }
    var tenure by remember(amount) { mutableStateOf<Int?>(tenureChoices.lastOrNull()) }
    LaunchedEffect(tenureChoices) {
        if (tenure !in tenureChoices) tenure = tenureChoices.lastOrNull()
    }
    val activeTenure = tenure ?: 14
    val effectiveRate = loanInterestRateForDays(activeTenure)
    val interest = (amount * effectiveRate).toInt()
    val processingFee = (amount * 0.03f).toInt()
    val total = amount.toInt() + interest + processingFee
    val amountError = amount.toInt() > availableLimit
    val capError = !isAmountValidForTenure(amount.toInt(), activeTenure, approvedLimit)
    val dueDate = remember(tenure) { tenure?.let(::dueDateLabel).orEmpty() }
    val canApply = !amountError && !capError && tenure != null

    if (activeOrOverdueLoanId != null) {
        Card(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = LoansSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoansBorder),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Active Loan Details", color = LoansTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                activeOrOverdueLoan?.let { loan ->
                    Text(loan.title, color = LoansTextMuted, fontWeight = FontWeight.SemiBold)
                    Text("Amount: KES %,d".format(loan.amount), color = LoansTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text("Disbursed: ${loan.disbursedDate}", color = LoansTextFaint, fontSize = 12.sp)
                    Text("Due: ${loan.dueDate}", color = if (loan.status == LoanStatus.OVERDUE) LoansError else LoansTextFaint, fontSize = 12.sp)
                    val totalRepayable = loan.totalRepayment.toInt()
                    val totalInterest = loan.totalInterest.toInt().coerceAtLeast(0)
                    val termDays = (loan.tenureMonths.coerceAtLeast(1)) * 30
                    val remaining = loan.remainingAmount.toInt().coerceAtLeast(0)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(10.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            SummaryRow("Total balance", "KES %,d".format(totalRepayable))
                            SummaryRow("Interest", "KES %,d".format(totalInterest))
                            SummaryRow("Term", "$termDays days")
                        }
                    }
                    Text(
                        text = if (loan.status == LoanStatus.PAID) "Outstanding: KES 0" else "Outstanding: KES %,d".format(remaining),
                        color = if (loan.status == LoanStatus.OVERDUE) LoansError else LoansTextMuted,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(LoansPrimary)
                                .clickable { navController.navigate("repay/$activeOrOverdueLoanId") }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text("Repay Now ->", color = Color.White, fontWeight = FontWeight.ExtraBold)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(LoansBackground)
                                .border(1.dp, LoansBorder, RoundedCornerShape(10.dp))
                                .clickable { navController.navigate("loan_detail/$activeOrOverdueLoanId") }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text("View Details", color = LoansPrimary, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                } ?: run {
                    Text("You already have an active loan. Repay it to apply for a new one.", color = LoansError, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoansBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(stringResource(R.string.loans_how_much), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = LoansTextPrimary)
                Text(
                    stringResource(R.string.loans_select_up_to, "KES %,d".format(availableLimit)),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LoansTextMuted,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "KES %,d".format(amount.toInt()),
                        fontSize = if (compact) 32.sp else 38.sp,
                        lineHeight = if (compact) 32.sp else 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1.5).sp,
                        color = LoansPrimary,
                    )
                    Text(
                        stringResource(R.string.loans_drag_hint),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LoansTextFaint,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                val sliderMin = minAmount.toFloat().coerceAtMost(maxAmount.toFloat())
                Slider(
                    value = amount,
                    onValueChange = { amount = it.coerceIn(sliderMin, maxAmount.toFloat()) },
                    valueRange = sliderMin..maxAmount.toFloat(),
                    steps = 0,
                    colors = SliderDefaults.colors(
                        activeTrackColor = LoansPrimary,
                        inactiveTrackColor = LoansPrimaryLight,
                        thumbColor = Color.White,
                    ),
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("KES %,d".format(sliderMin.toInt()), color = LoansTextFaint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    Text("KES %,d".format(maxAmount), color = LoansTextFaint, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
                if (amountError) {
                    Text(
                        stringResource(R.string.loans_error_limit, "KES %,d".format(availableLimit)),
                        color = LoansError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                if (capError) {
                    Text(
                        "For ${activeTenure} days, amount must be KES %,d - KES %,d".format(minAmount, maxAmount),
                        color = LoansError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                val quarter = (availableLimit / 4f)
                val generatedChips = listOf(
                    (quarter * 1).toInt(),
                    (quarter * 2).toInt(),
                    (quarter * 3).toInt(),
                    availableLimit,
                ).map { roundedPresetStep(it).coerceIn(sliderMin.toInt(), maxAmount) }
                    .distinct()
                val chips = if (generatedChips.size == 4) {
                    generatedChips
                } else {
                    listOf(
                        sliderMin.toInt(),
                        ((sliderMin + maxAmount) * 0.4f).toInt(),
                        ((sliderMin + maxAmount) * 0.7f).toInt(),
                        maxAmount,
                    ).map { roundedPresetStep(it).coerceIn(sliderMin.toInt(), maxAmount) }.distinct()
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    chips.take(4).forEach { value ->
                        val label = "KES %,d".format(value)
                        val selected = amount.toInt() == value
                        val chipBg by animateColorAsState(if (selected) LoansPrimary else LoansBackground, label = "amount-chip-bg")
                        val chipText by animateColorAsState(if (selected) Color.White else LoansTextMuted, label = "amount-chip-text")
                        val chipBorder by animateColorAsState(if (selected) LoansPrimary else LoansBorder, label = "amount-chip-border")
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(chipBg)
                                .border(1.5.dp, chipBorder, RoundedCornerShape(10.dp))
                                .clickable { amount = value.toFloat() }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = chipText,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, LoansBorder),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text(stringResource(R.string.loans_tenure_title), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = LoansTextPrimary)
                Text(
                    stringResource(R.string.loans_tenure_sub),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LoansTextMuted,
                    modifier = Modifier.padding(top = 3.dp),
                )
                Text(
                    "Eligible tenure for this amount: ${tenureChoices.joinToString(", ") { "$it days" }}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LoansPrimary,
                    modifier = Modifier.padding(top = 8.dp),
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 12.dp)) {
                    val tenures = listOf(14, 30, 45, 60)
                    items(tenures) { days ->
                        val enabled = days in tenureChoices
                        val selected = tenure == days
                        val bg by animateColorAsState(if (selected) LoansPrimary else LoansBackground, label = "tenure-bg")
                        val text by animateColorAsState(if (selected) Color.White else LoansTextMuted, label = "tenure-text")
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (enabled) bg else LoansTextFaint.copy(alpha = 0.15f))
                                    .border(1.5.dp, if (selected) LoansPrimary else LoansBorder, RoundedCornerShape(10.dp))
                                    .clickable(enabled = enabled) { tenure = days }
                                    .padding(horizontal = 14.dp, vertical = 9.dp),
                            ) {
                                Text(
                                    tenureLabel(days),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (enabled) text else LoansTextFaint,
                                )
                            }
                            if (days == 30) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 4.dp, y = (-7).dp)
                                        .background(LoansGold, RoundedCornerShape(999.dp))
                                        .padding(horizontal = 5.dp, vertical = 2.dp),
                                ) {
                                    Text(stringResource(R.string.loans_best), color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                                }
                            }
                        }
                    }
                }
                if (tenure == null) {
                    Text(
                        stringResource(R.string.loans_error_tenure),
                        color = LoansTextMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = LoansBackground),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LoansBorder),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                        SummaryRow(stringResource(R.string.loans_principal), "KES %,d".format(amount.toInt()))
                        SummaryRow(
                            stringResource(
                                R.string.loans_interest,
                                String.format(Locale.getDefault(), "%.2f", effectiveRate * 100f),
                            ),
                            "KES %,d".format(interest),
                        )
                        SummaryRow(stringResource(R.string.loans_processing), "KES %,d".format(processingFee))
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = LoansBorder)
                        SummaryRow(
                            label = stringResource(R.string.loans_total_payable),
                            value = "KES %,d".format(total),
                            labelColor = LoansTextPrimary,
                            labelWeight = FontWeight.ExtraBold,
                            valueColor = LoansPrimary,
                            valueSize = 18.sp,
                        )
                        SummaryRow(
                            label = stringResource(R.string.loans_due_date),
                            value = dueDate,
                            labelSize = 12.sp,
                            valueSize = 12.sp,
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (canApply) {
                                Brush.linearGradient(listOf(LoansPrimary, LoansPrimaryDark))
                            } else {
                                Brush.linearGradient(listOf(LoansTextFaint, LoansTextFaint))
                            },
                        )
                        .clickable(enabled = canApply) {
                            navController.navigate("borrow_review/${amount.toInt()}/${tenure ?: 30}/Personal")
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            stringResource(R.string.loans_apply_cta),
                            color = Color.White.copy(alpha = if (canApply) 1f else 0.5f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = if (canApply) 1f else 0.5f),
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MyLoansContent(navController: NavController, loans: List<LoanTransaction>) {
    val filterAll = stringResource(R.string.loans_filter_all)
    val filterActive = stringResource(R.string.loans_filter_active)
    val filterPaid = stringResource(R.string.loans_filter_paid)
    val filterOverdue = stringResource(R.string.loans_filter_overdue)
    var filter by remember { mutableStateOf(filterAll) }

    val filteredLoans = remember(filter, loans) {
        loans.filter {
            when (filter) {
                filterActive -> it.status == LoanStatus.ACTIVE
                filterPaid -> it.status == LoanStatus.PAID
                filterOverdue -> it.status == LoanStatus.OVERDUE
                else -> true
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 14.dp)) {
            items(listOf(filterAll, filterActive, filterPaid, filterOverdue)) { label ->
                val selected = filter == label
                val bg by animateColorAsState(if (selected) LoansPrimary else LoansSurface, label = "filter-bg")
                val txt by animateColorAsState(if (selected) Color.White else LoansTextMuted, label = "filter-text")
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(bg)
                        .border(1.5.dp, if (selected) LoansPrimary else LoansBorder, RoundedCornerShape(999.dp))
                        .clickable { filter = label }
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                ) {
                    Text(label, color = txt, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        if (filteredLoans.isEmpty()) {
            Card(shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(1.dp, LoansBorder)) {
                Text(stringResource(R.string.loans_empty_state), modifier = Modifier.padding(20.dp), color = LoansTextMuted)
            }
        }
        filteredLoans.forEachIndexed { idx, loan ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(idx * 60L)
                visible = true
            }
            AnimatedVisibility(visible = visible, enter = fadeIn(animationSpec = tween(280)) + slideInVertically(initialOffsetY = { 20 })) {
                LoanListCard(loan = loan, onClick = { navController.navigate("loan_detail/${loan.id}") })
            }
        }
    }
}

@Composable
private fun LoanListCard(
    loan: com.loki.deni.ui.model.LoanTransaction,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = LoansSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, LoansBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            val accent = when (loan.status) {
                LoanStatus.ACTIVE -> LoansPrimary
                LoanStatus.PAID -> LoansSuccess
                LoanStatus.OVERDUE -> LoansError
            }
            Box(modifier = Modifier.width(5.dp).fillMaxHeight().background(accent))
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    val days = loan.tenureMonths * 30
                    val paddedId = loan.id.toString().padStart(8, '0')
                    Text(
                        text = if (loan.status == LoanStatus.OVERDUE) {
                            stringResource(R.string.loans_loan_id_over, paddedId, days)
                        } else {
                            stringResource(R.string.loans_loan_id, paddedId, days)
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoansTextFaint,
                    )
                    RepaymentStatusChip(status = loan.status)
                }

                Text(
                    "KES %,d".format(loan.amount),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = LoansTextPrimary,
                    letterSpacing = (-0.4).sp,
                )
                Text(
                    stringResource(R.string.loans_disbursed_due, loan.disbursedDate, loan.dueDate),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LoansTextFaint,
                    modifier = Modifier.padding(top = 3.dp),
                )

                val repaid = when (loan.status) {
                    LoanStatus.PAID -> loan.totalRepayment.toInt()
                    else -> loan.repaidAmount.toInt().coerceAtLeast(0)
                }
                val remaining = loan.remainingAmount.toInt().coerceAtLeast(0)
                val progress = when (loan.status) {
                    LoanStatus.PAID -> 1f
                    else -> if (loan.totalRepayment <= 0.0) 0f else (repaid.toFloat() / loan.totalRepayment.toFloat()).coerceIn(0f, 1f)
                }

                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(LoansBackground),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .height(4.dp)
                            .background(
                                when (loan.status) {
                                    LoanStatus.ACTIVE -> Brush.linearGradient(listOf(LoansPrimary, LoansPrimaryDark))
                                    LoanStatus.PAID -> Brush.linearGradient(listOf(LoansSuccess, LoansSuccess))
                                    LoanStatus.OVERDUE -> Brush.linearGradient(listOf(LoansError, Color(0xFFD84040)))
                                },
                            ),
                    )
                }

                Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        stringResource(R.string.loans_paid, "KES %,d".format(repaid)),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = LoansTextMuted,
                    )
                    Text(
                        text = if (loan.status == LoanStatus.PAID) {
                            stringResource(R.string.loans_fully_settled)
                        } else {
                            stringResource(R.string.loans_remaining, "KES %,d".format(remaining))
                        },
                        fontSize = 11.sp,
                        fontWeight = if (loan.status == LoanStatus.PAID) FontWeight.ExtraBold else FontWeight.Bold,
                        color = when (loan.status) {
                            LoanStatus.PAID -> LoansSuccess
                            LoanStatus.OVERDUE -> LoansError
                            LoanStatus.ACTIVE -> LoansTextMuted
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SegmentedTopTabs(
    selectedTab: LoansTab,
    onSelect: (LoansTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.10f))
            .border(1.dp, Color.White.copy(alpha = 0.10f), RoundedCornerShape(14.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SegmentChip(
            text = stringResource(R.string.loans_tab_apply),
            selected = selectedTab == LoansTab.APPLY,
            onClick = { onSelect(LoansTab.APPLY) },
            modifier = Modifier.weight(1f),
        )
        SegmentChip(
            text = stringResource(R.string.loans_tab_my),
            selected = selectedTab == LoansTab.MY_LOANS,
            onClick = { onSelect(LoansTab.MY_LOANS) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SegmentChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            color = if (selected) LoansPrimary else Color.White.copy(alpha = 0.55f),
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold,
        )
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    labelColor: Color = LoansTextMuted,
    labelSize: TextUnit = 13.sp,
    labelWeight: FontWeight = FontWeight.SemiBold,
    valueColor: Color = LoansTextPrimary,
    valueSize: TextUnit = 13.sp,
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, color = labelColor, fontSize = labelSize, fontWeight = labelWeight, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(value, color = valueColor, fontSize = valueSize, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun DotGridOverlay(alpha: Float = 0.05f) {
    val density = LocalDensity.current
    Canvas(modifier = Modifier.fillMaxWidth().height(260.dp)) {
        val step = with(density) { 22.dp.toPx() }
        var y = 0f
        while (y <= size.height) {
            var x = 0f
            while (x <= size.width) {
                drawCircle(Color.White.copy(alpha = alpha), radius = 1.5.dp.toPx(), center = Offset(x, y))
                x += step
            }
            y += step
        }
    }
}

@Composable
private fun RingDecoration(modifier: Modifier, size: Dp) {
    Box(modifier = modifier.size(size).border(1.dp, Color.White.copy(alpha = 0.06f), CircleShape))
}

@Composable
private fun tenureLabel(days: Int): String = when (days) {
    14 -> stringResource(R.string.loans_tenure_14)
    30 -> stringResource(R.string.loans_tenure_30)
    45 -> "45 days"
    60 -> stringResource(R.string.loans_tenure_60)
    else -> "60 days"
}

private fun dueDateLabel(days: Int): String {
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, days)
    return SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(cal.time)
}

private fun loanInterestRateForDays(days: Int): Float {
    return 0.20f * (days.coerceAtLeast(1) / 60f)
}

private fun isAmountValidForTenure(amount: Int, tenureDays: Int, userLimit: Int): Boolean {
    if (amount < 500) return false
    val allowed = LoanCalculator.allowedTenureDaysFor(userLimit = userLimit, principal = amount)
    return tenureDays in allowed
}

private fun roundedPresetStep(value: Int): Int {
    val step = if (value < 10_000) 500 else 1_000
    val halfStep = step / 2
    return ((value + halfStep) / step) * step
}

private fun com.loki.deni.data.local.entity.LoanEntity.toUiLoan(): LoanTransaction {
    val totalRepayment = amount + (amount * interestRate)
    val remaining = (totalRepayment - repaidAmount).coerceAtLeast(0.0)
    val status = when {
        isPaid || remaining <= 1.0 -> LoanStatus.PAID
        dueDate < System.currentTimeMillis() -> LoanStatus.OVERDUE
        else -> LoanStatus.ACTIVE
    }
    return LoanTransaction(
        id = loanId,
        title = type,
        amount = amount.toInt(),
        monthlyEmi = if (tenureMonths <= 0) totalRepayment else totalRepayment / tenureMonths,
        totalInterest = totalRepayment - amount,
        totalRepayment = totalRepayment,
        repaidAmount = repaidAmount.coerceAtLeast(0.0),
        remainingAmount = remaining,
        disbursedDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(disbursedDate)),
        dueDate = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(dueDate)),
        tenureMonths = tenureMonths,
        status = status,
        monthGroup = SimpleDateFormat("MMM yyyy", Locale.ENGLISH).format(Date(timestamp)),
    )
}

private fun outstandingForLoan(loan: com.loki.deni.data.local.entity.LoanEntity): Double {
    val totalRepayment = loan.amount + (loan.amount * loan.interestRate)
    return (totalRepayment - loan.repaidAmount).coerceAtLeast(0.0)
}
