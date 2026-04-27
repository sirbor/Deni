package com.loki.deni.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SouthEast
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniBottomNav
import com.loki.deni.ui.components.RepaymentStatusChip
import com.loki.deni.ui.viewmodel.AccountDataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionsFeedScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val sorted = transactions.sortedByDescending { it.timestamp }
    val loanById = loans.associateBy { it.loanId }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = { DeniBottomNav(navController = navController) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF015B61), Color(0xFF014D52), Color(0xFF012E31)),
                            ),
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                ) {
                    Text("Transactions", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text("All transactions", color = Color.White.copy(alpha = 0.75f), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(10.dp))
            }

            if (sorted.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    ) {
                        Text(
                            "No transactions yet.",
                            modifier = Modifier.padding(14.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        )
                    }
                }
            } else {
                items(sorted) { tx ->
                    val isRepay = tx.type.equals("debit", true)
                    val icon = if (isRepay) Icons.Default.Check else Icons.Default.SouthEast
                    val tint = if (isRepay) Color(0xFF3F8C4A) else MaterialTheme.colorScheme.primary
                    val sign = if (isRepay) "+" else "-"
                    val statusLabel = tx.loanId?.let { id ->
                        loanById[id]?.let { loan ->
                            when {
                                loan.isPaid -> "Paid"
                                loan.dueDate < System.currentTimeMillis() -> "Overdue"
                                else -> "Active"
                            }
                        }
                    } ?: when {
                        tx.status.equals("paid", true) -> "Paid"
                        tx.status.equals("overdue", true) -> "Overdue"
                        else -> "Active"
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .clickable { navController.navigate("receipt/${tx.transId}") },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Card(
                                    shape = CircleShape,
                                    colors = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.15f)),
                                ) {
                                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.padding(9.dp))
                                }
                                Column {
                                    Text(
                                        tx.title.ifBlank { if (isRepay) "Repayment" else "Disbursement" },
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH).format(Date(tx.timestamp)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier.padding(start = 10.dp),
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Text(
                                    "$sign KES ${"%,d".format(tx.amount.toInt())}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isRepay) Color(0xFF3F8C4A) else MaterialTheme.colorScheme.error,
                                    fontSize = 13.sp,
                                )
                                RepaymentStatusChip(status = statusLabel)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(14.dp)) }
        }
    }
}
