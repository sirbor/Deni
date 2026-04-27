package com.loki.deni.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun DeniHeroBackground(
    height: Dp,
    modifier: Modifier = Modifier,
    showBottomCurve: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF014D52), Color(0xFF012E31)),
                    start = Offset(0f, 0f),
                    end = Offset(1200f, 900f),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val spacing = 24.dp.toPx()
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(Color.White.copy(alpha = 0.05f), radius = 1.1.dp.toPx(), center = Offset(x, y))
                    y += spacing
                }
                x += spacing
            }
            drawCircle(
                color = Color.White.copy(alpha = 0.06f),
                radius = 180.dp.toPx(),
                center = Offset(size.width + 40.dp.toPx(), -30.dp.toPx()),
                style = Stroke(1.4.dp.toPx()),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 140.dp.toPx(),
                center = Offset(-30.dp.toPx(), size.height - 10.dp.toPx()),
                style = Stroke(1.2.dp.toPx()),
            )
        }
        if (showBottomCurve) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .background(
                        color = MaterialTheme.colorScheme.background,
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    ),
            )
        }
        content()
    }
}
