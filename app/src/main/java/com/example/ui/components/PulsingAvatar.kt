package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanionType
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PremiumBlack

@Composable
fun PulsingAvatar(
    companion: CompanionType,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    isSpeaking: Boolean = false,
    isListening: Boolean = false,
    showOnlineBadge: Boolean = true
) {
    val isAnimated = isSpeaking || isListening
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isAnimated) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 800 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = if (isAnimated) 0.6f else 0.2f,
        targetValue = 0.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isSpeaking) 800 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val ringColor = when {
        isSpeaking -> ElectricBlue
        isListening -> BrightCyan
        companion == CompanionType.ABHI -> ElectricBlue
        else -> NeonPurple
    }

    Box(
        modifier = modifier.size(size * 1.35f),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(size * 1.25f)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(ringColor.copy(alpha = pulseAlpha))
        )

        // Middle aura ring
        Box(
            modifier = Modifier
                .size(size + 10.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ringColor.copy(alpha = 0.45f),
                            DeepViolet.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Companion Avatar Image
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(
                    width = 2.5.dp,
                    brush = Brush.sweepGradient(
                        listOf(
                            ElectricBlue,
                            NeonPurple,
                            BrightCyan,
                            ElectricBlue
                        )
                    ),
                    shape = CircleShape
                )
        ) {
            Image(
                painter = painterResource(id = companion.avatarRes),
                contentDescription = "${companion.displayName} Avatar",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
            )
        }

        // Online / Active Badge
        if (showOnlineBadge) {
            Box(
                modifier = Modifier
                    .size(size * 0.26f)
                    .align(Alignment.BottomEnd)
                    .offset(x = (-size * 0.08f), y = (-size * 0.08f))
                    .clip(CircleShape)
                    .background(PremiumBlack)
                    .border(1.5.dp, Color.White.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(size * 0.16f)
                        .clip(CircleShape)
                        .background(if (isListening) BrightCyan else EmeraldGreen)
                )
            }
        }
    }
}
