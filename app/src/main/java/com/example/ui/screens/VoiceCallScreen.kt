package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.components.AudioWaveform
import com.example.ui.components.PulsingAvatar
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.CoralRed
import com.example.ui.theme.DarkPurpleBg
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepPurpleCard
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlinx.coroutines.delay

@Composable
fun VoiceCallScreen(
    viewModel: MainViewModel,
    onRequestRecordAudioPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val companion by viewModel.companion.collectAsState()
    val personality by viewModel.personality.collectAsState()
    val language by viewModel.language.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val speechRms by viewModel.speechRmsLevel.collectAsState()
    val recognizedLiveText by viewModel.recognizedText.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isAutoTts by viewModel.isAutoTts.collectAsState()

    var callDurationSeconds by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }

    // Intercept hardware or gesture back button
    BackHandler {
        viewModel.navigateTo(ScreenDestination.CHAT)
    }

    // Call timer ticker
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            callDurationSeconds++
        }
    }

    val minutes = callDurationSeconds / 60
    val seconds = callDurationSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val lastAiMessage = messages.lastOrNull { !it.isUser }?.text ?: companion.getGreeting(language)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DeepViolet.copy(alpha = 0.35f),
                        DarkPurpleBg,
                        PremiumBlack
                    )
                )
            )
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Companion Info & Call Duration
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                // Call status chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DeepPurpleCard.copy(alpha = 0.8f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "🟢 Live Voice Call • $timeFormatted",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = companion.displayName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )

                Text(
                    text = "${companion.roleTitle} • ${personality.emoji} ${personality.title}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )

                Text(
                    text = "Speaking in ${language.displayName} (${language.nativeName})",
                    color = BrightCyan,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Center Area: Pulsing Avatar & Waveforms
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                PulsingAvatar(
                    companion = companion,
                    size = 140.dp,
                    isSpeaking = isSpeaking,
                    isListening = isListening
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Real-time audio waveform
                AudioWaveform(
                    isActive = isSpeaking || isListening,
                    barCount = 24,
                    maxHeight = 36.dp,
                    rmsLevel = speechRms
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Live status text
                Text(
                    text = when {
                        isSpeaking -> "🔊 ${companion.displayName} is speaking..."
                        isListening -> "🎤 Listening to your voice..."
                        isMuted -> "🔇 Microphone is Muted"
                        else -> "Tap the microphone below to talk"
                    },
                    color = if (isSpeaking) ElectricBlue else if (isListening) BrightCyan else TextMuted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subtitle preview card showing live spoken dialog
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.7f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    val subtitleText = if (isListening && recognizedLiveText.isNotBlank()) {
                        "You: \"$recognizedLiveText\""
                    } else {
                        "${companion.displayName}: \"${lastAiMessage.take(160)}${if (lastAiMessage.length > 160) "..." else ""}\""
                    }

                    Text(
                        text = subtitleText,
                        color = TextPrimary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Bottom Call Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speaker / Voice Output Toggle
                IconButton(
                    onClick = { viewModel.setAutoTts(!isAutoTts) },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (isAutoTts) DeepViolet.copy(alpha = 0.5f) else DarkSurfaceElevated)
                        .border(1.dp, GlassBorder, CircleShape)
                        .testTag("call_speaker_toggle")
                ) {
                    Icon(
                        imageVector = if (isAutoTts) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                        contentDescription = "Speaker",
                        tint = if (isAutoTts) ElectricBlue else TextMuted,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Microphone Speak / Listen Button (Center Large)
                IconButton(
                    onClick = {
                        if (isListening) {
                            viewModel.stopVoiceRecording()
                        } else {
                            onRequestRecordAudioPermission()
                            viewModel.startVoiceRecording()
                        }
                    },
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) {
                                Brush.radialGradient(listOf(BrightCyan, ElectricBlue))
                            } else {
                                Brush.linearGradient(listOf(DeepViolet, NeonPurple))
                            }
                        )
                        .border(2.dp, if (isListening) BrightCyan else GlassBorder, CircleShape)
                        .testTag("call_mic_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.Mic,
                        contentDescription = "Microphone",
                        tint = if (isListening) Color.Black else Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // End Call Button (Red)
                IconButton(
                    onClick = {
                        viewModel.stopSpeaking()
                        viewModel.stopVoiceRecording()
                        viewModel.navigateTo(ScreenDestination.CHAT)
                    },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CoralRed)
                        .testTag("end_voice_call_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}
