package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.model.LoginEvent
import com.loki.deni.ui.components.DeniOutlinedActionButton
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.viewmodel.AccountDataViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoginActivityScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val tx by viewModel.transactions.collectAsStateWithLifecycle()
    val loginActivity = tx.take(5).mapIndexed { index, t ->
        LoginEvent(
            device = "Android Device",
            location = "Kenya",
            time = SimpleDateFormat("MMM d, HH:mm", Locale.ENGLISH).format(Date(t.timestamp)),
            isCurrent = index == 0,
        )
    }
    Column(modifier = Modifier.fillMaxSize()) {
        DeniTopBar(title = stringResource(R.string.login_activity_title), showBackArrow = true, onBack = { navController.navigateUp() })
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(loginActivity) { item ->
                Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.device, fontWeight = FontWeight.Bold)
                        Text(item.location)
                        Text(item.time, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
                        if (item.isCurrent) {
                            Text(stringResource(R.string.current_session), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            item {
                DeniOutlinedActionButton(
                    text = stringResource(R.string.sign_out_other_devices),
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                )
            }
        }
    }
}
