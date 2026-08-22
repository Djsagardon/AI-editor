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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TemplateEntity
import com.example.ui.components.GlowingButton
import com.example.ui.components.NovaHeader
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

@Composable
fun TemplatesScreen(
    viewModel: NovaViewModel,
    onNavigateToStudio: () -> Unit
) {
    val templates by viewModel.templates.collectAsState()
    val languageMode by viewModel.selectedLanguageMode.collectAsState()
    val selectedCategory by viewModel.selectedTemplateCategory.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTemplateForDialog by remember { mutableStateOf<TemplateEntity?>(null) }
    var variableInputs by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var showAdminAddDialog by remember { mutableStateOf(false) }

    val categories = listOf(
        "All", "Wedding", "Cinematic", "YouTube Thumbnail", "Product Advertisement",
        "Festival", "Professional Headshot", "Instagram Post", "Logo Concept"
    )

    val filteredTemplates = templates.filter { tpl ->
        val matchesCategory = (selectedCategory == "All" || tpl.category.equals(selectedCategory, ignoreCase = true))
        val matchesSearch = searchQuery.isBlank() || tpl.name.contains(searchQuery, ignoreCase = true) || tpl.description.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepSpace)
                .testTag("templates_screen_container"),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            item {
                NovaHeader(
                    title = "AI Template Studio",
                    subtitle = "1-Click Cinematic & Commercial Presets",
                    currentLanguage = languageMode,
                    onLanguageSelected = { viewModel.setLanguagePreference(it) }
                )
            }

            // Search Bar
            item {
                Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search templates (e.g. Wedding, Cyberpunk, YouTube)...", fontSize = 13.sp, color = TextMuted) },
                        leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        singleLine = true
                    )
                }
            }

            // Category Chips Row
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) NeonCyan else SurfaceDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) NeonCyan else SurfaceBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTemplateCategory(cat) }
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) DeepSpace else TextPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // Templates Count Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Available Templates (${filteredTemplates.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }

            // Template Cards List
            items(filteredTemplates) { template ->
                TemplateListItemCard(
                    template = template,
                    onUse = {
                        selectedTemplateForDialog = template
                        // Initialize placeholder variable fields
                        val variables = extractVariables(template.promptTemplate)
                        variableInputs = variables.associateWith { "" }
                    }
                )
            }
        }

        // Add Template FAB (Admin)
        FloatingActionButton(
            onClick = { showAdminAddDialog = true },
            containerColor = NeonPurple,
            contentColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Template")
        }
    }

    // Template Customization Dialog
    selectedTemplateForDialog?.let { tpl ->
        AlertDialog(
            onDismissRequest = { selectedTemplateForDialog = null },
            title = {
                Text(
                    text = "Configure Template: ${tpl.name}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = tpl.description,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val variables = extractVariables(tpl.promptTemplate)
                    if (variables.isNotEmpty()) {
                        Text(
                            text = "Fill in details:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        variables.forEach { varName ->
                            OutlinedTextField(
                                value = variableInputs[varName] ?: "",
                                onValueChange = { newVal ->
                                    variableInputs = variableInputs.toMutableMap().apply { put(varName, newVal) }
                                },
                                label = { Text(varName.replace("_", " ").capitalize(), fontSize = 11.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = ObsidianBg,
                                    unfocusedContainerColor = ObsidianBg,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                )
                            )
                        }
                    } else {
                        Text(
                            text = "Ready to generate with optimal settings (${tpl.aspectRatio})",
                            fontSize = 12.sp,
                            color = Color(0xFF00FF9D)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.applyTemplate(tpl, variableInputs)
                        selectedTemplateForDialog = null
                        onNavigateToStudio()
                    }
                ) {
                    Text("Apply & Open in Studio", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedTemplateForDialog = null }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceElevated
        )
    }

    // Admin Add Custom Template Dialog
    if (showAdminAddDialog) {
        var newTplName by remember { mutableStateOf("") }
        var newTplCategory by remember { mutableStateOf("Cinematic") }
        var newTplDesc by remember { mutableStateOf("") }
        var newTplPrompt by remember { mutableStateOf("") }
        var newTplAspect by remember { mutableStateOf("1:1") }

        AlertDialog(
            onDismissRequest = { showAdminAddDialog = false },
            title = { Text("Add Custom Studio Template", color = TextPrimary) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTplName,
                        onValueChange = { newTplName = it },
                        label = { Text("Template Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newTplCategory,
                        onValueChange = { newTplCategory = it },
                        label = { Text("Category") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = newTplPrompt,
                        onValueChange = { newTplPrompt = it },
                        label = { Text("Prompt Template (use {variable})") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary),
                        minLines = 3
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newTplName.isNotBlank() && newTplPrompt.isNotBlank()) {
                            viewModel.createTemplateAdmin(
                                name = newTplName,
                                description = newTplDesc.ifBlank { "Custom user template" },
                                category = newTplCategory,
                                promptTemplate = newTplPrompt,
                                aspectRatio = newTplAspect,
                                isPremium = false
                            )
                            showAdminAddDialog = false
                        }
                    }
                ) {
                    Text("Save Template", color = NeonCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAdminAddDialog = false }) {
                    Text("Cancel", color = TextMuted)
                }
            },
            containerColor = SurfaceElevated
        )
    }
}

@Composable
fun TemplateListItemCard(
    template: TemplateEntity,
    onUse: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .background(SurfaceDark)
            .clickable { onUse() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NeonPurple.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = template.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Aspect ${template.aspectRatio}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                if (template.isPremium) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0x33FFB703))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = "⭐ PRO", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB703))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = template.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = template.description,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Used ${template.usageCount} times",
                    fontSize = 11.sp,
                    color = TextMuted
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0x3300E5FF),
                    modifier = Modifier.clip(RoundedCornerShape(8.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Use Template", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                    }
                }
            }
        }
    }
}

private fun extractVariables(templateStr: String): List<String> {
    val regex = Regex("\\{([a-zA-Z0-9_]+)\\}")
    return regex.findAll(templateStr).map { it.groupValues[1] }.distinct().toList()
}
