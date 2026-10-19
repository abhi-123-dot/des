package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CompanionType
import com.example.data.model.PersonalityMode
import com.example.ui.components.AudioWaveform
import com.example.ui.components.ChatBubble
import com.example.ui.theme.BrightCyan
import com.example.ui.theme.DarkPurpleBg
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepPurpleCard
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassBorderPurple
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onRequestRecordAudioPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val companion by viewModel.companion.collectAsState()
    val personality by viewModel.personality.collectAsState()
    val language by viewModel.language.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val isListening by viewModel.isListening.collectAsState()
    val speechRms by viewModel.speechRmsLevel.collectAsState()
    val recognizedLiveText by viewModel.recognizedText.collectAsState()
    val speakingMessageText by viewModel.speakingMessageText.collectAsState()
    val isAutoTts by viewModel.isAutoTts.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll on new message
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        PremiumBlack,
                        DarkPurpleBg,
                        PremiumBlack
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top App Bar Header
            ChatHeader(
                companion = companion,
                personality = personality,
                language = language,
                isSpeaking = isSpeaking,
                isAutoTts = isAutoTts,
                onCompanionSwitch = {
                    val nextCompanion = if (companion == CompanionType.ABHI) CompanionType.MAYA else CompanionType.ABHI
                    viewModel.setCompanion(nextCompanion)
                },
                onVoiceCallClick = {
                    viewModel.navigateTo(ScreenDestination.VOICE_CALL)
                },
                onToggleAutoTts = {
                    viewModel.setAutoTts(!isAutoTts)
                }
            )

            // Selectors row: Personality & Language chips
            QuickControlsRow(
                currentPersonality = personality,
                currentLanguage = language,
                onSelectPersonality = { viewModel.setPersonality(it) },
                onSelectLanguage = { viewModel.setLanguage(it) }
            )

            // Waveform bar if speaking or listening
            AnimatedVisibility(visible = isSpeaking || isListening) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceElevated.copy(alpha = 0.8f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .padding(vertical = 6.dp, horizontal = 12.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isListening) "🎤 Listening to your voice..." else "🔊 ${companion.displayName} is speaking...",
                            color = if (isListening) BrightCyan else ElectricBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        AudioWaveform(
                            isActive = true,
                            rmsLevel = speechRms,
                            barCount = 20,
                            maxHeight = 24.dp
                        )
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        companion = companion,
                        isSpeakingThisMessage = isSpeaking && speakingMessageText == message.text,
                        onSpeakClick = { viewModel.speakText(it) },
                        onStopSpeakClick = { viewModel.stopSpeaking() }
                    )
                }

                if (isLoading) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ElectricBlue,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "${companion.displayName} is thinking...",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Quick Topic Suggestion Pills
            QuickPromptPills(
                language = language,
                personality = personality,
                onPromptSelected = { prompt ->
                    inputText = prompt
                    viewModel.sendMessage(prompt)
                }
            )

            // Live speech transcript preview if active
            AnimatedVisibility(visible = isListening && recognizedLiveText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepViolet.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Hearing: \"$recognizedLiveText\"",
                        color = Color.White,
                        fontSize = 12.sp
                    )
                }
            }

            // Bottom Input Bar
            BottomInputBar(
                inputText = inputText,
                onInputChanged = { inputText = it },
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    }
                },
                isListening = isListening,
                onMicClick = {
                    if (isListening) {
                        viewModel.stopVoiceRecording()
                    } else {
                        onRequestRecordAudioPermission()
                        viewModel.startVoiceRecording()
                    }
                }
            )
        }
    }
}

