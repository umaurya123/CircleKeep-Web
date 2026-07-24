package com.circlekeep.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Preview(showBackground = true, widthDp = 1024, heightDp = 500)
@Composable
fun PlayStoreFeatureImage() {
    // Feature image requirements: 1024 x 500
    Box(
        modifier = Modifier
            .size(width = 1024.dp, height = 500.dp)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6200EE), Color(0xFF3700B3))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background decorative circles
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 300f,
                center = androidx.compose.ui.geometry.Offset(100f, 100f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.05f),
                radius = 400f,
                center = androidx.compose.ui.geometry.Offset(900f, 400f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(48.dp),
            modifier = Modifier.padding(horizontal = 64.dp)
        ) {
            // Large Brand Icon
            Box(modifier = Modifier.size(240.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = size.width / 2
                    val outerRadius = size.width * 0.4f
                    
                    // Draw the outer "Circle" ring
                    drawCircle(
                        color = Color.White,
                        radius = outerRadius,
                        style = Stroke(width = size.width * 0.04f)
                    )
                    
                    // Draw the three dots (The Inner Circle)
                    val dotRadius = size.width * 0.08f
                    val dotDistance = outerRadius * 0.55f
                    
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

            // App Name and Tagline
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "CircleKeep",
                    color = Color.White,
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Your Personal Relationship Manager",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Manage family details, birthdays, and milestones.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 20.sp
                )
            }
        }
    }
}
