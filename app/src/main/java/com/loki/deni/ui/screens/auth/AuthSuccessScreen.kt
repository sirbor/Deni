package com.loki.deni.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.loki.deni.R
import com.loki.deni.ui.navigation.Routes

@Composable
fun AuthSuccessScreen(navController: NavController) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow),
        label = "success-scale",
    )

    Column(modifier = Modifier.fillMaxSize().background(AuthBackground).navigationBarsPadding()) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            AuthGradientHero(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.size(100.dp).scale(scale).background(Color.White.copy(alpha = 0.10f), CircleShape)
                            .border(2.dp, Color.White.copy(alpha = 0.20f), CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(modifier = Modifier.size(70.dp).background(Color.White, CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.Check, null, tint = AuthPrimary, modifier = Modifier.size(34.dp))
                        }
                    }
                    Spacer(Modifier.size(20.dp))
                    AnimatedVisibility(visible = visible, enter = fadeIn()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                stringResource(R.string.success_title),
                                fontSize = 26.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                lineHeight = 31.sp,
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(
                                stringResource(R.string.success_sub),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White.copy(alpha = 0.60f),
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                            )
                        }
                    }
                }
            }
        }
        AnimatedVisibility(visible = visible, enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(AuthBackground, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(horizontal = 26.dp, vertical = 26.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Perk(stringResource(R.string.success_perk_1))
                    Perk(stringResource(R.string.success_perk_2))
                    Perk(stringResource(R.string.success_perk_3))
                }
                Spacer(Modifier.size(22.dp))
                PrimaryCta(
                    text = stringResource(R.string.success_cta),
                    trailingArrow = true,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun Perk(text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(8.dp).background(AuthPrimary, CircleShape))
        Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AuthTextMuted)
    }
}
