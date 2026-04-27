package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.model.LinkedAccount
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.viewmodel.AccountDataViewModel

@Composable
fun LinkedAccountsScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val user by viewModel.user.collectAsStateWithLifecycle()
    val phoneTail = user?.phone?.takeLast(3).orEmpty()
    val linkedAccounts = buildList {
        if (user?.phone != null) {
            add(
                LinkedAccount(
                    id = 1,
                    provider = "M-Pesa",
                    accountLabel = "Primary Wallet",
                    maskedNumber = "07** *** $phoneTail",
                    isPrimary = true,
                ),
            )
        }
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = stringResource(R.string.linked_accounts_title), showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            linkedAccounts.forEach { account ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(account.provider, fontWeight = FontWeight.Bold)
                        Text(account.accountLabel)
                        Text(account.maskedNumber, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        if (account.isPrimary) {
                            Text(stringResource(R.string.primary), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            DeniButton(text = stringResource(R.string.add_mpesa_number), onClick = {}, modifier = Modifier.fillMaxWidth())
        }
    }
}
