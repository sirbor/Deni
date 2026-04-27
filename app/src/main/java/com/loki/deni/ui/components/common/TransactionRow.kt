package com.loki.deni.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loki.deni.presentation.ui.theme.DeniError
import com.loki.deni.presentation.ui.theme.DeniSuccess

@Composable
fun TransactionRow(
    icon: ImageVector,
    name: String,
    date: String,
    amount: String,
    isCredit: Boolean,
    iconBg: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
    iconTint: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        modifier = Modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(40.dp).background(iconBg, androidx.compose.foundation.shape.RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint)
            }
            Column {
                Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(date, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), fontSize = 12.sp)
            }
        }
        Text(amount, color = if (isCredit) DeniSuccess else DeniError, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
