package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.sin

@OptIn(ExperimentalTextApi::class)
@Composable
fun WaterBottleCanvas(
    modifier: Modifier = Modifier,
    progress: Float // 0f to 1f
) {
    // Wave animation phases
    val infiniteTransition = rememberInfiniteTransition(label = "water_waves")
    val wavePhase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (Math.PI * 2).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_1"
    )
    val wavePhase2 by infiniteTransition.animateFloat(
        initialValue = (Math.PI * 2).toFloat(),
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase_2"
    )

    // Animated bubble heights (for floating bubble effects)
    val bubbleOffset1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_1"
    )
    val bubbleOffset2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_2"
    )
    val bubbleOffset3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(4800, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Restart
        ),
        label = "bubble_3"
    )

    // Text Measurer to render level notations nicely inside canvas
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        // Define exact physical design layout of the bottle
        // Bottle takes central 60% of horizontal canvas width
        val bottleWidth = width * 0.52f
        val bottleHeight = height * 0.76f
        val bottleLeft = (width - bottleWidth) / 2f
        val bottleTop = height * 0.15f
        val bottleBottom = bottleTop + bottleHeight

        val cornerRadius = bottleWidth * 0.18f
        val maxLiquidY = bottleTop + (bottleHeight * 0.05f) // top shoulder

        // 1. Draw subtle ambient back shadow glow for the bottle
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(SapphireBlue.copy(alpha = 0.22f), Color.Transparent),
                center = Offset(width / 2f, bottleTop + bottleHeight / 2f),
                radius = bottleWidth * 1.1f
            )
        )

        // Pre-create the bottle path for clipping of fluid waves
        val bottlePath = Path().apply {
            // Upper neck dimensions
            val neckWidth = bottleWidth * 0.55f
            val neckHeight = bottleHeight * 0.08f
            val neckLeft = bottleLeft + (bottleWidth - neckWidth) / 2f
            val neckRight = neckLeft + neckWidth
            val neckTop = bottleTop - neckHeight

            // Neck base curves out to shoulders
            val shoulderRadius = bottleWidth * 0.12f

            moveTo(neckLeft, neckTop)
            lineTo(neckRight, neckTop)
            // Neck side
            lineTo(neckRight, bottleTop - shoulderRadius)
            // Shoulder curve to main body
            quadraticTo(
                neckRight, bottleTop,
                neckRight + shoulderRadius, bottleTop
            )
            // Main body straight line to bottom right corner
            lineTo(bottleLeft + bottleWidth - shoulderRadius, bottleTop)
            lineTo(bottleLeft + bottleWidth, bottleTop + shoulderRadius)
            lineTo(bottleLeft + bottleWidth, bottleBottom - cornerRadius)
            // Bottom rounded corners
            quadraticTo(
                bottleLeft + bottleWidth, bottleBottom,
                bottleLeft + bottleWidth - cornerRadius, bottleBottom
            )
            lineTo(bottleLeft + cornerRadius, bottleBottom)
            quadraticTo(
                bottleLeft, bottleBottom,
                bottleLeft, bottleBottom - cornerRadius
            )
            // Back up left side
            lineTo(bottleLeft, bottleTop + shoulderRadius)
            lineTo(bottleLeft + shoulderRadius, bottleTop)
            // Left shoulder curve
            quadraticTo(
                neckLeft, bottleTop,
                neckLeft, bottleTop - shoulderRadius
            )
            lineTo(neckLeft, neckTop)
            close()
        }

        // 2. Draw animated fluid inside, clipped to bottle outline
        clipPath(bottlePath) {
            // Fill background with light transparent navy-blue glass hue
            drawPath(
                path = bottlePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NavyLight.copy(alpha = 0.2f),
                        NavyLight.copy(alpha = 0.45f)
                    )
                )
            )

            // Define physical height of liquid inside bottle
            // Liquid top depends on progress (from bottleBottom to bottleTop)
            val minLiquidY = bottleBottom
            val liquidY = minLiquidY - (minLiquidY - maxLiquidY) * progress

            if (progress > 0.01f) {
                val waveAmplitude = 14.dp.toPx()
                val waveFrequency = (Math.PI * 2 / bottleWidth).toFloat()

                // Back Wave: Darker/Sheer, phase 2
                val backWavePath = Path().apply {
                    moveTo(bottleLeft - 10f, bottleBottom + 10f)
                    for (x in bottleLeft.toInt() - 10.. (bottleLeft + bottleWidth).toInt() + 10 step 4) {
                        val relativeX = x - bottleLeft
                        val y = liquidY + sin(relativeX * waveFrequency + wavePhase2) * waveAmplitude * 0.8f
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(bottleLeft + bottleWidth + 10f, bottleBottom + 10f)
                    close()
                }
                drawPath(
                    path = backWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            AzureBlue.copy(alpha = 0.5f),
                            NavyDark.copy(alpha = 0.8f)
                        )
                    )
                )

                // Front Wave: Bright Sapphire, phase 1
                val frontWavePath = Path().apply {
                    moveTo(bottleLeft - 10f, bottleBottom + 10f)
                    for (x in bottleLeft.toInt() - 10.. (bottleLeft + bottleWidth).toInt() + 10 step 4) {
                        val relativeX = x - bottleLeft
                        val y = liquidY + sin(relativeX * waveFrequency + wavePhase1) * waveAmplitude
                        lineTo(x.toFloat(), y)
                    }
                    lineTo(bottleLeft + bottleWidth + 10f, bottleBottom + 10f)
                    close()
                }
                drawPath(
                    path = frontWavePath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            SapphireBlue.copy(alpha = 0.95f),
                            AzureBlue.copy(alpha = 0.75f),
                            NavyDark.copy(alpha = 0.9f)
                        )
                    )
                )

                // 3. Render tiny interactive floating bubbles inside the liquid level
                val bubbles = listOf(
                    Triple(bottleLeft + bottleWidth * 0.25f, bubbleOffset1, 5.dp),
                    Triple(bottleLeft + bottleWidth * 0.5f, bubbleOffset2, 3.5.dp),
                    Triple(bottleLeft + bottleWidth * 0.75f, bubbleOffset3, 6.dp),
                    Triple(bottleLeft + bottleWidth * 0.4f, (bubbleOffset1 + 0.5f) % 1f, 4.dp),
                    Triple(bottleLeft + bottleWidth * 0.65f, (bubbleOffset3 + 0.3f) % 1f, 5.dp)
                )

                bubbles.forEach { (x, offset, size) ->
                    // bubbles only float between wave level and bottom
                    val startY = bottleBottom - 20f
                    val bubbleY = startY - (startY - liquidY) * offset
                    if (bubbleY > liquidY + 10f) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.35f),
                            radius = size.toPx(),
                            center = Offset(x, bubbleY)
                        )
                        // tiny specular dot on bubble
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f),
                            radius = size.toPx() * 0.25f,
                            center = Offset(x - size.toPx() * 0.28f, bubbleY - size.toPx() * 0.28f)
                        )
                    }
                }
            }
        }

        // 4. Draw outer glass container structures (specular gloss edges & highlights)
        // High-contrast clean outline
        drawPath(
            path = bottlePath,
            color = Color.White.copy(alpha = 0.42f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Glass reflection highlight: Draw 3D curved gloss light reflecting running along the left border
        val reflectionWidth = bottleWidth * 0.04f
        val reflectionPath = Path().apply {
            val offsetLeft = bottleLeft + bottleWidth * 0.08f
            moveTo(offsetLeft, bottleTop + bottleHeight * 0.1f)
            lineTo(offsetLeft, bottleBottom - cornerRadius)
        }
        drawPath(
            path = reflectionPath,
            color = Color.White.copy(alpha = 0.35f),
            style = Stroke(
                width = reflectionWidth,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(height * 0.15f, 20f, 30f, 20f), 0f),
                cap = StrokeCap.Round
            )
        )

        // Right side soft white inner shadow highlight
        val rightReflectionPath = Path().apply {
            val offsetRight = bottleLeft + bottleWidth * 0.91f
            moveTo(offsetRight, bottleTop + bottleHeight * 0.2f)
            lineTo(offsetRight, bottleBottom - cornerRadius)
        }
        drawPath(
            path = rightReflectionPath,
            color = Color.White.copy(alpha = 0.18f),
            style = Stroke(width = reflectionWidth * 0.5f, cap = StrokeCap.Round)
        )

        // Draw bottle plastic screw thread ring on neck top
        val neckWidth = bottleWidth * 0.55f
        val neckLeft = bottleLeft + (bottleWidth - neckWidth) / 2f
        val neckHeight = bottleHeight * 0.08f
        val neckTop = bottleTop - neckHeight
        val capHeight = neckHeight * 0.8f
        val capTop = neckTop - capHeight

        // 5. Build bottle cap
        // Rounded glossy screw cap placed above neck
        val capCorner = 6.dp.toPx()
        val capLeft = neckLeft - 4.dp.toPx()
        val capWidth = neckWidth + 8.dp.toPx()

        // Cap base fill with stylish dark metallic gradient
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(NavyLight, NavyDark, NavyDark)
            ),
            topLeft = Offset(capLeft, capTop),
            size = Size(capWidth, capHeight),
            cornerRadius = CornerRadius(capCorner, capCorner)
        )

        // Cap ribbed stripes
        val capStripeCount = 10
        val capStep = capWidth / (capStripeCount + 1)
        for (i in 1..capStripeCount) {
            val sx = capLeft + (i * capStep)
            drawLine(
                color = Color.White.copy(alpha = 0.18f),
                start = Offset(sx, capTop + 2.dp.toPx()),
                end = Offset(sx, capTop + capHeight - 2.dp.toPx()),
                strokeWidth = 1.6.dp.toPx()
            )
        }

        // Cap border highlight stroke
        drawRoundRect(
            color = Color.White.copy(alpha = 0.22f),
            topLeft = Offset(capLeft, capTop),
            size = Size(capWidth, capHeight),
            cornerRadius = CornerRadius(capCorner, capCorner),
            style = Stroke(width = 1.2.dp.toPx())
        )

        // Draw dynamic cap strap (ribbon attached to cap) for that premium designer aesthetic
        val strapPath = Path().apply {
            val strapStart = Offset(capLeft + capWidth - 4.dp.toPx(), capTop + capHeight * 0.5f)
            moveTo(strapStart.x, strapStart.y)
            // curve out and down representing a flexible loop strap
            cubicTo(
                strapStart.x + 35.dp.toPx(), strapStart.y + 10.dp.toPx(),
                strapStart.x + 30.dp.toPx(), strapStart.y + 45.dp.toPx(),
                strapStart.x - 4.dp.toPx(), strapStart.y + 35.dp.toPx()
            )
        }
        drawPath(
            path = strapPath,
            color = Color(0xFF22365D).copy(alpha = 0.7f),
            style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
        )
        drawPath(
            path = strapPath,
            color = Color.White.copy(alpha = 0.15f),
            style = Stroke(width = 1.dp.toPx(), cap = StrokeCap.Round)
        )

        // 6. DRAW LEVEL MEASUREMENT MARKS ON RIGHT SIDE OF BOTTLE (Y coordinates scaled to 0% - 100%)
        val markLevels = listOf(
            2.0f to "2.0L",
            1.5f to "1.5L",
            1.0f to "1.0L",
            0.5f to "0.5L",
            0.0f to "0L"
        )

        val totalLiterRange = 2.0f
        val markingLeftX = bottleLeft + bottleWidth + 14.dp.toPx()

        markLevels.forEach { (volumeFraction, label) ->
            // volumeFraction scale matches physical height
            val normProgress = volumeFraction / totalLiterRange // 0f to 1f
            val markY = (bottleBottom - (bottleBottom - maxLiquidY) * normProgress)

            // Active marking glows blue if current progress is at least this volume fraction
            val isPassed = progress >= (volumeFraction / totalLiterRange) - 0.01f
            val textColor = if (isPassed) SapphireBlue else GlassTextSecondary
            val markColor = if (isPassed) SapphireBlue.copy(alpha = 0.85f) else Color.White.copy(alpha = 0.25f)
            val strokeSize = if (isPassed) 2.dp.toPx() else 1.dp.toPx()
            val textBold = if (isPassed) FontWeight.Bold else FontWeight.Normal

            // Draw small ticks
            drawLine(
                color = markColor,
                start = Offset(markingLeftX, markY),
                end = Offset(markingLeftX + 10.dp.toPx(), markY),
                strokeWidth = strokeSize
            )

            // Draw secondary miniature indicator dash for details
            if (volumeFraction > 0f) {
                val halfVolumeY = (markY + (bottleBottom - maxLiquidY) * (0.25f / totalLiterRange))
                if (halfVolumeY < bottleBottom) {
                    drawLine(
                        color = Color.White.copy(alpha = 0.15f),
                        start = Offset(markingLeftX, halfVolumeY),
                        end = Offset(markingLeftX + 5.dp.toPx(), halfVolumeY),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // Draw level labels: max is written with a 'MAX' suffix
            val fullLabel = if (volumeFraction == 2.0f) "$label\nMAX" else label
            val textLayoutResult = textMeasurer.measure(
                text = AnnotatedString(fullLabel),
                style = TextStyle(
                    color = textColor,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Default,
                    fontWeight = textBold,
                    lineHeight = 12.sp
                )
            )

            // draw label right next to tick
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(markingLeftX + 16.dp.toPx(), markY - textLayoutResult.size.height / 2f)
            )
        }

        // Decorative 75% complete badge label placed dynamically inside bottle if selected
        if (progress > 0.01f) {
            val pctString = "${(progress * 100).toInt()}%"
            val compString = "Completed"

            val pctLayout = textMeasurer.measure(
                text = AnnotatedString(pctString),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            val compLayout = textMeasurer.measure(
                text = AnnotatedString(compString),
                style = TextStyle(
                    color = GlassTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            )

            val badgeWidth = maxOf(pctLayout.size.width, compLayout.size.width) + 24.dp.toPx()
            val badgeHeight = pctLayout.size.height + compLayout.size.height + 16.dp.toPx()
            val badgeX = bottleLeft - badgeWidth * 0.42f
            val badgeY = bottleBottom - bottleHeight * 0.45f

            // Draw glass card background for percent marker on Left
            drawRoundRect(
                color = GlassCardBg,
                topLeft = Offset(badgeX, badgeY),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
            )
            drawRoundRect(
                color = GlassCardBorder,
                topLeft = Offset(badgeX, badgeY),
                size = Size(badgeWidth, badgeHeight),
                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                style = Stroke(width = 1.2.dp.toPx())
            )

            // Draw text centered inside the small glass card badge
            drawText(
                textLayoutResult = pctLayout,
                topLeft = Offset(badgeX + (badgeWidth - pctLayout.size.width) / 2f, badgeY + 6.dp.toPx())
            )

            drawText(
                textLayoutResult = compLayout,
                topLeft = Offset(badgeX + (badgeWidth - compLayout.size.width) / 2f, badgeY + pctLayout.size.height + 6.dp.toPx())
            )

            // Small active underline
            val lineY = badgeY + badgeHeight - 6.dp.toPx()
            drawLine(
                color = SapphireBlue,
                start = Offset(badgeX + 12.dp.toPx(), lineY),
                end = Offset(badgeX + badgeWidth - 12.dp.toPx(), lineY),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
