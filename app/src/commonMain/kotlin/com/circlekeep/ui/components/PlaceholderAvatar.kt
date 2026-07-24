package com.circlekeep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun PlaceholderAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = rememberBackgroundColor(name)
    val initials = name.trim().split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .let { parts ->
            when {
                parts.size >= 2 -> "${parts.first().take(1)}${parts.last().take(1)}"
                parts.isNotEmpty() -> parts.first().take(if (parts.first().length >= 2) 2 else 1)
                else -> "?"
            }
        }.uppercase()

    val textColor = if (backgroundColor.luminance() > 0.5f) Color.Black else Color.White

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium.copy(
                color = textColor,
                fontWeight = FontWeight.Bold
            )
        )
    }
}

@Composable
private fun rememberBackgroundColor(name: String): Color {
    val colors = listOf(
        Color(0xFFEF5350), // Red
        Color(0xFFEC407A), // Pink
        Color(0xFFAB47BC), // Purple
        Color(0xFF7E57C2), // Deep Purple
        Color(0xFF5C6BC0), // Indigo
        Color(0xFF42A5F5), // Blue
        Color(0xFF29B6F6), // Light Blue
        Color(0xFF26C6DA), // Cyan
        Color(0xFF26A69A), // Teal
        Color(0xFF66BB6A), // Green
        Color(0xFF9CCC65), // Light Green
        Color(0xFFD4E157), // Lime
        Color(0xFFFFEE58), // Yellow
        Color(0xFFFFCA28), // Amber
        Color(0xFFFFA726), // Orange
        Color(0xFFFF7043), // Deep Orange
    )
    
    val hash = name.trim().lowercase().hashCode()
    val index = kotlin.math.abs(hash) % colors.size
    return colors[index]
}
