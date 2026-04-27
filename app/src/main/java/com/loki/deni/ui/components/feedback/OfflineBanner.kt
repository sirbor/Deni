package com.loki.deni.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OfflineBanner(lastUpdatedText: String = "Last updated moments ago") {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF4D6))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(
            text = "Offline mode. Some actions are disabled. $lastUpdatedText",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF8A6200),
        )
    }
}

@Composable
fun OfflineBanner(isOnline: Boolean) {
    if (!isOnline) {
        OfflineBanner()
    }
}
