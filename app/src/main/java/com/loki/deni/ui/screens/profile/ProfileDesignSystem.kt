package com.loki.deni.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.loki.deni.ui.components.DeniOutlinedActionButton
import com.loki.deni.ui.components.GradientPrimaryButton

object ProfileTokens {
    val HeroRadius = RoundedCornerShape(18.dp)
    val CardRadius = RoundedCornerShape(14.dp)
    val HeroGradient: Brush
        @Composable get() = Brush.linearGradient(
            if (isSystemInDarkTheme()) listOf(Color(0xFF0C2528), Color(0xFF114247)) else listOf(Color(0xFF014D52), Color(0xFF01696F)),
        )
    val HeroTopActionsTopPadding = 8.dp
    const val HeroTitleSp = 17
    const val HeroSubtitleSp = 12
}

@Composable
fun ProfileHeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ProfileTokens.HeroRadius,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(ProfileTokens.HeroGradient, ProfileTokens.HeroRadius)
                .padding(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = ProfileTokens.HeroTitleSp.sp,
                )
                Text(
                    subtitle,
                    color = Color.White.copy(alpha = 0.78f),
                    fontSize = ProfileTokens.HeroSubtitleSp.sp,
                )
            }
        }
    }
}

@Composable
fun ProfileSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = ProfileTokens.CardRadius,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Box(modifier = Modifier.padding(12.dp)) { content() }
    }
}

@Composable
fun ProfilePrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    GradientPrimaryButton(
        text = text,
        onClick = onClick,
        showArrow = false,
        modifier = modifier.fillMaxWidth().height(52.dp),
    )
}

@Composable
fun ProfileSecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    DeniOutlinedActionButton(
        text = text,
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
    )
}
