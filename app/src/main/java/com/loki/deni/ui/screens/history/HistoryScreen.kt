package com.loki.deni.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.presentation.ui.theme.DeniAccent
import com.loki.deni.presentation.ui.theme.DeniBlue
import com.loki.deni.presentation.ui.theme.DeniPrimary
import com.loki.deni.presentation.ui.theme.DeniPrimaryDark
import com.loki.deni.presentation.ui.theme.DeniSuccess
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.OfflineBanner
import com.loki.deni.ui.components.RepaymentStatusChip
import com.loki.deni.ui.model.ChartPeriod
import com.loki.deni.ui.model.HistorySummary
import com.loki.deni.ui.model.LoanStatus
import com.loki.deni.ui.model.LoanTransaction
import com.loki.deni.ui.model.MonthlyChartPoint
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.util.ConnectivityObserver
import com.loki.deni.ui.viewmodel.HistoryUiState
import com.loki.deni.ui.viewmodel.HistoryViewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.axis.AxisItemPlacer
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.entryModelOf

@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    var isOnline by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        ConnectivityObserver(context).observe().collect { isOnline = it }
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val search by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    val expandedTxId by viewModel.expandedTxId.collectAsStateWithLifecycle()
    val activePeriod by viewModel.activePeriod.collectAsStateWithLifecycle()

    val textMuted = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    val textFaint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    val border = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
    val divider = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val surface2 = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)

    Scaffold(
        topBar = {
            DeniTopBar(
                title = stringResource(R.string.history_title),
                showBackArrow = true,
                onBack = { navController.navigateUp() },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            HistoryUiState.Loading -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    items(4) { SkeletonHistoryRow() }
                }
            }
            HistoryUiState.Empty -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    item { SearchAndFilters(search, activeFilter, viewModel, border) }
                    item { EmptyState(textMuted, textFaint) }
                }
            }
            is HistoryUiState.Error -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    item { Text(state.message, modifier = Modifier.padding(16.dp)) }
                }
            }
            is HistoryUiState.Success -> {
                LazyColumn(
                    modifier = Modifier.padding(padding),
                    contentPadding = PaddingValues(bottom = 80.dp),
                ) {
                    item { OfflineBanner(isOnline) }
                    item { SummaryCards(summary = state.summary, formatter = viewModel::formatCurrency, textMuted = textMuted, border = border) }
                    item { ActivityChartSection(activePeriod = activePeriod, chartData = state.chartData, onPeriodChange = viewModel::onPeriodChange, textMuted = textMuted, border = border) }
                    item { DonutSection(summary = state.summary, textMuted = textMuted, border = border, surface2 = surface2) }
                    item { SearchAndFilters(search, activeFilter, viewModel, border) }
                    state.groupedTransactions.forEach { (month, transactions) ->
                        item {
                            Text(
                                text = month.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textFaint,
                                letterSpacing = 0.8.sp,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            )
                        }
                        items(transactions) { tx ->
                            TxRow(
                                tx = tx,
                                expanded = expandedTxId == tx.id,
                                onToggle = { viewModel.toggleExpandTx(tx.id) },
                                formatter = viewModel::formatCurrency,
                                textMuted = textMuted,
                                divider = divider,
                                surface2 = surface2,
                                onViewDetails = { navController.navigate(DeniRoutes.LoanDetail.createRoute(tx.id)) },
                            )
                        }
                    }
                    if (state.filteredTransactions.isEmpty()) {
                        item { EmptyState(textMuted, textFaint) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryCards(
    summary: HistorySummary,
    formatter: (Int) -> String,
    textMuted: Color,
    border: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        ) {
            Box(
                modifier = Modifier.background(Brush.linearGradient(listOf(DeniPrimary, DeniPrimaryDark))).padding(14.dp),
            ) {
                Column {
                    Text(stringResource(R.string.history_total_borrowed), color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
                    Text(formatter(summary.totalBorrowed), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.history_loans_total, summary.totalLoans), color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
        }
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, border),
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(stringResource(R.string.history_total_repaid), color = textMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(formatter(summary.totalRepaid), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.history_repay_rate_up, summary.repayRate), color = DeniSuccess, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ActivityChartSection(
    activePeriod: ChartPeriod,
    chartData: List<MonthlyChartPoint>,
    onPeriodChange: (ChartPeriod) -> Unit,
    textMuted: Color,
    border: Color,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.history_loan_activity), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            SingleChoiceSegmentedButtonRow {
                ChartPeriod.entries.forEachIndexed { index, period ->
                    SegmentedButton(
                        selected = period == activePeriod,
                        onClick = { onPeriodChange(period) },
                        shape = androidx.compose.material3.SegmentedButtonDefaults.itemShape(index, ChartPeriod.entries.size),
                    ) { Text(period.label) }
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, border),
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                val borrowed = chartData.map { it.borrowed }
                val repaid = chartData.map { it.repaid }
                val borrowedEntries = borrowed.mapIndexed { index, value -> FloatEntry(index.toFloat(), value) }
                val repaidEntries = repaid.mapIndexed { index, value -> FloatEntry(index.toFloat(), value) }
                Chart(
                    chart = columnChart(),
                    model = entryModelOf(borrowedEntries, repaidEntries),
                    startAxis = rememberStartAxis(valueFormatter = { value, _ -> "KES ${(value / 1000f).toInt()}K" }),
                    bottomAxis = rememberBottomAxis(
                        itemPlacer = AxisItemPlacer.Horizontal.default(),
                        valueFormatter = { value, _ ->
                            chartData.getOrNull(value.toInt())?.monthLabel.orEmpty()
                        },
                    ),
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    LegendDot(color = DeniPrimary, label = stringResource(R.string.history_borrowed), textMuted = textMuted)
                    LegendDot(color = DeniAccent, label = stringResource(R.string.history_repaid), textMuted = textMuted)
                }
            }
        }
    }
}

@Composable
private fun DonutSection(
    summary: HistorySummary,
    textMuted: Color,
    border: Color,
    surface2: Color,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Card(modifier = Modifier.weight(1f), border = BorderStroke(1.dp, border), colors = CardDefaults.cardColors(containerColor = surface2)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.history_by_status), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMuted)
                DonutChart(
                    values = listOf(summary.paidCount, summary.activeCount, summary.overdueCount),
                    colors = listOf(DeniSuccess, DeniBlue, MaterialTheme.colorScheme.error),
                    centerTitle = stringResource(R.string.history_loans_center, summary.totalLoans),
                )
                Text(stringResource(R.string.history_status_paid, summary.paidCount), fontSize = 11.sp, color = textMuted)
                Text(stringResource(R.string.history_status_active, summary.activeCount), fontSize = 11.sp, color = textMuted)
                Text(stringResource(R.string.history_status_overdue, summary.overdueCount), fontSize = 11.sp, color = textMuted)
            }
        }
        Card(modifier = Modifier.weight(1f), border = BorderStroke(1.dp, border), colors = CardDefaults.cardColors(containerColor = surface2)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(stringResource(R.string.history_repayment), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textMuted)
                DonutChart(
                    values = listOf(summary.onTimeCount, summary.lateCount),
                    colors = listOf(DeniPrimary, DeniAccent),
                    centerTitle = stringResource(R.string.history_on_time_center, summary.repayRate),
                )
                Text(stringResource(R.string.history_on_time, summary.onTimeCount), fontSize = 11.sp, color = textMuted)
                Text(stringResource(R.string.history_late, summary.lateCount), fontSize = 11.sp, color = textMuted)
            }
        }
    }
}

