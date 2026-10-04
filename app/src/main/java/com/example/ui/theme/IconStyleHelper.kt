package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.IconStyle

object IconStyleHelper {

    fun getShapeForStyle(style: IconStyle): Shape {
        return when (style) {
            IconStyle.SQUIRCLE -> RoundedCornerShape(18.dp)
            IconStyle.CIRCLE -> CircleShape
            IconStyle.ROUNDED_SQUARE -> RoundedCornerShape(11.dp)
            IconStyle.TEARDROP -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 24.dp, bottomEnd = 6.dp)
            IconStyle.HEXAGON -> CutCornerShape(percent = 26)
            IconStyle.GLASSMORPHISM -> RoundedCornerShape(16.dp)
            IconStyle.NEON_GLOW -> RoundedCornerShape(16.dp)
            IconStyle.NEUMORPHIC -> RoundedCornerShape(16.dp)
        }
    }

    fun getBorderForStyle(style: IconStyle, baseColor: Color): BorderStroke? {
        return when (style) {
            IconStyle.GLASSMORPHISM -> BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f))
            IconStyle.NEON_GLOW -> BorderStroke(2.dp, Brush.linearGradient(listOf(Color(0xFF00E5FF), Color(0xFFFF007F))))
            IconStyle.NEUMORPHIC -> BorderStroke(1.5.dp, Color.White.copy(alpha = 0.35f))
            IconStyle.HEXAGON -> BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
            else -> null
        }
    }

    fun getElevationForStyle(style: IconStyle): Dp {
        return when (style) {
            IconStyle.NEUMORPHIC -> 8.dp
            IconStyle.NEON_GLOW -> 6.dp
            IconStyle.GLASSMORPHISM -> 4.dp
            IconStyle.SQUIRCLE, IconStyle.ROUNDED_SQUARE -> 4.dp
            IconStyle.CIRCLE -> 3.dp
            IconStyle.TEARDROP, IconStyle.HEXAGON -> 4.dp
        }
    }

    fun getBackgroundTint(style: IconStyle, baseColor: Color): Color {
        return when (style) {
            IconStyle.GLASSMORPHISM -> baseColor.copy(alpha = 0.75f)
            else -> baseColor
        }
    }
}
