package com.loki.deni.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniButton
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.model.KenyaCounties
import com.loki.deni.ui.viewmodel.ProfileUiState
import com.loki.deni.ui.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch

@Composable
fun EditProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var county by remember { mutableStateOf("") }
    var employerName by remember { mutableStateOf("") }
    var monthlyIncome by remember { mutableStateOf("") }
    var countyExpanded by remember { mutableStateOf(false) }
    var countySearch by remember { mutableStateOf("") }
    var photoUri by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(uiState) {
        val state = uiState as? ProfileUiState.Success ?: return@LaunchedEffect
        if (name.isBlank()) name = state.profile.fullName
        if (email.isBlank()) email = state.profile.email
        if (county.isBlank()) county = state.profile.county
        if (employerName.isBlank()) employerName = state.profile.employerName
        if (monthlyIncome.isBlank() && state.profile.monthlyIncome > 0) monthlyIncome = state.profile.monthlyIncome.toString()
    }
    val filteredCounties = remember(countySearch) {
        if (countySearch.isBlank()) KenyaCounties.all
        else KenyaCounties.all.filter { it.contains(countySearch, ignoreCase = true) }
    }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        photoUri = uri?.toString()
    }

    Scaffold(
        topBar = { DeniTopBar(title = "Edit Profile", showBackArrow = true, onBack = { navController.navigateUp() }) },
        snackbarHost = { SnackbarHost(host) },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            DeniButton(
                text = if (photoUri == null) "Pick Photo" else "Change Photo",
                onClick = {
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
            )
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full name") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )
            Box {
                OutlinedTextField(
                    value = countySearch.ifBlank { county },
                    onValueChange = {
                        countySearch = it
                        countyExpanded = true
                    },
                    label = { Text("County") },
                    modifier = Modifier.fillMaxWidth(),
                )
                DropdownMenu(expanded = countyExpanded, onDismissRequest = { countyExpanded = false }) {
                    filteredCounties.forEach { item ->
                        DropdownMenuItem(
                            text = { Text(item) },
                            onClick = {
                                county = item
                                countySearch = item
                                countyExpanded = false
                            },
                        )
                    }
                }
            }
            OutlinedTextField(
                value = employerName,
                onValueChange = { employerName = it },
                label = { Text("Employer / business name") },
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = monthlyIncome,
                onValueChange = { monthlyIncome = it.filter(Char::isDigit).take(9) },
                label = { Text("Monthly income (KES)") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
            DeniButton(
                text = "Save",
                onClick = {
                    val validName = Regex("^[A-Za-z ]{2,60}$").matches(name.trim())
                    val validEmail = email.isBlank() || Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$").matches(email)
                    if (!validName || !validEmail || county.isBlank()) {
                        scope.launch { host.showSnackbar("Invalid profile details") }
                        return@DeniButton
                    }
                    scope.launch {
                        viewModel.updateProfile(
                            name = name.trim(),
                            photoUri = photoUri,
                            email = email.trim(),
                            county = county.trim(),
                            employerName = employerName.trim(),
                            monthlyIncome = monthlyIncome.toIntOrNull() ?: 0,
                        )
                        host.showSnackbar("Profile updated")
                        navController.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            )
        }
    }
}
