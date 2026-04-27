package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.ProfileUiState
import com.loki.deni.ui.viewmodel.ProfileViewModel

@Composable
fun PersonalInfoScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    androidx.compose.foundation.layout.Column(modifier = Modifier.fillMaxSize()) {
        DeniTopBar(title = "Personal Information", showBackArrow = true, onBack = { navController.navigateUp() })
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
            ProfileHeroCard("Profile Identity", "Your verified account details from Deni records.")
            }
            when (val state = uiState) {
                ProfileUiState.Loading -> {
                    item { Text("Loading profile...", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)) }
                }
                is ProfileUiState.Error -> {
                    item { Text(state.message, color = MaterialTheme.colorScheme.error) }
                }
                is ProfileUiState.Success -> {
                    val rows = listOf(
                        InfoRowData("Name", state.profile.fullName, "Tap to edit", true),
                        InfoRowData("Phone", viewModel.maskPhone(state.profile.phone), "Protected - support ticket", false),
                        InfoRowData("National ID", if (state.profile.nationalId.isBlank()) "Not set" else viewModel.maskId(state.profile.nationalId), "Protected - support ticket", false),
                        InfoRowData("Date of birth", if (state.profile.dateOfBirth.isBlank()) "Not set" else state.profile.dateOfBirth, "Protected - support ticket", false),
                        InfoRowData("County", if (state.profile.county.isBlank()) "Not set" else state.profile.county, "Protected - support ticket", false),
                        InfoRowData("Monthly income", if (state.profile.monthlyIncome <= 0) "Not set" else viewModel.formatCurrency(state.profile.monthlyIncome), "Protected - support ticket", false),
                        InfoRowData("Salary range", if (state.profile.salaryRange.isBlank()) "Not set" else state.profile.salaryRange, "Protected - support ticket", false),
                        InfoRowData("Employer", if (state.profile.employerName.isBlank()) "Not set" else state.profile.employerName, "Protected - support ticket", false),
                        InfoRowData("Employment status", if (state.profile.employmentStatus.isBlank()) "Not set" else state.profile.employmentStatus.replace("_", " "), "Protected - support ticket", false),
                        InfoRowData("Email", if (state.profile.email.isBlank()) "Not set" else state.profile.email, "Tap to edit", true),
                        InfoRowData("Member since", state.profile.memberSince, "Read-only", false),
                    )
                    items(rows) { row ->
                        ProfileSurfaceCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    enabled = row.clickable,
                                    onClick = {
                                        if (row.editable) navController.navigate(Routes.EDIT_PROFILE) else navController.navigate("support_ticket")
                                    },
                                ),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                androidx.compose.foundation.layout.Column(modifier = Modifier.weight(1f)) {
                                    Text(row.label, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.66f))
                                    Text(row.value, fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(row.hint, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f))
                                }
                                if (row.clickable) {
                                    Text(">", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            item {
                ProfilePrimaryButton(
                text = "Edit profile",
                onClick = { navController.navigate(Routes.EDIT_PROFILE) },
                modifier = Modifier.fillMaxWidth(),
            )
            }
            item {
            ProfileSurfaceCard(modifier = Modifier.fillMaxWidth()) {
                Text("Need to change protected details? Submit a support ticket.", modifier = Modifier.padding(12.dp))
            }
            }
            item { Spacer(modifier = Modifier.height(6.dp)) }
        }
    }
}

private data class InfoRowData(
    val label: String,
    val value: String,
    val hint: String,
    val editable: Boolean,
) {
    val clickable: Boolean get() = editable || hint.contains("support", ignoreCase = true)
}
