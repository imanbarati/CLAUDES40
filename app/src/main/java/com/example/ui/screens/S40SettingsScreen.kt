package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ApiProvider
import com.example.model.AppSettings
import com.example.model.KeyboardMode
import com.example.model.S40Theme
import com.example.model.S40ViewMode

@Composable
fun S40SettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var geminiKey by remember { mutableStateOf(settings.geminiApiKey) }
    var provider by remember { mutableStateOf(settings.apiProvider) }
    var modelName by remember { mutableStateOf(settings.modelName) }
    var proxyUrl by remember { mutableStateOf(settings.proxyUrl) }
    var language by remember { mutableStateOf(settings.language) }
    var theme by remember { mutableStateOf(settings.theme) }
    var viewMode by remember { mutableStateOf(settings.viewMode) }
    var keyboardMode by remember { mutableStateOf(settings.keyboardMode) }
    var soundEnabled by remember { mutableStateOf(settings.soundEnabled) }
    var vibrateEnabled by remember { mutableStateOf(settings.vibrateEnabled) }
    var webSearchEnabled by remember { mutableStateOf(settings.webSearchEnabled) }
    var systemNotes by remember { mutableStateOf(settings.systemNotes) }

    val primaryColor = Color(theme.primaryHex)

    fun applyChange(newSettings: AppSettings) {
        onSaveSettings(newSettings)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        // Section: AI Connectivity
        SectionHeader(title = "AI Engine & Connectivity", icon = Icons.Default.Key, color = primaryColor)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Provider:",
                color = Color(0xFF1E293B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            // Provider selection pills
            val providers = listOf(
                Pair(ApiProvider.BUILTIN_SMART, "Built-in Smart Engine"),
                Pair(ApiProvider.ANTHROPIC, "Anthropic Claude API"),
                Pair(ApiProvider.GEMINI, "Google Gemini API"),
                Pair(ApiProvider.CUSTOM_PROXY, "S40 Go Proxy Server")
            )

            providers.forEach { (p, label) ->
                val isSelected = provider == p
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else Color(0xFFF8FAFC))
                        .border(
                            1.dp,
                            if (isSelected) primaryColor else Color(0xFFE2E8F0),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            provider = p
                            applyChange(settings.copy(apiProvider = p))
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Tune,
                        contentDescription = null,
                        tint = if (isSelected) primaryColor else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = label,
                        color = if (isSelected) primaryColor else Color(0xFF1E293B),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            if (provider == ApiProvider.ANTHROPIC) {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        applyChange(settings.copy(apiKey = it))
                    },
                    label = { Text("Anthropic API Key (sk-ant-...)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_anthropic_key_input"),
                    singleLine = true
                )

                // Model picker
                Text(
                    text = "Model:",
                    color = Color(0xFF1E293B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                val models = listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022", "claude-3-7-sonnet-20250219")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    models.forEach { m ->
                        val isSelected = modelName == m
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) primaryColor else Color(0xFFE2E8F0))
                                .clickable {
                                    modelName = m
                                    applyChange(settings.copy(modelName = m))
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (m.contains("haiku")) "Haiku" else if (m.contains("3-7")) "3.7 Sonnet" else "3.5 Sonnet",
                                color = if (isSelected) Color.White else Color(0xFF1E293B),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else if (provider == ApiProvider.GEMINI) {
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = {
                        geminiKey = it
                        applyChange(settings.copy(geminiApiKey = it))
                    },
                    label = { Text("Gemini API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else if (provider == ApiProvider.CUSTOM_PROXY) {
                OutlinedTextField(
                    value = proxyUrl,
                    onValueChange = {
                        proxyUrl = it
                        applyChange(settings.copy(proxyUrl = it))
                    },
                    label = { Text("Claude S40 Go Proxy URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Personal Notes for Claude
        SectionHeader(title = "Personal Notes for Claude", icon = Icons.Default.Info, color = primaryColor)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "These notes are prepended to every message sent to Claude (e.g. 'Answer concisely', 'Respond in Turkish'):",
                color = Color(0xFF64748B),
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = systemNotes,
                onValueChange = {
                    systemNotes = it
                    applyChange(settings.copy(systemNotes = it))
                },
                placeholder = { Text("E.g. Keep answers under 150 words. Format with bullet points.") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Retro Themes & Views
        SectionHeader(title = "Display & Themes", icon = Icons.Default.Palette, color = primaryColor)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = "Color Palette:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

            S40Theme.values().forEach { t ->
                val isSelected = theme == t
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else Color(0xFFF8FAFC))
                        .border(
                            1.dp,
                            if (isSelected) primaryColor else Color(0xFFE2E8F0),
                            RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            theme = t
                            applyChange(settings.copy(theme = t))
                        }
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(t.primaryHex))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = t.displayName,
                        color = Color(0xFF1E293B),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            // View Mode
            Text(text = "View Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isDevice = viewMode == S40ViewMode.NOKIA_6300_DEVICE
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isDevice) primaryColor else Color(0xFFE2E8F0))
                        .clickable {
                            viewMode = S40ViewMode.NOKIA_6300_DEVICE
                            applyChange(settings.copy(viewMode = S40ViewMode.NOKIA_6300_DEVICE))
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "📱 Nokia 6300 Frame",
                        color = if (isDevice) Color.White else Color(0xFF1E293B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val isFullscreen = viewMode == S40ViewMode.FULLSCREEN_S40
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isFullscreen) primaryColor else Color(0xFFE2E8F0))
                        .clickable {
                            viewMode = S40ViewMode.FULLSCREEN_S40
                            applyChange(settings.copy(viewMode = S40ViewMode.FULLSCREEN_S40))
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🖥️ Fullscreen S40",
                        color = if (isFullscreen) Color.White else Color(0xFF1E293B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Keyboard Mode
            Text(text = "Default Typing Mode:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isT9 = keyboardMode == KeyboardMode.T9_KEYPAD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isT9) primaryColor else Color(0xFFE2E8F0))
                        .clickable {
                            keyboardMode = KeyboardMode.T9_KEYPAD
                            applyChange(settings.copy(keyboardMode = KeyboardMode.T9_KEYPAD))
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🔢 T9 Multitap Keypad",
                        color = if (isT9) Color.White else Color(0xFF1E293B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val isQwerty = keyboardMode == KeyboardMode.SYSTEM_KEYBOARD
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isQwerty) primaryColor else Color(0xFFE2E8F0))
                        .clickable {
                            keyboardMode = KeyboardMode.SYSTEM_KEYBOARD
                            applyChange(settings.copy(keyboardMode = KeyboardMode.SYSTEM_KEYBOARD))
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⌨️ Android Keyboard",
                        color = if (isQwerty) Color.White else Color(0xFF1E293B),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Audio & Haptics
        SectionHeader(title = "Audio & Haptic Feedback", icon = Icons.Default.VolumeUp, color = primaryColor)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Nokia Key Sounds & SMS Chime", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Retro dialpad tones and Morse SMS tone", color = Color(0xFF64748B), fontSize = 10.sp)
                }
                Switch(
                    checked = soundEnabled,
                    onCheckedChange = {
                        soundEnabled = it
                        applyChange(settings.copy(soundEnabled = it))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tactile Vibration (Galaxy A53)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "Haptic pulse on key click & actions", color = Color(0xFF64748B), fontSize = 10.sp)
                }
                Switch(
                    checked = vibrateEnabled,
                    onCheckedChange = {
                        vibrateEnabled = it
                        applyChange(settings.copy(vibrateEnabled = it))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = primaryColor)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Section: Language
        SectionHeader(title = "Language / Dil", icon = Icons.Default.Settings, color = primaryColor)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val isEn = language == "en"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isEn) primaryColor else Color(0xFFE2E8F0))
                    .clickable {
                        language = "en"
                        applyChange(settings.copy(language = "en"))
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🇬🇧 English",
                    color = if (isEn) Color.White else Color(0xFF1E293B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            val isTr = language == "tr"
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(6.dp))
                .background(if (isTr) primaryColor else Color(0xFFE2E8F0))
                    .clickable {
                        language = "tr"
                        applyChange(settings.copy(language = "tr"))
                    }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🇹🇷 Türkçe",
                    color = if (isTr) Color.White else Color(0xFF1E293B),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.SansSerif
        )
    }
}
