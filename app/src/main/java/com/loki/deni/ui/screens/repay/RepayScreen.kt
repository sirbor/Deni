package com.loki.deni.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import com.loki.deni.ui.components.DeniHeroBackground
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.components.SectionHeader
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.util.CurrencyFormatter
import com.loki.deni.ui.viewmodel.LoanDetailViewModel
import kotlinx.coroutines.launch

@Composable
fun RepayScreen(navController: NavController, loanId: Int = 0, viewModel: LoanDetailViewModel = hiltViewModel()) {
    val compact = LocalConfiguration.current.screenWidthDp < 360
    val isDark = isSystemInDarkTheme()
    val loan by viewModel.loan.collectAsStateWithLifecycle()
    val outstandingKes by viewModel.outstandingKes.collectAsStateWithLifecycle()
    val repayState by viewModel.repayState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    androidx.compose.runtime.LaunchedEffect(loanId) { viewModel.loadLoan(loanId) }
    LaunchedEffect(repayState) {
        when (val state = repayState) {
            is LoanDetailViewModel.RepayUiState.Success -> {
                navController.currentBackStackEntry?.savedStateHandle?.set("lastReceiptTxId", state.transactionId)
                val successLoanId = loan?.id ?: loanId
                navController.navigate(DeniRoutes.RepaySuccess.createRoute(successLoanId, state.receiptRef))
                viewModel.resetRepayState()
            }
            is LoanDetailViewModel.RepayUiState.Error -> {
                snackbarHostState.showSnackbar("Repayment failed: ${state.message}")
                viewModel.resetRepayState()
            }
            else -> Unit
        }
    }
    val outstanding = outstandingKes.coerceAtLeast(0)
    val installment = if (outstanding > 0) ((outstanding * 0.34f).toInt()).coerceAtLeast(1000).coerceAtMost(outstanding) else 0
    val partial = if (outstanding > 0) ((outstanding * 0.5f).toInt()).coerceAtLeast(installment).coerceAtMost(outstanding) else 0
    val methodMpesa = stringResource(R.string.method_mpesa)
    var selectedAmount by remember { mutableIntStateOf(0) }
    val loading = repayState is LoanDetailViewModel.RepayUiState.Loading
    val fullPayable = outstanding
    LaunchedEffect(outstanding, installment) {
        if (outstanding <= 0) {
            selectedAmount = 0
        } else if (selectedAmount <= 0 || selectedAmount > outstanding) {
            selectedAmount = if (installment > 0) installment else outstanding
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState()),
                ) {
            DeniHeroBackground(height = 220.dp, showBottomCurve = true) {
                Card(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = (-12).dp)
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) {
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.24f)
                        } else {
                            Color.White.copy(alpha = 0.14f)
                        },
                    ),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.24f else 0.18f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(stringResource(R.string.make_repayment), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        Text(stringResource(R.string.outstanding_balance), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                        Text(CurrencyFormatter.formatKes(outstanding.toDouble()), color = Color.White, fontSize = if (compact) 28.sp else 34.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Loan ID: ${loan?.id?.toString()?.padStart(8, '0') ?: "--------"}", color = Color.White.copy(alpha = 0.72f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Spacer(Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text("Selected repayment", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                            Text(
                                CurrencyFormatter.formatKes(selectedAmount.toDouble()),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 20.sp,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                                .padding(8.dp),
                        ) {
                            androidx.compose.material3.Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        SectionHeader(title = stringResource(R.string.payment_amount))
                        Text(CurrencyFormatter.formatKes(selectedAmount.toDouble()), fontSize = if (compact) 28.sp else 34.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            AmountChip("Inst.", installment, selectedAmount, Modifier.weight(1f)) { selectedAmount = installment }
                            AmountChip("Part.", partial, selectedAmount, Modifier.weight(1f)) { selectedAmount = partial }
                            AmountChip("Full", outstanding, selectedAmount, Modifier.weight(1f)) { selectedAmount = outstanding }
                        }
                    }
                }
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        SummaryLine("Outstanding", CurrencyFormatter.formatKes(outstanding.toDouble()))
                        SummaryLine("Full settlement", CurrencyFormatter.formatKes(fullPayable.toDouble()))
                        SummaryLine("After this payment", CurrencyFormatter.formatKes((outstanding - selectedAmount).coerceAtLeast(0).toDouble()))
                        SummaryLine("Payment channel", methodMpesa)
                    }
                }
                Spacer(Modifier.height(16.dp))
                GradientPrimaryButton(
                    text = stringResource(R.string.pay_via_method, CurrencyFormatter.formatKes(selectedAmount.toDouble()), methodMpesa),
                    onClick = {
                        val currentLoanId = loan?.id
                        if (currentLoanId == null) {
                            scope.launch { snackbarHostState.showSnackbar("No active loan found. Open Loans first.") }
                        } else {
                            viewModel.repay(currentLoanId, selectedAmount.toDouble(), null)
                        }
                    },
                    enabled = !loading && loan != null && selectedAmount > 0,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                )
            }
        }
    }

}

@Composable
private fun SummaryLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun AmountChip(
    label: String,
    amount: Int,
    selectedAmount: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val selected = selectedAmount == amount
    TextButton(
        onClick = onClick,
        modifier = modifier
            .background(
                if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f),
                RoundedCornerShape(50),
            )
            .padding(horizontal = 2.dp),
    ) {
        Text(
            "$label (${CurrencyFormatter.formatKes(amount.toDouble())})",
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

