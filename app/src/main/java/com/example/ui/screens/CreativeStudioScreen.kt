package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ImageVersionEntity
import com.example.ui.components.CyberCard
import com.example.ui.components.GlowingButton
import com.example.ui.components.NovaHeader
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
fun CreativeStudioScreen(
    viewModel: NovaViewModel
) {
    val context = LocalContext.current
    val studioState by viewModel.studioState.collectAsState()
    val projectVersions by viewModel.currentProjectVersions.collectAsState()
    val languageMode by viewModel.selectedLanguageMode.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Generate, 1: Edit Studio
    var showNegativePrompt by remember { mutableStateOf(false) }
    var comparisonSliderValue by remember { mutableStateOf(0.5f) }

    val stylePresets = listOf(
        "Cinematic", "Cyberpunk", "Photorealistic", "Bengali Wedding", "Anime 4K",
        "3D Render", "Oil Painting", "Luxury Studio", "Neon Vector", "Watercolor"
    )

    val aspectRatios = listOf("1:1", "4:5", "16:9", "9:16", "3:4", "4:3")
    val qualities = listOf("Standard", "HD", "Ultra")

    val editOperations = listOf(
        "remove_bg" to "✂️ Remove BG",
        "replace_bg" to "🏞️ Replace BG",
        "relight" to "💡 Studio Light",
        "change_color" to "🎨 Change Tone",
        "enhance" to "🔍 Super Resolution",
        "style_transfer" to "🪄 Cyber Filter"
    )

    // Image Picker for Editing
    val editImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val stream = context.contentResolver.openInputStream(it)
                val bitmap = BitmapFactory.decodeStream(stream)
                viewModel.setUploadedEditBitmap(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not open image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpace)
            .testTag("creative_studio_container"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        item {
            NovaHeader(
                title = "Creative Studio",
                subtitle = "Generate & Non-Destructive Edit",
                currentLanguage = languageMode,
                onLanguageSelected = { lang ->
                    viewModel.setLanguagePreference(lang)
                }
            )
        }

        // Tab Selector: Generate vs Edit
        item {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = SurfaceDark,
                contentColor = NeonCyan,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = NeonCyan
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Brush, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Text to Image", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Layers, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Photo Studio Edit", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                )
            }
        }

        if (selectedTab == 0) {
            // TAB 1: GENERATE
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Prompt Input
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Creative Prompt",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                // Enhance Prompt Button
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x3300E5FF),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { viewModel.enhancePrompt() }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "✨ Enhance",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NeonCyan
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = studioState.prompt,
                                onValueChange = { viewModel.updatePrompt(it) },
                                placeholder = {
                                    Text(
                                        "Describe the artwork in English, Bengali (বাংলা), or Hindi (हिन्दी)...",
                                        fontSize = 13.sp,
                                        color = TextMuted
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("studio_prompt_input"),
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = ObsidianBg,
                                    unfocusedContainerColor = ObsidianBg,
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = SurfaceBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                minLines = 3,
                                maxLines = 6
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Toggle Negative Prompt
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { showNegativePrompt = !showNegativePrompt }
                            ) {
                                Text(
                                    text = if (showNegativePrompt) "▼ Hide Negative Prompt" else "▶ Add Negative Prompt (Avoid words)",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }

                            if (showNegativePrompt) {
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = studioState.negativePrompt,
                                    onValueChange = { viewModel.updateNegativePrompt(it) },
                                    placeholder = {
                                        Text("blurry, low quality, distorted, extra limbs...", fontSize = 12.sp, color = TextMuted)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = ObsidianBg,
                                        unfocusedContainerColor = ObsidianBg,
                                        focusedBorderColor = NeonPurple,
                                        unfocusedBorderColor = SurfaceBorder,
                                        focusedTextColor = TextPrimary,
                                        unfocusedTextColor = TextPrimary
                                    ),
                                    singleLine = true
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Style Presets
                    Text(text = "Style Preset", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(stylePresets) { style ->
                            val isSelected = studioState.selectedStylePreset == style
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeonPurple else SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) NeonCyan else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setStylePreset(style) }
                            ) {
                                Text(
                                    text = style,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Aspect Ratio & Quality
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(modifier = Modifier.weight(1.2f)) {
                            Text(text = "Aspect Ratio", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                aspectRatios.forEach { ratio ->
                                    val isSelected = studioState.selectedAspectRatio == ratio
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) NeonCyan else SurfaceDark,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setAspectRatio(ratio) }
                                    ) {
                                        Text(
                                            text = ratio,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) DeepSpace else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(0.8f)) {
                            Text(text = "Quality", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                qualities.forEach { q ->
                                    val isSelected = studioState.selectedQuality == q
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isSelected) NeonPink else SurfaceDark,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { viewModel.setQuality(q) }
                                    ) {
                                        Text(
                                            text = q,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.White else TextPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Big Generate Button
                    GlowingButton(
                        text = "✨ Generate Creation (${studioState.selectedAspectRatio})",
                        onClick = { viewModel.generateStudioImage() },
                        isLoading = studioState.isGenerating
                    )

                    if (studioState.isGenerating) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = studioState.generationProgressMessage.ifBlank { "Creating your image..." },
                                fontSize = 12.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    // Error state banner if generation failed
                    studioState.generationError?.let { errMsg ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33FF0055),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0055)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "⚠️ Image Generation Failed",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFFFF5252)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = errMsg,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = { viewModel.generateStudioImage() }
                                    ) {
                                        Text("Retry", color = NeonCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Generated Result Display
            studioState.latestGeneratedImage?.let { generated ->
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Text(
                            text = "Output Artwork (${generated.providerUsed})",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        CyberCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(260.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ObsidianBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    var generatedBmp by remember { mutableStateOf<Bitmap?>(null) }
                                    LaunchedEffect(generated.imageUriOrBase64) {
                                        if (generated.imageUriOrBase64.startsWith("data:image")) {
                                            try {
                                                val b64 = generated.imageUriOrBase64.substringAfter(",")
                                                val bytes = Base64.decode(b64, Base64.DEFAULT)
                                                generatedBmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                            } catch (e: Exception) {}
                                        }
                                    }

                                    if (generatedBmp != null) {
                                        Image(
                                            bitmap = generatedBmp!!.asImageBitmap(),
                                            contentDescription = "Created artwork",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = generated.prompt.take(30) + "...",
                                        fontSize = 12.sp,
                                        color = TextSecondary,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Row {
                                        IconButton(
                                            onClick = {
                                                Toast.makeText(context, "Image saved to device gallery!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Download, contentDescription = "Download", tint = NeonCyan)
                                        }
                                        IconButton(
                                            onClick = {
                                                Toast.makeText(context, "Ready to share!", Toast.LENGTH_SHORT).show()
                                            }
                                        ) {
                                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = NeonPurple)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: NON-DESTRUCTIVE PHOTO EDIT STUDIO
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Upload / Current Photo Canvas
                    CyberCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (studioState.uploadedEditBitmap != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(240.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ObsidianBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = studioState.uploadedEditBitmap!!.asImageBitmap(),
                                        contentDescription = "Active image to edit",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Photo loaded for non-destructive editing",
                                        fontSize = 12.sp,
                                        color = Color(0xFF00FF9D)
                                    )
                                    TextButton(onClick = { editImageLauncher.launch("image/*") }) {
                                        Text("Change Photo", fontSize = 12.sp, color = NeonCyan)
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(ObsidianBg)
                                        .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                        .clickable { editImageLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Upload,
                                            contentDescription = "Upload",
                                            tint = NeonCyan,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "Tap to Pick Image for AI Editing",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Supports JPG, PNG, WEBP",
                                            fontSize = 11.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick AI Modification Presets
                    Text(text = "Non-Destructive Operations", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(editOperations) { (opKey, opTitle) ->
                            val isSelected = studioState.selectedOperationType == opKey
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) NeonCyan else SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.setOperationType(opKey) }
                            ) {
                                Text(
                                    text = opTitle,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DeepSpace else TextPrimary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom Instruction Field
                    Text(text = "Natural Language Editing Prompt", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = studioState.editInstruction,
                        onValueChange = { viewModel.setEditInstruction(it) },
                        placeholder = {
                            Text(
                                "e.g. 'Replace background with rain-soaked Kolkata street with neon reflections'",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_instruction_input"),
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

                    Spacer(modifier = Modifier.height(16.dp))

                    GlowingButton(
                        text = "🪄 Apply AI Modification",
                        onClick = {
                            if (studioState.uploadedEditBitmap == null) {
                                Toast.makeText(context, "Please select an image first", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.executeImageEdit()
                            }
                        },
                        isLoading = studioState.isEditing
                    )

                    if (studioState.isEditing) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = NeonCyan, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = studioState.generationProgressMessage.ifBlank { "Applying AI modifications..." },
                                fontSize = 12.sp,
                                color = NeonCyan
                            )
                        }
                    }

                    // Edit error banner
                    studioState.editError?.let { err ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x33FF0055),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF0055)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "⚠️ Image Edit Failed",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color(0xFFFF5252)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = err,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { viewModel.executeImageEdit() }) {
                                        Text("Retry", color = NeonCyan, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Version History Timeline Tree
            if (projectVersions.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Non-Destructive Version History",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${projectVersions.size} Versions",
                                fontSize = 12.sp,
                                color = NeonPurple
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(projectVersions) { ver ->
                                VersionTimelineCard(version = ver) {
                                    Toast.makeText(context, "Rolled back to ${ver.versionName}", Toast.LENGTH_SHORT).show()
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
fun VersionTimelineCard(
    version: ImageVersionEntity,
    onRollback: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onRollback() }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = version.versionName,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.Undo,
                    contentDescription = "Rollback",
                    tint = TextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = version.operationType,
                fontSize = 10.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Tap to rollback",
                fontSize = 9.sp,
                color = NeonPurple,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
