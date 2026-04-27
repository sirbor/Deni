package com.loki.deni.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.viewmodel.AccountDataViewModel

@Composable
fun PricingScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val user by viewModel.user.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val dynamicLimit = when (user?.creditScore ?: 0) {
        in 750..850 -> 50000
        in 600..749 -> 30000
        else -> 10000
    }
    val creditScore = user?.creditScore ?: 0
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = "Limits & Pricing", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileHeroCard("Your Limit Profile", "Credit score: $creditScore  •  Current limit: KES $dynamicLimit")
            ProfileSurfaceCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Interest rate", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("12%", fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Service fee", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Text("KES 0", fontWeight = FontWeight.Bold)
                    }
                }
            }
            ProfileSurfaceCard {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tier Limits", fontWeight = FontWeight.ExtraBold)
                    TierRow(label = "Bronze", limit = "KES 10,000")
                    TierRow(label = "Silver", limit = "KES 30,000")
                    TierRow(label = "Gold", limit = "KES 50,000")
                }
            }
            ProfileSurfaceCard(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text("How is my score calculated?", fontWeight = FontWeight.Bold)
                    }
                    if (expanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Repayment history, utilization, consistency, and account activity.")
                    }
                }
            }
            ProfilePrimaryButton(
                text = "View Loans",
                onClick = { navController.navigate(Routes.LOANS) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TierRow(label: String, limit: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val icon = when (label) {
                "Gold" -> Icons.Outlined.VerifiedUser
                "Silver" -> Icons.Outlined.TrendingUp
                else -> Icons.Outlined.Info
            }
            Box(modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), RoundedCornerShape(8.dp)).padding(4.dp)) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Text(label, fontWeight = FontWeight.Bold)
        }
        Text(limit, fontWeight = FontWeight.ExtraBold)
    }
}
