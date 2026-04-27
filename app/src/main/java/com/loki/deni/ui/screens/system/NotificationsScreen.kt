package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.SystemUpdate
import androidx.compose.material.icons.outlined.Work
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.model.DeniNotification
import com.loki.deni.ui.model.NotifType
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.NotificationsViewModel
import java.util.Calendar

@Composable
fun NotificationsScreen(
    navController: NavController,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadCount by viewModel.unreadCount.collectAsStateWithLifecycle()
    val tabs = listOf("All", "Loans", "Payments", "Offers", "System")

    Column(modifier = Modifier.fillMaxSize()) {
        DeniTopBar(title = "Notifications", showBackArrow = true, onBack = { navController.navigateUp() })
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
            items(tabs) { tab ->
                FilterChip(selected = tab == "All", onClick = {}, label = { Text(tab) })
            }
        }
        if (unreadCount > 0) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = viewModel::markAllAsRead) {
                    Text("Mark all read")
                }
            }
        }

        if (notifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Notifications, contentDescription = null, modifier = Modifier.size(34.dp))
                    Text("No notifications yet")
                }
            }
        } else {
            val grouped = remember(notifications) { notifications.groupBy { notificationSection(it.createdAt) } }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                grouped.forEach { (section, itemsList) ->
                    item {
                        Text(
                            section,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                    items(itemsList, key = { it.id }) { notification ->
                        NotificationRow(
                            notification = notification,
                            onTap = {
                                viewModel.markAsRead(notification.id)
                                notification.refId?.let { refId ->
                                    when (notification.type) {
                                        NotifType.APPROVAL, NotifType.REPAYMENT -> navController.navigate(Routes.loanDetail(refId))
                                        NotifType.REMINDER -> navController.navigate(Routes.repay(refId))
                                        NotifType.OFFER -> navController.navigate(Routes.LOANS)
                                        NotifType.SYSTEM -> Unit
                                    }
                                }
                            },
                            onDelete = { viewModel.deleteNotification(notification.id) },
                        )
                    }
                }
            }
        }
    }
}

private fun notificationSection(createdAt: Long): String {
    val now = Calendar.getInstance()
    val created = Calendar.getInstance().apply { timeInMillis = createdAt }
    val dayNow = now.get(Calendar.DAY_OF_YEAR)
    val yearNow = now.get(Calendar.YEAR)
    val dayCreated = created.get(Calendar.DAY_OF_YEAR)
    val yearCreated = created.get(Calendar.YEAR)
    return when {
        yearNow == yearCreated && dayNow == dayCreated -> "Today"
        yearNow == yearCreated && dayNow - dayCreated == 1 -> "Yesterday"
        else -> "Earlier"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationRow(
    notification: DeniNotification,
    onTap: () -> Unit,
    onDelete: () -> Unit,
) {
    SwipeToDismissBox(
        state = androidx.compose.material3.rememberSwipeToDismissBoxState(
            confirmValueChange = {
                if (it == SwipeToDismissBoxValue.EndToStart || it == SwipeToDismissBoxValue.StartToEnd) {
                    onDelete()
                }
                true
            },
        ),
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 18.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            }
        },
    ) {
        val iconBg = when (notification.type) {
            NotifType.APPROVAL -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            NotifType.REMINDER -> androidx.compose.ui.graphics.Color(0xFFFFF8EC)
            NotifType.OFFER -> androidx.compose.ui.graphics.Color(0xFFF0F8FF)
            NotifType.REPAYMENT -> androidx.compose.ui.graphics.Color(0xFFF0FFF0)
            NotifType.SYSTEM -> androidx.compose.ui.graphics.Color(0xFFF5F0FF)
        }
        val icon = when (notification.type) {
            NotifType.APPROVAL -> Icons.Outlined.Work
            NotifType.REMINDER -> Icons.Outlined.Notifications
            NotifType.OFFER -> Icons.Outlined.Campaign
            NotifType.REPAYMENT -> Icons.Outlined.Payments
            NotifType.SYSTEM -> Icons.Outlined.SystemUpdate
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
            shape = RoundedCornerShape(14.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (notification.isRead) 0.dp else 3.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            onClick = onTap,
        ) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(42.dp).background(iconBg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
                    Text(notification.title, fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold, fontSize = 14.sp)
                    Text(notification.body, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f), maxLines = 2)
                    Text(notification.timestamp, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                if (!notification.isRead) {
                    Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                }
            }
        }
    }
}
