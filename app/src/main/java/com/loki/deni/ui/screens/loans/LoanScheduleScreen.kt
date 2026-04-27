package com.loki.deni.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.model.ScheduleItem
import com.loki.deni.ui.model.ScheduleStatus
import com.loki.deni.ui.viewmodel.LoanDetailViewModel

@Composable
fun LoanScheduleScreen(navController: NavController, loanId: Int, viewModel: LoanDetailViewModel = hiltViewModel()) {
    LaunchedEffect(loanId) {
        viewModel.loadLoan(loanId)
    }
    val schedule by viewModel.schedule.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            DeniTopBar(
                title = stringResource(R.string.repayment_schedule_title),
                showBackArrow = true,
                onBack = { navController.navigateUp() },
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Column {
                            Text("Repayment timeline", fontWeight = FontWeight.ExtraBold)
                            Text(
                                if (schedule.isEmpty()) "No installments available yet."
                                else "${schedule.count { it.status == ScheduleStatus.PAID }} of ${schedule.size} installments paid",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            }

            if (schedule.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(
                                Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                                modifier = Modifier.size(28.dp),
                            )
                            Text("Schedule is being prepared", fontWeight = FontWeight.Bold)
                            Text(
                                "Open loan details again after refresh to see your repayment breakdown.",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            )
                        }
                    }
                }
            } else {
                items(schedule) { item ->
                    ScheduleTimelineCard(item = item)
                }
            }
        }
    }
}

@Composable
private fun ScheduleTimelineCard(item: ScheduleItem) {
    val statusColor = when (item.status) {
        ScheduleStatus.PAID -> Color(0xFF2E7D32)
        ScheduleStatus.OVERDUE -> MaterialTheme.colorScheme.error
        ScheduleStatus.UPCOMING -> MaterialTheme.colorScheme.primary
    }
    val statusIcon = when (item.status) {
        ScheduleStatus.PAID -> Icons.Outlined.CheckCircle
        ScheduleStatus.OVERDUE -> Icons.Outlined.EventBusy
        ScheduleStatus.UPCOMING -> Icons.Outlined.Schedule
    }
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(statusColor.copy(alpha = 0.14f), CircleShape)
                        .border(1.dp, statusColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(item.installmentNumber.toString(), color = statusColor, fontWeight = FontWeight.Bold)
                }
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .width(2.dp)
                        .height(56.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(item.dueDate, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(16.dp))
                        Text(item.status.name, color = statusColor, fontWeight = FontWeight.SemiBold)
                    }
                }
                Text(
                    stringResource(R.string.loan_amount_kes, item.emiAmount.toInt()),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Principal KES ${item.principal.toInt()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    Text("Interest KES ${item.interest.toInt()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                }
                Text(
                    "Balance after payment: KES ${item.balance.toInt()}",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
        }
    }
}
