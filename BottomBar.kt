package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryTeal
import kotlin.math.atan2

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

/**
 * Returns the exact contour path of the two ears on the bottom bar
 */
fun getBottomBarEarsPath(
    width: Float,
    density: Float,
    yOffset: Float = 0f
): Path {
    val peakHeight = 28f * density
    val peakWidth = 84f * density

    val leftPeakCenter = width * 0.18f
    val rightPeakCenter = width * 0.82f

    return Path().apply {
        moveTo(0f, peakHeight + yOffset)

        // Left peak (Left Ear)
        lineTo(leftPeakCenter - peakWidth / 2, peakHeight + yOffset)
        cubicTo(
            leftPeakCenter - peakWidth * 0.35f, peakHeight + yOffset,
            leftPeakCenter - peakWidth * 0.18f, yOffset,
            leftPeakCenter, yOffset
        )
        cubicTo(
            leftPeakCenter + peakWidth * 0.18f, yOffset,
            leftPeakCenter + peakWidth * 0.35f, peakHeight + yOffset,
            leftPeakCenter + peakWidth / 2, peakHeight + yOffset
        )

        // Connecting bridge between ears
        lineTo(rightPeakCenter - peakWidth / 2, peakHeight + yOffset)

        // Right peak (Right Ear)
        cubicTo(
            rightPeakCenter - peakWidth * 0.35f, peakHeight + yOffset,
            rightPeakCenter - peakWidth * 0.18f, yOffset,
            rightPeakCenter, yOffset
        )
        cubicTo(
            rightPeakCenter + peakWidth * 0.18f, yOffset,
            rightPeakCenter + peakWidth * 0.35f, peakHeight + yOffset,
            rightPeakCenter + peakWidth / 2, peakHeight + yOffset
        )

        lineTo(width, peakHeight + yOffset)
    }
}

class CustomBottomBarShape : androidx.compose.ui.graphics.Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: androidx.compose.ui.unit.Density
    ): androidx.compose.ui.graphics.Outline {
        val path = getBottomBarEarsPath(size.width, density.density).apply {
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        return androidx.compose.ui.graphics.Outline.Generic(path)
    }
}

/**
 * Draws a tiny running baby cheetah at the head of the video progress line.
 * Features cheetah's tawny gold body, black spots, running paws animation, cute rounded ears,
 * facial tear stripes, and a curved animated tail.
 */
