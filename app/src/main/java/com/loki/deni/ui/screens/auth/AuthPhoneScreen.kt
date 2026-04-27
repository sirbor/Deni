package com.loki.deni.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextButton
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.loki.deni.R
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.AuthUiState
import com.loki.deni.ui.viewmodel.AuthViewModel
import com.loki.deni.util.BiometricAuth
import kotlinx.coroutines.launch

private enum class Tab { SIGN_IN, SIGN_UP }
private data class CountryUi(val flag: String, val name: String, val dialCode: String)

@Composable
fun AuthPhoneScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val signInState by viewModel.signInState.collectAsStateWithLifecycle()
    val biometricsEnabled by viewModel.biometricsEnabled.collectAsStateWithLifecycle()
    val wrongAttempts by viewModel.wrongAttempts.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(Tab.SIGN_IN) }
    val countries = remember {
        listOf(
            CountryUi("\uD83C\uDDF0\uD83C\uDDEA", "Kenya", "+254"),
            CountryUi("\uD83C\uDDFA\uD83C\uDDEC", "Uganda", "+256"),
            CountryUi("\uD83C\uDDF9\uD83C\uDDFF", "Tanzania", "+255"),
            CountryUi("\uD83C\uDDF7\uD83C\uDDFC", "Rwanda", "+250"),
            CountryUi("\uD83C\uDDE7\uD83C\uDDEE", "Burundi", "+257"),
            CountryUi("\uD83C\uDDF8\uD83C\uDDF8", "South Sudan", "+211"),
            CountryUi("\uD83C\uDDE8\uD83C\uDDE9", "Democratic Republic of the Congo", "+243"),
            CountryUi("\uD83C\uDDF8\uD83C\uDDF4", "Somalia", "+252"),
        )
    }
    var selectedCountry by remember { mutableStateOf(countries.first()) }
    var phone by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var showPin by remember { mutableStateOf(false) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var pinError by remember { mutableStateOf<String?>(null) }
    val shake = remember { Animatable(0f) }

    val validPhone = viewModel.validatePhone(phone, selectedCountry.dialCode)
    val canSignIn = validPhone && pin.length == 4
    val canSignUp = validPhone
    val wrongCredsText = stringResource(R.string.error_wrong_creds)
    val phoneInvalidText = stringResource(R.string.error_phone_invalid)
    val phoneFirstText = "Enter your phone first"
    val backInteraction = remember { MutableInteractionSource() }
    val backPressed by backInteraction.collectIsPressedAsState()
    val backScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (backPressed) 0.95f else 1f,
        label = "auth-phone-back-scale",
    )

    LaunchedEffect(signInState, selectedTab) {
        when (val state = signInState) {
            is AuthUiState.Error -> {
                val message = state.message
                if (message == "wrong_credentials") {
                    pinError = wrongCredsText
                    shake.animateTo(-6f, tween(80)); shake.animateTo(6f, tween(80)); shake.animateTo(-6f, tween(80)); shake.animateTo(0f, tween(80))
                } else {
                    host.showSnackbar(message)
                }
                viewModel.resetSignInState()
            }
            is AuthUiState.Success -> {
                navController.navigate(Routes.HOME) {
                    popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                }
                viewModel.resetSignInState()
            }
            else -> Unit
        }
    }
    Scaffold(snackbarHost = { SnackbarHost(host) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(AuthBackground).navigationBarsPadding()) {
            AuthGradientHero(modifier = Modifier.fillMaxWidth().height(208.dp)) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    interactionSource = backInteraction,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp).scale(backScale).size(38.dp)
                        .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp)),
                ) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, null, tint = Color.White) }

                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier.size(60.dp).background(Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Outlined.PhoneAndroid, null, tint = Color.White, modifier = Modifier.size(26.dp)) }
                    Spacer(Modifier.height(10.dp))
                    Crossfade(targetState = selectedTab, label = "tab-hero") { tab ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (tab == Tab.SIGN_IN) stringResource(R.string.auth_signin_title) else stringResource(R.string.auth_signup_title),
                                color = Color.White,
                                fontSize = AuthHeroTitleSize,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                text = if (tab == Tab.SIGN_IN) stringResource(R.string.auth_signin_sub) else stringResource(R.string.auth_signup_sub),
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = AuthBodySize,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Secure phone sign in",
                        color = Color.White.copy(alpha = 0.72f),
                        fontSize = AuthCaptionSize,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .background(AuthBackground, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(horizontal = 28.dp, vertical = 28.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
            ) {
                AuthTabRow(
                    selectedSignIn = selectedTab == Tab.SIGN_IN,
                    onSignIn = { selectedTab = Tab.SIGN_IN; pinError = null },
                    onSignUp = { selectedTab = Tab.SIGN_UP; pinError = null },
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, AuthRadiusLg, clip = false)
                        .background(
                            brush = Brush.linearGradient(
                                listOf(AuthSurface, AuthSurface.copy(alpha = 0.96f)),
                            ),
                            shape = AuthRadiusLg,
                        )
                        .border(1.dp, AuthBorder, AuthRadiusLg)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                ) {
                    Crossfade(targetState = selectedTab, label = "tab-content") { tab ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = shake.value.dp),
                            verticalArrangement = Arrangement.spacedBy(AuthSpacingSm),
                        ) {
                            Text(stringResource(R.string.label_phone), fontSize = AuthBodySize, fontWeight = FontWeight.ExtraBold, color = AuthTextMuted)
                            PhoneRow(
                                selectedCountry = selectedCountry,
                                countries = countries,
                                phone = phone,
                                error = phoneError,
                                onCountry = { selectedCountry = it },
                                onPhone = {
                                phone = it.filter(Char::isDigit).take(if (selectedCountry.dialCode == "+254") 9 else 12)
                                phoneError = null
                            },
                            )
                            Text(
                                phoneError ?: stringResource(R.string.hint_phone),
                                fontSize = AuthCaptionSize,
                                fontWeight = FontWeight.SemiBold,
                                color = if (phoneError != null) AuthError else AuthTextFaint,
                            )

                            if (tab == Tab.SIGN_IN) {
                                Text(stringResource(R.string.label_pin), fontSize = AuthBodySize, fontWeight = FontWeight.ExtraBold, color = AuthTextMuted)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    TextField(
                                        value = pin,
                                        onValueChange = { pin = it.filter(Char::isDigit).take(4); pinError = null },
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).height(54.dp),
                                        textStyle = androidx.compose.ui.text.TextStyle(
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = AuthTextPrimary,
                                        ),
                                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                        visualTransformation = if (showPin) VisualTransformation.None else PasswordVisualTransformation(),
                                        placeholder = {
                                            Text(
                                                text = "Enter 4-digit PIN",
                                                color = AuthTextFaint,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                            )
                                        },
                                        trailingIcon = {
                                            IconButton(onClick = { showPin = !showPin }) {
                                                Icon(if (showPin) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility, null, tint = AuthTextFaint)
                                            }
                                        },
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = AuthSurface,
                                            unfocusedContainerColor = AuthSurface,
                                            focusedTextColor = AuthTextPrimary,
                                            unfocusedTextColor = AuthTextPrimary,
                                            cursorColor = AuthPrimary,
                                            focusedIndicatorColor = AuthPrimary.copy(alpha = 0.55f),
                                            unfocusedIndicatorColor = AuthBorder,
                                        ),
                                    )
                                    if (biometricsEnabled) {
                                        val biometricInteraction = remember { MutableInteractionSource() }
                                        val biometricPressed by biometricInteraction.collectIsPressedAsState()
                                        val biometricScale by animateFloatAsState(
                                            targetValue = if (biometricPressed) 0.96f else 1f,
                                            label = "biometric-button-scale",
                                        )
                                        Box(
                                            modifier = Modifier
                                                .scale(biometricScale)
                                                .size(54.dp)
                                                .background(AuthPrimary.copy(alpha = 0.11f), CircleShape)
                                                .border(1.3.dp, AuthPrimary.copy(alpha = 0.45f), CircleShape)
                                                .clickable(
                                                    interactionSource = biometricInteraction,
                                                    indication = null,
                                                ) {
                                                    if (!BiometricAuth.isAvailable(context)) {
                                                        scope.launch { host.showSnackbar("Biometric authentication is not available on this device.") }
                                                        return@clickable
                                                    }
                                                    BiometricAuth.authenticate(
                                                        context = context,
                                                        title = "Sign in to Deni",
                                                        subtitle = "Use your fingerprint to continue.",
                                                        onSuccess = { viewModel.signInWithBiometrics() },
                                                        onError = { message -> scope.launch { host.showSnackbar(message) } },
                                                    )
                                                },
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = AuthPrimary, modifier = Modifier.size(24.dp))
                                        }
                                    }
                                }
                                if (!pinError.isNullOrBlank()) {
                                    Text(pinError.orEmpty(), fontSize = AuthCaptionSize, fontWeight = FontWeight.Bold, color = AuthError)
                                }
                                TextButton(
                                    onClick = {
                                        if (validPhone) navController.navigate(Routes.authPinNew(viewModel.normalizePhone(phone, selectedCountry.dialCode)))
                                        else {
                                            phoneError = phoneInvalidText
                                            scope.launch { host.showSnackbar(phoneFirstText) }
                                        }
                                    },
                                    modifier = Modifier.align(Alignment.End),
                                ) {
                                    Text(stringResource(R.string.forgot_pin), color = AuthPrimary, fontWeight = FontWeight.Bold)
                                }
                                PrimaryCta(
                                    text = stringResource(R.string.cta_signin),
                                    trailingArrow = true,
                                    enabled = canSignIn && signInState !is AuthUiState.Loading,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        if (!validPhone) phoneError = phoneInvalidText else viewModel.signIn(phone, pin, selectedCountry.dialCode)
                                    },
                                )
                                if (wrongAttempts >= 3) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(AuthBackground, RoundedCornerShape(12.dp))
                                            .border(1.dp, AuthBorder, RoundedCornerShape(12.dp))
                                            .padding(12.dp),
                                    ) {
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Too many failed attempts.", color = AuthError, fontWeight = FontWeight.ExtraBold)
                                            TextButton(onClick = {
                                                if (validPhone) navController.navigate(Routes.authPinNew(viewModel.normalizePhone(phone, selectedCountry.dialCode)))
                                                else phoneError = phoneInvalidText
                                            }) {
                                                Text("Reset PIN", color = AuthPrimary, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            } else {
                                PrimaryCta(
                                    text = stringResource(R.string.cta_create),
                                    trailingArrow = true,
                                    enabled = canSignUp,
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        if (!validPhone) {
                                            phoneError = phoneInvalidText
                                            return@PrimaryCta
                                        }
                                        navController.navigate(Routes.authPinNew(viewModel.normalizePhone(phone, selectedCountry.dialCode)))
                                    },
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("Phone number sign in only.", fontSize = AuthCaptionSize, fontWeight = FontWeight.SemiBold, color = AuthTextFaint)
            }
        }
    }
}


