package com.loki.deni.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.viewmodel.LoanDetailViewModel

@Composable
fun LoanTopupReviewScreen(
    navController: NavController,
    loanId: Int,
    amount: Int,
    days: Int,
    purpose: String,
    viewModel: LoanDetailViewModel = hiltViewModel(),
) {
    val topupState by viewModel.topupState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showSuccessCard by remember { mutableStateOf(false) }
    var successDueDate by remember { mutableStateOf("") }
    val successCardScale by animateFloatAsState(
        targetValue = if (showSuccessCard) 1f else 0.92f,
        label = "successCardScale",
    )
    val principal = amount.toDouble()
    val rate = LoanCalculator.proratedRateForDays(days)
    val interest = LoanCalculator.interestForDays(principal, days).toInt()
    val fee = LoanCalculator.processingFeeForAmount(principal).toInt()
    val total = LoanCalculator.totalRepaymentForDays(principal, days).toInt()

    LaunchedEffect(topupState) {
        when (val state = topupState) {
            is LoanDetailViewModel.TopupUiState.Success -> {
                successDueDate = state.newDueDate
                showSuccessCard = true
                delay(2100)
                showSuccessCard = false
                delay(220)
                viewModel.resetTopupState()
                navController.navigate(DeniRoutes.Loans.route) {
                    popUpTo(DeniRoutes.Loans.route) { inclusive = false }
                    launchSingleTop = true
                }
            }
            is LoanDetailViewModel.TopupUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetTopupState()
            }
            else -> Unit
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                DeniTopBar(title = "Review Top Up", showBackArrow = true, onBack = { navController.navigateUp() })
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ReviewRow("Top up amount", "KES %,d".format(amount))
                            ReviewRow("Purpose", purpose)
                            ReviewRow("Extension", "$days days")
                            ReviewRow("Interest (${String.format("%.2f", rate * 100)}%)", "KES %,d".format(interest))
                            ReviewRow("Processing fee (3%)", "KES %,d".format(fee))
                            ReviewRow("Total repayable", "KES %,d".format(total), highlight = true)
                        }
                    }

                    GradientPrimaryButton(
                        text = if (topupState is LoanDetailViewModel.TopupUiState.Loading) "Applying..." else "Confirm Top Up",
                        onClick = { viewModel.topUpLoan(loanId, amount.toDouble(), days, purpose) },
                        enabled = topupState !is LoanDetailViewModel.TopupUiState.Loading,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                    )
                }
            }

            AnimatedVisibility(
                visible = showSuccessCard,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically),
                modifier = Modifier.align(Alignment.Center),
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .scale(successCardScale),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    listOf(
                                        Color(0xFF024F55),
                                        Color(0xFF01656D),
                                        Color(0xFF0B8891),
                                    ),
                                ),
                            )
                            .padding(horizontal = 20.dp, vertical = 22.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .padding(12.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Top Up Successful",
                                color = Color.White,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Your extension and amount were applied.",
                                color = Color.White.copy(alpha = 0.88f),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            if (successDueDate.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "New due date: $successDueDate",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(
            value,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (highlight) androidx.compose.ui.text.font.FontWeight.ExtraBold else androidx.compose.ui.text.font.FontWeight.SemiBold,
        )
    }
}