fun DrawScope.drawTinyRunningCheetah(
    position: Offset,
    tangentAngleDeg: Float,
    runCycle: Float, // 0f..1f running phase
    isPaused: Boolean
) {
    // We scale everything around 0,0 then translate and rotate
    val cheetahScale = 0.95f

    rotate(degrees = tangentAngleDeg, pivot = position) {
        val cx = position.x
        val cy = position.y - 6f // sit right atop the progress line

        // Leg cycle oscillations
        val legPhase = if (isPaused) 0f else runCycle * 2f * Math.PI.toFloat()
        val legSwingFront1 = kotlin.math.sin(legPhase) * 4.5f
        val legSwingFront2 = kotlin.math.sin(legPhase + Math.PI.toFloat()) * 4.5f
        val legSwingBack1 = kotlin.math.sin(legPhase + Math.PI.toFloat() * 0.5f) * 4.5f
        val legSwingBack2 = kotlin.math.sin(legPhase + Math.PI.toFloat() * 1.5f) * 4.5f
        val bodyBob = if (isPaused) 0f else kotlin.math.sin(legPhase * 2f) * 1.2f

        val cheetahGold = Color(0xFFF59E0B) // Amber/Tawny cheetah coat
        val cheetahLightGold = Color(0xFFFDE68A) // Belly/chest lighter fur
        val cheetahSpots = Color(0xFF1F1A14) // Classic black cheetah spots

        // --- Back Legs (Drawn behind body) ---
        // Back Left Leg
        drawLine(
            color = cheetahGold,
            start = Offset(cx - 7f * cheetahScale, cy + 2f * cheetahScale + bodyBob),
            end = Offset(cx - 10f * cheetahScale - legSwingBack1, cy + 8f * cheetahScale),
            strokeWidth = 2.0f * cheetahScale,
            cap = StrokeCap.Round
        )
        // Front Left Leg
        drawLine(
            color = cheetahGold,
            start = Offset(cx + 6f * cheetahScale, cy + 2f * cheetahScale + bodyBob),
            end = Offset(cx + 8f * cheetahScale + legSwingFront1, cy + 8f * cheetahScale),
            strokeWidth = 2.0f * cheetahScale,
            cap = StrokeCap.Round
        )

        // --- Animated Cheetah Tail (Long with black tip) ---
        val tailWag = if (isPaused) 0f else kotlin.math.sin(legPhase) * 2.5f
        val tailPath = Path().apply {
            moveTo(cx - 10f * cheetahScale, cy - 1f * cheetahScale + bodyBob)
            quadraticTo(
                cx - 15f * cheetahScale,
                cy - 6f * cheetahScale + tailWag,
                cx - 18f * cheetahScale,
                cy - 2f * cheetahScale + tailWag
            )
        }
        drawPath(
            path = tailPath,
            color = cheetahGold,
            style = Stroke(width = 1.8f * cheetahScale, cap = StrokeCap.Round)
        )
        // Black tip of tail
        drawCircle(
            color = cheetahSpots,
            radius = 1.2f * cheetahScale,
            center = Offset(cx - 18f * cheetahScale, cy - 2f * cheetahScale + tailWag)
        )

        // --- Sleek Athletic Cheetah Body ---
        val bodyRect = androidx.compose.ui.geometry.Rect(
            left = cx - 10f * cheetahScale,
            top = cy - 4f * cheetahScale + bodyBob,
            right = cx + 8f * cheetahScale,
            bottom = cy + 4f * cheetahScale + bodyBob
        )
        drawOval(
            color = cheetahGold,
            topLeft = Offset(bodyRect.left, bodyRect.top),
            size = androidx.compose.ui.geometry.Size(bodyRect.width, bodyRect.height)
        )
        // Light belly underbelly
        drawOval(
            color = cheetahLightGold,
            topLeft = Offset(cx - 5f * cheetahScale, cy + 0.5f * cheetahScale + bodyBob),
            size = androidx.compose.ui.geometry.Size(8f * cheetahScale, 3f * cheetahScale)
        )

        // Distinctive Cheetah Spots on body
        drawCircle(color = cheetahSpots, radius = 0.9f * cheetahScale, center = Offset(cx - 6f * cheetahScale, cy - 1.5f * cheetahScale + bodyBob))
        drawCircle(color = cheetahSpots, radius = 0.8f * cheetahScale, center = Offset(cx - 3f * cheetahScale, cy + 0.5f * cheetahScale + bodyBob))
        drawCircle(color = cheetahSpots, radius = 0.9f * cheetahScale, center = Offset(cx, cy - 2f * cheetahScale + bodyBob))
        drawCircle(color = cheetahSpots, radius = 0.8f * cheetahScale, center = Offset(cx + 3f * cheetahScale, cy - 0.5f * cheetahScale + bodyBob))

        // --- Fore Legs (In front of body) ---
        // Back Right Leg
        drawLine(
            color = cheetahGold,
            start = Offset(cx - 5f * cheetahScale, cy + 2f * cheetahScale + bodyBob),
            end = Offset(cx - 8f * cheetahScale + legSwingBack2, cy + 8f * cheetahScale),
            strokeWidth = 2.2f * cheetahScale,
            cap = StrokeCap.Round
        )
        // Front Right Leg
        drawLine(
            color = cheetahGold,
            start = Offset(cx + 7f * cheetahScale, cy + 2f * cheetahScale + bodyBob),
            end = Offset(cx + 10f * cheetahScale + legSwingFront2, cy + 8f * cheetahScale),
            strokeWidth = 2.2f * cheetahScale,
            cap = StrokeCap.Round
        )

        // --- Cute Baby Cheetah Head ---
        val headCenter = Offset(cx + 10f * cheetahScale, cy - 3f * cheetahScale + bodyBob)
        drawCircle(
            color = cheetahGold,
            radius = 4.8f * cheetahScale,
            center = headCenter
        )

        // Rounded small cheetah ears
        drawCircle(
            color = cheetahGold,
            radius = 1.9f * cheetahScale,
            center = Offset(headCenter.x - 2f * cheetahScale, headCenter.y - 4.5f * cheetahScale)
        )
        drawCircle(
            color = Color(0xFF92400E), // dark inner ear
            radius = 1.1f * cheetahScale,
            center = Offset(headCenter.x - 2f * cheetahScale, headCenter.y - 4.5f * cheetahScale)
        )

        // Muzzle
        drawOval(
            color = cheetahLightGold,
            topLeft = Offset(headCenter.x + 1f * cheetahScale, headCenter.y - 1.5f * cheetahScale),
            size = androidx.compose.ui.geometry.Size(3.8f * cheetahScale, 3.2f * cheetahScale)
        )
        // Cute black nose
        drawCircle(
            color = cheetahSpots,
            radius = 0.9f * cheetahScale,
            center = Offset(headCenter.x + 4.2f * cheetahScale, headCenter.y)
        )

        // Bright energetic eye
        drawCircle(
            color = cheetahSpots,
            radius = 1.1f * cheetahScale,
            center = Offset(headCenter.x + 1.2f * cheetahScale, headCenter.y - 1.8f * cheetahScale)
        )
        // Eye twinkle
        drawCircle(
            color = Color.White,
            radius = 0.5f * cheetahScale,
            center = Offset(headCenter.x + 1.5f * cheetahScale, headCenter.y - 2.1f * cheetahScale)
        )

        // Iconic Cheetah "Tear Stripe" (black line from eye down to mouth)
        drawLine(
            color = cheetahSpots,
            start = Offset(headCenter.x + 1.2f * cheetahScale, headCenter.y - 0.7f * cheetahScale),
            end = Offset(headCenter.x + 3.0f * cheetahScale, headCenter.y + 1.2f * cheetahScale),
            strokeWidth = 0.8f * cheetahScale,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Animated video progress scrubber line that traces over the two bottom bar ears
 * and displays remaining video time.
 */
@Composable
fun VideoEarsProgressBar(
    progress: Float,
    currentSeconds: Float,
    durationSeconds: Int,
    isPlaying: Boolean,
    isScrubbing: Boolean,
    onSeek: (Float) -> Unit,
    onScrubStateChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = androidx.compose.ui.platform.LocalDensity.current.density

    // Running animation cycle for the cheetah
    val infiniteTransition = rememberInfiniteTransition(label = "cheetah_run")
    val runCycle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 240, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "cheetah_cycle"
    )

    val safeProgress = progress.coerceIn(0f, 1f)
    val curSec = currentSeconds.toInt().coerceAtLeast(0)
    val remSec = (durationSeconds - curSec).coerceAtLeast(0)

    val currentFormatted = String.format("%02d", curSec % 60)
    val remainingFormatted = String.format("%02d", remSec % 60)
    // In case duration is >= 60s, handle mm:ss, otherwise simple 2-digit format like 09/16
    val currentDisplay = if (durationSeconds >= 60) String.format("%02d:%02d", curSec / 60, curSec % 60) else currentFormatted
    val remainingDisplay = if (durationSeconds >= 60) String.format("%02d:%02d", remSec / 60, remSec % 60) else remainingFormatted

    // Auto-hide timer for briefly showing time on tap/release
    var showTimeBriefly by remember { mutableStateOf(false) }

    LaunchedEffect(isScrubbing) {
        if (!isScrubbing && showTimeBriefly) {
            kotlinx.coroutines.delay(1800)
            showTimeBriefly = false
        }
    }

    val isTimeVisible = isScrubbing || showTimeBriefly

    Box(modifier = modifier) {
        // 1. Drawing the line curving up and over the ears
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = { offset ->
                            showTimeBriefly = true
                            val newP = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek(newP)
                            tryAwaitRelease()
                            showTimeBriefly = true
                        },
                        onTap = { offset ->
                            showTimeBriefly = true
                            val newP = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek(newP)
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            showTimeBriefly = true
                            onScrubStateChange(true)
                            val newP = (offset.x / size.width).coerceIn(0f, 1f)
                            onSeek(newP)
                        },
                        onDragEnd = {
                            onScrubStateChange(false)
                            showTimeBriefly = true
                        },
                        onDragCancel = {
                            onScrubStateChange(false)
                            showTimeBriefly = false
                        },
                        onHorizontalDrag = { change, _ ->
                            change.consume()
                            showTimeBriefly = true
                            val newP = (change.position.x / size.width).coerceIn(0f, 1f)
                            onSeek(newP)
                        }
                    )
                }
        ) {
            val w = size.width
            val contourPath = getBottomBarEarsPath(w, density, yOffset = 1.dp.toPx())

            val pathMeasure = PathMeasure()
            pathMeasure.setPath(contourPath, forceClosed = false)
            val totalLength = pathMeasure.length

            if (totalLength > 0f) {
                // Background Track following the ear contour
                drawPath(
                    path = contourPath,
                    color = Color.White.copy(alpha = 0.30f),
                    style = Stroke(
                        width = 3.2.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Active Video Progress segment climbing up & over the ears
                val progressLength = (totalLength * safeProgress).coerceIn(0f, totalLength)
                if (progressLength > 0.5f) {
                    val progressSegment = Path()
                    pathMeasure.getSegment(0f, progressLength, progressSegment, startWithMoveTo = true)

                    drawPath(
                        path = progressSegment,
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFE2C55), // TikTok Red
                                Color(0xFFFD761A), // Coral Orange
                                Color(0xFFFFB038)  // Bright Amber
                            )
                        ),
                        style = Stroke(
                            width = if (isScrubbing) 5.2.dp.toPx() else 4.0.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // Scrubber Head Knob tracking the ear shape (always visible so cheetah and handle never disappear)
                val knobOffset = pathMeasure.getPosition(progressLength)

                // Calculate path tangent angle so the cheetah faces and angles up/down the ear slopes
                val sampleDelta = 3f
                val prevPos = pathMeasure.getPosition((progressLength - sampleDelta).coerceAtLeast(0f))
                val nextPos = pathMeasure.getPosition((progressLength + sampleDelta).coerceAtMost(totalLength))
                val dx = nextPos.x - prevPos.x
                val dy = nextPos.y - prevPos.y
                val angleDeg = if (dx != 0f || dy != 0f) {
                    (atan2(dy, dx) * 180f / Math.PI.toFloat()).coerceIn(-65f, 65f)
                } else 0f

                // Subtle energy glow at foot of cheetah
                drawCircle(
                    color = Color(0xFFFE2C55).copy(alpha = 0.35f),
                    radius = if (isScrubbing) 6.dp.toPx() else 4.5.dp.toPx(),
                    center = knobOffset
                )
                drawCircle(
                    color = Color(0xFFFFB038).copy(alpha = 0.8f),
                    radius = 2.2.dp.toPx(),
                    center = knobOffset
                )

                // Tiny Running Baby Cheetah at the front of the line
                drawTinyRunningCheetah(
                    position = knobOffset,
                    tangentAngleDeg = angleDeg,
                    runCycle = runCycle,
                    isPaused = !isPlaying
                )
            }
        }

        // 2. Video Time Remaining Pill (Only shows on tap/drag/scrub, otherwise hidden)
        AnimatedVisibility(
            visible = isTimeVisible,
            enter = fadeIn(animationSpec = tween(180)) + scaleIn(initialScale = 0.88f),
            exit = fadeOut(animationSpec = tween(260)) + scaleOut(targetScale = 0.88f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isScrubbing) Color(0xFFFD761A) else Color.Black.copy(alpha = 0.82f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                color = if (isPlaying) Color(0xFF22C55E) else Color(0xFFEF4444),
                                shape = CircleShape
                            )
                    )

                    Text(
                        text = "$currentDisplay / $remainingDisplay",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
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
