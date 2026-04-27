package com.loki.deni.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ChipType { DUE_SOON, OVERDUE }

@Composable
fun TimelineItem(
    date: String,
    title: String,
    id: String,
    amount: String,
    chipLabel: String?,
    chipType: ChipType?,
    dotColor: Color,
    isLast: Boolean,
    onClick: () -> Unit = {},
) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(34.dp)) {
            Box(modifier = Modifier.size(12.dp).background(dotColor, CircleShape))
            if (!isLast) {
                Box(modifier = Modifier.width(2.dp).height(54.dp).background(Color(0xFFE0DDD8)))
            }
        }
        Column(modifier = Modifier.weight(1f).padding(bottom = 14.dp)) {
            Text(date, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB0AFAC))
            Text(title, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A), modifier = Modifier.padding(top = 2.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(id, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF6B6A68))
                Text(amount, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF01696F))
            }
            if (chipLabel != null && chipType != null) {
                val bg = if (chipType == ChipType.DUE_SOON) Color(0xFFFFF8EC) else Color(0xFFFDEAEA)
                val fg = if (chipType == ChipType.DUE_SOON) Color(0xFFF5A623) else Color(0xFFB00020)
                Box(
                    modifier = Modifier
                        .padding(top = 5.dp)
                        .background(bg, RoundedCornerShape(999.dp))
                        .padding(horizontal = 9.dp, vertical = 3.dp),
                ) {
                    Text(chipLabel, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = fg)
                }
            }
        }
    }
}
