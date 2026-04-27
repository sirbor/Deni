package com.loki.deni.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val AuthPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary
val AuthPrimaryDark: Color
    @Composable get() = MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
val AuthPrimaryDeep: Color
    @Composable get() = Color(0xFF012E31)
val AuthBackground: Color
    @Composable get() = MaterialTheme.colorScheme.background
val AuthSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surface
val AuthTextPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
val AuthTextMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f)
val AuthTextFaint: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
val AuthBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
val AuthError: Color
    @Composable get() = MaterialTheme.colorScheme.error
val AuthErrorSurface: Color
    @Composable get() = MaterialTheme.colorScheme.error.copy(alpha = 0.12f)
val AuthRadiusSm: RoundedCornerShape
    @Composable get() = RoundedCornerShape(14.dp)
val AuthRadiusMd: RoundedCornerShape
    @Composable get() = RoundedCornerShape(16.dp)
val AuthRadiusLg: RoundedCornerShape
    @Composable get() = RoundedCornerShape(20.dp)
val AuthHeroTitleSize
    @Composable get() = 22.sp
val AuthSectionTitleSize
    @Composable get() = 14.sp
val AuthBodySize
    @Composable get() = 12.sp
val AuthCaptionSize
    @Composable get() = 11.sp
val AuthSpacingXs
    @Composable get() = 6.dp
val AuthSpacingSm
    @Composable get() = 10.dp
val AuthSpacingMd
    @Composable get() = 14.dp
val AuthSpacingLg
    @Composable get() = 18.dp

@Composable
fun AuthSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .background(AuthSurface, AuthRadiusLg)
            .border(1.dp, AuthBorder, AuthRadiusLg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(AuthSpacingSm),
        content = content,
    )
}

@Composable
fun AuthPill(
    text: String,
    modifier: Modifier = Modifier,
    tint: Color = AuthPrimary,
) {
    Box(
        modifier = modifier
            .background(tint.copy(alpha = 0.11f), RoundedCornerShape(999.dp))
            .border(1.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable
fun AuthGradientHero(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(
                colors = listOf(AuthPrimaryDeep, Color(0xFF014D52), AuthPrimary),
                start = Offset.Zero,
                end = Offset(1000f, 500f),
            ),
        ),
    ) {
        DotGrid(alpha = 0.05f)
        Ring(Modifier.align(Alignment.TopEnd).size(340.dp).offset(x = 80.dp, y = (-120).dp))
        Ring(Modifier.align(Alignment.BottomStart).size(200.dp).offset(x = (-60).dp, y = 60.dp))
        content()
    }
}

@Composable
private fun DotGrid(alpha: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val tile = 22.dp.toPx()
        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                drawCircle(Color.White.copy(alpha = alpha), radius = 1.2.dp.toPx(), center = Offset(x + tile / 2f, y + tile / 2f))
                x += tile
            }
            y += tile
        }
    }
}

@Composable
private fun Ring(modifier: Modifier) {
    Canvas(modifier = modifier) {
        drawCircle(Color.White.copy(alpha = 0.07f), style = Stroke(1.dp.toPx()))
    }
}

@Composable
fun PrimaryCta(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leading: (@Composable () -> Unit)? = null,
    trailingArrow: Boolean = false,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, disabledContainerColor = Color.Transparent),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Brush.linearGradient(listOf(AuthPrimary, AuthPrimaryDark)))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (leading != null) leading()
                Text(text, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                if (trailingArrow) Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
            }
        }
    }
}

@Composable
fun TermsText(onTerms: () -> Unit, onPrivacy: () -> Unit) {
    val text = buildAnnotatedString {
        append("By continuing you agree to our ")
        withLink(
            LinkAnnotation.Clickable(
                tag = "terms",
                styles = TextLinkStyles(style = SpanStyle(color = AuthPrimary, fontWeight = FontWeight.ExtraBold)),
                linkInteractionListener = { onTerms() },
            ),
        ) {
            append("Terms")
        }
        append(" and ")
        withLink(
            LinkAnnotation.Clickable(
                tag = "privacy",
                styles = TextLinkStyles(style = SpanStyle(color = AuthPrimary, fontWeight = FontWeight.ExtraBold)),
                linkInteractionListener = { onPrivacy() },
            ),
        ) {
            append("Privacy Policy")
        }
    }
    Text(
        text = text,
        style = TextStyle(
            color = AuthTextFaint,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
        ),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
fun BrandIcon(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(68.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color.White.copy(alpha = 0.12f))
            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(22.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("d", color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun AuthTabRow(
    selectedSignIn: Boolean,
    onSignIn: () -> Unit,
    onSignUp: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().background(AuthBorder, RoundedCornerShape(14.dp)).padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        TabChip("Sign In", selectedSignIn, onSignIn, Modifier.weight(1f))
        TabChip("Sign Up", !selectedSignIn, onSignUp, Modifier.weight(1f))
    }
}

@Composable
private fun TabChip(label: String, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color by animateColorAsState(if (active) AuthPrimary else AuthTextFaint, label = "tab-color")
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (pressed) 0.98f else 1f, label = "tab-chip-scale")
    Box(
        modifier = modifier
            .scale(scale)
            .height(40.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(if (active) Color.White else Color.Transparent)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = color)
    }
}
