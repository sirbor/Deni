package com.loki.deni.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Call
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniTopBar

@Composable
fun SupportScreen(navController: NavController) {
    val context = LocalContext.current
    val faqs = listOf(
        "How is my loan limit calculated?",
        "How do I repay early?",
        "What happens when a payment is overdue?",
        "How do I update my KYC details?",
    )
    Column(modifier = Modifier.fillMaxSize()) {
        DeniTopBar(title = "Support", showBackArrow = true, onBack = { navController.navigateUp() })
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ProfileHeroCard("Help Center", "Fast support for loans, repayments, profile access and account security.")
            }
            item {
                ProfileSurfaceCard {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SupportPill("FAQ", "Guides", Modifier.weight(1f))
                        SupportPill("24/7", "Assistance", Modifier.weight(1f))
                        SupportPill("FAST", "Response", Modifier.weight(1f))
                    }
                }
            }
            items(faqs) { q ->
                ProfileSurfaceCard {
                    androidx.compose.foundation.layout.Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                                .padding(8.dp),
                        ) {
                            Icon(Icons.Outlined.Quiz, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(q, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                SupportActionRow(
                    title = "Call Support",
                    subtitle = "Talk to a support specialist directly",
                    icon = Icons.Outlined.Call,
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:+254700000000"))
                        context.startActivity(intent)
                    },
                )
            }
            item {
                SupportActionRow(
                    title = "Email Support",
                    subtitle = "Send a detailed message to the support desk",
                    icon = Icons.Outlined.Mail,
                    onClick = {
                        val intent = Intent(Intent.ACTION_SENDTO).apply {
                            data = Uri.parse("mailto:support@deni.app")
                            putExtra(Intent.EXTRA_SUBJECT, "Support Request")
                        }
                        context.startActivity(intent)
                    },
                )
            }
            item {
                SupportActionRow(
                    title = "Start Live Chat",
                    subtitle = "Chat instantly with the support team",
                    icon = Icons.Outlined.Chat,
                    onClick = { navController.navigate("support_chat") },
                )
            }
            item {
                SupportActionRow(
                    title = "Submit Issue Ticket",
                    subtitle = "Track complex requests from open to resolution",
                    icon = Icons.Outlined.ReceiptLong,
                    onClick = { navController.navigate("support_ticket") },
                )
            }
        }
    }
}

@Composable
private fun SupportActionRow(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape)
                    .padding(8.dp),
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                )
            }
        }
        IconButton(onClick = onClick) {
            Icon(Icons.Outlined.ChevronRight, contentDescription = title, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SupportPill(value: String, label: String, modifier: Modifier = Modifier) {
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
