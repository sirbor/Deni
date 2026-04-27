package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.viewmodel.LoanDetailViewModel

@Composable
fun RepayAmountScreen(
    navController: NavController,
    loanId: Int,
    viewModel: LoanDetailViewModel = hiltViewModel(),
) {
    val outstanding by viewModel.outstandingKes.collectAsStateWithLifecycle()
    var amount by remember { mutableStateOf("") }
    LaunchedEffect(loanId) { viewModel.loadLoan(loanId) }
    LaunchedEffect(outstanding) {
        if (outstanding > 0 && amount.isBlank()) {
            amount = outstanding.toString()
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 8.dp),
    ) {
        DeniTopBar(title = "Enter Amount", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter(Char::isDigit) },
                label = { Text("Amount") },
                supportingText = {
                    Text(
                        text = if (outstanding > 0) "Outstanding: KES %,d".format(outstanding) else "",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
            GradientPrimaryButton(
                text = "Continue",
                onClick = { navController.navigate("repay_confirm/$loanId/${amount.toIntOrNull() ?: 0}") },
                enabled = (amount.toIntOrNull() ?: 0) > 0,
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
        }
    }
}
