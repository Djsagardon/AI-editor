package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NovaDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color(0xFF001F29),
    primaryContainer = Color(0xFF004D61),
    onPrimaryContainer = Color(0xFFBCE9FF),
    secondary = NeonPurple,
    onSecondary = Color(0xFF2E004F),
    secondaryContainer = Color(0xFF4E1D74),
    onSecondaryContainer = Color(0xFFE9D5FF),
    tertiary = NeonPink,
    onTertiary = Color(0xFF3E0018),
    tertiaryContainer = Color(0xFF6E0030),
    onTertiaryContainer = Color(0xFFFFD8E4),
    background = DeepSpace,
    onBackground = TextPrimary,
    surface = ObsidianBg,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceDark,
    onSurfaceVariant = TextSecondary,
    surfaceTint = NeonCyan,
    outline = SurfaceBorder,
    outlineVariant = Color(0xFF1E283D),
    error = NeonCoral,
    onError = Color(0xFF37000B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to futuristic dark AI theme
    dynamicColor: Boolean = false, // Keep brand aesthetic consistent
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            dynamicDarkColorScheme(context)
        }
        else -> NovaDarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
