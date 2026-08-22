package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.model.AIProviderType
import com.example.ai.model.LanguageMode
import com.example.data.local.entity.ToolExecutionEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.GlowingButton
import com.example.ui.components.NovaHeader
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NovaCyberGradient
import com.example.ui.theme.NovaHotGradient
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NovaViewModel

@Composable
fun ProfileAdminScreen(
    viewModel: NovaViewModel
) {
    val context = LocalContext.current
    val languageMode by viewModel.selectedLanguageMode.collectAsState()
    val providerType by viewModel.selectedProviderType.collectAsState()
    val customKey by viewModel.customApiKey.collectAsState()
    val localUrl by viewModel.localServerUrl.collectAsState()
    val conversations by viewModel.conversations.collectAsState()
    val projects by viewModel.projects.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val toolLogs by viewModel.toolExecutions.collectAsState()

    var keyInput by remember(customKey) { mutableStateOf(customKey) }
    var urlInput by remember(localUrl) { mutableStateOf(localUrl) }
    var showProviderMenu by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .testTag("profile_admin_container"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            NovaHeader(
                title = "Settings & Admin",
                subtitle = "AI Model Engine & Diagnostics",
                currentLanguage = languageMode,
                onLanguageSelected = { viewModel.setLanguagePreference(it) }
            )
        }

        // Profile Identity Card
        item {
            val currentUser by viewModel.currentUser.collectAsState()
            val isGuest = currentUser?.isGuest ?: false

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(20.dp))
                    .background(SurfaceDark)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(NovaHotGradient)
                                    .padding(2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clip(CircleShape)
                                        .background(SurfaceElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (currentUser?.displayName?.take(1) ?: "N").uppercase(),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonPink
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentUser?.displayName ?: "Cosmic Explorer",
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isGuest) NeonPurple.copy(alpha = 0.3f) else Color(0x3300FF9D))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (isGuest) "GUEST TRIAL" else "PRO STUDIO",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isGuest) NeonCyan else Color(0xFF00FF9D)
                                        )
                                    }
                                }
                                Text(
                                    text = currentUser?.email ?: "Personal AI Agent • Bengali / Hindi / English",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }

                        // Auth Action button
                        if (isGuest) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x3300E5FF),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.openAuthDialog("Access Cloud Sync & Studio Pro") }
                            ) {
                                Text(
                                    text = "Sign In",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonCyan,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0x33FF007A),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.logout() }
                            ) {
                                Text(
                                    text = "Log Out",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonPink,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        ProfileStatItem(title = "Conversations", value = "${conversations.size}", color = NeonCyan)
                        ProfileStatItem(title = "Studio Arts", value = "${projects.size}", color = NeonPurple)
                        ProfileStatItem(title = "Memories", value = "${memories.size}", color = NeonPink)
                        ProfileStatItem(title = "Tool Runs", value = "${toolLogs.size}", color = Color(0xFF00FF9D))
                    }
                }
            }
        }

        // AI Provider Architecture Configuration
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    text = "AI Model Engine & Provider",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                CyberCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Selected AI Architecture",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // Provider dropdown button
                        Box {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = ObsidianBg,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showProviderMenu = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Dns, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = providerType.displayName,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    }
                                    Text("Change ▼", fontSize = 11.sp, color = NeonCyan)
                                }
                            }

                            DropdownMenu(
                                expanded = showProviderMenu,
                                onDismissRequest = { showProviderMenu = false },
                                modifier = Modifier.background(SurfaceElevated)
                            ) {
                                AIProviderType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = type.displayName,
                                                color = if (type == providerType) NeonCyan else TextPrimary,
                                                fontWeight = if (type == providerType) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            viewModel.setAIProvider(type)
                                            showProviderMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Gemini API Key Input
                        Text(
                            text = "Google Gemini API Key",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = keyInput,
                            onValueChange = { keyInput = it },
                            placeholder = { Text("Enter Gemini API Key (or leave blank for default)", fontSize = 12.sp, color = TextMuted) },
                            leadingIcon = { Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = NeonPurple) },
                            trailingIcon = {
                                TextButton(
                                    onClick = {
                                        viewModel.setCustomApiKey(keyInput)
                                        Toast.makeText(context, "API Key saved successfully!", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("Save", color = NeonCyan, fontWeight = FontWeight.Bold)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = ObsidianBg,
                                unfocusedContainerColor = ObsidianBg,
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Local Server URL (if selected)
                        if (providerType != AIProviderType.GEMINI_DIRECT) {
                            Text(
                                text = "Self-Hosted / Local Server Base URL",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = urlInput,
                                onValueChange = { urlInput = it },
                                placeholder = { Text("http://localhost:8080/v1", fontSize = 12.sp, color = TextMuted) },
                                leadingIcon = { Icon(imageVector = Icons.Default.Cloud, contentDescription = null, tint = NeonCyan) },
                                trailingIcon = {
                                    TextButton(
                                        onClick = {
                                            viewModel.setLocalServerUrl(urlInput)
                                            Toast.makeText(context, "Local Server URL saved!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("Save", color = NeonCyan, fontWeight = FontWeight.Bold)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = ObsidianBg,
                                    unfocusedContainerColor = ObsidianBg,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }

        // Live Tool Logs & System Health
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Tool Execution Logs",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${toolLogs.size} Events",
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (toolLogs.isEmpty()) {
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "No tool executions yet. Try using Calculator, Date/Time, or Web Search in chat.",
                            fontSize = 12.sp,
                            color = TextMuted,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }

        items(toolLogs.take(5)) { log ->
            ToolExecutionLogRow(log = log)
        }

        // App Information & Version
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NOVA AI Studio v3.5 Enterprise",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = "Powered by Google Gemini 3.5 & 2.5 Flash Image",
                    fontSize = 11.sp,
                    color = TextMuted
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Offline SQLite Room DB • Bengali & Hindi NLU • Secure Token Architecture",
                    fontSize = 10.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
fun ProfileStatItem(
    title: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun ToolExecutionLogRow(log: ToolExecutionEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            .background(SurfaceDark)
            .padding(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(NeonCyan.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = log.toolName, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = log.inputJson.take(28) + "...", fontSize = 11.sp, color = TextPrimary)
                }
            }
            Text(
                text = "${log.durationMs}ms",
                fontSize = 10.sp,
                color = TextSecondary
            )
        }
    }
}