@Composable
private fun ChatHeader(
    companion: CompanionType,
    personality: PersonalityMode,
    language: AppLanguage,
    isSpeaking: Boolean,
    isAutoTts: Boolean,
    onCompanionSwitch: () -> Unit,
    onVoiceCallClick: () -> Unit,
    onToggleAutoTts: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Companion profile pill
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(DeepPurpleCard.copy(alpha = 0.8f))
                .border(1.dp, GlassBorder, RoundedCornerShape(24.dp))
                .clickable(onClick = onCompanionSwitch)
                .padding(horizontal = 10.dp, vertical = 6.dp)
                .testTag("companion_switch_header")
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .border(1.5.dp, if (companion == CompanionType.ABHI) ElectricBlue else NeonPurple, CircleShape)
            ) {
                Image(
                    painter = painterResource(id = companion.avatarRes),
                    contentDescription = companion.displayName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.matchParentSize()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = companion.displayName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = "Switch Companion",
                        tint = ElectricBlue,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(EmeraldGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${companion.roleTitle} • ${language.displayName}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Header Actions: Voice Call button & Auto-read toggle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Auto-read audio toggle
            IconButton(
                onClick = onToggleAutoTts,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (isAutoTts) DeepViolet.copy(alpha = 0.5f) else DarkSurfaceElevated)
                    .border(1.dp, GlassBorder, CircleShape)
                    .testTag("auto_tts_toggle")
            ) {
                Icon(
                    imageVector = if (isAutoTts) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Toggle Auto-Speech",
                    tint = if (isAutoTts) ElectricBlue else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Voice Call button
            IconButton(
                onClick = onVoiceCallClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                EmeraldGreen,
                                ElectricBlue
                            )
                        )
                    )
                    .testTag("start_voice_call_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Voice Call",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickControlsRow(
    currentPersonality: PersonalityMode,
    currentLanguage: AppLanguage,
    onSelectPersonality: (PersonalityMode) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Languages
        AppLanguage.entries.forEach { lang ->
            val isSelected = lang == currentLanguage
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) ElectricBlue.copy(alpha = 0.25f) else DarkSurfaceElevated.copy(alpha = 0.6f))
                .border(
                    1.dp,
                    if (isSelected) ElectricBlue else GlassBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onSelectLanguage(lang) }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${lang.displayName} (${lang.nativeName})",
                    color = if (isSelected) ElectricBlue else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Personalities
        PersonalityMode.entries.forEach { mode ->
            val isSelected = mode == currentPersonality
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) NeonPurple.copy(alpha = 0.25f) else DarkSurfaceElevated.copy(alpha = 0.6f))
                .border(
                    1.dp,
                    if (isSelected) NeonPurple else GlassBorder,
                    RoundedCornerShape(16.dp)
                )
                .clickable { onSelectPersonality(mode) }
                .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "${mode.emoji} ${mode.title}",
                    color = if (isSelected) Color.White else TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun QuickPromptPills(
    language: AppLanguage,
    personality: PersonalityMode,
    onPromptSelected: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    val prompts = when (language) {
        AppLanguage.TELUGU -> listOf(
            "ఒక మంచి కథ చెప్పు",
            "తెలుగులో మంచి జోక్ చెప్పు",
            "ఈ రోజు నా మూడ్ బాగోలేదు, ఉత్సాహం ఇవ్వు",
            "చదువులో ఏకాగ్రత ఎలా సాధించాలి?",
            "BGMI గేమ్ టిప్స్ చెప్పు",
            "నా జ్ఞాపకాలను గుర్తుంచుకున్నావా?"
        )
        AppLanguage.HINDI -> listOf(
            "एक मजेदार कहानी सुनाओ",
            "एक बढ़िया चुटकुला सुनाओ",
            "आज का मोटिवेशनल विचार क्या है?",
            "पढ़ाई में फोकस कैसे बढ़ाएं?",
            "गेमिंग के बेस्ट टिप्स बताओ",
            "मेरी पसंदीदा चीजें क्या हैं?"
        )
        AppLanguage.ENGLISH -> when (personality) {
            PersonalityMode.STUDY_HELPER -> listOf(
                "Explain Quantum Computing in simple terms",
                "How to build strong study habits?",
                "Give me a 5-question science quiz",
                "Tips to speak fluent English"
            )
            PersonalityMode.GAMER_BUDDY -> listOf(
                "Best clutch strategies in BGMI / Valorant",
                "What's your all-time favorite game lore?",
                "Tell me a funny gamer joke",
                "Give me gamer hype motivation!"
            )
            PersonalityMode.MOTIVATOR -> listOf(
                "Give me a powerful morning motivation",
                "How to overcome laziness and procrastination?",
                "Share a mindset rule for success",
                "Cheer me up, had a tough day"
            )
            PersonalityMode.FUNNY_COMPANION -> listOf(
                "Tell me a hilarious witty joke",
                "Roast me in a friendly way",
                "What's the funniest thing you know?",
                "Make me laugh right now"
            )
            PersonalityMode.BEST_FRIEND -> listOf(
                "Tell me an intriguing mystery story",
                "What should I do this weekend?",
                "How was your day, Abhi?",
                "Help me practice my conversation"
            )
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        prompts.forEach { prompt ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkPurpleBg.copy(alpha = 0.85f))
                    .border(0.8.dp, GlassBorder, RoundedCornerShape(14.dp))
                    .clickable { onPromptSelected(prompt) }
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "💡 $prompt",
                    color = BrightCyan,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun BottomInputBar(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    isListening: Boolean,
    onMicClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .imePadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Voice microphone button with glowing state
        IconButton(
            onClick = onMicClick,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    if (isListening) {
                        Brush.radialGradient(listOf(BrightCyan, ElectricBlue))
                    } else {
                        Brush.linearGradient(listOf(DeepViolet, DeepPurpleCard))
                    }
                )
                .border(
                    1.5.dp,
                    if (isListening) BrightCyan else GlassBorder,
                    CircleShape
                )
                .testTag("chat_mic_button")
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                contentDescription = if (isListening) "Stop Recording" else "Speak to AI",
                tint = if (isListening) Color.Black else ElectricBlue,
                modifier = Modifier.size(22.dp)
            )
        }

        // Text input field
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChanged,
            placeholder = {
                Text(
                    text = if (isListening) "Listening... speak now" else "Chat with your AI friend...",
                    color = TextMuted,
                    fontSize = 14.sp
                )
            },
            modifier = Modifier
                .weight(1f)
                .testTag("chat_text_input"),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = GlassBorder,
                focusedContainerColor = DarkSurfaceElevated,
                unfocusedContainerColor = DarkSurfaceElevated,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )

        // Send Button
        IconButton(
            onClick = onSend,
            enabled = inputText.isNotBlank(),
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(
                    if (inputText.isNotBlank()) {
                        Brush.linearGradient(listOf(ElectricBlue, NeonPurple))
                    } else {
                        Brush.linearGradient(listOf(DeepPurpleCard, PremiumBlack))
                    }
                )
                .testTag("chat_send_button")
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send Message",
                tint = if (inputText.isNotBlank()) Color.White else TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
