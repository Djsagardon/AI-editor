package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ai.model.LanguageMode
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.navigation.Screen
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NovaGradient
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NovaViewModel
import com.example.voice.VoiceState

@Composable
fun VoiceScreen(
    viewModel: NovaViewModel,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val voiceState by viewModel.voiceManager.voiceState.collectAsState()
    val spokenText by viewModel.voiceManager.spokenText.collectAsState()
    val rmsLevel by viewModel.voiceManager.audioRmsLevel.collectAsState()
    val languageMode by viewModel.selectedLanguageMode.collectAsState()

    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (isGranted) {
            viewModel.voiceManager.startListening(LanguageMode.fromCode(languageMode))
        } else {
            Toast.makeText(context, "Microphone permission is required for Voice Agent", Toast.LENGTH_SHORT).show()
        }
    }

    // Orb Pulse Animation
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .padding(20.dp)
            .testTag("voice_screen_container"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NOVA Voice Intelligence",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            IconButton(
                onClick = {
                    viewModel.voiceManager.stopListening()
                    viewModel.voiceManager.stopSpeaking()
                    onClose()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Central Holographic Glowing Orb
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .scale(if (voiceState == VoiceState.LISTENING) (pulseScale + rmsLevel * 0.3f) else 1f),
                contentAlignment = Alignment.Center
            ) {
                // Outer Glow Ring 1
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (voiceState == VoiceState.LISTENING) NeonCyan.copy(alpha = 0.25f) else NeonPurple.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Middle Ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .border(
                            2.dp,
                            Brush.linearGradient(listOf(NeonCyan, NeonPink)),
                            CircleShape
                        )
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    if (voiceState == VoiceState.LISTENING) NeonCyan.copy(alpha = 0.4f) else NeonPurple.copy(alpha = 0.3f),
                                    ObsidianBg
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (voiceState == VoiceState.SPEAKING) Icons.Default.VolumeUp else Icons.Default.Mic,
                        contentDescription = "Voice State",
                        tint = if (voiceState == VoiceState.LISTENING) NeonCyan else Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // State Badge
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = SurfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder)
            ) {
                Text(
                    text = when (voiceState) {
                        VoiceState.LISTENING -> "🎙️ Listening to you in $languageMode..."
                        VoiceState.PROCESSING -> "🧠 Reasoning response..."
                        VoiceState.SPEAKING -> "🔊 Speaking..."
                        VoiceState.MUTED -> "🔇 Muted"
                        VoiceState.ERROR -> "⚠️ Voice unavailable"
                        VoiceState.IDLE -> "Tap mic to talk in বাংলা / हिन्दी / English"
                    },
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = when (voiceState) {
                        VoiceState.LISTENING -> NeonCyan
                        VoiceState.PROCESSING -> NeonPurple
                        VoiceState.SPEAKING -> Color(0xFF00FF9D)
                        else -> TextSecondary
                    },
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Real-time Audio Waveform
            AudioWaveformVisualizer(
                rmsLevel = rmsLevel,
                isListening = voiceState == VoiceState.LISTENING,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Live Transcription View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceDark)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (spokenText.isNotBlank()) "\"$spokenText\"" else "Speak your question or prompt freely...",
                    fontSize = 14.sp,
                    color = if (spokenText.isNotBlank()) TextPrimary else TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }

        // Bottom Controls Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute / Stop speech
            IconButton(
                onClick = { viewModel.voiceManager.stopSpeaking() },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeOff,
                    contentDescription = "Stop speech",
                    tint = TextSecondary
                )
            }

            // Central Push-To-Talk Mic Action
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(NovaGradient)
                    .clickable {
                        if (!hasAudioPermission) {
                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        } else {
                            if (voiceState == VoiceState.LISTENING) {
                                viewModel.voiceManager.stopListening()
                            } else {
                                viewModel.voiceManager.startListening(LanguageMode.fromCode(languageMode))
                            }
                        }
                    }
                    .testTag("voice_talk_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (voiceState == VoiceState.LISTENING) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mic Toggle",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // Language Switcher shortcut
            IconButton(
                onClick = {
                    val nextLang = when (languageMode) {
                        "AUTO" -> LanguageMode.BENGALI
                        "BN" -> LanguageMode.HINDI
                        "HI" -> LanguageMode.ENGLISH
                        else -> LanguageMode.AUTO
                    }
                    viewModel.setLanguagePreference(nextLang)
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(SurfaceElevated)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Switch language",
                    tint = NeonCyan
                )
            }
        }
    }
}
