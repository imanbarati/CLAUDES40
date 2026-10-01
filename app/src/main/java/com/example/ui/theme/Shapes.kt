package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Samsung One UI Corner Radii Design System
val OneUiShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),      // Chips, small tags
    medium = RoundedCornerShape(20.dp),     // Buttons, inner containers
    large = RoundedCornerShape(26.dp),      // One UI Cards, Dialogs
    extraLarge = RoundedCornerShape(30.dp)  // One UI Floating Pills, Bottom Sheets
)
