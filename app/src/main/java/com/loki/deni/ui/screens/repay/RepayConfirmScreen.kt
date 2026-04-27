package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniInfoRow
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.viewmodel.LoanDetailViewModel

@Composable
fun RepayConfirmScreen(
    navController: NavController,
    loanId: Int,
    amount: Int,
    viewModel: LoanDetailViewModel = hiltViewModel(),
) {
    val outstanding by viewModel.outstandingKes.collectAsStateWithLifecycle()
    LaunchedEffect(loanId) { viewModel.loadLoan(loanId) }
    val remaining = (outstanding - amount).coerceAtLeast(0)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp),
    ) {
        DeniTopBar(title = "Confirm Payment", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DeniInfoRow("Amount", "KES %,d".format(amount))
                    DeniInfoRow("Method", "M-Pesa")
                    DeniInfoRow("Loan Ref", "DN-2026-%06d".format(loanId.coerceAtLeast(0)))
                    DeniInfoRow("Remaining After", "KES %,d".format(remaining))
                }
            }
            Text("M-Pesa prompt will appear on your phone.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            GradientPrimaryButton(
                text = "Pay Now",
                onClick = { navController.navigate("repay_success/$loanId/manual-confirm") },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
        }
    }
}
