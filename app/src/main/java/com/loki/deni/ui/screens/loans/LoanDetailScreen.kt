package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.components.OutlineButton
import com.loki.deni.ui.components.RepaymentStatusChip
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.util.CurrencyFormatter
import com.loki.deni.ui.model.LoanStatus
import com.loki.deni.ui.viewmodel.LoanDetailViewModel

@Composable
fun LoanDetailScreen(navController: NavController, loanId: Int, viewModel: LoanDetailViewModel = hiltViewModel()) {
    val compact = LocalConfiguration.current.screenWidthDp < 360
    val isDark = isSystemInDarkTheme()
    val loan by viewModel.loan.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(loanId) { viewModel.loadLoan(loanId) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = stringResource(R.string.loan_details_title), showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = if (isDark) 0.14f else 0.08f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                if (isDark) {
                                    listOf(Color(0xFF0D2C30), Color(0xFF11383D), Color(0xFF0A2225))
                                } else {
                                    listOf(Color(0xFF015B61), Color(0xFF014D52), Color(0xFF012E31))
                                },
                            ),
                        )
                        .padding(16.dp),
                ) {
                    Column {
                        Text(loan?.title ?: stringResource(R.string.loan_fallback_title), color = Color.White.copy(alpha = 0.86f), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            CurrencyFormatter.formatKes((loan?.amount ?: 0).toDouble()),
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (compact) 28.sp else 34.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            stringResource(R.string.loan_disbursed_due, loan?.disbursedDate.orEmpty(), loan?.dueDate.orEmpty()),
                            color = Color.White.copy(alpha = 0.72f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        RepaymentStatusChip(
                            status = when (loan?.status) {
                                LoanStatus.PAID -> "Paid"
                                LoanStatus.OVERDUE -> "Overdue"
                                else -> "Active"
                            },
                            modifier = Modifier.padding(top = 10.dp),
                        )
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            StatPill("Repaid", CurrencyFormatter.formatKes((loan?.totalRepayment ?: 0.0) - (loan?.monthlyEmi ?: 0.0)))
                            StatPill("EMI", CurrencyFormatter.formatKes(loan?.monthlyEmi ?: 0.0))
                        }
                    }
                }
            }
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DetailRow("Principal", CurrencyFormatter.formatKes((loan?.amount ?: 0).toDouble()))
                    DetailRow("Total repayment", CurrencyFormatter.formatKes(loan?.totalRepayment ?: 0.0))
                    DetailRow("Monthly installment", CurrencyFormatter.formatKes(loan?.monthlyEmi ?: 0.0))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
                    DetailRow("Tenure", "${loan?.tenureMonths ?: 0} months")
                    DetailRow("Disbursed", loan?.disbursedDate.orEmpty())
                    DetailRow("Due date", loan?.dueDate.orEmpty())
                }
            }
            GradientPrimaryButton(
                text = stringResource(R.string.repay),
                onClick = { navController.navigate(DeniRoutes.RepayByLoan.createRoute(loanId)) },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineButton(
                    text = stringResource(R.string.schedule),
                    onClick = { navController.navigate(DeniRoutes.LoanSchedule.createRoute(loanId)) },
                    modifier = Modifier.weight(1f).height(52.dp),
                )
                OutlineButton(
                    text = stringResource(R.string.top_up),
                    onClick = { navController.navigate(DeniRoutes.LoanTopup.createRoute(loanId)) },
                    modifier = Modifier.weight(1f).height(52.dp),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
            ) {
                Text(
                    stringResource(R.string.early_repayment_tip),
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatPill(label: String, value: String) {
    Row(
        modifier = Modifier
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label, color = Color.White.copy(alpha = 0.8f), fontWeight = FontWeight.Bold, fontSize = 10.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
    }
}

