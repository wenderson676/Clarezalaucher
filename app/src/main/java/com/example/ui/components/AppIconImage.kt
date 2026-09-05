package com.example.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.launcher.AppItem

@Composable
fun AppIconImage(
    app: AppItem?,
    defaultLabel: String = "",
    modifier: Modifier = Modifier,
    iconSize: Dp = 48.dp,
    shapeRadius: Dp = 16.dp
) {
    val drawable = app?.icon
    val label = app?.label ?: defaultLabel

    if (drawable != null) {
        val bitmap = remember(drawable) {
            try {
                drawable.toBitmap(width = 144, height = 144).asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
        if (bitmap != null) {
            Image(
                bitmap = bitmap,
                contentDescription = label,
                modifier = modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(shapeRadius))
            )
            return
        }
    }

    // High quality fallback avatar
    val letter = label.firstOrNull()?.uppercaseChar() ?: 'A'
    val backgroundColor = remember(label) {
        val palette = listOf(
            Color(0xFF1E88E5), // Vivid Blue
            Color(0xFF43A047), // Vivid Emerald
            Color(0xFF8E24AA), // Vivid Purple
            Color(0xFFE64A19), // Vivid Orange
            Color(0xFF00897B), // Vivid Teal
            Color(0xFFD81B60), // Vivid Pink
            Color(0xFF3949AB), // Vivid Indigo
            Color(0xFFFB8C00)  // Vivid Amber
        )
        val hash = kotlin.math.abs(label.hashCode())
        palette[hash % palette.size]
    }

    Box(
        modifier = modifier
            .size(iconSize)
            .clip(RoundedCornerShape(shapeRadius))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = letter.toString(),
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            )
        )
    }
}
