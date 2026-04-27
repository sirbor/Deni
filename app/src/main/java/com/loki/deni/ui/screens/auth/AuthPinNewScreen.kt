package com.loki.deni.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.loki.deni.R
import com.loki.deni.ui.navigation.Routes
import com.loki.deni.ui.viewmodel.AuthUiState
import com.loki.deni.ui.viewmodel.AuthViewModel
import com.loki.deni.util.BiometricAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AuthPinNewScreen(
    navController: NavController,
    phone: String,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val createState by authViewModel.createState.collectAsStateWithLifecycle()
    val host = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val shake = remember { Animatable(0f) }

    var pinStep by rememberSaveable { mutableStateOf(1) }
    var pin1 by rememberSaveable { mutableStateOf("") }
    var pin2 by rememberSaveable { mutableStateOf("") }
    var showBiometricPrompt by rememberSaveable { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val regFailText = stringResource(R.string.snack_reg_fail)
    val weakText = stringResource(R.string.pin_error_weak)
    val mismatchText = stringResource(R.string.pin_error_mismatch)
    val stepEnter = stringResource(R.string.pin_step_enter)
    val stepConfirm = stringResource(R.string.pin_step_confirm)

    val weakPins = setOf(
        "0000", "1111", "2222", "3333", "4444", "5555",
        "6666", "7777", "8888", "9999", "1234", "4321", "2580", "1470",
    )
    val backInteraction = remember { MutableInteractionSource() }
    val backPressed by backInteraction.collectIsPressedAsState()
    val backScale by animateFloatAsState(
        targetValue = if (backPressed) 0.95f else 1f,
        label = "auth-pin-back-scale",
    )

    LaunchedEffect(createState) {
        when (createState) {
            is AuthUiState.Success -> {
                authViewModel.resetCreateState()
                navController.navigate(Routes.AUTH_SUCCESS) {
                    popUpTo(Routes.AUTH_PHONE) { inclusive = true }
                }
            }
            is AuthUiState.Error -> {
                authViewModel.resetCreateState()
                host.showSnackbar(regFailText)
            }
            else -> Unit
        }
    }

    suspend fun showError(message: String) {
        errorText = message
        shake.animateTo(-6f, tween(80))
        shake.animateTo(6f, tween(80))
        shake.animateTo(-6f, tween(80))
        shake.animateTo(0f, tween(80))
        delay(1500)
        errorText = null
    }

    fun validatePins() {
        scope.launch {
            if (pin1 in weakPins) {
                pinStep = 1
                pin1 = ""
                pin2 = ""
                showError(weakText)
                return@launch
            }
            if (pin1 != pin2) {
                pin2 = ""
                showError(mismatchText)
                return@launch
            }
            showBiometricPrompt = true
        }
    }

    fun onDigit(digit: String) {
        if (createState is AuthUiState.Loading) return
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        errorText = null
        if (pinStep == 1) {
            if (pin1.length < 4) pin1 += digit
            if (pin1.length == 4) pinStep = 2
        } else {
            if (pin2.length < 4) pin2 += digit
            if (pin2.length == 4) validatePins()
        }
    }

    fun onDelete() {
        if (createState is AuthUiState.Loading) return
        if (pinStep == 1) pin1 = pin1.dropLast(1) else pin2 = pin2.dropLast(1)
    }

    Scaffold(snackbarHost = { SnackbarHost(host) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(AuthBackground).navigationBarsPadding()) {
            AuthGradientHero(modifier = Modifier.fillMaxWidth().height(220.dp)) {
                IconButton(
                    onClick = {
                        if (pinStep == 2) {
                            pinStep = 1
                            pin2 = ""
                        } else {
                            navController.popBackStack()
                        }
                    },
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
                    ) { Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp)) }
                    Spacer(Modifier.height(12.dp))
                    Crossfade(targetState = pinStep, label = "pin-hero") { step ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                if (step == 1) stringResource(R.string.pin_create_title) else stringResource(R.string.pin_confirm_title),
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                            )
                            Text(
                                if (step == 1) stringResource(R.string.pin_create_sub) else stringResource(R.string.pin_confirm_sub),
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 28.dp)
                            .background(Color.White.copy(alpha = 0.10f), RoundedCornerShape(18.dp))
                            .border(1.dp, Color.White.copy(alpha = 0.18f), RoundedCornerShape(18.dp))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column {
                                Text("PIN strength", color = Color.White.copy(alpha = 0.66f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("Protected", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Text(
                                text = if (pinStep == 1) "Step 1 of 2" else "Step 2 of 2",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().weight(1f)
                    .background(AuthBackground, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .padding(horizontal = 28.dp, vertical = 28.dp)
                    .imePadding()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(AuthSurface, RoundedCornerShape(20.dp))
                        .border(1.dp, AuthBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Dot(active = pinStep == 1)
                            Dot(active = pinStep == 2)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.pin_step_label, pinStep, if (pinStep == 1) stepEnter else stepConfirm),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AuthTextMuted,
                        )
                        Spacer(Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(18.dp), modifier = Modifier.offset(x = shake.value.dp)) {
                            repeat(4) { index ->
                                val isFilled = if (pinStep == 1) pin1.length > index else pin2.length > index
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (isFilled) AuthPrimary else Color.Transparent,
                                            CircleShape,
                                        )
                                        .border(2.dp, if (isFilled) AuthPrimary else AuthBorder, CircleShape),
                                )
                            }
                        }
                        val alpha by animateFloatAsState(if (errorText.isNullOrBlank()) 0f else 1f, label = "error-alpha")
                        Text(
                            text = errorText.orEmpty(),
                            modifier = Modifier.padding(top = 8.dp).alpha(alpha),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AuthError,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                Spacer(Modifier.height(14.dp))
                if (createState is AuthUiState.Loading) {
                    CircularProgressIndicator(color = AuthPrimary)
                } else {
                    PinPad(onDigit = ::onDigit, onDelete = ::onDelete)
                }
            }
        }
    }

    if (showBiometricPrompt) {
        Dialog(onDismissRequest = { }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(AuthSurface, RoundedCornerShape(22.dp))
                    .border(1.dp, AuthBorder, RoundedCornerShape(22.dp))
                    .padding(16.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .background(AuthPrimary.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
                            .border(1.dp, AuthBorder, RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(Icons.Outlined.Fingerprint, contentDescription = null, tint = AuthPrimary, modifier = Modifier.size(24.dp))
                    }
                    Text("Enable Fingerprint Login?", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = AuthTextPrimary)
                    Text(
                        "Use your fingerprint for faster sign in on this device.",
                        color = AuthTextMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        ProfileSecondaryButton(
                            text = "Skip",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                authViewModel.setPendingSignup(phone = phone, pin = pin1, biometricsEnabled = false)
                                showBiometricPrompt = false
                                navController.navigate(Routes.AUTH_PROFILE_SETUP)
                            },
                        )
                        ProfilePrimaryButton(
                            text = "Enable",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                if (!BiometricAuth.isAvailable(context)) {
                                    scope.launch { host.showSnackbar("Fingerprint is not available on this device. You can continue with Skip.") }
                                    return@ProfilePrimaryButton
                                }
                                BiometricAuth.authenticate(
                                    context = context,
                                    title = "Enable Fingerprint Login",
                                    subtitle = "Confirm your identity to enable biometric sign in.",
                                    onSuccess = {
                                        authViewModel.setPendingSignup(phone = phone, pin = pin1, biometricsEnabled = true)
                                        showBiometricPrompt = false
                                        navController.navigate(Routes.AUTH_PROFILE_SETUP)
                                    },
                                    onError = {
                                        scope.launch { host.showSnackbar(it) }
                                    },
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Dot(active: Boolean) {
    Box(modifier = Modifier.size(8.dp).background(if (active) AuthPrimary else AuthBorder, CircleShape))
}

@Composable
private fun PinPad(onDigit: (String) -> Unit, onDelete: () -> Unit) {
    val keyWidth = 84.dp
    val keyHeight = 58.dp
    Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
        listOf("123", "456", "789").forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                row.forEach { digit ->
                    PinKey(
                        value = digit.toString(),
                        width = keyWidth,
                        height = keyHeight,
                        onTap = { onDigit(digit.toString()) },
                    )
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(modifier = Modifier.size(width = keyWidth, height = keyHeight))
            PinKey(value = "0", width = keyWidth, height = keyHeight, onTap = { onDigit("0") })
            val deleteInteraction = remember { MutableInteractionSource() }
            val deletePressed by deleteInteraction.collectIsPressedAsState()
            val deleteScale by animateFloatAsState(
                targetValue = if (deletePressed) 0.96f else 1f,
                label = "pin-delete-scale",
            )
            Box(
                modifier = Modifier
                    .scale(deleteScale)
                    .size(width = keyWidth, height = keyHeight).background(AuthBackground, RoundedCornerShape(16.dp))
                    .border(1.5.dp, AuthBorder, RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(
                    onClick = onDelete,
                    interactionSource = deleteInteraction,
                ) { Icon(Icons.AutoMirrored.Outlined.Backspace, null, tint = AuthTextMuted) }
            }
        }
    }
}

@Composable
private fun PinKey(value: String, width: androidx.compose.ui.unit.Dp, height: androidx.compose.ui.unit.Dp, onTap: () -> Unit) {
    val shape = when (value) {
        "2", "5", "8", "0" -> RoundedCornerShape(20.dp)
        else -> RoundedCornerShape(14.dp)
    }
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.96f else 1f,
        label = "pin-key-scale-$value",
    )
    Box(
        modifier = Modifier
            .scale(scale)
            .size(width = width, height = height).background(AuthSurface, shape)
            .border(1.5.dp, AuthBorder, shape)
            .clickable(
                interactionSource = interaction,
                indication = null,
                onClick = onTap,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = AuthTextPrimary)
    }
}
