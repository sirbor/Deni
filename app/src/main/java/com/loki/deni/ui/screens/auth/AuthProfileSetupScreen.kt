package com.loki.deni.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.provider.Telephony
import android.net.Uri
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Sms
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.compose.foundation.layout.ColumnScope
import com.loki.deni.ui.components.DeniTextField
import com.loki.deni.ui.model.KenyaCounties
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.AuthUiState
import com.loki.deni.ui.viewmodel.AuthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AuthProfileSetupScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val createState by viewModel.createState.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var dob by rememberSaveable { mutableStateOf("") }
    var nationalId by rememberSaveable { mutableStateOf("") }
    var county by rememberSaveable { mutableStateOf("") }
    var nearestLandmark by rememberSaveable { mutableStateOf("") }
    val salaryOptions = remember { listOf("0-25k", "25-50k", "50-100k", "above 100k") }
    val employerOptions = remember {
        listOf(
            "Teachers",
            "Police",
            "Military",
            "Prisons",
            "KWS",
            "KFS",
            "Universities",
            "Financial Institutions",
            "GOK - Ministries",
            "Parastatals",
            "Private Sector - Corporate",
            "SMES",
            "NGOs",
            "County Governments",
            "Enteprenuers",
        )
    }
    val educationOptions = remember {
        listOf("Primary", "Secondary", "Certificate", "Diploma", "Bachelor", "Masters", "PhD")
    }
    val maritalOptions = remember { listOf("Single", "Married", "Divorced", "Widowed") }
    val genderOptions = remember { listOf("Male", "Female") }
    val kinRelationshipOptions = remember { listOf("Spouse", "Parent", "Sibling", "Friend", "Guardian", "Colleague", "Other") }
    var salaryRange by rememberSaveable { mutableStateOf("") }
    var salaryExpanded by remember { mutableStateOf(false) }
    var employerCategory by rememberSaveable { mutableStateOf("") }
    var employerExpanded by remember { mutableStateOf(false) }
    var educationLevel by rememberSaveable { mutableStateOf("") }
    var educationExpanded by remember { mutableStateOf(false) }
    var maritalStatus by rememberSaveable { mutableStateOf("") }
    var maritalExpanded by remember { mutableStateOf(false) }
    var gender by rememberSaveable { mutableStateOf("") }
    var genderExpanded by remember { mutableStateOf(false) }
    var idFrontUri by rememberSaveable { mutableStateOf("") }
    var idBackUri by rememberSaveable { mutableStateOf("") }
    var kraPinUri by rememberSaveable { mutableStateOf("") }
    var passportPhotoUri by rememberSaveable { mutableStateOf("") }
    var kinOneName by rememberSaveable { mutableStateOf("") }
    var kinOnePhone by rememberSaveable { mutableStateOf("") }
    var kinOneRelationship by rememberSaveable { mutableStateOf("") }
    var kinOneRelationshipExpanded by remember { mutableStateOf(false) }
    var kinTwoName by rememberSaveable { mutableStateOf("") }
    var kinTwoPhone by rememberSaveable { mutableStateOf("") }
    var kinTwoRelationship by rememberSaveable { mutableStateOf("") }
    var kinTwoRelationshipExpanded by remember { mutableStateOf(false) }
    var kinThreeName by rememberSaveable { mutableStateOf("") }
    var kinThreePhone by rememberSaveable { mutableStateOf("") }
    var kinThreeRelationship by rememberSaveable { mutableStateOf("") }
    var kinThreeRelationshipExpanded by remember { mutableStateOf(false) }
    var showDobPicker by rememberSaveable { mutableStateOf(false) }
    var countyExpanded by remember { mutableStateOf(false) }
    var collectingSnapshots by rememberSaveable { mutableStateOf(false) }
    var submitError by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    var contactsPermissionGranted by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_CONTACTS,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    var smsPermissionGranted by rememberSaveable {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.READ_SMS,
            ) == PackageManager.PERMISSION_GRANTED,
        )
    }
    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        contactsPermissionGranted = granted
    }
    val smsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        smsPermissionGranted = granted
    }
    val idFrontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { persistReadPermission(context, it) }
        idFrontUri = uri?.toString().orEmpty()
    }
    val idBackLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { persistReadPermission(context, it) }
        idBackUri = uri?.toString().orEmpty()
    }
    val kraPinLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { persistReadPermission(context, it) }
        kraPinUri = uri?.toString().orEmpty()
    }
    val passportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let { persistReadPermission(context, it) }
        passportPhotoUri = uri?.toString().orEmpty()
    }
    val kinOneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        resolveContact(context, uri)?.let {
            kinOneName = it.first
            kinOnePhone = it.second
        }
    }
    val kinTwoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        resolveContact(context, uri)?.let {
            kinTwoName = it.first
            kinTwoPhone = it.second
        }
    }
    val kinThreeLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickContact()) { uri ->
        resolveContact(context, uri)?.let {
            kinThreeName = it.first
            kinThreePhone = it.second
        }
    }

    LaunchedEffect(createState) {
        when (createState) {
            is AuthUiState.Success -> {
                collectingSnapshots = false
                submitError = ""
                viewModel.resetCreateState()
                navController.navigate(Routes.HOME) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
            }
            is AuthUiState.Error -> {
                collectingSnapshots = false
                val message = (createState as AuthUiState.Error).message
                submitError = message
                host.showSnackbar(message)
                viewModel.resetCreateState()
            }
            else -> Unit
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(host) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(AuthBackground)
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState()),
        ) {
            AuthGradientHero(modifier = Modifier.fillMaxWidth().height(210.dp)) {
                Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 28.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Complete Your Profile", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Tell us a bit about you so we can assign your first limit instantly.", color = Color.White.copy(alpha = 0.78f))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, AuthRadiusMd, clip = false)
                            .background(Color.White.copy(alpha = 0.12f), AuthRadiusMd)
                            .border(1.dp, Color.White.copy(alpha = 0.18f), AuthRadiusMd)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("Profile quality", color = Color.White.copy(alpha = 0.70f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Verified-ready setup", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            AuthPill(text = "Priority scoring", tint = Color.White)
                        }
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AuthBackground, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(AuthSpacingMd),
            ) {
                SectionCard("Personal Details") {
                    RowTwo(
                        left = { DeniTextField(firstName, { firstName = it }, "First name", modifier = Modifier.weight(1f)) },
                        right = { DeniTextField(lastName, { lastName = it }, "Last name", modifier = Modifier.weight(1f)) },
                    )
                    DeniTextField(
                        email,
                        { email = it.trim() },
                        "Email address",
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDobPicker = true },
                        label = { Text("Date of birth") },
                        placeholder = { Text("Select from calendar") },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AuthPrimary,
                            unfocusedBorderColor = AuthBorder,
                            focusedContainerColor = AuthSurface,
                            unfocusedContainerColor = AuthSurface,
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = AuthTextMuted,
                                modifier = Modifier
                                    .size(20.dp)
                                    .clickable { showDobPicker = true },
                            )
                        },
                    )
                    DeniTextField(
                        nationalId,
                        { nationalId = it.filter(Char::isDigit).take(8) },
                        "National ID",
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Box {
                        PickerField(
                            value = county,
                            label = "County",
                            placeholder = "Select county",
                            leadingIcon = Icons.Outlined.LocationOn,
                            onClick = { countyExpanded = true },
                        )
                        DropdownMenu(
                            expanded = countyExpanded,
                            onDismissRequest = { countyExpanded = false },
                        ) {
                            KenyaCounties.all.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item) },
                                    onClick = {
                                        county = item
                                        countyExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    DeniTextField(
                        nearestLandmark,
                        { nearestLandmark = it.take(80) },
                        "Nearest landmark",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                SectionCard("Employment Details") {
                    Box {
                        PickerField(
                            value = salaryRange,
                            label = "Monthly salary range",
                            placeholder = "Choose salary range",
                            leadingIcon = Icons.Outlined.Payments,
                            onClick = { salaryExpanded = true },
                        )
                        DropdownMenu(
                            expanded = salaryExpanded,
                            onDismissRequest = { salaryExpanded = false },
                        ) {
                            salaryOptions.forEach { range ->
                                DropdownMenuItem(
                                    text = { Text(range) },
                                    onClick = {
                                        salaryRange = range
                                        salaryExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Box {
                        PickerField(
                            value = employerCategory,
                            label = "Employment category",
                            placeholder = "Select category",
                            leadingIcon = Icons.Outlined.WorkOutline,
                            onClick = { employerExpanded = true },
                        )
                        DropdownMenu(
                            expanded = employerExpanded,
                            onDismissRequest = { employerExpanded = false },
                        ) {
                            employerOptions.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        employerCategory = category
                                        employerExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Box {
                        PickerField(
                            value = educationLevel,
                            label = "Education level",
                            placeholder = "Select education",
                            leadingIcon = Icons.Outlined.AccountBalance,
                            onClick = { educationExpanded = true },
                        )
                        DropdownMenu(
                            expanded = educationExpanded,
                            onDismissRequest = { educationExpanded = false },
                        ) {
                            educationOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        educationLevel = option
                                        educationExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Box {
                        PickerField(
                            value = maritalStatus,
                            label = "Marital status",
                            placeholder = "Select marital status",
                            leadingIcon = Icons.Outlined.AccountBalance,
                            onClick = { maritalExpanded = true },
                        )
                        DropdownMenu(
                            expanded = maritalExpanded,
                            onDismissRequest = { maritalExpanded = false },
                        ) {
                            maritalOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        maritalStatus = option
                                        maritalExpanded = false
                                    },
                                )
                            }
                        }
                    }
                    Box {
                        PickerField(
                            value = gender,
                            label = "Gender",
                            placeholder = "Select gender",
                            leadingIcon = Icons.Outlined.AccountBalance,
                            onClick = { genderExpanded = true },
                        )
                        DropdownMenu(
                            expanded = genderExpanded,
                            onDismissRequest = { genderExpanded = false },
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        gender = option
                                        genderExpanded = false
                                    },
                                )
                            }
                        }
                    }
                }
                SectionCard("KYC Document Uploads (JPG)") {
                    UploadActionButton(
                        text = if (idFrontUri.isBlank()) "Upload front ID (JPG)" else "Front ID selected",
                        selected = idFrontUri.isNotBlank(),
                        onClick = { idFrontLauncher.launch(arrayOf("image/jpeg", "image/jpg", "image/*")) },
                    )
                    UploadActionButton(
                        text = if (idBackUri.isBlank()) "Upload back ID (JPG)" else "Back ID selected",
                        selected = idBackUri.isNotBlank(),
                        onClick = { idBackLauncher.launch(arrayOf("image/jpeg", "image/jpg", "image/*")) },
                    )
                    UploadActionButton(
                        text = if (kraPinUri.isBlank()) "Upload KRA PIN (JPG)" else "KRA PIN selected",
                        selected = kraPinUri.isNotBlank(),
                        onClick = { kraPinLauncher.launch(arrayOf("image/jpeg", "image/jpg", "image/*")) },
                    )
                    UploadActionButton(
                        text = if (passportPhotoUri.isBlank()) "Upload passport photo (JPG)" else "Passport photo selected",
                        selected = passportPhotoUri.isNotBlank(),
                        onClick = { passportLauncher.launch(arrayOf("image/jpeg", "image/jpg", "image/*")) },
                    )
                }
                SectionCard("Next of Kin") {
                    UploadActionButton(
                        text = if (kinOnePhone.isBlank()) "Pick next of kin 1" else "$kinOneName - $kinOnePhone",
                        selected = kinOnePhone.isNotBlank(),
                        onClick = {
                            if (!contactsPermissionGranted) contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            else kinOneLauncher.launch(null)
                        },
                    )
                    Box {
                        PickerField(
                            value = kinOneRelationship,
                            label = "Relationship (Kin 1)",
                            placeholder = "Select relationship",
                            leadingIcon = Icons.Outlined.AccountBalance,
                            onClick = { kinOneRelationshipExpanded = true },
                        )
                        DropdownMenu(expanded = kinOneRelationshipExpanded, onDismissRequest = { kinOneRelationshipExpanded = false }) {
                            kinRelationshipOptions.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                    kinOneRelationship = option
                                    kinOneRelationshipExpanded = false
                                })
                            }
                        }
                    }
                    UploadActionButton(
                        text = if (kinTwoPhone.isBlank()) "Pick next of kin 2" else "$kinTwoName - $kinTwoPhone",
                        selected = kinTwoPhone.isNotBlank(),
                        onClick = {
                            if (!contactsPermissionGranted) contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            else kinTwoLauncher.launch(null)
                        },
                    )
                    Box {
                        PickerField(
                            value = kinTwoRelationship,
                            label = "Relationship (Kin 2)",
                            placeholder = "Select relationship",
                            leadingIcon = Icons.Outlined.AccountBalance,
                            onClick = { kinTwoRelationshipExpanded = true },
                        )
                        DropdownMenu(expanded = kinTwoRelationshipExpanded, onDismissRequest = { kinTwoRelationshipExpanded = false }) {
                            kinRelationshipOptions.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                    kinTwoRelationship = option
                                    kinTwoRelationshipExpanded = false
                                })
                            }
                        }
                    }
                    UploadActionButton(
                        text = if (kinThreePhone.isBlank()) "Pick next of kin 3" else "$kinThreeName - $kinThreePhone",
                        selected = kinThreePhone.isNotBlank(),
                        onClick = {
                            if (!contactsPermissionGranted) contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            else kinThreeLauncher.launch(null)
                        },
                    )
                    Box {
                        PickerField(
                            value = kinThreeRelationship,
                            label = "Relationship (Kin 3)",
                            placeholder = "Select relationship",
                            leadingIcon = Icons.Outlined.AccountBalance,
                            onClick = { kinThreeRelationshipExpanded = true },
                        )
                        DropdownMenu(expanded = kinThreeRelationshipExpanded, onDismissRequest = { kinThreeRelationshipExpanded = false }) {
                            kinRelationshipOptions.forEach { option ->
                                DropdownMenuItem(text = { Text(option) }, onClick = {
                                    kinThreeRelationship = option
                                    kinThreeRelationshipExpanded = false
                                })
                            }
                        }
                    }
                }
                SectionCard("Required Data Access") {
                    Text(
                        "Grant access so we can scan contacts and financial SMS, then save risk signals to your profile.",
                        color = AuthTextMuted,
                        fontSize = 12.sp,
                    )
                    OutlinedButton(
                        onClick = { contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (contactsPermissionGranted) "Contacts access granted" else "Grant contacts access",
                            color = if (contactsPermissionGranted) AuthTextPrimary else AuthTextMuted,
                        )
                    }
                    OutlinedButton(
                        onClick = { smsPermissionLauncher.launch(Manifest.permission.READ_SMS) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            if (smsPermissionGranted) "SMS access granted - ready to scan" else "Grant SMS access",
                            color = if (smsPermissionGranted) AuthTextPrimary else AuthTextMuted,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        PermissionPill(
                            label = if (contactsPermissionGranted) "Contacts ready" else "Contacts pending",
                            granted = contactsPermissionGranted,
                            icon = Icons.Outlined.Phone,
                        )
                        PermissionPill(
                            label = if (smsPermissionGranted) "SMS ready" else "SMS pending",
                            granted = smsPermissionGranted,
                            icon = Icons.Outlined.Sms,
                        )
                    }
                }
                PrimaryCta(
                    text = if (createState is AuthUiState.Loading || collectingSnapshots) "Completing..." else "Complete Account",
                    trailingArrow = true,
                    enabled = createState !is AuthUiState.Loading && !collectingSnapshots,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    onClick = {
                        submitError = ""
                        if (firstName.isBlank() || lastName.isBlank() || email.isBlank() || dob.isBlank() || nationalId.length != 8 || county.isBlank() || nearestLandmark.isBlank() || salaryRange.isBlank() || employerCategory.isBlank() || educationLevel.isBlank() || maritalStatus.isBlank() || gender.isBlank()) {
                            scope.launch { host.showSnackbar("Please complete all required fields.") }
                            return@PrimaryCta
                        }
                        if (!contactsPermissionGranted) {
                            contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                            scope.launch { host.showSnackbar("Grant contacts access to continue.") }
                            return@PrimaryCta
                        }
                        if (!smsPermissionGranted) {
                            smsPermissionLauncher.launch(Manifest.permission.READ_SMS)
                            scope.launch { host.showSnackbar("Grant SMS access so we can scan financial transactions.") }
                            return@PrimaryCta
                        }
                        if (idFrontUri.isBlank() || idBackUri.isBlank() || kraPinUri.isBlank() || passportPhotoUri.isBlank()) {
                            scope.launch { host.showSnackbar("Please upload all required JPG documents.") }
                            return@PrimaryCta
                        }
                        if (kinOnePhone.isBlank() || kinTwoPhone.isBlank() || kinThreePhone.isBlank()) {
                            scope.launch { host.showSnackbar("Please pick all three next of kin contacts.") }
                            return@PrimaryCta
                        }
                        if (kinOneRelationship.isBlank() || kinTwoRelationship.isBlank() || kinThreeRelationship.isBlank()) {
                            scope.launch { host.showSnackbar("Please select relationship for each next of kin.") }
                            return@PrimaryCta
                        }
                        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                            scope.launch { host.showSnackbar("Please enter a valid email address.") }
                            return@PrimaryCta
                        }
                        scope.launch {
                            collectingSnapshots = true
                            try {
                                val contactsSnapshot = withTimeoutOrNull(2_000) {
                                    if (contactsPermissionGranted) withContext(Dispatchers.IO) { readContactsSnapshot(context) } else ""
                                } ?: ""
                                val contactsEntriesJson = withTimeoutOrNull(12_000) {
                                    if (contactsPermissionGranted) withContext(Dispatchers.IO) { readContactsEntriesJson(context) } else ""
                                } ?: ""
                                val smsEntriesJson = withTimeoutOrNull(15_000) {
                                    if (smsPermissionGranted) withContext(Dispatchers.IO) { readAllSmsEntriesJson(context) } else ""
                                } ?: ""
                                val smsSnapshot = withTimeoutOrNull(3_000) {
                                    if (smsPermissionGranted) withContext(Dispatchers.IO) { readFinancialSmsSnapshot(context) } else ""
                                } ?: ""
                                viewModel.completeSignupProfile(
                                    firstName = firstName.trim(),
                                    lastName = lastName.trim(),
                                    email = email.trim(),
                                    dob = dob.trim(),
                                    nationalId = nationalId.trim(),
                                    county = county.trim(),
                                    nearestLandmark = nearestLandmark.trim(),
                                    salaryRange = salaryRange,
                                    employer = employerCategory,
                                    educationLevel = educationLevel,
                                    maritalStatus = maritalStatus,
                                    gender = gender,
                                    idFrontImageUri = idFrontUri,
                                    idBackImageUri = idBackUri,
                                    kraPinImageUri = kraPinUri,
                                    passportPhotoImageUri = passportPhotoUri,
                                    nextOfKinOneName = kinOneName,
                                    nextOfKinOnePhone = kinOnePhone,
                                    nextOfKinOneRelationship = kinOneRelationship,
                                    nextOfKinTwoName = kinTwoName,
                                    nextOfKinTwoPhone = kinTwoPhone,
                                    nextOfKinTwoRelationship = kinTwoRelationship,
                                    nextOfKinThreeName = kinThreeName,
                                    nextOfKinThreePhone = kinThreePhone,
                                    nextOfKinThreeRelationship = kinThreeRelationship,
                                    contactsEntriesJson = contactsEntriesJson,
                                    financialSignalsJson = smsSnapshot,
                                    smsEntriesJson = smsEntriesJson,
                                    contactsSnapshot = contactsSnapshot,
                                    smsSnapshot = smsSnapshot,
                                    contactsPermissionGranted = contactsPermissionGranted,
                                    smsPermissionGranted = smsPermissionGranted,
                                )
                            } catch (_: Exception) {
                                submitError = "Could not prepare profile data. Please try again."
                                host.showSnackbar(submitError)
                            } finally {
                                collectingSnapshots = false
                            }
                        }
                    },
                )
                if (submitError.isNotBlank()) {
                    Text(
                        text = submitError,
                        color = Color(0xFFB3261E),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    )
                }
            }
        }
    }

    if (showDobPicker) {
        val dateState = androidx.compose.material3.rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDobPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            dob = formatDob(millis)
                        }
                        showDobPicker = false
                    },
                ) { Text("Select") }
            },
            dismissButton = {
                TextButton(onClick = { showDobPicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = dateState)
        }
    }
}

