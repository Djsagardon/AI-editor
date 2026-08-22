package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.model.LanguageMode
import com.example.data.local.entity.CreativeProjectEntity
import com.example.data.local.entity.TemplateEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.NovaHeader
import com.example.ui.navigation.Screen
import com.example.ui.theme.DeepSpace
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.NeonPink
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NovaCyberGradient
import com.example.ui.theme.NovaGradient
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceDark
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.NovaViewModel

@Composable
fun HomeScreen(
    viewModel: NovaViewModel,
    onNavigate: (Screen) -> Unit,
    onOpenTemplate: (TemplateEntity) -> Unit
) {
    val languageMode by viewModel.selectedLanguageMode.collectAsState()
    val templates by viewModel.templates.collectAsState()
    val projects by viewModel.projects.collectAsState()

    var quickInputText by remember { mutableStateOf("") }

    val quickActionPrompts = listOf(
        "একটা cinematic photo বানিয়ে দাও but backgroundটা sunset হবে",
        "এক চমৎকার কলকাতা শহরের সাইবারপাঙ্ক ছবি বানাও",
        "Explain Quantum Computing in simple Bengali & English",
        "Calculate 15% discount on ₹4,500 + 18% GST",
        "What is the latest today's weather & tech updates?"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .testTag("home_screen_container"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            NovaHeader(
                currentLanguage = languageMode,
                onLanguageSelected = { lang ->
                    viewModel.setLanguagePreference(lang)
                },
                onProfileClick = {
                    onNavigate(Screen.PROFILE_ADMIN)
                }
            )
        }

        // Hero Banner: "Create. Ask. Imagine."
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(
                        1.dp,
                        Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.6f), NeonPurple.copy(alpha = 0.3f), Color.Transparent)),
                        RoundedCornerShape(20.dp)
                    )
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF131B2E), Color(0xFF0F1422), Color(0xFF1B112E))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00FF9D))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "NOVA AGENT 3.5 ONLINE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00FF9D),
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Create. Ask. Imagine.",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your multilingual personal AI agent for Bengali, Hindi & English conversations, studio creative generation and deep task orchestration.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Omnibox Quick AI Input
                    OutlinedTextField(
                        value = quickInputText,
                        onValueChange = { quickInputText = it },
                        placeholder = {
                            Text(
                                text = "What can I help you with? (বাং / हि / Eng)",
                                fontSize = 13.sp,
                                color = TextMuted
                            )
                        },
                        trailingIcon = {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(NovaGradient)
                                    .clickable {
                                        if (quickInputText.isNotBlank()) {
                                            viewModel.sendMessage(quickInputText)
                                            quickInputText = ""
                                            onNavigate(Screen.CHAT)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Send prompt",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_quick_ai_input"),
                        shape = RoundedCornerShape(14.dp),
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

        // Quick Action Hub Grid
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = "AI Studio Capabilities",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionHubCard(
                        title = "Ask AI",
                        desc = "Multilingual Chat",
                        icon = Icons.Default.Chat,
                        accentColor = NeonCyan,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(Screen.CHAT)
                    }
                    QuickActionHubCard(
                        title = "Create Image",
                        desc = "AI Studio Art",
                        icon = Icons.Default.Image,
                        accentColor = NeonPurple,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(Screen.STUDIO)
                    }
                    QuickActionHubCard(
                        title = "Voice AI",
                        desc = "Speech Agent",
                        icon = Icons.Default.Mic,
                        accentColor = NeonPink,
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(Screen.VOICE)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    QuickActionHubCard(
                        title = "Edit Photo",
                        desc = "Non-Destructive",
                        icon = Icons.Default.Tune,
                        accentColor = Color(0xFF00FF9D),
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(Screen.STUDIO)
                    }
                    QuickActionHubCard(
                        title = "Web Search",
                        desc = "Live Intelligence",
                        icon = Icons.Default.Search,
                        accentColor = Color(0xFFFFB703),
                        modifier = Modifier.weight(1f)
                    ) {
                        viewModel.sendMessage("Search live information on current trends")
                        onNavigate(Screen.CHAT)
                    }
                    QuickActionHubCard(
                        title = "Knowledge Base",
                        desc = "Memory & Files",
                        icon = Icons.Default.Description,
                        accentColor = Color(0xFF0077FF),
                        modifier = Modifier.weight(1f)
                    ) {
                        onNavigate(Screen.MEMORY)
                    }
                }
            }
        }

        // Multilingual Prompt Suggestions
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "⚡ Instant Multilingual Prompts",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quickActionPrompts) { prompt ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SurfaceElevated,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.sendMessage(prompt)
                                    onNavigate(Screen.CHAT)
                                }
                        ) {
                            Text(
                                text = prompt,
                                fontSize = 12.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Popular Ready-Made Templates
        item {
            Column(modifier = Modifier.padding(vertical = 12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Popular AI Templates",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "View All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan,
                        modifier = Modifier.clickable { onNavigate(Screen.TEMPLATES) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(templates.take(6)) { template ->
                        TemplateCardItem(template = template) {
                            onOpenTemplate(template)
                        }
                    }
                }
            }
        }

        // Recent Creations Grid / Gallery
        if (projects.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Recent Studio Projects",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    projects.take(4).forEach { project ->
                        ProjectItemRow(project = project) {
                            viewModel.loadProjectVersions(project.id)
                            onNavigate(Screen.STUDIO)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionHubCard(
    title: String,
    desc: String,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(14.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = desc,
                fontSize = 10.sp,
                color = TextSecondary,
                maxLines = 1
            )
        }
    }
}

@Composable
fun TemplateCardItem(
    template: TemplateEntity,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable { onClick() }
    ) {
        Column {
            // Gradient Header Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                NeonCyan.copy(alpha = 0.7f),
                                NeonPurple.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(8.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ObsidianBg.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = template.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonCyan
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = template.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = template.description,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    lineHeight = 14.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Aspect ${template.aspectRatio}",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                    Text(
                        text = "Use →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonPurple
                    )
                }
            }
        }
    }
}

@Composable
fun ProjectItemRow(
    project: CreativeProjectEntity,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NovaGradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Image,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = project.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${project.stylePreset} • ${project.aspectRatio} • ${project.quality}",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }

            Text(
                text = "Open",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = NeonCyan
            )
        }
    }
}
