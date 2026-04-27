package com.loki.deni.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loki.deni.ui.model.ScorePoint

@Composable
fun ScoreLineChart(data: List<ScorePoint>, modifier: Modifier = Modifier) {
    val reveal = remember { Animatable(0f) }
    val pulse = remember { Animatable(0.65f) }
    LaunchedEffect(Unit) {
        reveal.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(Unit) {
        while (true) {
            pulse.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
            pulse.animateTo(0.65f, tween(850, easing = FastOutSlowInEasing))
        }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(80.dp)) {
        if (data.size < 2) return@Canvas
        val maxScore = 740f
        val minScore = 680f
        val stepX = size.width / (data.size - 1)
        val points = data.mapIndexed { index, point ->
            val x = index * stepX * reveal.value
            val y = size.height - ((point.score - minScore) / (maxScore - minScore) * size.height)
            Offset(x, y)
        }

        val fillPath = Path().apply {
            moveTo(points.first().x, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, size.height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0x3801696F), Color.Transparent),
            ),
        )
        for (i in 0 until points.lastIndex) {
            drawLine(
                color = Color(0xFF01696F),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        points.forEachIndexed { idx, point ->
            drawCircle(
                color = Color(0x6601696F),
                radius = 3.dp.toPx(),
                center = point,
            )
            if (idx == points.lastIndex) {
                drawCircle(color = Color(0xFF01696F), radius = 5.dp.toPx(), center = point)
                drawCircle(color = Color(0x4D01696F), radius = 9.dp.toPx() * pulse.value, center = point)
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        data.forEach { point ->
            Text(
                text = point.month,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFB0AFAC),
            )
        }
    }
}
