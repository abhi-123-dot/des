package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CompanionSelectScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.VoiceCallScreen
import com.example.ui.theme.DarkPurpleBg
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.DeepPurpleCard
import com.example.ui.theme.DeepViolet
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.PremiumBlack
import com.example.ui.theme.TextMuted
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Audio recording permission launcher
                val audioPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        Toast.makeText(this, "Microphone enabled! You can speak now.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Microphone permission is needed for voice chat.", Toast.LENGTH_LONG).show()
                    }
                }

                fun checkAndRequestAudioPermission() {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                }

                // Handle system back navigation
                if (currentScreen != ScreenDestination.CHAT) {
                    BackHandler {
                        viewModel.navigateTo(ScreenDestination.CHAT)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = PremiumBlack,
                    bottomBar = {
                        // Hide bottom bar during immersive Voice Call mode
                        if (currentScreen != ScreenDestination.VOICE_CALL) {
                            AbhiBottomNavigation(
                                currentScreen = currentScreen,
                                onSelectScreen = { destination ->
                                    if (destination == ScreenDestination.VOICE_CALL) {
                                        checkAndRequestAudioPermission()
                                    }
                                    viewModel.navigateTo(destination)
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = if (currentScreen != ScreenDestination.VOICE_CALL) innerPadding.calculateBottomPadding() else 0.dp)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                ScreenDestination.CHAT -> {
                                    ChatScreen(
                                        viewModel = viewModel,
                                        onRequestRecordAudioPermission = { checkAndRequestAudioPermission() }
                                    )
                                }
                                ScreenDestination.VOICE_CALL -> {
                                    VoiceCallScreen(
                                        viewModel = viewModel,
                                        onRequestRecordAudioPermission = { checkAndRequestAudioPermission() }
                                    )
                                }
                                ScreenDestination.COMPANION_SELECT -> {
                                    CompanionSelectScreen(viewModel = viewModel)
                                }
                                ScreenDestination.MEMORY -> {
                                    MemoryScreen(viewModel = viewModel)
                                }
                                ScreenDestination.PROFILE -> {
                                    ProfileScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AbhiBottomNavigation(
    currentScreen: ScreenDestination,
    onSelectScreen: (ScreenDestination) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(DarkPurpleBg.copy(alpha = 0.95f))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        ElectricBlue.copy(alpha = 0.4f),
                        NeonPurple.copy(alpha = 0.3f),
                        ElectricBlue.copy(alpha = 0.4f)
                    )
                ),
                RoundedCornerShape(26.dp)
            )
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            modifier = Modifier.height(64.dp)
        ) {
            val navItems = listOf(
                Triple(ScreenDestination.CHAT, Icons.Default.ChatBubble, "Chat"),
                Triple(ScreenDestination.VOICE_CALL, Icons.Default.Call, "Voice Call"),
                Triple(ScreenDestination.COMPANION_SELECT, Icons.Default.People, "Companions"),
                Triple(ScreenDestination.MEMORY, Icons.Default.AutoAwesome, "Memory"),
                Triple(ScreenDestination.PROFILE, Icons.Default.Person, "Profile")
            )

            navItems.forEach { (destination, icon, label) ->
                val isSelected = currentScreen == destination
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectScreen(destination) },
                    icon = {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) ElectricBlue else TextMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    },
                    label = {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextMuted,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = DeepViolet.copy(alpha = 0.35f),
                        selectedIconColor = ElectricBlue,
                        unselectedIconColor = TextMuted
                    ),
                    modifier = Modifier.testTag("nav_tab_${destination.name.lowercase()}")
                )
            }
        }
    }
}
