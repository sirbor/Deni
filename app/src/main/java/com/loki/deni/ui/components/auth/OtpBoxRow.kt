package com.loki.deni.ui.components

import android.view.KeyEvent
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loki.deni.presentation.ui.theme.DeniPrimary

@Composable
fun OtpBoxRow(
    otp: String,
    isError: Boolean,
    modifier: Modifier = Modifier,
    digits: Int = 4,
    onOtpChange: (String) -> Unit,
) {
    val requesters = List(digits) { remember { FocusRequester() } }
    var focusedIndex by remember { mutableIntStateOf(-1) }
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = modifier) {
        repeat(digits) { index ->
            val char = otp.getOrNull(index)?.toString().orEmpty()
            val borderColor by animateColorAsState(
                targetValue = when {
                    isError -> Color(0xFFB00020)
                    focusedIndex == index -> DeniPrimary
                    char.isNotEmpty() -> DeniPrimary
                    else -> Color(0x33000000)
                },
                label = "otp-border",
            )
            val backgroundColor by animateColorAsState(
                targetValue = when {
                    focusedIndex == index -> Color.White
                    char.isNotEmpty() -> DeniPrimary.copy(alpha = 0.12f)
                    else -> Color.Transparent
                },
                label = "otp-background",
            )
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 64.dp)
                    .background(backgroundColor, RoundedCornerShape(14.dp))
                    .border(1.5.dp, borderColor, RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center,
            ) {
                BasicTextField(
                    value = char,
                    onValueChange = { raw ->
                        val digit = raw.filter(Char::isDigit).take(1)
                        val current = otp.padEnd(digits, ' ').toCharArray()
                        current[index] = if (digit.isEmpty()) ' ' else digit.first()
                        val merged = String(current).replace(" ", "")
                        onOtpChange(merged)
                        if (digit.isNotEmpty() && index < digits - 1) requesters[index + 1].requestFocus()
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .focusRequester(requesters[index])
                        .onFocusChanged { state ->
                            if (state.isFocused) focusedIndex = index
                        }
                        .onKeyEvent { event ->
                            if (event.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DEL && char.isEmpty() && index > 0) {
                                requesters[index - 1].requestFocus()
                            }
                            false
                        },
                )
            }
        }
    }
}
