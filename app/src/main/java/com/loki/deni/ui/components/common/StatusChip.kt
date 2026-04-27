package com.loki.deni.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loki.deni.presentation.ui.theme.DeniBlue
import com.loki.deni.presentation.ui.theme.DeniError
import com.loki.deni.presentation.ui.theme.DeniSuccess
import com.loki.deni.ui.model.LoanStatus

@Composable
fun RepaymentStatusChip(
    status: String,
    modifier: Modifier = Modifier,
) {
    val normalized = status.trim().lowercase()
    val background = when (normalized) {
        "paid" -> DeniSuccess
        "overdue" -> DeniError
        "active" -> DeniBlue
        else -> MaterialTheme.colorScheme.primary
    }
    val stroke = background.copy(alpha = 0.25f)
    val text = when (normalized) {
        "paid" -> "Paid"
        "overdue" -> "Overdue"
        else -> "Active"
    }
    Box(
        modifier = modifier
            .background(background.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
            .border(1.dp, stroke, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = text,
            color = background,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 10.sp,
        )
    }
}

@Composable
fun RepaymentStatusChip(status: LoanStatus, modifier: Modifier = Modifier) {
    val label = when (status) {
        LoanStatus.PAID -> "Paid"
        LoanStatus.ACTIVE -> "Active"
        LoanStatus.OVERDUE -> "Overdue"
    }
    RepaymentStatusChip(status = label, modifier = modifier)
}

@Composable
fun StatusChip(status: String, modifier: Modifier = Modifier) {
    RepaymentStatusChip(status = status, modifier = modifier)
}

@Composable
fun StatusChip(status: LoanStatus, modifier: Modifier = Modifier) {
    RepaymentStatusChip(status = status, modifier = modifier)
}
