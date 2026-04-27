package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.viewmodel.SupportViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportTicketScreen(
    navController: NavController,
    viewModel: SupportViewModel = hiltViewModel(),
) {
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val tickets by viewModel.tickets.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    val categories = listOf("Repayment", "Account", "Technical", "Other")
    var category by remember { mutableStateOf(categories.first()) }
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize()) {
        DeniTopBar(title = "Submit Ticket", showBackArrow = true, onBack = { navController.navigateUp() })
        SnackbarHost(hostState = host)
        LazyColumn(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ProfileHeroCard("Submit a Ticket", "Report issues and track resolution updates from support.")
            }
            item {
                ProfileSurfaceCard {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        TicketPill("FAST", "Triage", Modifier.weight(1f))
                        TicketPill("24/7", "Coverage", Modifier.weight(1f))
                        TicketPill("LIVE", "Tracking", Modifier.weight(1f))
                    }
                }
            }
            item {
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        readOnly = true,
                        value = category,
                        onValueChange = {},
                        label = { Text("Category") },
                        leadingIcon = { Icon(Icons.Outlined.Label, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    expanded = false
                                },
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Subject") },
                    leadingIcon = { Icon(Icons.Outlined.BugReport, contentDescription = null) },
                )
            }
            item {
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    label = { Text("Description") },
                    leadingIcon = { Icon(Icons.Outlined.Description, contentDescription = null) },
                )
            }
            item {
                GradientPrimaryButton(
                    text = "Submit Ticket",
                    onClick = {
                        val saved = viewModel.submitTicket(category, subject, description)
                        scope.launch { host.showSnackbar(if (saved) "Ticket submitted successfully" else "Please provide more details") }
                        if (saved) {
                            subject = ""
                            description = ""
                        }
                    },
                )
            }
            item {
                Text("Recent Tickets", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
            }
            if (tickets.isEmpty()) {
                item {
                    Text("No tickets yet. Submit one and it will appear here.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                }
            } else {
                items(tickets.take(6), key = { it.id }) { ticket ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    ) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${ticket.id} • ${ticket.category}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            Text(ticket.subject, fontWeight = FontWeight.SemiBold)
                            Text(ticket.description, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f))
                            Text(
                                "${ticket.status} • ${
                                    SimpleDateFormat("MMM d, HH:mm", Locale.ENGLISH).format(Date(ticket.createdAt))
                                }",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TicketPill(value: String, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(56.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f), RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
