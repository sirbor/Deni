package com.loki.deni.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.presentation.ui.theme.DeniAccent
import com.loki.deni.presentation.ui.theme.DeniBlue
import com.loki.deni.presentation.ui.theme.DeniError
import com.loki.deni.presentation.ui.theme.DeniPrimary
import com.loki.deni.presentation.ui.theme.DeniPrimaryDark
import com.loki.deni.presentation.ui.theme.DeniSuccess
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.ShimmerBox
import com.loki.deni.ui.model.InsightPeriod
import com.loki.deni.ui.model.InsightsUiData
import com.loki.deni.ui.model.InsightsUiState
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.viewmodel.InsightsViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.coroutines.launch

@Composable
fun InsightsScreen(navController: NavController, viewModel: InsightsViewModel = hiltViewModel()) {
    val compact = LocalConfiguration.current.screenWidthDp < 360
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.selectedPeriod.collectAsStateWithLifecycle()
    val scorePeriod by viewModel.scoreChartPeriod.collectAsStateWithLifecycle()
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val detailsSoonText = stringResource(R.string.insights_details_soon)
    Scaffold(
        snackbarHost = { SnackbarHost(hostState) },
        topBar = { DeniTopBar(title = stringResource(R.string.insights_title), showBackArrow = true, onBack = { navController.navigateUp() }) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                InsightPeriodSwitcher(selected = selectedPeriod, onSelect = viewModel::onPeriodChange)
            }
            when (val state = uiState) {
                InsightsUiState.Loading -> {
                    item { LoadingBlock() }
                }
                is InsightsUiState.Error -> {
                    item {
                        ErrorBlock(message = state.message, onRetry = viewModel::loadInsights)
                    }
                }
                is InsightsUiState.Success -> {
                    item { ScoreHero(state.data, viewModel) }
                    item { ScoreHistorySection(data = state.data, scorePeriod = scorePeriod, onPeriodSelect = viewModel::onScorePeriodChange) }
                    item { MetricsSection(data = state.data, compact = compact) }
                    item {
                        BorrowingActivitySection(data = state.data, onDetails = {
                            scope.launch { hostState.showSnackbar(detailsSoonText) }
                        })
                    }
                    item { LoanBreakdownSection(data = state.data) }
                    item { PaymentActivitySection(data = state.data) }
                    item { CostComparisonSection(data = state.data) }
                    item {
                        UpcomingPaymentsSection(
                            data = state.data,
                            onSchedule = { navController.navigate(DeniRoutes.LoanSchedule.createRoute(1)) },
                        )
                    }
                    item {
                        SavingsTipSection(
                            data = state.data,
                            onClick = { navController.navigate(DeniRoutes.LoanDetail.createRoute(1)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightPeriodSwitcher(selected: InsightPeriod, onSelect: (InsightPeriod) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)).padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        InsightPeriod.entries.forEach { period ->
            val isSelected = selected == period
            Box(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSelect(period) }.padding(vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(period.label, color = if (isSelected) DeniPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f), fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ScoreHero(data: InsightsUiData, viewModel: InsightsViewModel) {
    val isDark = isSystemInDarkTheme()
    val progress by animateFloatAsState(viewModel.getScoreProgress(data.currentScore), label = "score-progress")
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF111421), Color(0xFF121B30), Color(0xFF112847)) else listOf(Color(0xFF1A1A2E), Color(0xFF16213E), Color(0xFF0F3460)),
                    ),
                )
                .padding(20.dp),
        ) {
            Box(modifier = Modifier.align(Alignment.TopEnd).size(86.dp).background(Color.White.copy(alpha = 0.08f), CircleShape))
            Box(modifier = Modifier.align(Alignment.BottomStart).size(72.dp).background(DeniAccent.copy(alpha = if (isDark) 0.14f else 0.2f), CircleShape))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    ScoreRing(score = data.currentScore, progress = progress)
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("${data.scoreBand} Standing", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(data.percentileLabel, color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                        Box(modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(DeniSuccess.copy(alpha = 0.25f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                            Text("${data.scoreDeltaLabel}", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(12.dp)),
                    color = Color.Transparent,
                    trackColor = Color.White.copy(alpha = 0.1f),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.horizontalGradient(
                                if (isDark) listOf(DeniError.copy(alpha = 0.85f), DeniAccent.copy(alpha = 0.88f), DeniSuccess.copy(alpha = 0.88f)) else listOf(DeniError, DeniAccent, DeniSuccess),
                            ),
                        ),
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("Poor 300", "Fair 550", "Good 700", "Excellent 850").forEach {
                        Text(it, color = Color.White.copy(alpha = 0.35f), fontSize = 10.sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(data.onTimeRateLabel, data.loansCompletedLabel, data.tierLabel).forEach {
                        Box(modifier = Modifier.clip(RoundedCornerShape(20.dp)).background(Color.White.copy(alpha = 0.14f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                            Text(it, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScoreRing(score: Int, progress: Float) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
        Canvas(modifier = Modifier.size(80.dp)) {
            drawArc(Color.White.copy(alpha = 0.12f), -90f, 360f, false, style = Stroke(8.dp.toPx()))
            drawArc(
                brush = Brush.linearGradient(listOf(DeniAccent, DeniSuccess)),
                startAngle = -90f,
                sweepAngle = progress * 360f,
                useCenter = false,
                style = Stroke(8.dp.toPx(), cap = StrokeCap.Round),
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(score.toString(), color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("SCORE", color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
        }
    }
}

@Composable
private fun ScoreHistorySection(data: InsightsUiData, scorePeriod: InsightPeriod, onPeriodSelect: (InsightPeriod) -> Unit) {
    SectionCard(stringResource(R.string.insights_score_history)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            listOf(InsightPeriod.THREE_MONTHS, InsightPeriod.SIX_MONTHS, InsightPeriod.ONE_YEAR).forEach { period ->
                Text(
                    period.label,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).clickable { onPeriodSelect(period) }.padding(horizontal = 8.dp, vertical = 6.dp),
                    color = if (scorePeriod == period) DeniPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                )
            }
        }
        val entries = data.scorePoints.mapIndexed { idx, point -> FloatEntry(idx.toFloat(), point.score.toFloat()) }
        Chart(
            chart = lineChart(),
            model = entryModelOf(entries),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                itemPlacer = AxisItemPlacer.Horizontal.default(),
                valueFormatter = { value, _ -> data.scorePoints.getOrNull(value.toInt())?.month.orEmpty() },
            ),
            modifier = Modifier.fillMaxWidth().height(160.dp),
        )
    }
}

@Composable
private fun MetricsSection(data: InsightsUiData, compact: Boolean) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        items(data.metricCards) { metric ->
            Card(
                modifier = Modifier.width(if (compact) 128.dp else 140.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(metric.icon, fontSize = 22.sp)
                    Text(metric.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(metric.value, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    val deltaColor = when (metric.isPositive) {
                        true -> DeniSuccess
                        false -> DeniError
                        null -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    }
                    Text(metric.delta, fontSize = 11.sp, color = deltaColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BorrowingActivitySection(data: InsightsUiData, onDetails: () -> Unit) {
    SectionCard(stringResource(R.string.insights_borrowing_activity), actionText = stringResource(R.string.insights_details_arrow), onAction = onDetails) {
        val borrowed = data.trendPoints.mapIndexed { idx, point -> FloatEntry(idx.toFloat(), point.borrowed) }
        val repaid = data.trendPoints.mapIndexed { idx, point -> FloatEntry(idx.toFloat(), point.repaid) }
        Chart(
            chart = columnChart(),
            model = entryModelOf(borrowed, repaid),
            startAxis = rememberStartAxis(),
            bottomAxis = rememberBottomAxis(
                itemPlacer = AxisItemPlacer.Horizontal.default(),
                valueFormatter = { value, _ -> data.trendPoints.getOrNull(value.toInt())?.month.orEmpty() },
            ),
            modifier = Modifier.fillMaxWidth().height(160.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            LegendDot(DeniPrimary, stringResource(R.string.insights_borrowed))
            LegendDot(DeniAccent, stringResource(R.string.insights_repaid))
        }
    }
}

@Composable
private fun LoanBreakdownSection(data: InsightsUiData) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(modifier = Modifier.weight(1f), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.insights_by_purpose), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                DonutChart(values = data.purposeBreakdown.map { it.percent }, colors = listOf(DeniPrimary, DeniAccent, DeniError), centerTop = "7", centerBottom = stringResource(R.string.insights_loans))
                data.purposeBreakdown.forEach { item -> Text("${item.label} ${item.percent}%", fontSize = 11.sp) }
            }
        }
        Card(modifier = Modifier.weight(1f), border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))) {
            Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.insights_by_status), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                DonutChart(values = data.statusBreakdown.map { it.count }, colors = listOf(DeniSuccess, DeniBlue, DeniError), centerTop = "98%", centerBottom = stringResource(R.string.insights_repaid_short))
                data.statusBreakdown.forEach { item -> Text("${item.label} ${item.count}", fontSize = 11.sp) }
            }
        }
    }
}

@Composable
private fun DonutChart(values: List<Int>, colors: List<Color>, centerTop: String, centerBottom: String) {
    val total = values.sum().coerceAtLeast(1)
    Box(modifier = Modifier.size(120.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(100.dp)) {
            var start = -90f
            values.forEachIndexed { idx, value ->
                val sweep = value.toFloat() / total.toFloat() * 360f
                drawArc(colors[idx], start, sweep, false, style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round))
                start += sweep
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(centerTop, fontWeight = FontWeight.Bold)
            Text(centerBottom, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
    }
}

@Composable
private fun PaymentActivitySection(data: InsightsUiData) {
    SectionCard(stringResource(R.string.insights_payment_activity), actionText = stringResource(R.string.insights_last_12_weeks), onAction = null) {
        data.heatmap.chunked(7).forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(bottom = 4.dp)) {
                week.forEach { cell ->
                    val color = when (cell.level) {
                        0 -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                        1 -> DeniPrimary.copy(alpha = 0.2f)
                        2 -> DeniPrimary.copy(alpha = 0.4f)
                        3 -> DeniPrimary
                        else -> DeniPrimaryDark
                    }
                    Box(modifier = Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(color))
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Jan", "Feb", "Mar", "Apr").forEach { Text(it, fontSize = 11.sp) }
        }
    }
}

@Composable
private fun CostComparisonSection(data: InsightsUiData) {
    SectionCard(stringResource(R.string.insights_cost_comparison)) {
        Text(stringResource(R.string.insights_cost_caption), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Tenure", "EMI", "Interest", "Total").forEach { Text(it, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
        }
        Spacer(modifier = Modifier.height(6.dp))
        data.costComparison.forEach { row ->
            val bg = if (row.highlighted) DeniPrimary.copy(alpha = 0.1f) else Color.Transparent
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).background(bg).padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("${row.tenureMonths} Month")
                Text(row.emiLabel, fontSize = 12.sp)
                Text(row.interestLabel, fontSize = 12.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(row.totalLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    if (row.highlighted) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(DeniPrimary).padding(horizontal = 6.dp, vertical = 2.dp)) {
                            Text(stringResource(R.string.insights_active_badge), color = Color.White, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
        Text(stringResource(R.string.insights_cost_footnote), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun UpcomingPaymentsSection(data: InsightsUiData, onSchedule: () -> Unit) {
    SectionCard(stringResource(R.string.insights_upcoming_payments), actionText = stringResource(R.string.insights_full_schedule), onAction = onSchedule) {
        data.paymentTimeline.forEachIndexed { index, item ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
                    val color = when (item.status) {
                        "PAID" -> DeniPrimary
                        "DUE_SOON" -> DeniPrimary
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                    Box(modifier = Modifier.size(12.dp).background(color, CircleShape))
                    if (index != data.paymentTimeline.lastIndex) {
                        Box(modifier = Modifier.width(2.dp).height(32.dp).background(color.copy(alpha = 0.4f)))
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text("${item.dueDate}${item.daysLabel?.let { " - $it" } ?: ""}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(item.amountLabel, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(item.status.replace("_", " "), fontSize = 10.sp, color = DeniPrimary)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SavingsTipSection(data: InsightsUiData, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        if (isDark) listOf(Color(0xFF8B6A17), DeniAccent.copy(alpha = 0.84f)) else listOf(Color(0xFFDAA520), DeniAccent),
                    ),
                )
                .padding(18.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.insights_savings_title), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(
                    stringResource(R.string.insights_savings_body, data.savingsAmountLabel, data.savingsPayoffLabel),
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.85f),
                )
                TextButton(onClick = onClick, modifier = Modifier.clip(RoundedCornerShape(12.dp)).background(Color.White.copy(alpha = 0.2f))) {
                    Text(stringResource(R.string.insights_calculate_savings), color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, actionText: String? = null, onAction: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    val isDark = isSystemInDarkTheme()
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (actionText != null) {
                Text(actionText, fontSize = 12.sp, color = DeniPrimary, modifier = Modifier.clickable(enabled = onAction != null) { onAction?.invoke() })
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.24f else 0.14f)),
        ) {
            Column(modifier = Modifier.padding(16.dp), content = content)
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
    }
}

@Composable
private fun LoadingBlock() {
    Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(220.dp).padding(horizontal = 20.dp).clip(RoundedCornerShape(20.dp)))
        ShimmerBox(modifier = Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 20.dp).clip(RoundedCornerShape(16.dp)))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(3) {
                ShimmerBox(modifier = Modifier.weight(1f).height(92.dp).clip(RoundedCornerShape(14.dp)))
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(2) {
                ShimmerBox(modifier = Modifier.weight(1f).height(130.dp).clip(RoundedCornerShape(14.dp)))
            }
        }
    }
}

@Composable
private fun ErrorBlock(message: String, onRetry: () -> Unit) {
    val compact = LocalConfiguration.current.screenWidthDp < 360
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("!", fontSize = if (compact) 36.sp else 44.sp)
        Text(message, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        DeniButton(text = stringResource(R.string.retry), onClick = onRetry)
    }
}
