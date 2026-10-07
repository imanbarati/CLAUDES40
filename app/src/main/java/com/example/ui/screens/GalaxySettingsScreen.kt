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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.api.ClaudeApiService
import com.example.model.ApiProvider
import com.example.model.AppSettings
import com.example.model.DEFAULT_OPEN_SOURCE_MODELS
import com.example.model.GalaxyTheme
import kotlinx.coroutines.launch

@Composable
fun GalaxySettingsScreen(
    settings: AppSettings,
    onSaveSettings: (AppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var apiKey by remember { mutableStateOf(settings.apiKey) }
    var geminiKey by remember { mutableStateOf(settings.geminiApiKey) }
    var provider by remember { mutableStateOf(settings.apiProvider) }
    var modelName by remember { mutableStateOf(settings.modelName) }
    var proxyUrl by remember { mutableStateOf(settings.proxyUrl) }
    var localEndpoint by remember { mutableStateOf(settings.localEndpointUrl) }
    var localModel by remember { mutableStateOf(settings.localModelName) }
    var localGgufPower by remember { mutableStateOf(settings.enableLocalGgufPower) }
    var isTestingConnection by remember { mutableStateOf(false) }
    var testResult by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var theme by remember { mutableStateOf(settings.theme) }
    var soundEnabled by remember { mutableStateOf(settings.soundEnabled) }
    var vibrateEnabled by remember { mutableStateOf(settings.vibrateEnabled) }
    var webSearchEnabled by remember { mutableStateOf(settings.webSearchEnabled) }
    var systemNotes by remember { mutableStateOf(settings.systemNotes) }

    val primaryColor = Color(theme.primaryHex)
    val isDark = theme.isDark
    val screenBg = Color(theme.bgHex)
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)
    val textColor = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)

    fun applyChange(newSettings: AppSettings) {
        onSaveSettings(newSettings)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(screenBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Model & Provider Card
        OneUiSettingsSection(title = "AI Model & Provider", icon = Icons.Default.Key, theme = theme) {
            val providers = listOf(
                Pair(ApiProvider.BUILTIN_SMART, "Built-in Smart Engine"),
                Pair(ApiProvider.LOCAL_NETWORK, "Local GGUF / Network AI (Ollama / NVIDIA / GhostBrain)"),
                Pair(ApiProvider.ANTHROPIC, "Anthropic Claude API"),
                Pair(ApiProvider.GEMINI, "Google Gemini API"),
                Pair(ApiProvider.CUSTOM_PROXY, "S40 Go Proxy Server")
            )

            providers.forEach { (p, label) ->
                val isSelected = provider == p
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                        .border(1.dp, if (isSelected) primaryColor else cardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            provider = p
                            applyChange(settings.copy(apiProvider = p))
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Tune,
                        contentDescription = null,
                        tint = if (isSelected) primaryColor else Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = label,
                        color = if (isSelected) primaryColor else textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            if (provider == ApiProvider.LOCAL_NETWORK) {
                OutlinedTextField(
                    value = localEndpoint,
                    onValueChange = {
                        localEndpoint = it
                        applyChange(settings.copy(localEndpointUrl = it))
                    },
                    label = { Text("Local Server Endpoint URL") },
                    placeholder = { Text("http://192.168.1.100:11434/v1") },
                    modifier = Modifier.fillMaxWidth().testTag("local_endpoint_field"),
                    singleLine = true
                )

                // Quick presets
                Text(
                    text = "Quick Presets:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        Pair("🦙 Ollama", "http://192.168.1.100:11434/v1"),
                        Pair("⚡ llama.cpp", "http://192.168.1.100:8080/v1"),
                        Pair("🟢 NVIDIA", "http://192.168.1.100:8000/v1")
                    )
                    presets.forEach { (name, url) ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (localEndpoint == url) primaryColor.copy(alpha = 0.2f) else if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .border(1.dp, if (localEndpoint == url) primaryColor else cardBorder, RoundedCornerShape(8.dp))
                                .clickable {
                                    localEndpoint = url
                                    applyChange(settings.copy(localEndpointUrl = url))
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                color = if (localEndpoint == url) primaryColor else textColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = localModel,
                    onValueChange = {
                        localModel = it
                        applyChange(settings.copy(localModelName = it))
                    },
                    label = { Text("Model Tag / GGUF File") },
                    placeholder = { Text("llama3.2:latest, deepseek-r1:7b") },
                    modifier = Modifier.fillMaxWidth().testTag("local_model_field"),
                    singleLine = true
                )

                // Test Connection Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isTestingConnection = true
                                testResult = null
                                val res = ClaudeApiService().testLocalConnection(localEndpoint)
                                testResult = res.second
                                isTestingConnection = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isTestingConnection) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Testing...")
                        } else {
                            Icon(imageVector = Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ping Local Host", fontSize = 12.sp)
                        }
                    }
                }

                if (testResult != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (testResult!!.startsWith("✅")) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFEF4444).copy(alpha = 0.15f))
                            .border(1.dp, if (testResult!!.startsWith("✅")) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Text(
                            text = testResult!!,
                            color = if (testResult!!.startsWith("✅")) Color(0xFF10B981) else Color(0xFFEF4444),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (provider == ApiProvider.ANTHROPIC) {
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = {
                        apiKey = it
                        applyChange(settings.copy(apiKey = it))
                    },
                    label = { Text("Anthropic API Key (sk-ant-...)") },
                    modifier = Modifier.fillMaxWidth().testTag("anthropic_key_field"),
                    singleLine = true
                )

                Text(text = "Claude Model:", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)

                val models = listOf(
                    Pair("claude-3-5-sonnet-20241022", "3.5 Sonnet"),
                    Pair("claude-3-5-haiku-20241022", "3.5 Haiku"),
                    Pair("claude-3-7-sonnet-20250219", "3.7 Sonnet")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    models.forEach { (id, name) ->
                        val isSelected = modelName == id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) primaryColor else if (isDark) Color(0xFF0F172A) else Color(0xFFF1F5F9))
                                .border(1.dp, if (isSelected) primaryColor else cardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    modelName = id
                                    applyChange(settings.copy(modelName = id))
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = name,
                                color = if (isSelected) Color.White else textColor,
                                fontSize = 12.sp,
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
                    label = { Text("Proxy URL (e.g. http://10.0.2.2:8080)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

        // Open-Source Models & PirateFace Hub Card
        OneUiSettingsSection(title = "Open-Source Models & PirateFace Hub", icon = Icons.Default.CloudDownload, theme = theme) {
            Text(
                text = "PirateFace (pirateface.co) indexes decentralized BitTorrent mirrors for open-weight models and GGUF quantizations. Connect your local PC / Mac / NVIDIA GPU host (via Ollama, llama.cpp, or vLLM) to run these models privately without API fees.",
                color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            // Local GPU Power Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                    .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Local Network GPU Offload", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Route prompts directly to LAN host ($localEndpoint)",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
                Switch(
                    checked = localGgufPower || provider == ApiProvider.LOCAL_NETWORK,
                    onCheckedChange = { checked ->
                        localGgufPower = checked
                        val newProvider = if (checked) ApiProvider.LOCAL_NETWORK else ApiProvider.BUILTIN_SMART
                        provider = newProvider
                        applyChange(settings.copy(enableLocalGgufPower = checked, apiProvider = newProvider))
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                )
            }

            Text(
                text = "Curated Open-Weights & GGUF Models:",
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )

            DEFAULT_OPEN_SOURCE_MODELS.forEach { model ->
                val isCurrent = localModel.equals(model.ollamaTag, ignoreCase = true)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isCurrent) primaryColor.copy(alpha = 0.08f) else if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                        .border(1.dp, if (isCurrent) primaryColor else cardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = model.name,
                            color = textColor,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(primaryColor.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = model.parameterSize,
                                color = primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Text(
                        text = model.description,
                        color = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B),
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "GGUF: ${model.recommendedGguf}",
                            color = Color(0xFF10B981),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrent) primaryColor else if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                                .clickable {
                                    localModel = model.ollamaTag
                                    provider = ApiProvider.LOCAL_NETWORK
                                    localGgufPower = true
                                    applyChange(settings.copy(
                                        localModelName = model.ollamaTag,
                                        apiProvider = ApiProvider.LOCAL_NETWORK,
                                        enableLocalGgufPower = true
                                    ))
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isCurrent) "Active Model" else "Use with Local Host",
                                color = if (isCurrent) Color.White else textColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Display & One UI Themes Card
        OneUiSettingsSection(title = "Display Themes", icon = Icons.Default.Palette, theme = theme) {
            GalaxyTheme.values().forEach { t ->
                val isSelected = theme == t
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) primaryColor.copy(alpha = 0.12f) else if (isDark) Color(0xFF0F172A) else Color(0xFFF8FAFC))
                        .border(1.dp, if (isSelected) primaryColor else cardBorder, RoundedCornerShape(12.dp))
                        .clickable {
                            theme = t
                            applyChange(settings.copy(theme = t))
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color(t.primaryHex))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = t.displayName,
                        color = textColor,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    if (isSelected) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Feedback & Haptics Card
        OneUiSettingsSection(title = "Haptics & Sound", icon = Icons.Default.VolumeUp, theme = theme) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Samsung Haptic Feedback", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Tactile vibration tuned for Galaxy A53", color = Color(0xFF94A3B8), fontSize = 11.sp)
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

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Soft Notification Tones", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "Subtle message chimes and send feedback", color = Color(0xFF94A3B8), fontSize = 11.sp)
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
        }

        // Custom System Notes Card
        OneUiSettingsSection(title = "Personal Notes for Claude", icon = Icons.Default.Info, theme = theme) {
            Text(
                text = "These instructions are sent with every message (e.g. 'Format answers with bullet points', 'Always reply in Turkish'):",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )
            OutlinedTextField(
                value = systemNotes,
                onValueChange = {
                    systemNotes = it
                    applyChange(settings.copy(systemNotes = it))
                },
                placeholder = { Text("E.g. Be concise and prioritize code examples.") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        // About & Version Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Claude for Samsung Galaxy • v1.1.0",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Optimized for Galaxy A53 5G • Anthropic Claude 3.5 Sonnet",
                color = Color(0xFF64748B),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun OneUiSettingsSection(
    title: String,
    icon: ImageVector,
    theme: GalaxyTheme,
    content: @Composable () -> Unit
) {
    val primaryColor = Color(theme.primaryHex)
    val isDark = theme.isDark
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val cardBorder = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = primaryColor, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(cardBg)
                .border(1.dp, cardBorder, RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            content()
        }
    }
}
