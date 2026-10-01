package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.S40Theme
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NokiaStatusBar(
    theme: S40Theme,
    title: String,
    modifier: Modifier = Modifier
) {
    var currentTime by remember {
        mutableStateOf(SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(10000)
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        }
    }

    val primaryColor = Color(theme.primaryHex)
    val onPrimaryColor = Color.White

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(primaryColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Cellular Antenna Signal (5 bars) + Network Type
            Row(verticalAlignment = Alignment.Bottom) {
                // 5 vertical signal bars
                for (i in 1..5) {
                    val barHeight = (3 + i * 2.2).dp
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 0.8.dp)
                            .width(2.5.dp)
                            .height(barHeight)
                            .background(onPrimaryColor.copy(alpha = if (i <= 4) 1f else 0.4f))
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "5G",
                    color = onPrimaryColor,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Center: Screen Title
            Text(
                text = title,
                color = onPrimaryColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif,
                maxLines = 1
            )

            // Right: Time + Battery Meter (4 bars inside frame)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = currentTime,
                    color = onPrimaryColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(6.dp))

                // Battery Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(onPrimaryColor.copy(alpha = 0.25f), RoundedCornerShape(2.dp))
                        .padding(horizontal = 2.dp, vertical = 2.dp)
                ) {
                    for (b in 1..4) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 0.5.dp)
                                .size(width = 2.5.dp, height = 7.dp)
                                .background(if (b <= 3) Color(0xFF66BB6A) else onPrimaryColor.copy(alpha = 0.3f))
                        )
                    }
                    // Battery terminal nub
                    Box(
                        modifier = Modifier
                            .size(width = 1.dp, height = 3.dp)
                            .background(onPrimaryColor.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}