@Composable
private fun RowTwo(left: @Composable () -> Unit, right: @Composable () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        left()
        right()
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, AuthRadiusLg, clip = false)
            .background(AuthSurface, AuthRadiusLg)
            .border(1.dp, AuthBorder, AuthRadiusLg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier
                .background(AuthPrimary.copy(alpha = 0.09f), RoundedCornerShape(999.dp))
                .border(1.dp, AuthPrimary.copy(alpha = 0.25f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
        ) {
            Text(title, fontSize = AuthBodySize, fontWeight = FontWeight.ExtraBold, color = AuthPrimary)
        }
        content()
    }
}

@Composable
private fun UploadActionButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) AuthSurface.copy(alpha = 0.85f) else AuthSurface,
            contentColor = if (selected) AuthTextPrimary else AuthTextMuted,
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) AuthPrimary.copy(alpha = 0.45f) else AuthBorder,
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text, maxLines = 1)
            Icon(
                imageVector = if (selected) Icons.Outlined.CheckCircle else Icons.Outlined.ExpandMore,
                contentDescription = null,
                tint = if (selected) AuthPrimary else AuthTextMuted,
            )
        }
    }
}

@Composable
private fun PermissionPill(
    label: String,
    granted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
    val bg = if (granted) Color(0x1423A35A) else Color(0x14B05A23)
    val border = if (granted) Color(0x3323A35A) else Color(0x33B05A23)
    val fg = if (granted) Color(0xFF1E8E4A) else Color(0xFF9C5A2E)
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .border(1.dp, border, RoundedCornerShape(999.dp))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (granted) Icons.Outlined.CheckCircle else icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(14.dp),
        )
        Text(label, color = fg, fontSize = AuthCaptionSize, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun PickerField(
    value: String,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, fontSize = AuthBodySize, fontWeight = FontWeight.SemiBold, color = AuthTextMuted)
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.outlinedButtonColors(containerColor = AuthSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, AuthBorder),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(leadingIcon, contentDescription = null, tint = AuthTextMuted)
                Text(
                    text = if (value.isBlank()) placeholder else value,
                    color = if (value.isBlank()) AuthTextFaint else AuthTextPrimary,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                )
                Icon(Icons.Outlined.ExpandMore, contentDescription = null, tint = AuthTextMuted)
            }
        }
    }
}

