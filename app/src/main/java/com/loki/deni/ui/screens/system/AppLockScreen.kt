package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.loki.deni.R
import com.loki.deni.ui.model.AppLockSettings
import com.loki.deni.ui.components.DeniHeroBackground
import com.loki.deni.ui.components.GradientPrimaryButton
import com.loki.deni.ui.viewmodel.AppLockViewModel
import kotlinx.coroutines.launch

@Composable
fun AppLockScreen(
    onUnlock: () -> Unit,
    viewModel: AppLockViewModel = hiltViewModel(),
) {
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val config = AppLockSettings(pinEnabled = true, biometricsEnabled = false, timeoutMinutes = 5)
    val incorrectPinText = stringResource(R.string.app_lock_incorrect_pin)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniHeroBackground(height = 220.dp) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.size(50.dp).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                    androidx.compose.material3.Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
                Text(stringResource(R.string.app_lock_title), color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(top = 8.dp))
                Text(stringResource(R.string.app_lock_timeout, config.timeoutMinutes), color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.7f))
            }
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(stringResource(R.string.app_lock_enter_pin), fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.app_lock_pin_mask, "*".repeat(pin.length)), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f))
                if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
                NumericPad(
                    onDigit = { if (pin.length < 4) pin += it },
                    onBack = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                    onSubmit = {
                        scope.launch {
                            if (viewModel.verifyPin(pin)) onUnlock() else {
                                error = incorrectPinText
                                pin = ""
                            }
                        }
                    },
                )
                GradientPrimaryButton(text = stringResource(R.string.unlock), onClick = {
                    scope.launch {
                        if (viewModel.verifyPin(pin)) onUnlock() else {
                            error = incorrectPinText
                            pin = ""
                        }
                    }
                })
            }
        }
    }
}

@Composable
private fun NumericPad(onDigit: (String) -> Unit, onBack: () -> Unit, onSubmit: () -> Unit) {
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9")
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        keys.chunked(3).forEach { row ->
            androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                row.forEach { digit ->
                    androidx.compose.material3.OutlinedButton(onClick = { onDigit(digit) }) { Text(digit) }
                }
            }
        }
        androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 6.dp)) {
            androidx.compose.material3.OutlinedButton(onClick = onBack) { Text("<") }
            androidx.compose.material3.OutlinedButton(onClick = { onDigit("0") }) { Text("0") }
            androidx.compose.material3.OutlinedButton(onClick = onSubmit) { Text("OK") }
        }
    }
}
