package com.example.ui.components

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.model.ChatMessage
import com.example.model.S40Theme

@Composable
fun ReadingModeView(
    message: ChatMessage,
    currentPage: Int,
    totalPages: Int,
    theme: S40Theme,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    onExitReading: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fontSizeSp by remember { mutableIntStateOf(14) }

    val pageSize = 400
    val startIdx = currentPage * pageSize
    val endIdx = minOf(message.content.length, startIdx + pageSize)
    val pageText = if (startIdx < message.content.length) {
        message.content.substring(startIdx, endIdx)
    } else {
        message.content
    }

    val primaryColor = Color(theme.primaryHex)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Toolbar: Page Indicator + Font Size Toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E8F0))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📖 Page ${currentPage + 1} of $totalPages",
                color = Color(0xFF1E293B),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            // Font size controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFCBD5E1))
                        .clickable { if (fontSizeSp > 11) fontSizeSp -= 2 }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "A-", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFFCBD5E1))
                        .clickable { if (fontSizeSp < 20) fontSizeSp += 2 }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "A+", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Reading Content Body
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(6.dp))
                .padding(12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = pageText,
                color = Color(0xFF0F172A),
                fontSize = fontSizeSp.sp,
                lineHeight = (fontSizeSp + 7).sp,
                fontFamily = FontFamily.SansSerif
            )
        }

        // Bottom Navigation Bar: Prev | Exit | Next
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(primaryColor)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Prev Page
            Row(
                modifier = Modifier
                    .clickable(enabled = currentPage > 0, onClick = onPrevPage)
                    .padding(vertical = 4.dp)
                    .testTag("reading_prev"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Page",
                    tint = if (currentPage > 0) Color.White else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Prev",
                    color = if (currentPage > 0) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Exit Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.2f))
                    .clickable(onClick = onExitReading)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .testTag("reading_exit")
            ) {
                Text(
                    text = "Close",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Next Page
            Row(
                modifier = Modifier
                    .clickable(enabled = currentPage < totalPages - 1, onClick = onNextPage)
                    .padding(vertical = 4.dp)
                    .testTag("reading_next"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Next",
                    color = if (currentPage < totalPages - 1) Color.White else Color.White.copy(alpha = 0.4f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Page",
                    tint = if (currentPage < totalPages - 1) Color.White else Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
