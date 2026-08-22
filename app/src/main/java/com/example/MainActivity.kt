package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AuthDialog
import com.example.ui.navigation.Screen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreativeStudioScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryKnowledgeScreen
import com.example.ui.screens.ProfileAdminScreen
import com.example.ui.screens.TemplatesScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NovaGradient
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NovaViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: NovaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                NovaApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun NovaApp(viewModel: NovaViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    val showAuthDialog by viewModel.showAuthDialog.collectAsState()
    val authContext by viewModel.authDialogContext.collectAsState()
    val authError by viewModel.authErrorMessage.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace),
        containerColor = DeepSpace,
        bottomBar = {
            if (currentScreen != Screen.VOICE) {
                NovaBottomNavigationBar(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    Screen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigate = { currentScreen = it },
                        onOpenTemplate = { tpl ->
                            viewModel.applyTemplate(tpl, emptyMap())
                            currentScreen = Screen.STUDIO
                        }
                    )
                    Screen.CHAT -> ChatScreen(
                        viewModel = viewModel,
                        onNavigateToStudio = { currentScreen = Screen.STUDIO },
                        onNavigateToVoice = { currentScreen = Screen.VOICE }
                    )
                    Screen.STUDIO -> CreativeStudioScreen(
                        viewModel = viewModel
                    )
                    Screen.VOICE -> VoiceScreen(
                        viewModel = viewModel,
                        onClose = { currentScreen = Screen.CHAT }
                    )
                    Screen.TEMPLATES -> TemplatesScreen(
                        viewModel = viewModel,
                        onNavigateToStudio = { currentScreen = Screen.STUDIO }
                    )
                    Screen.MEMORY -> MemoryKnowledgeScreen(
                        viewModel = viewModel
                    )
                    Screen.PROFILE_ADMIN -> ProfileAdminScreen(
                        viewModel = viewModel
                    )
                }
            }

            // Global Auth Dialog
            AuthDialog(
                isOpen = showAuthDialog,
                actionContext = authContext,
                errorMessage = authError,
                isLoading = isAuthLoading,
                onDismiss = { viewModel.dismissAuthDialog() },
                onLogin = { email, pass -> viewModel.login(email, pass) },
                onRegister = { name, email, pass -> viewModel.register(name, email, pass) },
                onForgotPassword = { email -> viewModel.forgotPassword(email) },
                onContinueAsGuest = { viewModel.continueAsGuest() }
            )
        }
    }
}

@Composable
fun NovaBottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit
) {
    Surface(
        color = ObsidianBg.copy(alpha = 0.95f),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .testTag("nova_bottom_nav_bar")
        ) {
            val navItems = listOf(
                NavItem(Screen.HOME, "Home", Icons.Default.Home),
                NavItem(Screen.CHAT, "Chat", Icons.Default.Chat),
                NavItem(Screen.STUDIO, "Studio", Icons.Default.Brush),
                NavItem(Screen.TEMPLATES, "Templates", Icons.Default.CollectionsBookmark),
                NavItem(Screen.MEMORY, "Memory", Icons.Default.Psychology),
                NavItem(Screen.PROFILE_ADMIN, "Admin", Icons.Default.Settings)
            )

            navItems.forEach { item ->
                val isSelected = currentScreen == item.screen
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onScreenSelected(item.screen) },
                    icon = {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NeonCyan,
                        selectedTextColor = NeonCyan,
                        indicatorColor = NeonCyan.copy(alpha = 0.15f),
                        unselectedIconColor = TextMuted,
                        unselectedTextColor = TextMuted
                    )
                )
            }
        }
    }
}

data class NavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)
