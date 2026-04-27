package com.loki.deni.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTextField
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.viewmodel.AppLockViewModel
import kotlinx.coroutines.launch

@Composable
fun ChangePinScreen(
    navController: NavController,
    appLockViewModel: AppLockViewModel = hiltViewModel(),
) {
    var current by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val errCurrent = stringResource(R.string.current_pin_incorrect)
    val errInvalid = stringResource(R.string.new_pin_invalid)
    val errMismatch = stringResource(R.string.pin_mismatch)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = stringResource(R.string.change_pin), showBackArrow = true, onBack = { navController.navigateUp() })
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.change_pin_subtitle), fontWeight = FontWeight.Bold)
                DeniTextField(current, { current = it }, stringResource(R.string.current_pin), modifier = Modifier.fillMaxWidth())
                DeniTextField(newPin, { newPin = it }, stringResource(R.string.new_pin), modifier = Modifier.fillMaxWidth())
                DeniTextField(confirm, { confirm = it }, stringResource(R.string.confirm_pin), modifier = Modifier.fillMaxWidth())
                if (error.isNotEmpty()) Text(error, color = MaterialTheme.colorScheme.error)
                DeniButton(
                    text = stringResource(R.string.update_pin),
                    onClick = {
                        scope.launch {
                            error = when {
                                !appLockViewModel.verifyPin(current) -> errCurrent
                                newPin.length != 4 -> errInvalid
                                confirm != newPin -> errMismatch
                                else -> ""
                            }
                            if (error.isEmpty()) navController.navigateUp()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