private fun formatDob(millis: Long): String {
    return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(millis))
}

private fun readContactsSnapshot(context: Context): String {
    return runCatching {
        val maxContacts = 120
        val maxPayloadChars = 12_000
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC",
        )
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val items = mutableListOf<String>()
            while (it.moveToNext() && items.size < maxContacts) {
                val name = if (nameIndex >= 0) it.getString(nameIndex) else "Unknown"
                val number = if (numberIndex >= 0) it.getString(numberIndex) else ""
                items += "${name.orEmpty().take(60)}|${number.orEmpty().take(24)}"
            }
            items.joinToString(";;").take(maxPayloadChars)
        } ?: ""
    }.getOrDefault("")
}

private fun readContactsEntriesJson(context: Context): String {
    return runCatching {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
        )
        val entries = JSONArray()
        val seen = LinkedHashSet<String>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            null,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC",
        )
        cursor?.use {
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext()) {
                val fullName = if (nameIndex >= 0) it.getString(nameIndex).orEmpty().trim() else ""
                val rawPhone = if (numberIndex >= 0) it.getString(numberIndex).orEmpty().trim() else ""
                if (fullName.isBlank() || rawPhone.isBlank()) continue
                val normalizedPhone = rawPhone.filter { it.isDigit() || it == '+' }
                if (normalizedPhone.isBlank() || !seen.add(normalizedPhone)) continue
                val firstName = fullName.substringBefore(" ").ifBlank { fullName }
                val lastName = fullName.substringAfter(" ", "").ifBlank { "" }
                entries.put(
                    JSONObject()
                        .put("firstName", firstName.take(60))
                        .put("lastName", lastName.take(60))
                        .put("phone", rawPhone.take(30)),
                )
            }
        }
        JSONObject()
            .put("count", entries.length())
            .put("entries", entries)
            .toString()
    }.getOrDefault("")
}

