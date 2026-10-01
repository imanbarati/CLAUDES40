package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun NokiaKeypad(
    isCaps: Boolean,
    isNumberMode: Boolean,
    onDigitPress: (Char) -> Unit,
    onBackspace: () -> Unit,
    onSpace: () -> Unit,
    onToggleCaps: () -> Unit,
    onToggleNumberMode: () -> Unit,
    onLeftSoftKey: () -> Unit,
    onRightSoftKey: () -> Unit,
    onCenterNaviKey: () -> Unit,
    onSwitchToSystemKeyboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Nokia 6300 metallic silver and dark bezel palette
    val keypadBg = Color(0xFF1E242B)
    val buttonBg = Brush.verticalGradient(
        colors = listOf(Color(0xFF424A54), Color(0xFF282F38))
    )
    val naviRingBg = Brush.radialGradient(
        colors = listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8))
    )
    val textColor = Color(0xFFF1F5F9)
    val subTextColor = Color(0xFF94A3B8)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(keypadBg)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP CLUSTER: Softkeys, Call, End & 5-way D-pad (Navi-key)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: LSK + Call Key
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Left Softkey (Options)
                Box(
                    modifier = Modifier
                        .size(width = 62.dp, height = 30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(buttonBg)
                        .border(1.dp, Color(0xFF64748B), RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = onLeftSoftKey
                        )
                        .testTag("keypad_lsk"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Options",
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Call Key (Green)
                Box(
                    modifier = Modifier
                        .size(width = 62.dp, height = 30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF166534))
                        .border(1.dp, Color(0xFF22C55E), RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = onCenterNaviKey
                        )
                        .testTag("keypad_call"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Send Call",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Center: Nokia 5-Way Navi-Key (Outer Ring + Center Button)
            Box(
                modifier = Modifier
                    .size(86.dp)
                    .clip(CircleShape)
                    .background(naviRingBg)
                    .border(2.dp, Color(0xFF64748B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // Center Action Button
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E293B))
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = onCenterNaviKey
                        )
                        .testTag("keypad_center_navi"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send/Select",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // D-Pad directional indicators
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Up",
                    tint = Color(0xFF334155),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .size(14.dp)
                        .padding(top = 2.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Down",
                    tint = Color(0xFF334155),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .size(14.dp)
                        .padding(bottom = 2.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Left",
                    tint = Color(0xFF334155),
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .size(14.dp)
                        .padding(start = 2.dp)
                )
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Right",
                    tint = Color(0xFF334155),
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(14.dp)
                        .padding(end = 2.dp)
                )
            }

            // Right Column: RSK + End Key
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Right Softkey (Back)
                Box(
                    modifier = Modifier
                        .size(width = 62.dp, height = 30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(buttonBg)
                        .border(1.dp, Color(0xFF64748B), RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = onRightSoftKey
                        )
                        .testTag("keypad_rsk"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Back",
                        color = textColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // End Key (Red)
                Box(
                    modifier = Modifier
                        .size(width = 62.dp, height = 30.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF991B1B))
                        .border(1.dp, Color(0xFFEF4444), RoundedCornerShape(6.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = onBackspace
                        )
                        .testTag("keypad_end"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CallEnd,
                        contentDescription = "End Call/Clear",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Quick Mode Bar: Caps indicator, Number mode, Soft Keyboard toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                // Caps Lock indicator
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isCaps) Color(0xFF0284C7) else Color(0xFF334155))
                        .clickable(onClick = onToggleCaps)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isCaps) "ABC" else "abc",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // 123 Number Mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isNumberMode) Color(0xFF0284C7) else Color(0xFF334155))
                        .clickable(onClick = onToggleNumberMode)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "123",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Switch to Android Soft Keyboard button
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF334155))
                    .clickable(onClick = onSwitchToSystemKeyboard)
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Keyboard,
                    contentDescription = "System Keyboard",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "QWERTY",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 12-KEY ALPHANUMERIC GRID
        val keys = listOf(
            Triple('1', "1", ".,-?!"),
            Triple('2', "2", "abc"),
            Triple('3', "3", "def"),
            Triple('4', "4", "ghi"),
            Triple('5', "5", "jkl"),
            Triple('6', "6", "mno"),
            Triple('7', "7", "pqrs"),
            Triple('8', "8", "tuv"),
            Triple('9', "9", "wxyz"),
            Triple('*', "*", "⇧ / +"),
            Triple('0', "0", "␣ space"),
            Triple('#', "#", "⌫ del")
        )

        for (row in 0 until 4) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.5.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (col in 0 until 3) {
                    val index = row * 3 + col
                    val (digit, primary, sub) = keys[index]

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(buttonBg)
                            .border(1.dp, Color(0xFF475569), RoundedCornerShape(8.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(),
                                onClick = {
                                    when (digit) {
                                        '*' -> onToggleCaps()
                                        '0' -> onSpace()
                                        '#' -> onBackspace()
                                        else -> onDigitPress(digit)
                                    }
                                }
                            )
                            .testTag("keypad_btn_$digit"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = primary,
                                color = textColor,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.SansSerif
                            )
                            Text(
                                text = sub,
                                color = subTextColor,
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
