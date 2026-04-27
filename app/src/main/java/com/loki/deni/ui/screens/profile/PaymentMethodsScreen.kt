package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.viewmodel.ProfileUiState
import com.loki.deni.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun PaymentMethodsScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    val profile = (uiState as? ProfileUiState.Success)?.profile
    Scaffold(
        topBar = { DeniTopBar(title = "Payment Methods", showBackArrow = true, onBack = { navController.navigateUp() }) },
        snackbarHost = { SnackbarHost(host) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileHeroCard("Primary Method", "Linked disbursement and repayment channel")
            ProfileSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("M-Pesa", fontWeight = FontWeight.ExtraBold)
                    Text("+254 ${viewModel.maskPhone(profile?.phone.orEmpty())}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f))
                }
            }
            ProfileSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Repayments and disbursements are tied to your verified phone number.",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                )
            }
            ProfileSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Settlement preference", fontWeight = FontWeight.Bold)
                    Text("Auto-route disbursement to registered M-Pesa wallet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
                }
            }
            ProfilePrimaryButton(
                text = "Add channel",
                onClick = { scope.launch { host.showSnackbar("New payment channels coming soon") } },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
