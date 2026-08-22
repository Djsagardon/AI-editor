package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// NOVA Dark Obsidian & Cyber Futuristic Palette
val DeepSpace = Color(0xFF07090E)
val ObsidianBg = Color(0xFF0C101A)
val SurfaceDark = Color(0xFF131826)
val SurfaceElevated = Color(0xFF1B2236)
val SurfaceBorder = Color(0xFF26314D)
val GlassSurface = Color(0xCC131826)

// Neon & Electric Accents
val NeonCyan = Color(0xFF00E5FF)
val NeonPurple = Color(0xFF9D4EDD)
val NeonPink = Color(0xFFFF007A)
val ElectricBlue = Color(0xFF0077FF)
val NeonGreen = Color(0xFF00FF9D)
val NeonAmber = Color(0xFFFFB703)
val NeonCoral = Color(0xFFFF5470)

// Text & Neutral Colors
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextTertiary = Color(0xFF64748B)
val TextMuted = Color(0xFF475569)

// Gradients
val NovaGradient = Brush.linearGradient(
    colors = listOf(NeonCyan, NeonPurple)
)

val NovaHotGradient = Brush.linearGradient(
    colors = listOf(NeonPurple, NeonPink)
)

val NovaCyberGradient = Brush.linearGradient(
    colors = listOf(NeonCyan, ElectricBlue, NeonPurple)
)

val CardGlowGradient = Brush.linearGradient(
    colors = listOf(Color(0x3300E5FF), Color(0x119D4EDD), Color(0x00000000))
)

// Legacy Material Palette mapping
val Purple80 = NeonPurple
val PurpleGrey80 = Color(0xFF8B9BB4)
val Pink80 = NeonPink

val Purple40 = Color(0xFF7B2CBF)
val PurpleGrey40 = Color(0xFF4A5568)
val Pink40 = Color(0xFFC70039)
