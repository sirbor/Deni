package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.components.OutlineButton
import com.loki.deni.ui.navigation.DeniRoutes

@Composable
fun LoanSuccessScreen(navController: NavController, loanId: Int) {
    val isDark = isSystemInDarkTheme()
    val heroRadius = RoundedCornerShape(22.dp)
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = heroRadius,
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.28f else 0.18f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            if (isDark) listOf(Color(0xFF0D2C30), Color(0xFF11545A)) else listOf(Color(0xFF014D52), Color(0xFF01696F)),
                        ),
                        heroRadius,
                    )
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(12.dp),
                    ) {
                        Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White)
                    }
                    Text("Loan Approved", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Your loan is being disbursed to M-Pesa.", color = Color.White.copy(alpha = 0.8f))
                    Text("Reference: LN-${loanId.toString().padStart(8, '0')}", color = Color.White.copy(alpha = 0.75f))
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            GradientPrimaryButton(
                text = "View Active Loan",
                onClick = { navController.navigate(DeniRoutes.Loans.route) },
                modifier = Modifier.weight(1f).height(54.dp),
            )
            OutlineButton(
                text = "Back Home",
                onClick = { navController.navigate("home") },
                modifier = Modifier.weight(1f).height(54.dp),
            )
        }
    }
}
