package com.loki.deni.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.model.LoanPurpose
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.viewmodel.LoanDetailViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LoanTopupScreen(
    navController: NavController,
    loanId: Int,
    viewModel: LoanDetailViewModel = hiltViewModel(),
) {
    val loan by viewModel.loan.collectAsStateWithLifecycle()
    val topupHeadroom by viewModel.topupHeadroomKes.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(loanId) { viewModel.loadLoan(loanId) }

    val hasHeadroom = topupHeadroom >= 1000
    val maxTopup = if (hasHeadroom) topupHeadroom.toFloat() else 1000f
    var amount by remember(maxTopup) { mutableFloatStateOf((maxTopup * 0.4f).coerceAtLeast(1000f)) }
    var extensionDays by remember { mutableIntStateOf(30) }
    val currentOutstanding = ((loan?.totalRepayment ?: 0.0) - (loan?.amount ?: 0)).coerceAtLeast(0.0)
    val approvedLimitForUser = (topupHeadroom + currentOutstanding.toInt()).coerceAtLeast(0)
    val mergedPrincipal = (currentOutstanding + amount).coerceAtLeast(amount.toDouble())
    val topupRate = LoanCalculator.proratedRateForDays(extensionDays)
    val topupInterest = LoanCalculator.interestForDays(mergedPrincipal, extensionDays)
    val topupFee = LoanCalculator.processingFeeForAmount(mergedPrincipal)
    val totalRepayable = mergedPrincipal + topupInterest + topupFee
    val eligibleDays = remember(mergedPrincipal, approvedLimitForUser) {
        LoanCalculator.allowedTenureDaysFor(
            userLimit = approvedLimitForUser,
            principal = mergedPrincipal.toInt(),
        )
    }
    val purposeOptions = remember { LoanPurpose.entries.toList() }
    var selectedPurpose by remember { mutableStateOf(LoanPurpose.PERSONAL) }
    val reviewPurpose = selectedPurpose.toDisplayLabel()
    LaunchedEffect(eligibleDays) {
        if (extensionDays !in eligibleDays) extensionDays = eligibleDays.last()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            DeniTopBar(title = "Loan Top Up", showBackArrow = true, onBack = { navController.navigateUp() })
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(listOf(Color(0xFF015B61), Color(0xFF014D52), Color(0xFF012E31))),
                            )
                            .padding(16.dp),
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Loan #${loan?.id ?: loanId}", color = Color.White.copy(alpha = 0.84f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("Top up and extend safely", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Recalculate interest, fees and due date before applying.", color = Color.White.copy(alpha = 0.72f), fontSize = 12.sp)
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Top up amount", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text("KES ${amount.toInt()}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp)
                        Slider(
                            value = amount,
                            onValueChange = { amount = it },
                            valueRange = 1000f..maxTopup,
                            enabled = hasHeadroom,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary,
                                inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            ),
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("KES 1,000", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                            Text("KES ${maxTopup.toInt()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 11.sp)
                        }
                        Text(
                            "Available top up headroom: KES ${topupHeadroom.coerceAtLeast(0)}",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (!hasHeadroom) {
                            Text(
                                "No top up headroom available. Repay first to unlock top up.",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Purpose of top up", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text(
                            "Select why you need this top up",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            purposeOptions.forEach { purpose ->
                                val selected = selectedPurpose == purpose
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                                            RoundedCornerShape(999.dp),
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
                                            RoundedCornerShape(999.dp),
                                        )
                                        .clickable { selectedPurpose = purpose }
                                        .padding(vertical = 10.dp),
                                ) {
                                    Text(
                                        purpose.toDisplayLabel(),
                                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                    )
                                }
                            }
                        }
                        Text(
                            "Selected purpose: $reviewPurpose",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Extend repayment days", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                        Text(
                            "Eligible tenure for this amount: ${eligibleDays.joinToString(", ") { "$it days" }}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(14, 30, 45, 60).forEach { days ->
                                val selected = extensionDays == days
                                val enabled = days in eligibleDays
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(
                                            if (!enabled) {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.12f)
                                            } else if (selected) {
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
                                            },
                                            RoundedCornerShape(12.dp),
                                        )
                                        .border(
                                            1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            RoundedCornerShape(12.dp),
                                        )
                                        .clickable(enabled = enabled) { extensionDays = days }
                                        .padding(vertical = 10.dp),
                                ) {
                                    Text(
                                        "$days d",
                                        modifier = Modifier.align(androidx.compose.ui.Alignment.Center),
                                        fontWeight = FontWeight.Bold,
                                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f),
                                    )
                                }
                            }
                        }
                    }
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        TopupRow("Merged principal", "KES ${mergedPrincipal.toInt()}")
                        TopupRow("Interest (${String.format("%.2f", topupRate * 100)}%)", "KES ${topupInterest.toInt()}")
                        TopupRow("Processing fee (3%)", "KES ${topupFee.toInt()}")
                        TopupRow("Total repayable", "KES ${totalRepayable.toInt()}", highlight = true)
                    }
                }

                GradientPrimaryButton(
                    text = "Continue to Review",
                    onClick = {
                        if (loan == null) return@GradientPrimaryButton
                        navController.navigate(
                            DeniRoutes.LoanTopupReview.createRoute(
                                loanId = loanId,
                                amount = amount.toInt(),
                                days = extensionDays,
                                purpose = reviewPurpose,
                            ),
                        )
                    },
                    enabled = hasHeadroom,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                )
            }
        }
    }
}

@Composable
private fun TopupRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), fontWeight = if (highlight) FontWeight.Bold else FontWeight.SemiBold)
        Text(value, color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
    }
}

private fun LoanPurpose.toDisplayLabel(): String = name.lowercase()
    .replaceFirstChar { it.uppercase() }
