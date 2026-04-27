package com.loki.deni.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val isCompactHeight = config.screenHeightDp < 740
    val isTallHeight = config.screenHeightDp >= 860
    val snackbarHostState = remember { SnackbarHostState() }
    var showContent by remember { mutableStateOf(false) }
    var showTrustBlocks by remember { mutableStateOf(false) }
    var showPrimaryAction by remember { mutableStateOf(false) }
    var showSupportingInfo by remember { mutableStateOf(false) }
    val heroFloatTransition = rememberInfiniteTransition(label = "welcome-hero-float")
    val heroFloatY by heroFloatTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "welcome-hero-float-y",
    )

    LaunchedEffect(Unit) {
        showContent = true
        delay(140)
        showTrustBlocks = true
        delay(120)
        showPrimaryAction = true
        delay(120)
        showSupportingInfo = true
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).background(AuthBackground).navigationBarsPadding().verticalScroll(rememberScrollState()),
        ) {
            val heroHeight = when {
                isCompactHeight -> 360.dp
                isTallHeight -> 430.dp
                else -> 392.dp
            }
            val brandSize = if (isCompactHeight) 34.sp else if (isTallHeight) 42.sp else 40.sp
            val taglineSize = if (isCompactHeight) 11.sp else 12.sp
            val valueSize = if (isCompactHeight) 22.sp else if (isTallHeight) 28.sp else 26.sp
            val topPadding = if (isCompactHeight) 10.dp else 16.dp
            val metricSpacer = if (isCompactHeight) 8.dp else 12.dp
            Box(modifier = Modifier.fillMaxWidth().height(heroHeight)) {
                AuthGradientHero(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = showContent,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 4 }),
                        modifier = Modifier.align(Alignment.Center),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .padding(top = topPadding)
                                .offset(y = heroFloatY.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(96.dp)
                                    .background(
                                        Brush.radialGradient(
                                            listOf(Color.White.copy(alpha = 0.35f), Color.Transparent),
                                        ),
                                        RoundedCornerShape(999.dp),
                                    ),
                            )
                            BrandIcon()
                            Spacer(Modifier.height(6.dp))
                            Text("deni", color = Color.White, fontSize = brandSize, letterSpacing = (-1.5).sp, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                stringResource(R.string.welcome_tagline),
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = taglineSize,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                lineHeight = 18.sp,
                                modifier = Modifier.fillMaxWidth(0.70f),
                            )
                            Spacer(Modifier.height(metricSpacer))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(horizontal = 16.dp)) {
                                HeroMetricPill(Icons.Outlined.Bolt, "2 min payout")
                                HeroMetricPill(Icons.AutoMirrored.Outlined.TrendingUp, "Up to KES 50K")
                            }
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 22.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.10f)),
                                        ),
                                        shape = RoundedCornerShape(22.dp),
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.22f), RoundedCornerShape(22.dp))
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column {
                                        Text("Available to borrow", color = Color.White.copy(alpha = 0.70f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Text("KES 50,000", color = Color.White, fontSize = valueSize, fontWeight = FontWeight.ExtraBold)
                                    }
                                    Text("98% on time", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            androidx.compose.animation.AnimatedVisibility(visible = showContent, enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn()) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(AuthBackground, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                        .padding(horizontal = 28.dp, vertical = 28.dp),
                ) {
                    Text(stringResource(R.string.welcome_title), fontSize = AuthHeroTitleSize, fontWeight = FontWeight.ExtraBold, color = AuthTextPrimary)
                    Spacer(Modifier.height(8.dp))
                    Text(stringResource(R.string.welcome_sub), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = AuthTextMuted, lineHeight = 20.sp)
                    Spacer(Modifier.height(16.dp))
                    AnimatedVisibility(
                        visible = showTrustBlocks,
                        enter = fadeIn(animationSpec = tween(260)) + slideInVertically(initialOffsetY = { it / 3 }),
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(8.dp, AuthRadiusMd, clip = false)
                                .background(
                                    brush = Brush.linearGradient(
                                        listOf(
                                            AuthPrimaryDeep.copy(alpha = 0.92f),
                                            AuthPrimary.copy(alpha = 0.88f),
                                        ),
                                    ),
                                    shape = AuthRadiusMd,
                                )
                                .border(1.dp, Color.White.copy(alpha = 0.18f), AuthRadiusMd)
                                .padding(14.dp),
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                AuthPill(
                                    text = "Why people trust deni",
                                    tint = Color.White,
                                )
                                FeatureRow(Icons.Outlined.Security, "Bank-grade security", "Data encrypted end to end")
                                FeatureRow(Icons.AutoMirrored.Outlined.TrendingUp, "Smarter limits", "Better terms as you repay on time")
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    AnimatedVisibility(
                        visible = showPrimaryAction,
                        enter = fadeIn(animationSpec = tween(340)) + slideInVertically(initialOffsetY = { it / 3 }),
                    ) {
                        Column {
                            PrimaryCta(
                                text = stringResource(R.string.welcome_cta_phone),
                                modifier = Modifier.fillMaxWidth(),
                                leading = { Icon(Icons.Outlined.PhoneAndroid, null, tint = Color.White, modifier = Modifier.size(18.dp)) },
                                onClick = { navController.navigate(Routes.AUTH_PHONE) },
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Phone number sign in only.",
                                fontSize = AuthCaptionSize,
                                fontWeight = FontWeight.SemiBold,
                                color = AuthTextFaint,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            )
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    AnimatedVisibility(
                        visible = showSupportingInfo,
                        enter = fadeIn(animationSpec = tween(380)),
                    ) {
                        TermsText(
                            onTerms = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://deni.app/terms"))) },
                            onPrivacy = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://deni.app/privacy"))) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroMetricPill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.97f else 1f, label = "hero-pill-scale")
    Box(
        modifier = Modifier
            .scale(scale)
            .background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = {},
            )
            .padding(horizontal = 9.dp, vertical = 6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.95f), modifier = Modifier.size(12.dp))
            Text(text, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color.White.copy(alpha = 0.92f))
        }
    }
}

@Composable
private fun FeatureRow(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, subtitle: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, null, tint = Color.White, modifier = Modifier.size(17.dp)) }
        Column {
            Text(title, fontSize = AuthBodySize, fontWeight = FontWeight.Bold, color = Color.White)
            Text(subtitle, fontSize = AuthCaptionSize, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.50f))
        }
    }
}