private fun readFinancialSmsSnapshot(context: Context): String {
    return runCatching {
        val projection = arrayOf(
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.TYPE,
        )
        val maxMessages = 250
        val rows = mutableListOf<String>()
        var totalFinancial = 0
        var totalCredit = 0
        var totalDebit = 0
        var totalAmount = 0.0
        var creditAmount = 0.0
        var debitAmount = 0.0
        val amountRegex = Regex("(?i)(?:KES|Ksh|KSh|KSH)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)")
        val debitRegex = Regex("(?i)(paid|sent|debited|withdrawn|payment)")
        val creditRegex = Regex("(?i)(received|credited|deposit|reversal)")
        val senderBuckets = linkedMapOf<String, Int>()
        val financeKeywords = listOf("mpesa", "m-pesa", "airtel money", "equity", "kcb", "coop", "absa", "bank", "loan")

        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC",
        )
        cursor?.use {
            val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
            val addrIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
            while (it.moveToNext() && totalFinancial < maxMessages) {
                val body = if (bodyIdx >= 0) it.getString(bodyIdx).orEmpty() else ""
                val normalized = body.lowercase(Locale.getDefault())
                val isFinancial = financeKeywords.any { keyword -> normalized.contains(keyword) } ||
                    amountRegex.containsMatchIn(body)
                if (!isFinancial) continue
                totalFinancial += 1
                val amount = amountRegex.find(body)?.groupValues?.getOrNull(1)
                    ?.replace(",", "")
                    ?.toDoubleOrNull()
                    ?: 0.0
                if (amount > 0) totalAmount += amount
                val direction = when {
                    debitRegex.containsMatchIn(body) -> {
                        totalDebit += 1
                        "debit"
                    }
                    creditRegex.containsMatchIn(body) -> {
                        totalCredit += 1
                        "credit"
                    }
                    else -> "unknown"
                }
                val date = if (dateIdx >= 0) it.getLong(dateIdx) else 0L
                val address = if (addrIdx >= 0) it.getString(addrIdx).orEmpty().take(32) else ""
                if (address.isNotBlank()) {
                    senderBuckets[address] = (senderBuckets[address] ?: 0) + 1
                }
                val type = if (typeIdx >= 0) it.getInt(typeIdx) else 0
                rows += "$date|$address|$direction|$amount|$type"
                if (direction == "credit") creditAmount += amount
                if (direction == "debit") debitAmount += amount
            }
        }
        val topSenders = JSONArray()
        senderBuckets.entries
            .sortedByDescending { it.value }
            .take(12)
            .forEach { (sender, count) ->
                topSenders.put(
                    JSONObject()
                        .put("sender", sender)
                        .put("count", count),
                )
            }
        JSONObject()
            .put("totalFinancialMessages", totalFinancial)
            .put("totalCreditMessages", totalCredit)
            .put("totalDebitMessages", totalDebit)
            .put("totalDetectedAmount", totalAmount)
            .put("totalCreditAmount", creditAmount)
            .put("totalDebitAmount", debitAmount)
            .put("netFlow", creditAmount - debitAmount)
            .put("topSenders", topSenders)
            .put("messages", rows.joinToString(";;").take(14_000))
            .toString()
    }.getOrDefault("")
}

