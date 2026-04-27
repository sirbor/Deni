package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.loki.deni.ui.components.DeniInfoRow
import com.loki.deni.ui.components.DeniTopBar

@Composable
fun HistoryDetailScreen(navController: NavController, loanId: Int) {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = "History Detail", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Loan #$loanId", fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleLarge)
                    DeniInfoRow("Status", "Paid")
                    DeniInfoRow("Amount", "KES 15,000")
                    DeniInfoRow("Tenure", "3 months")
                    DeniInfoRow("Closed Date", "Jun 2026")
                }
            }
        }
    }
}
