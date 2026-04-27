package com.loki.deni.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun AuthHeroBackground(
    modifier: Modifier = Modifier,
    showBottomCurve: Boolean = false,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF014D52), Color(0xFF012E31), Color(0xFF010F10)),
                ),
            ),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // dot grid overlay
            val spacing = 24.dp.toPx()
            var x = 0f
            while (x < size.width) {
                var y = 0f
                while (y < size.height) {
                    drawCircle(Color.White.copy(alpha = 0.055f), radius = 1.2.dp.toPx(), center = Offset(x, y))
                    y += spacing
                }
                x += spacing
            }
            // thin rings
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 230.dp.toPx(),
                center = Offset(size.width + 70.dp.toPx(), -30.dp.toPx()),
                style = Stroke(1.2.dp.toPx()),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 160.dp.toPx(),
                center = Offset(-40.dp.toPx(), size.height + 20.dp.toPx()),
                style = Stroke(1.2.dp.toPx()),
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.02f),
                radius = 40.dp.toPx(),
                center = Offset(size.width - 40.dp.toPx(), 30.dp.toPx()),
            )
        }
        if (showBottomCurve) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .align(androidx.compose.ui.Alignment.BottomCenter)
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
                    ),
            )
        }
        content()
    }
}
