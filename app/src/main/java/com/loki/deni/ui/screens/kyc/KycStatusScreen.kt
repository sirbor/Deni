package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.components.DeniTopBar
import com.loki.deni.ui.viewmodel.AccountDataViewModel

@Composable
fun KycStatusScreen(
    navController: NavController,
    viewModel: AccountDataViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) { viewModel.load() }
    val user by viewModel.user.collectAsStateWithLifecycle()
    val profileChecklist = listOf(
        Triple("Profile name check", "Checks account profile identity fields", user?.name?.isNotBlank() == true),
        Triple("Phone number check", "Checks verified phone presence", user?.phone?.isNotBlank() == true),
        Triple("Email check", "Checks email captured", user?.email?.isNotBlank() == true),
        Triple("Date of birth check", "Checks DOB captured", user?.dateOfBirth?.isNotBlank() == true),
    )
    val docsChecklist = listOf(
        Triple("ID check", "Front and back ID document provided", user?.idFrontImageUri?.isNotBlank() == true && user?.idBackImageUri?.isNotBlank() == true),
        Triple("KRA PIN check", "KRA document provided", user?.kraPinImageUri?.isNotBlank() == true),
        Triple("Passport photo check", "Passport photo provided", user?.passportPhotoImageUri?.isNotBlank() == true),
    )
    val kinChecklist = listOf(
        Triple("Next of kin check", "Three next of kin contacts + relationships provided", !user?.nextOfKinOnePhone.isNullOrBlank() &&
            !user?.nextOfKinOneRelationship.isNullOrBlank() &&
            !user?.nextOfKinTwoPhone.isNullOrBlank() &&
            !user?.nextOfKinTwoRelationship.isNullOrBlank() &&
            !user?.nextOfKinThreePhone.isNullOrBlank() &&
            !user?.nextOfKinThreeRelationship.isNullOrBlank()),
    )
    val profileContextChecklist = listOf(
        Triple("Employer check", "Employment profile captured", !user?.employerName.isNullOrBlank()),
        Triple("Location check", "County and nearest landmark captured", !user?.county.isNullOrBlank() && !user?.nearestLandmark.isNullOrBlank()),
    )
    val permissionChecklist = listOf(
        Triple("Contacts permission", if (user?.contactsPermissionGranted == true) "Granted" else "Missing", user?.contactsPermissionGranted == true),
        Triple("SMS permission", if (user?.smsPermissionGranted == true) "Granted" else "Missing", user?.smsPermissionGranted == true),
    )
    val allItems = profileChecklist + docsChecklist + kinChecklist + profileContextChecklist + permissionChecklist
    val verifiedCount = allItems.count { it.third }
    val progress = if (allItems.isEmpty()) 0 else ((verifiedCount * 100f) / allItems.size).toInt()
    val role = user?.userRole?.trim()?.lowercase().orEmpty().ifBlank { "owner" }
    val isSupportOrAdmin = role == "support" || role == "admin"

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        DeniTopBar(title = stringResource(R.string.identity_verification_title), showBackArrow = true, onBack = { navController.navigateUp() })
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ProfileHeroCard("KYC Verification", "Expanded checklist of your verified details.")
            ProfileSurfaceCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Verification progress", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Text("$verifiedCount/${allItems.size} • $progress%", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold)
                }
            }
            if (isSupportOrAdmin) {
                KycSection(
                    "Support/Admin Diagnostics",
                    listOf(
                        Triple("User role", role.uppercase(), true),
                        Triple("Verification completion", "$progress% complete", progress >= 0),
                        Triple("Contacts captured", "${user?.contactsTotalCount ?: 0}", (user?.contactsTotalCount ?: 0) > 0),
                        Triple("Financial SMS count", "${user?.financialSmsCount ?: 0}", (user?.financialSmsCount ?: 0) > 0),
                        Triple("Financial credit count", "${user?.financialCreditCount ?: 0}", (user?.financialCreditCount ?: 0) >= 0),
                        Triple("Financial debit count", "${user?.financialDebitCount ?: 0}", (user?.financialDebitCount ?: 0) >= 0),
                    ),
                )
            } else {
                KycSection("Profile Details", profileChecklist)
                KycSection("Documents", docsChecklist)
                KycSection("Next of Kin", kinChecklist)
                KycSection("Context Checks", profileContextChecklist)
                KycSection("Permissions", permissionChecklist)
            }
        }
    }
}

@Composable
private fun KycSection(title: String, items: List<Triple<String, String, Boolean>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(title, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (item.third) Color(0x122E7D32) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        )
                        .border(
                            1.dp,
                            if (item.third) Color(0x332E7D32) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        )
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = if (item.third) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (item.third) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                        modifier = Modifier.size(18.dp),
                    )
                    Column {
                        Text(item.first, fontWeight = FontWeight.SemiBold)
                        Text(
                            item.second.ifBlank { "Missing" },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                        )
                    }
                }
            }
        }
    }
}
