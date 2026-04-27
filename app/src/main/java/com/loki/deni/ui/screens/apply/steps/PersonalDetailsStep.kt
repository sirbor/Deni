package com.loki.deni.ui.screens.apply.steps

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.loki.deni.R
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTextField
import com.loki.deni.ui.model.KenyaCounties
import com.loki.deni.ui.viewmodel.ApplyViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun PersonalDetailsStep(
    viewModel: ApplyViewModel,
    onContinue: () -> Unit,
) {
    val personal by viewModel.personal.collectAsStateWithLifecycle()
    val errors by viewModel.personalErrors.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    var countySearch by remember { mutableStateOf("") }
    val counties = remember { KenyaCounties.all }
    val filteredCounties = remember(countySearch, counties) {
        if (countySearch.isBlank()) counties
        else counties.filter { it.contains(countySearch, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.apply_personal_heading), fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.apply_personal_subheading), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DeniTextField(
                value = personal.firstName,
                onValueChange = { viewModel.updatePersonal("firstName", it) },
                label = stringResource(R.string.apply_first_name),
                modifier = Modifier.weight(1f),
                isError = errors["firstName"] != null,
                errorText = errors["firstName"],
            )
            DeniTextField(
                value = personal.lastName,
                onValueChange = { viewModel.updatePersonal("lastName", it) },
                label = stringResource(R.string.apply_last_name),
                modifier = Modifier.weight(1f),
                isError = errors["lastName"] != null,
                errorText = errors["lastName"],
            )
        }
        OutlinedTextField(
            value = personal.dateOfBirth,
            onValueChange = { viewModel.updatePersonal("dateOfBirth", it) },
            label = { Text(stringResource(R.string.apply_dob)) },
            trailingIcon = { Icon(Icons.Default.CalendarToday, null) },
            isError = errors["dateOfBirth"] != null,
            modifier = Modifier.fillMaxWidth(),
            supportingText = { errors["dateOfBirth"]?.let { Text(it) } },
        )
        DeniTextField(
            value = personal.nationalId,
            onValueChange = { viewModel.updatePersonal("nationalId", it) },
            label = stringResource(R.string.apply_national_id),
            modifier = Modifier.fillMaxWidth(),
            isError = errors["nationalId"] != null,
            errorText = errors["nationalId"],
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )
        DeniTextField(
            value = personal.phone,
            onValueChange = { viewModel.updatePersonal("phone", it) },
            label = stringResource(R.string.apply_phone_number),
            modifier = Modifier.fillMaxWidth(),
            isError = errors["phone"] != null,
            errorText = errors["phone"],
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
        )
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            OutlinedTextField(
                value = countySearch.ifBlank { personal.county },
                onValueChange = {
                    countySearch = it
                    expanded = true
                },
                label = { Text(stringResource(R.string.apply_county)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                isError = errors["county"] != null,
                supportingText = { errors["county"]?.let { Text(it) } },
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                filteredCounties.forEach { county ->
                    DropdownMenuItem(
                        text = { Text(county) },
                        onClick = {
                            viewModel.updatePersonal("county", county)
                            countySearch = county
                            expanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        DeniButton(
            text = stringResource(R.string.apply_continue),
            onClick = onContinue,
            modifier = Modifier.fillMaxWidth().height(52.dp),
        )
    }
}
