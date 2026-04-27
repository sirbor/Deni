package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.domain.LoanCalculator
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.util.CurrencyFormatter
import com.loki.deni.ui.viewmodel.HomeViewModel
import com.loki.deni.ui.viewmodel.SummaryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun SummaryScreen(
    navController: NavController,
    amount: Int,
    tenure: Int,
    summaryViewModel: SummaryViewModel = viewModel(),
    homeViewModel: HomeViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val accepted by summaryViewModel.acceptedTerms.collectAsStateWithLifecycle()
    val monthly = LoanCalculator.calculateEMI(amount.toDouble(), 0.15, tenure)
    val interest = (monthly * tenure) - amount
    val total = monthly * tenure
    val now = System.currentTimeMillis()
    val dueDate = now + tenure * 30L * 24L * 60L * 60L * 1000L
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Scaffold(
        topBar = { DeniTopBar(stringResource(R.string.loan_summary), onBackClick = { navController.popBackStack() }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SummaryRow(stringResource(R.string.loan_amount), CurrencyFormatter.formatKes(amount.toDouble()))
            SummaryRow(stringResource(R.string.interest_rate), "15% p.a.")
            SummaryRow(stringResource(R.string.tenure), "$tenure months")
            SummaryRow(stringResource(R.string.monthly_payment), CurrencyFormatter.formatKes(monthly))
            SummaryRow(stringResource(R.string.total_interest), CurrencyFormatter.formatKes(interest))
            SummaryRow(stringResource(R.string.total_repayment), CurrencyFormatter.formatKes(total))
            SummaryRow(stringResource(R.string.disbursement_date), dateFormat.format(Date(now)))
            SummaryRow(stringResource(R.string.due_date), dateFormat.format(Date(dueDate)))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = accepted, onCheckedChange = summaryViewModel::setAccepted)
                Text(stringResource(R.string.agree_terms))
            }
            DeniButton(
                text = stringResource(R.string.confirm_loan),
                enabled = accepted,
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        val reference = "DENI" + (1..8).map { Random.nextInt(0, 10) }.joinToString("")
                        val result = homeViewModel.applyLoan(amount.toDouble(), tenure, monthly, total, reference)
                        result.onSuccess {
                            navController.navigate(DeniRoutes.Success.createRoute(amount)) {
                                popUpTo(DeniRoutes.Apply.route) { inclusive = true }
                            }
                        }.onFailure {
                            snackbarHostState.showSnackbar(it.message ?: "Unable to apply for loan.")
                        }
                    }
                },
            )
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}
