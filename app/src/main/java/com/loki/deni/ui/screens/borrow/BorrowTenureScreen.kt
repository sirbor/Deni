package com.loki.deni.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton

@Composable
fun BorrowTenureScreen(navController: NavController, amount: Int) {
    val tenureOptions = remember(amount) {
        when {
            amount <= 4_000 -> listOf(14)
            amount in 4_099..11_999 -> listOf(14, 30)
            else -> listOf(14, 30, 45, 60)
        }
    }
    var tenureDays by remember(amount) { mutableIntStateOf(tenureOptions.firstOrNull() ?: 14) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = "Repayment Term", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf(14, 30, 45, 60).forEach { days ->
                val allowed = days in tenureOptions
                val interest = LoanCalculator.interestForDays(amount.toDouble(), days).toInt()
                val total = LoanCalculator.totalRepaymentForDays(amount.toDouble(), days).toInt()
                val selected = tenureDays == days
                Card(
                    modifier = Modifier.fillMaxWidth().clickable(enabled = allowed) { tenureDays = days },
                    border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            !allowed -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                            selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                    ),
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(
                            if (allowed) "$days days" else "$days days (not available for this amount)",
                            fontWeight = FontWeight.Bold,
                            color = if (allowed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        )
                        Text(
                            "Int KES %,d | Total KES %,d".format(interest, total),
                            color = if (allowed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        )
                    }
                }
            }
            GradientPrimaryButton(
                text = "Continue",
                onClick = { navController.navigate("borrow_summary/$amount/$tenureDays") },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
        }
    }
}