@Composable
private fun SearchAndFilters(
    search: String,
    activeFilter: LoanStatus?,
    viewModel: HistoryViewModel,
    border: Color,
) {
    Column {
        OutlinedTextField(
            value = search,
            onValueChange = viewModel::onSearchQueryChange,
            placeholder = { Text(stringResource(R.string.history_search_placeholder)) },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            trailingIcon = {
                if (search.isNotEmpty()) {
                    IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                        Icon(Icons.Default.Close, null)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            item {
                FilterChip(
                    selected = activeFilter == null,
                    onClick = { viewModel.onFilterChange(null) },
                    label = { Text(stringResource(R.string.history_filter_all)) },
                )
            }
            items(LoanStatus.entries) { status ->
                FilterChip(
                    selected = activeFilter == status,
                    onClick = { viewModel.onFilterChange(status) },
                    label = {
                        Text(
                            when (status) {
                                LoanStatus.PAID -> stringResource(R.string.history_filter_paid)
                                LoanStatus.ACTIVE -> stringResource(R.string.history_filter_active)
                                LoanStatus.OVERDUE -> stringResource(R.string.history_filter_overdue)
                            },
                        )
                    },
                    colors = androidx.compose.material3.FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DeniPrimary,
                        selectedLabelColor = Color.White,
                    ),
                    border = androidx.compose.material3.FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = activeFilter == status,
                        borderColor = border,
                        selectedBorderColor = DeniPrimary,
                    ),
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
    }
}

@Composable
private fun TxRow(
    tx: LoanTransaction,
    expanded: Boolean,
    onToggle: () -> Unit,
    formatter: (Int) -> String,
    textMuted: Color,
    divider: Color,
    surface2: Color,
    onViewDetails: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val (icon, bg, tint) = when (tx.status) {
                LoanStatus.PAID -> Triple(Icons.Default.CheckCircle, DeniSuccess.copy(alpha = 0.15f), DeniSuccess)
                LoanStatus.ACTIVE -> Triple(Icons.Default.CreditCard, DeniBlue.copy(alpha = 0.15f), DeniBlue)
                LoanStatus.OVERDUE -> Triple(Icons.Default.Warning, MaterialTheme.colorScheme.error.copy(alpha = 0.12f), MaterialTheme.colorScheme.error)
            }
            Box(
                modifier = Modifier.size(42.dp).background(bg, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(tx.title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(stringResource(R.string.history_month_tenure, tx.disbursedDate, tx.tenureMonths), fontSize = 12.sp, color = textMuted)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(formatter(tx.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                RepaymentStatusChip(tx.status)
            }
        }
        HorizontalDivider(color = divider)
        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = surface2),
                border = BorderStroke(1.dp, divider),
            ) {
                val details = listOf(
                    stringResource(R.string.history_detail_loan_amount) to formatter(tx.amount),
                    stringResource(R.string.history_detail_monthly_emi) to formatter(tx.monthlyEmi.toInt()),
                    stringResource(R.string.history_detail_interest) to formatter(tx.totalInterest.toInt()),
                    stringResource(R.string.history_detail_total_repay) to formatter(tx.totalRepayment.toInt()),
                    stringResource(R.string.history_detail_disbursed) to tx.disbursedDate,
                    stringResource(R.string.history_detail_due_date) to tx.dueDate,
                )
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    details.chunked(2).forEach { pair ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            pair.forEach { item ->
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.first, fontSize = 11.sp, color = textMuted)
                                    Text(item.second, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onViewDetails) { Text("View Details") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    values: List<Int>,
    colors: List<Color>,
    centerTitle: String,
) {
    val total = values.sum().coerceAtLeast(1)
    Box(modifier = Modifier.size(130.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(110.dp)) {
            var start = -90f
            values.forEachIndexed { index, value ->
                val sweep = (value.toFloat() / total.toFloat()) * 360f
                drawArc(
                    color = colors[index],
                    startAngle = start,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = 18.dp.toPx(), cap = StrokeCap.Round),
                )
                start += sweep
            }
        }
        Text(centerTitle, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun LegendDot(color: Color, label: String, textMuted: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
        Text(label, fontSize = 12.sp, color = textMuted)
    }
}

@Composable
private fun EmptyState(textMuted: Color, textFaint: Color) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Outlined.ReceiptLong, contentDescription = null, modifier = Modifier.size(64.dp), tint = textFaint)
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.no_transactions), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = textMuted)
        Spacer(modifier = Modifier.height(8.dp))
        Text(stringResource(R.string.history_empty_subtitle), fontSize = 13.sp, color = textFaint)
    }
}

@Composable
private fun SkeletonHistoryRow() {
    val block = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.size(42.dp).background(block, RoundedCornerShape(12.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp).background(block, RoundedCornerShape(6.dp)))
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(10.dp).background(block, RoundedCornerShape(6.dp)))
        }
        Box(modifier = Modifier.width(70.dp).height(12.dp).background(block, RoundedCornerShape(6.dp)))
    }
}
