package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.ui.components.DeniInfoRow
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton

@Composable
fun BorrowSummaryScreen(navController: NavController, amount: Int, tenure: Int) {
    val processingFee = LoanCalculator.processingFeeForAmount(amount.toDouble()).toInt()
    val total = LoanCalculator.totalRepaymentForDays(amount.toDouble(), tenure).toInt()
    val interest = LoanCalculator.interestForDays(amount.toDouble(), tenure).toInt()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = "Loan Summary", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Summary", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    DeniInfoRow("Amount", "KES %,d".format(amount))
                    DeniInfoRow("Tenure", "$tenure days")
                    DeniInfoRow("Interest rate", String.format("%.2f%%", LoanCalculator.proratedRateForDays(tenure) * 100))
                    DeniInfoRow("Interest", "KES %,d".format(interest))
                    DeniInfoRow("Processing fee (3%)", "KES %,d".format(processingFee))
                    DeniInfoRow("Total Repayment", "KES %,d".format(total))
                }
            }
            GradientPrimaryButton(
                text = "Continue",
                onClick = { navController.navigate("borrow_review/$amount/$tenure") },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
        }
    }
}
