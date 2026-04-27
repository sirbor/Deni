package com.loki.deni.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.CreditScore
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ScheduleSend
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.loki.deni.R
import com.loki.deni.presentation.ui.theme.DeniAccent
import com.loki.deni.presentation.ui.theme.DeniPrimary
import com.loki.deni.presentation.ui.theme.DeniSuccess
import com.loki.deni.ui.components.AuthHeroBackground
import com.loki.deni.ui.components.DeniTextLogo
import com.loki.deni.ui.components.WhitePrimaryButton
import com.loki.deni.ui.navigation.DeniRoutes
import com.loki.deni.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun OnboardingScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    var showPill1 by remember { mutableStateOf(false) }
    var showPill2 by remember { mutableStateOf(false) }
    var showPill3 by remember { mutableStateOf(false) }
    var showStats by remember { mutableStateOf(false) }
    var showScore by remember { mutableStateOf(false) }
    var showSteps by remember { mutableStateOf(false) }
    var showCta by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(300); showPill1 = true
        delay(300); showPill2 = true
        delay(300); showPill3 = true
        delay(200); showStats = true
        delay(400); showScore = true
        delay(400); showSteps = true
        delay(400); showCta = true
    }

    AuthHeroBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            DeniTextLogo(size = 34.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                stringResource(R.string.splash_tagline_modern),
                color = Color.White.copy(alpha = 0.45f),
                fontSize = 13.sp,
                letterSpacing = 0.4.sp,
            )
            Spacer(modifier = Modifier.height(16.dp))

            FeaturePill(
                visible = showPill1,
                icon = Icons.Outlined.ScheduleSend,
                title = stringResource(R.string.launch_pill_1_title),
                subtitle = stringResource(R.string.launch_pill_1_sub),
                badge = stringResource(R.string.launch_pill_1_badge),
                tint = DeniPrimary,
            )
            FeaturePill(
                visible = showPill2,
                icon = Icons.Outlined.CreditScore,
                title = stringResource(R.string.launch_pill_2_title),
                subtitle = stringResource(R.string.launch_pill_2_sub),
                badge = stringResource(R.string.launch_pill_2_badge),
                tint = DeniAccent,
            )
            FeaturePill(
                visible = showPill3,
                icon = Icons.Outlined.AccessTime,
                title = stringResource(R.string.launch_pill_3_title),
                subtitle = stringResource(R.string.launch_pill_3_sub),
                badge = stringResource(R.string.launch_pill_3_badge),
                tint = DeniSuccess,
            )

            AnimatedVisibility(visible = showStats, enter = fadeIn(tween(260)) + slideInVertically(animationSpec = tween(260), initialOffsetY = { 20 })) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StatGlassCard(stringResource(R.string.launch_stat_1_value), stringResource(R.string.launch_stat_1_title), stringResource(R.string.launch_stat_1_delta), Modifier.weight(1f))
                    StatGlassCard(stringResource(R.string.launch_stat_2_value), stringResource(R.string.launch_stat_2_title), stringResource(R.string.launch_stat_2_delta), Modifier.weight(1f))
                    StatGlassCard(stringResource(R.string.launch_stat_3_value), stringResource(R.string.launch_stat_3_title), stringResource(R.string.launch_stat_3_delta), Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(visible = showScore, enter = fadeIn(tween(260)) + slideInVertically(animationSpec = tween(260), initialOffsetY = { 20 })) {
                ScoreStripCard()
            }
            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(visible = showSteps, enter = fadeIn(tween(260)) + slideInVertically(animationSpec = tween(260), initialOffsetY = { 20 })) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    StepMiniCard("1", stringResource(R.string.launch_step_1), DeniPrimary, Modifier.weight(1f))
                    StepMiniCard("2", stringResource(R.string.launch_step_2), DeniAccent, Modifier.weight(1f))
                    StepMiniCard("3", stringResource(R.string.launch_step_3), DeniSuccess, Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            AnimatedVisibility(visible = showCta, enter = fadeIn(tween(260)) + slideInVertically(animationSpec = tween(260), initialOffsetY = { 20 })) {
                Column(
                    modifier = Modifier.fillMaxWidth().background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, Color(0xFF01282A)),
                            ),
                        )
                        .padding(bottom = 20.dp, top = 12.dp),
                ) {
                    WhitePrimaryButton(
                        text = stringResource(R.string.get_started),
                        onClick = {
                            authViewModel.setOnboardingSeen()
                            navController.navigate(DeniRoutes.AuthPhone.route) {
                                popUpTo(DeniRoutes.Onboarding.route) { inclusive = true }
                            }
                        },
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.launch_cta_hint),
                        color = Color.White.copy(alpha = 0.30f),
                        fontSize = 11.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturePill(
    visible: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    badge: String,
    tint: Color,
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(tween(260)) + slideInVertically(animationSpec = tween(260), initialOffsetY = { 20 })) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text(subtitle, color = Color.White.copy(alpha = 0.40f), fontSize = 11.sp)
                }
                Box(modifier = Modifier.clip(RoundedCornerShape(50)).background(Color.White.copy(alpha = 0.14f)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun StatGlassCard(value: String, title: String, delta: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Text(title, color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
            Text(delta, color = Color.White.copy(alpha = 0.70f), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ScoreStripCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(52.dp)) {
                Canvas(modifier = Modifier.size(52.dp)) {
                    drawArc(Color.White.copy(alpha = 0.10f), -90f, 360f, false, style = Stroke(5.dp.toPx()))
                    drawArc(
                        brush = Brush.linearGradient(listOf(DeniAccent, DeniSuccess)),
                        startAngle = -90f,
                        sweepAngle = 310f,
                        useCenter = false,
                        style = Stroke(5.dp.toPx(), cap = StrokeCap.Round),
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("732", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(stringResource(R.string.score_label_small), color = Color.White.copy(alpha = 0.45f), fontSize = 7.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.launch_score_title), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.launch_score_sub), color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(8.dp)).background(Color.White.copy(alpha = 0.12f))) {
                    Box(modifier = Modifier.fillMaxWidth(0.72f).height(5.dp).clip(RoundedCornerShape(8.dp)).background(DeniPrimary))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(stringResource(R.string.launch_limit_value), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.launch_limit_label), color = Color.White.copy(alpha = 0.45f), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun StepMiniCard(num: String, title: String, tint: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.10f)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(tint.copy(alpha = 0.22f)), contentAlignment = Alignment.Center) {
                Text(num, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(title, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}
