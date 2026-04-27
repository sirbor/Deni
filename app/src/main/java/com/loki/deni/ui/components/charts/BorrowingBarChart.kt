package com.loki.deni.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loki.deni.ui.model.BarEntry

@Composable
fun BorrowingBarChart(data: List<BarEntry>, modifier: Modifier = Modifier) {
    val reveal = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        reveal.animateTo(1f, tween(700, easing = FastOutSlowInEasing))
    }
    val max = (data.maxOfOrNull { it.amount } ?: 1).toFloat()

    Column(modifier = modifier.fillMaxWidth().padding(top = 12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(72.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            data.forEach { entry ->
                val ratio = (entry.amount / max) * reveal.value
                val barHeight = (ratio * 72f).dp
                val brush = if (entry.isCurrent) {
                    Brush.verticalGradient(listOf(Color(0xFF01696F), Color(0xFF025E63)))
                } else {
                    Brush.verticalGradient(listOf(Color(0xFFE6F2F2), Color(0xFFE6F2F2)))
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(barHeight)
                        .background(brush, RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)),
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            data.forEach { entry ->
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = entry.month,
                        fontSize = 9.sp,
                        fontWeight = if (entry.isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                        color = if (entry.isCurrent) Color(0xFF01696F) else Color(0xFFB0AFAC),
                    )
                }
            }
        }
    }
}
