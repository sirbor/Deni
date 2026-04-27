package com.loki.deni.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.viewmodel.AccountDataViewModel

@Composable
fun ReferralScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    val isDark = isSystemInDarkTheme()
    LaunchedEffect(Unit) { viewModel.load() }
    val user by viewModel.user.collectAsStateWithLifecycle()
    val tx by viewModel.transactions.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val base = user?.id?.takeLast(6)?.uppercase().orEmpty().ifBlank { "USER00" }
    val referralCode = "DENI-$base"
    val referralCount = tx.count { it.type.equals("credit", true) }
    val referralEarnings = tx.filter { it.type.equals("debit", true) }.sumOf { (it.amount * 0.01).toInt() }
    val shareMessage = stringResource(R.string.share_code_message, referralCode)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = stringResource(R.string.refer_and_earn_title), showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.28f else 0.18f)),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .background(
                            Brush.linearGradient(
                                if (isDark) listOf(Color(0xFF8B6A17), Color(0xFFB98920)) else listOf(Color(0xFFDAA520), Color(0xFFF5A623)),
                            ),
                        )
                        .padding(16.dp),
                ) {
                    Column {
                        Text(stringResource(R.string.refer_and_earn_heading), color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text(stringResource(R.string.refer_and_earn_sub), color = Color.White.copy(alpha = 0.8f))
                    }
                }
            }
            Card(shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.24f else 0.16f))) {
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.referral_code), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    Text(referralCode, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DeniButton(
                            text = stringResource(R.string.copy),
                            onClick = {
                                val manager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                manager.setPrimaryClip(ClipData.newPlainText("referral", referralCode))
                            },
                            modifier = Modifier.weight(1f),
                        )
                        DeniButton(
                            text = stringResource(R.string.share),
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, shareMessage)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Stat(stringResource(R.string.referred), referralCount.toString(), Modifier.weight(1f))
                Stat(stringResource(R.string.earned), "KES $referralEarnings", Modifier.weight(1f))
                Stat(stringResource(R.string.pending), "1", Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Stat(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f)),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Text(value, fontWeight = FontWeight.Bold)
        }
    }
}
