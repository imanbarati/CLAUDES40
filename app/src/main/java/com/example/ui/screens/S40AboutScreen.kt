package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.S40Theme

@Composable
fun S40AboutScreen(
    theme: S40Theme,
    modifier: Modifier = Modifier
) {
    val primaryColor = Color(theme.primaryHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Nokia S40 & Claude Badge
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(primaryColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "S40",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Claude S40 for Android",
            color = Color(0xFF0F172A),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Ported for Samsung Galaxy A53",
            color = primaryColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Card: Original Project Info
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Original Project: emir/claude-s40",
                    color = Color(0xFF1E293B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Created by Turkish developer Emir Karşıyakalı (@emir on GitHub). The original project connected a 2007 Nokia 6300 phone (8 MB RAM, MIDP 2.0 / CLDC 1.1) to Claude via a lightweight Go proxy that bridged legacy TLS 1.0 to modern Anthropic HTTPS APIs in 8 KiB chunks.",
                color = Color(0xFF475569),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Card: Galaxy A53 Android Port Features
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhoneAndroid,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Galaxy A53 Native Features",
                    color = Color(0xFF1E293B),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            FeaturePoint("📱 Dual Mode", "Switch between Nokia 6300 tactile frame and Fullscreen S40 OS.")
            FeaturePoint("⌨️ Authentic T9 Multitap", "Keypad with rapid multi-tap character cycling, timer commit, and QWERTY toggle.")
            FeaturePoint("🔊 Nokia Soundscapes", "Synthesized dialpad clicks, softkey prompt beeps, and the classic Morse code SMS ringtone.")
            FeaturePoint("📳 Tactile Haptics", "Haptic pulse feedback calibrated for Galaxy A53 vibrator motor.")
            FeaturePoint("📖 240x320 Paginated Reading Mode", "Page through detailed Claude essays in small digestible screens.")
            FeaturePoint("⚡ Message Actions", "Shorten, simplify (ELI5), translate (TR/EN), pin, and save to memory card.")
            FeaturePoint("🌐 Live Web Search", "Search the web and cite sources below responses.")
            FeaturePoint("🔒 Direct API Keys", "Support for your Anthropic Claude API key, Gemini, or custom Go proxy server.")
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Version 1.0.0 • Designed for Galaxy A53",
            color = Color(0xFF94A3B8),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun FeaturePoint(title: String, desc: String) {
    Column {
        Text(
            text = title,
            color = Color(0xFF0F172A),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = desc,
            color = Color(0xFF64748B),
            fontSize = 10.sp
        )
    }
}
