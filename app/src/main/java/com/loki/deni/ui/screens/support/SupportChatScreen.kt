package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniOutlinedActionButton
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.viewmodel.SupportViewModel

@Composable
fun SupportChatScreen(
    navController: NavController,
    viewModel: SupportViewModel = hiltViewModel(),
) {
    var input by remember { mutableStateOf("") }
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        DeniTopBar(title = "Support Chat", showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileHeroCard("Live Support Chat", "Chat with support in real time and switch to tickets when needed.")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                DeniOutlinedActionButton(text = "Live Chat", onClick = {}, modifier = Modifier.weight(1f))
                DeniOutlinedActionButton(text = "Tickets", onClick = { navController.navigate("support_ticket") }, modifier = Modifier.weight(1f))
            }
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(messages) { message ->
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = if (message.fromUser) Alignment.CenterEnd else Alignment.CenterStart) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (message.fromUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (message.fromUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
                                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                                        CircleShape,
                                    )
                                    .padding(6.dp),
                            ) {
                                Icon(
                                    imageVector = if (message.fromUser) Icons.Outlined.Send else Icons.Outlined.Forum,
                                    contentDescription = null,
                                    tint = if (message.fromUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            Text(
                                text = message.text,
                                fontWeight = if (message.fromUser) FontWeight.SemiBold else FontWeight.Normal,
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(4.dp)) }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Type a message") },
                leadingIcon = { Icon(Icons.Outlined.Chat, contentDescription = null) },
                singleLine = true,
            )
            GradientPrimaryButton(
                text = "Send",
                onClick = {
                    viewModel.sendMessage(input)
                    input = ""
                },
                modifier = Modifier.weight(0.42f),
            )
        }
    }
}
