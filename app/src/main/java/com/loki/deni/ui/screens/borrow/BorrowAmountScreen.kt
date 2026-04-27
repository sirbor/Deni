package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.components.DeniSectionHeader
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.components.GradientPrimaryButton
import androidx.compose.ui.res.stringResource
import com.loki.deni.ui.viewmodel.AccountDataViewModel

@Composable
fun BorrowAmountScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val maxLimit = stats.availableLimit.coerceAtLeast(500).toFloat()
    val minLimit = 500f.coerceAtMost(maxLimit)
    var amount by remember(minLimit, maxLimit) {
        mutableFloatStateOf(((maxLimit * 0.35f).coerceAtLeast(minLimit)).coerceAtMost(maxLimit))
    }
    val quarterPresets = remember(minLimit, maxLimit) {
        val q1 = (maxLimit * 0.25f).coerceAtLeast(minLimit)
        val q2 = (maxLimit * 0.50f).coerceAtLeast(minLimit)
        val q3 = (maxLimit * 0.75f).coerceAtLeast(minLimit)
        val q4 = maxLimit
        listOf(q1, q2, q3, q4).map { it.toInt() }.distinct()
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = stringResource(R.string.apply_for_loan), showBackArrow = true, onBack = { navController.navigateUp() })
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            DeniSectionHeader(title = stringResource(R.string.borrow_section_title))
            Card(shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Available limit: KES ${stats.availableLimit}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    )
                    Text(
                        "Eligible range: KES ${minLimit.toInt()} - KES ${maxLimit.toInt()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    )
                    Text("KES %,d".format(amount.toInt()), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    Slider(
                        value = amount,
                        onValueChange = { amount = it },
                        valueRange = minLimit..maxLimit,
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            activeTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                            inactiveTickColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        ),
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        quarterPresets.forEach { preset ->
                            val selected = amount.toInt() == preset
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        BorderStroke(
                                            1.dp,
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                                        ),
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                    )
                                    .clickable { amount = preset.toFloat() }
                                    .padding(vertical = 9.dp),
                                contentAlignment = androidx.compose.ui.Alignment.Center,
                            ) {
                                Text(
                                    text = "${(preset / 1000f).let { if (it % 1f == 0f) it.toInt().toString() else "%.1f".format(it) }}k",
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("KES ${minLimit.toInt()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text("KES ${maxLimit.toInt()}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                }
            }
            GradientPrimaryButton(
                text = stringResource(R.string.continue_borrow),
                onClick = { navController.navigate("borrow_tenure/${amount.toInt()}") },
                modifier = Modifier.fillMaxWidth().height(54.dp),
            )
        }
    }
}
