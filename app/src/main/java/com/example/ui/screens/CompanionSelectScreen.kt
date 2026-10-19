package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CompanionType
import com.example.ui.components.GlassCard
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.DarkPurpleBg
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepPurpleCard
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun CompanionSelectScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val activeCompanion by viewModel.companion.collectAsState()
    val language by viewModel.language.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        PremiumBlack,
                        DarkPurpleBg,
                        PremiumBlack
                    )
                )
            )
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Choose Your AI Companion",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = "Switch between AI Boy & AI Girl with distinctive voices and vibes",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
            )

            // Companion Cards
            CompanionCard(
                companion = CompanionType.ABHI,
                isSelected = activeCompanion == CompanionType.ABHI,
                accentColor = ElectricBlue,
                tags = listOf("AI Boy", "Tech Bro", "Gamer", "Witty", "Motivational"),
                voiceDesc = "Deeper masculine tone • Friendly brotherly voice",
                onSelect = {
                    viewModel.setCompanion(CompanionType.ABHI)
                    viewModel.navigateTo(ScreenDestination.CHAT)
                },
                onPreviewVoice = {
                    val previewMsg = CompanionType.ABHI.getGreeting(language)
                    viewModel.speakText(previewMsg)
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            CompanionCard(
                companion = CompanionType.MAYA,
                isSelected = activeCompanion == CompanionType.MAYA,
                accentColor = NeonPurple,
                tags = listOf("AI Girl", "Caring Bestie", "Empathetic", "Brilliant", "Storyteller"),
                voiceDesc = "Sweet melodic feminine tone • Warm caring voice",
                onSelect = {
                    viewModel.setCompanion(CompanionType.MAYA)
                    viewModel.navigateTo(ScreenDestination.CHAT)
                },
                onPreviewVoice = {
                    val previewMsg = CompanionType.MAYA.getGreeting(language)
                    viewModel.speakText(previewMsg)
                }
            )

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun CompanionCard(
    companion: CompanionType,
    isSelected: Boolean,
    accentColor: Color,
    tags: List<String>,
    voiceDesc: String,
    onSelect: () -> Unit,
    onPreviewVoice: () -> Unit
) {
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("companion_card_${companion.id}"),
        borderColors = if (isSelected) {
            listOf(accentColor, BrightCyan, accentColor)
        } else {
            listOf(GlassBorder, Color.Transparent)
        },
        borderWidth = if (isSelected) 2.dp else 1.dp,
        backgroundColor = if (isSelected) DeepPurpleCard.copy(alpha = 0.85f) else DarkSurfaceElevated.copy(alpha = 0.5f)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Large Avatar with glowing border
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, accentColor, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = companion.avatarRes),
                        contentDescription = companion.displayName,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = companion.displayName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )

                        if (isSelected) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldGreen.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Active",
                                    color = EmeraldGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = companion.roleTitle,
                        color = accentColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🔊 $voiceDesc",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = companion.description,
                color = TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Tags row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                tags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkPurpleBg)
                            .border(0.7.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = tag,
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons: Preview Voice & Select Companion
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onPreviewVoice,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = accentColor
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = Brush.linearGradient(listOf(accentColor, BrightCyan))
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Preview Voice",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Voice Sample", fontSize = 12.sp)
                }

                Button(
                    onClick = onSelect,
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelected) DeepViolet else accentColor,
                        contentColor = if (isSelected) Color.White else Color.Black
                    )
                ) {
                    Text(
                        text = if (isSelected) "Current Companion" else "Select ${companion.displayName}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