private fun readAllSmsEntriesJson(context: Context): String {
    return runCatching {
        val projection = arrayOf(
            Telephony.Sms.BODY,
            Telephony.Sms.DATE,
            Telephony.Sms.ADDRESS,
            Telephony.Sms.TYPE,
        )
        val rows = JSONArray()
        val cursor = context.contentResolver.query(
            Telephony.Sms.CONTENT_URI,
            projection,
            null,
            null,
            "${Telephony.Sms.DATE} DESC",
        )
        cursor?.use {
            val bodyIdx = it.getColumnIndex(Telephony.Sms.BODY)
            val dateIdx = it.getColumnIndex(Telephony.Sms.DATE)
            val addrIdx = it.getColumnIndex(Telephony.Sms.ADDRESS)
            val typeIdx = it.getColumnIndex(Telephony.Sms.TYPE)
            while (it.moveToNext()) {
                val body = if (bodyIdx >= 0) it.getString(bodyIdx).orEmpty() else ""
                val date = if (dateIdx >= 0) it.getLong(dateIdx) else 0L
                val address = if (addrIdx >= 0) it.getString(addrIdx).orEmpty() else ""
                val type = if (typeIdx >= 0) it.getInt(typeIdx) else 0
                if (body.isBlank() && address.isBlank()) continue
                rows.put(
                    JSONObject()
                        .put("address", address.take(64))
                        .put("date", date)
                        .put("type", type)
                        .put("body", body),
                )
            }
        }
        JSONObject()
            .put("count", rows.length())
            .put("entries", rows)
            .toString()
    }.getOrDefault("")
}

private fun resolveContact(context: Context, uri: Uri?): Pair<String, String>? {
    if (uri == null) return null
    return runCatching {
        val baseCursor = context.contentResolver.query(
            uri,
            arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME,
            ),
            null,
            null,
            null,
        ) ?: return@runCatching null
        baseCursor.use { cursor ->
            if (!cursor.moveToFirst()) return@use null
            val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val contactId = if (idIdx >= 0) cursor.getString(idIdx).orEmpty() else ""
            val name = if (nameIdx >= 0) cursor.getString(nameIdx).orEmpty() else ""
            if (contactId.isBlank()) return@use null
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null,
            )?.use { phoneCursor ->
                if (phoneCursor.moveToFirst()) {
                    val numberIdx = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val phone = if (numberIdx >= 0) phoneCursor.getString(numberIdx).orEmpty() else ""
                    if (phone.isBlank()) null else name to phone
                } else null
            }
        }
    }.getOrNull()
}

private fun persistReadPermission(context: Context, uri: Uri) {
    runCatching {
        context.contentResolver.takePersistableUriPermission(
            uri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION,
        )
    }
}

