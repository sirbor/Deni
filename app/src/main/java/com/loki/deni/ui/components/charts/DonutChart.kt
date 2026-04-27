package com.loki.deni.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class DonutSegment(val label: String, val pct: Float, val color: Color)

@Composable
fun DonutChart(
    segments: List<DonutSegment>,
    modifier: Modifier = Modifier,
    centerText: String? = null,
) {
    Box(modifier = modifier.size(68.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(68.dp)) {
            var startAngle = -90f
            segments.forEach { segment ->
                val sweep = (segment.pct.coerceAtLeast(0f) / 100f) * 360f
                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(12.dp.toPx(), cap = StrokeCap.Butt),
                )
                startAngle += sweep
            }
        }
        if (centerText != null) {
            Text(centerText, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
        }
    }
}
