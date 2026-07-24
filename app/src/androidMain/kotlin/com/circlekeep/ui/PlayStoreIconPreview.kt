package com.circlekeep.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Preview(showBackground = true, widthDp = 512, heightDp = 512)
@Composable
fun PlayStoreIcon() {
    // A high-quality representation of the CircleKeep brand
    Box(
        modifier = Modifier
            .size(512.dp)
            .background(Color(0xFF6200EE)) // Using the purple color mentioned
    ) {
        Canvas(modifier = Modifier.size(512.dp)) {
            val center = size.width / 2
            val outerRadius = size.width * 0.35f
            val innerRadius = size.width * 0.28f
            
            // Draw the outer "Circle" ring
            drawCircle(
                color = Color.White,
                radius = outerRadius,
                center = centerOffset(size.width, size.height),
                style = Stroke(width = size.width * 0.05f)
            )
            
            // Draw the three dots (The Inner Circle)
            val dotRadius = size.width * 0.07f
            val dotDistance = outerRadius * 0.5f
            
            // Top Dot
            drawCircle(
                color = Color.White,
                radius = dotRadius,
                center = androidx.compose.ui.geometry.Offset(center, center - dotDistance)
            )
            // Bottom Left Dot
            drawCircle(
                color = Color.White,
                radius = dotRadius,
                center = androidx.compose.ui.geometry.Offset(center - dotDistance, center + dotDistance * 0.5f)
            )
            // Bottom Right Dot
            drawCircle(
                color = Color.White,
                radius = dotRadius,
                center = androidx.compose.ui.geometry.Offset(center + dotDistance, center + dotDistance * 0.5f)
            )
        }
    }
}

fun centerOffset(w: Float, h: Float) = androidx.compose.ui.geometry.Offset(w/2, h/2)
