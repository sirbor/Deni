package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.viewmodel.HomeViewModel
import com.loki.deni.domain.LoanCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun BorrowReviewScreen(
    navController: NavController,
    amount: Int,
    tenure: Int,
    loanType: String,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val isDark = isSystemInDarkTheme()
    var accepted by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }
    val purposeOptions = remember {
        listOf("Personal", "Business", "Emergency")
    }
    fun normalizePurpose(raw: String): String {
        val normalized = raw.trim().lowercase()
        return when {
            normalized.contains("business") -> "Business"
            normalized.contains("emergency") -> "Emergency"
            else -> "Personal"
        }
    }
    var selectedPurpose by remember {
        mutableStateOf(
            normalizePurpose(
                loanType
                .replace("_", " ")
                .split(" ")
                .joinToString(" ") { it.lowercase().replaceFirstChar(Char::uppercaseChar) }
                .ifBlank { "Personal" },
            ),
        )
    }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val effectiveRate = LoanCalculator.proratedRateForDays(tenure)
    val processingFee = LoanCalculator.processingFeeForAmount(amount.toDouble()).toInt()
    val interest = LoanCalculator.interestForDays(amount.toDouble(), tenure).toInt()
    val totalRepayment = LoanCalculator.totalRepaymentForDays(amount.toDouble(), tenure).toInt()
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { DeniTopBar(title = "Review", showBackArrow = true, onBack = { navController.navigateUp() }) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                if (isDark) listOf(Color(0xFF0D2C30), Color(0xFF11545A)) else listOf(Color(0xFF014D52), Color(0xFF01696F)),
                            ),
                            RoundedCornerShape(20.dp),
                        )
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.18f), CircleShape)
                                .padding(10.dp),
                        ) {
                            Icon(Icons.Outlined.Description, contentDescription = null, tint = Color.White)
                        }
                        Column {
                            Text("Review your application", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.ExtraBold)
                            Text("Confirm details before disbursement", color = Color.White.copy(alpha = 0.78f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("Purpose of loan", fontWeight = FontWeight.ExtraBold)
                    }
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        purposeOptions.forEach { purpose ->
                            val selected = purpose.equals(selectedPurpose, ignoreCase = true)
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                        RoundedCornerShape(999.dp),
                                    )
                                    .border(
                                        1.dp,
                                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                                        RoundedCornerShape(999.dp),
                                    )
                                    .clickable { selectedPurpose = purpose }
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                            ) {
                                Text(
                                    purpose,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.82f),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SummaryRow("Requested amount", "KES %,d".format(amount))
                    SummaryRow("Purpose", selectedPurpose)
                    SummaryRow("Tenure", "$tenure days")
                    SummaryRow("Interest (${String.format("%.2f", effectiveRate * 100)}%)", "KES %,d".format(interest))
                    SummaryRow("Processing fee (3%)", "KES %,d".format(processingFee))
                    SummaryRow("Total repayable", "KES %,d".format(totalRepayment), highlight = true)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = { accepted = it })
                Text("I agree to Deni loan terms", modifier = Modifier.padding(top = 12.dp), fontWeight = FontWeight.SemiBold)
            }
            if (submitting) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        androidx.compose.foundation.layout.Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            CircularProgressIndicator(strokeWidth = 3.dp)
                            Column {
                                Text("Submitting your application", style = MaterialTheme.typography.titleSmall)
                                Text(
                                    "Please wait while we finalize your loan.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            GradientPrimaryButton(
                text = if (submitting) "Applying..." else "Confirm and Apply",
                enabled = accepted && !submitting,
                onClick = {
                    scope.launch {
                        submitting = true
                        val total = LoanCalculator.totalRepaymentForDays(amount.toDouble(), tenure)
                        val reference = "DENI" + (1..8).map { Random.nextInt(0, 10) }.joinToString("")
                        val result = homeViewModel.applyLoan(
                            amount = amount.toDouble(),
                            tenureDays = tenure,
                            effectiveRate = effectiveRate,
                            totalRepayment = total,
                            reference = reference,
                            purpose = selectedPurpose,
                        )
                        result.onSuccess {
                            delay(700)
                            navController.navigate(DeniRoutes.LoanSuccess.createRoute(1)) {
                                popUpTo("loans") { inclusive = false }
                            }
                        }.onFailure {
                            snackbarHostState.showSnackbar(it.message ?: "Unable to apply for loan.")
                        }
                        submitting = false
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String, highlight: Boolean = false) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
        Text(
            value,
            fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (highlight) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}
