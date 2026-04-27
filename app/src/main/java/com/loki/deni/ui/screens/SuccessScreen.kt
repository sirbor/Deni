package com.loki.deni.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.util.CurrencyFormatter
import kotlin.random.Random
import kotlinx.coroutines.delay

@Composable
fun SuccessScreen(
    navController: NavController,
    amount: Int,
) {
    val scale = remember { Animatable(0.1f) }
    val reference = remember { "DENI${Random.nextInt(10000000, 99999999)}" }
    LaunchedEffect(Unit) {
        delay(300)
        scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.45f))
    }
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.primary)) {
        ConfettiLayer()
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.scale(scale.value),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(stringResource(R.string.loan_approved), color = MaterialTheme.colorScheme.onPrimary, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.sent_to_mpesa, CurrencyFormatter.formatKes(amount.toDouble())),
                color = MaterialTheme.colorScheme.onPrimary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = stringResource(R.string.reference_prefix, reference), color = MaterialTheme.colorScheme.onPrimary)
            Spacer(modifier = Modifier.height(28.dp))
            DeniButton(
                text = stringResource(R.string.back_home),
                onClick = {
                    navController.navigate(DeniRoutes.Home.route) {
                        popUpTo(DeniRoutes.Home.route) { inclusive = false }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun ConfettiLayer() {
    val transition = rememberInfiniteTransition(label = "confetti")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "progress",
    )
    val palette = listOf(
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.onPrimary,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val columns = 14
        for (i in 0 until columns) {
            val x = (size.width / columns) * i + 12f
            val y = ((progress.value * size.height) + (i * 70)) % size.height
            drawCircle(
                color = palette[i % palette.size],
                radius = 6f + (i % 3),
                center = Offset(x, y),
            )
        }
    }
}
