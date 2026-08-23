package com.example.ui.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryTeal

@Composable
fun ShelterPeakIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(32.dp)) {
        val w = size.width
        val h = size.height
        
        // Let's draw the paw print elements.
        // Four toe pads (circles/ovals) on top
        // Leftmost toe (Toe 1)
        drawOval(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.10f, h * 0.28f),
            size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.20f)
        )
        // Inner left toe (Toe 2)
        drawOval(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.30f, h * 0.14f),
            size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.24f)
        )
        // Inner right toe (Toe 3)
        drawOval(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.14f),
            size = androidx.compose.ui.geometry.Size(w * 0.18f, h * 0.24f)
        )
        // Rightmost toe (Toe 4)
        drawOval(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.28f),
            size = androidx.compose.ui.geometry.Size(w * 0.16f, h * 0.20f)
        )
        
        // Large central pad (metacarpal pad)
        // A curved shape at the bottom
        val mainPadPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.42f)
            cubicTo(w * 0.28f, h * 0.40f, w * 0.20f, h * 0.58f, w * 0.22f, h * 0.76f)
            cubicTo(w * 0.24f, h * 0.88f, w * 0.38f, h * 0.92f, w * 0.50f, h * 0.88f)
            cubicTo(w * 0.62f, h * 0.92f, w * 0.76f, h * 0.88f, w * 0.78f, h * 0.76f)
            cubicTo(w * 0.80f, h * 0.58f, w * 0.72f, h * 0.40f, w * 0.5f, h * 0.42f)
            close()
        }
        drawPath(
            path = mainPadPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        
        // The heartbeat line cuts horizontally through the center of the main pad
        // It's a horizontal line starting on the left, pulsing in the center, and ending on the right.
        val heartbeatPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.04f, h * 0.63f)
            lineTo(w * 0.35f, h * 0.63f)
            
            // Pulse: slight down, sharp up, deep down, back to baseline
            lineTo(w * 0.40f, h * 0.72f)
            lineTo(w * 0.46f, h * 0.42f)
            lineTo(w * 0.52f, h * 0.82f)
            lineTo(w * 0.58f, h * 0.52f)
            lineTo(w * 0.62f, h * 0.63f)
            
            lineTo(w * 0.96f, h * 0.63f)
        }
        
        // Draw the heartbeat stroke with PrimaryTeal so it "cuts out" of the white paw print
        drawPath(
            path = heartbeatPath,
            color = PrimaryTeal, // Blend perfectly with bottom bar's background
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 3.2.dp.toPx(),
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

@Composable
fun VetPeakIcon(modifier: Modifier = Modifier, color: Color = Color.White) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(32.dp)) {
        val w = size.width
        val h = size.height
        
        // 1. Draw the Head/Face background (Solid color)
        val facePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.34f, h * 0.54f)
            lineTo(w * 0.66f, h * 0.54f)
            // Beautiful rounded chin
            quadraticBezierTo(w * 0.66f, h * 0.76f, w * 0.50f, h * 0.76f)
            quadraticBezierTo(w * 0.34f, h * 0.76f, w * 0.34f, h * 0.54f)
            close()
        }
        drawPath(
            path = facePath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        
        // 2. Draw the Hair/Cap on top (solid color)
        val hairPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.50f, h * 0.30f) // Start at top center
            // Left side dome
            quadraticBezierTo(w * 0.28f, h * 0.32f, w * 0.28f, h * 0.54f)
            lineTo(w * 0.34f, h * 0.54f)
            // Left bangs
            quadraticBezierTo(w * 0.38f, h * 0.44f, w * 0.48f, h * 0.44f)
            // Center small parting
            lineTo(w * 0.50f, h * 0.48f)
            lineTo(w * 0.52f, h * 0.44f)
            // Right bangs
            quadraticBezierTo(w * 0.62f, h * 0.44f, w * 0.66f, h * 0.54f)
            lineTo(w * 0.72f, h * 0.54f)
            // Right side dome
            quadraticBezierTo(w * 0.72f, h * 0.32f, w * 0.50f, h * 0.30f)
            close()
        }
        drawPath(
            path = hairPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Fill
        )
        
        // 3. Draw Eyes as perfect round cutouts using PrimaryTeal
        drawCircle(
            color = PrimaryTeal,
            radius = 1.4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(w * 0.43f, h * 0.61f)
        )
        drawCircle(
            color = PrimaryTeal,
            radius = 1.4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(w * 0.57f, h * 0.61f)
        )
        
        // 4. Draw Left Ear Pad
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.12f, h * 0.54f),
            size = androidx.compose.ui.geometry.Size(w * 0.09f, h * 0.18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, h * 0.04f)
        )
        
        // 5. Draw Right Ear Pad
        drawRoundRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.79f, h * 0.54f),
            size = androidx.compose.ui.geometry.Size(w * 0.09f, h * 0.18f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.04f, h * 0.04f)
        )
        
        // 6. Draw Headset Band (Arch over the head)
        val bandPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.165f, h * 0.56f)
            cubicTo(
                w * 0.15f, h * 0.14f, // control point 1
                w * 0.85f, h * 0.14f, // control point 2
                w * 0.835f, h * 0.56f // end point
            )
        }
        drawPath(
            path = bandPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 3.dp.toPx(),
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
        
        // 7. Draw Microphone Arm curving under the chin
        val micPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.835f, h * 0.72f)
            quadraticBezierTo(
                w * 0.82f, h * 0.86f,
                w * 0.54f, h * 0.86f
            )
            lineTo(w * 0.44f, h * 0.86f)
        }
        drawPath(
            path = micPath,
            color = color,
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.5.dp.toPx(),
                join = androidx.compose.ui.graphics.StrokeJoin.Round,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        )
    }
}

class CustomBottomBarShape : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = androidx.compose.ui.graphics.Path().apply {
            val width = size.width
            val height = size.height
            val peakHeight = 28f * density.density
            val peakWidth = 84f * density.density
            
            val leftPeakCenter = width * 0.18f
            val rightPeakCenter = width * 0.82f
            
            moveTo(0f, peakHeight)
            
            // Left peak
            lineTo(leftPeakCenter - peakWidth / 2, peakHeight)
            cubicTo(
                leftPeakCenter - peakWidth * 0.35f, peakHeight,
                leftPeakCenter - peakWidth * 0.18f, 0f,
                leftPeakCenter, 0f
            )
            cubicTo(
                leftPeakCenter + peakWidth * 0.18f, 0f,
                leftPeakCenter + peakWidth * 0.35f, peakHeight,
                leftPeakCenter + peakWidth / 2, peakHeight
            )
            
            // Flat line connecting to right peak
            lineTo(rightPeakCenter - peakWidth / 2, peakHeight)
            
            // Right peak
            cubicTo(
                rightPeakCenter - peakWidth * 0.35f, peakHeight,
                rightPeakCenter - peakWidth * 0.18f, 0f,
                rightPeakCenter, 0f
            )
            cubicTo(
                rightPeakCenter + peakWidth * 0.18f, 0f,
                rightPeakCenter + peakWidth * 0.35f, peakHeight,
                rightPeakCenter + peakWidth / 2, peakHeight
            )
            
            lineTo(width, peakHeight)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

@Composable
fun RowScope.CustomTabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: String,
    isProfile: Boolean = false
) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (selected) {
                        Modifier
                            .background(Color(0xFFFD761A), RoundedCornerShape(20.dp))
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    } else {
                        Modifier.padding(vertical = 6.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        
        Spacer(modifier = Modifier.height(2.dp))
        
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else Color.White.copy(alpha = 0.7f)
        )
    }
}
