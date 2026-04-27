package com.loki.deni.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun DeniTextLogo(
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    size: TextUnit = 36.sp,
) {
    Text(
        modifier = modifier,
        text = buildAnnotatedString {
            append("deni")
            withStyle(SpanStyle(color = color.copy(alpha = 0.4f))) {
                append(".")
            }
        },
        color = color,
        fontSize = size,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = (-2).sp,
    )
}