@Composable
private fun PhoneRow(
    selectedCountry: CountryUi,
    countries: List<CountryUi>,
    phone: String,
    error: String?,
    onCountry: (CountryUi) -> Unit,
    onPhone: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val countryInteraction = remember { MutableInteractionSource() }
    val countryPressed by countryInteraction.collectIsPressedAsState()
    val countryScale by animateFloatAsState(
        targetValue = if (countryPressed) 0.97f else 1f,
        label = "country-select-scale",
    )
    Row(
        modifier = Modifier.fillMaxWidth().height(54.dp)
            .background(AuthSurface, RoundedCornerShape(14.dp))
            .border(1.5.dp, if (error != null) AuthError else AuthBorder, RoundedCornerShape(14.dp)),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box {
            Row(
                modifier = Modifier
                    .height(54.dp)
                    .scale(countryScale)
                    .clickable(
                        interactionSource = countryInteraction,
                        indication = null,
                    ) { expanded = true }
                    .padding(horizontal = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(selectedCountry.flag, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(selectedCountry.dialCode, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = AuthPrimary)
                Icon(Icons.Outlined.ArrowDropDown, contentDescription = null, tint = AuthTextMuted)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                countries.forEach { country ->
                    DropdownMenuItem(
                        text = { Text("${country.flag} ${country.name} (${country.dialCode})") },
                        onClick = {
                            onCountry(country)
                            expanded = false
                        },
                    )
                }
            }
        }
        TextField(
            value = phone,
            onValueChange = onPhone,
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = AuthTextPrimary),
            placeholder = { Text(stringResource(R.string.hint_phone_placeholder), color = AuthTextFaint) },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = AuthPrimary.copy(alpha = 0.55f),
                unfocusedIndicatorColor = AuthBorder,
            ),
        )
    }
}
