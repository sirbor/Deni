package com.loki.deni.ui.screens.apply.steps

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loki.deni.R
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.model.LoanPurpose
import com.loki.deni.ui.viewmodel.ApplyViewModel

@Composable
fun PurposeStep(
    viewModel: ApplyViewModel,
    onBack: () -> Unit,
    onCheckOffer: () -> Unit,
) {
    val selectedPurpose by viewModel.selectedPurpose.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val purposes = LoanPurpose.entries

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.apply_purpose_heading), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.apply_purpose_subheading), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            purposes.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    pair.forEach { purpose ->
                        PurposeChip(
                            purpose = purpose,
                            selected = selectedPurpose == purpose,
                            onClick = { viewModel.selectPurpose(purpose) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                    if (pair.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        OutlinedTextField(
            value = notes,
            onValueChange = viewModel::updateNotes,
            modifier = Modifier.fillMaxWidth().height(110.dp),
            label = { Text(stringResource(R.string.apply_notes_label)) },
            placeholder = { Text(stringResource(R.string.apply_notes_placeholder)) },
            maxLines = 3,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            androidx.compose.material3.OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(52.dp),
            ) { Text(stringResource(R.string.apply_back)) }
            DeniButton(
                text = stringResource(R.string.apply_check_offer),
                onClick = onCheckOffer,
                modifier = Modifier.weight(1f).height(52.dp),
            )
        }
    }
}

@Composable
private fun PurposeChip(
    purpose: LoanPurpose,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emoji = when (purpose) {
        LoanPurpose.PERSONAL -> "PER"
        LoanPurpose.BUSINESS -> "BIZ"
        LoanPurpose.EMERGENCY -> "URG"
    }
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(1.dp, if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.16f)),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(emoji)
            Text(
                purpose.name.replace("_", " "),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
