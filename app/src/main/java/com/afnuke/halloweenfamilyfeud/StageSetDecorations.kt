package com.afnuke.halloweenfamilyfeud

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenOrange
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenOrangeBright
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenOrangeDark
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenPurple
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenPurpleDeep
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenPurpleLight
import kotlin.math.sin
import kotlin.random.Random

/**
 * 1) THE SET: Stage curtains, amber footlights, glossy floor reflection,
 * and animated marquee bulbs around the vintage TV bezel.
 */
@Composable
fun StageSetBackground(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 1. Glossy stage floor at bottom third
        val floorTop = height * 0.72f
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF140D20),
                    Color(0xFF0A0710),
                    Color(0xFF050408)
                ),
                startY = floorTop,
                endY = height
            ),
            topLeft = Offset(0f, floorTop),
            size = androidx.compose.ui.geometry.Size(width, height - floorTop)
        )

        // Glossy floor horizontal sheen line
        drawLine(
            brush = Brush.horizontalGradient(
                listOf(
                    Color.Transparent,
                    HalloweenPurpleLight.copy(alpha = 0.35f),
                    HalloweenOrangeBright.copy(alpha = 0.40f),
                    HalloweenPurpleLight.copy(alpha = 0.35f),
                    Color.Transparent
                )
            ),
            start = Offset(0f, floorTop),
            end = Offset(width, floorTop),
            strokeWidth = 2.dp.toPx()
        )

        // 2. Amber Footlights glowing along the floor
        val numFootlights = 7
        val footlightSpacing = width / (numFootlights + 1)
        for (i in 1..numFootlights) {
            val fx = i * footlightSpacing
            val fy = floorTop + 2.dp.toPx()

            // Warm upward radial beam
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        HalloweenOrangeBright.copy(alpha = 0.45f),
                        HalloweenOrange.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(fx, fy),
                    radius = 55.dp.toPx()
                ),
                radius = 55.dp.toPx(),
                center = Offset(fx, fy)
            )

            // Footlight small bulb fixture
            drawCircle(
                color = HalloweenOrangeBright,
                radius = 3.5.dp.toPx(),
                center = Offset(fx, fy)
            )
            drawCircle(
                color = Color.White,
                radius = 1.8.dp.toPx(),
                center = Offset(fx, fy)
            )
        }

        // 3. Deep purple stage curtains along the left and right edges
        val curtainWidth = 28.dp.toPx()
        // Left Curtain folds
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color(0xFF280A48),
                    Color(0xFF4C1282),
                    Color(0xFF1A0530),
                    Color(0xFF380D60),
                    Color.Transparent
                ),
                startX = 0f,
                endX = curtainWidth
            ),
            topLeft = Offset.Zero,
            size = androidx.compose.ui.geometry.Size(curtainWidth, height)
        )

        // Right Curtain folds
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    Color(0xFF380D60),
                    Color(0xFF1A0530),
                    Color(0xFF4C1282),
                    Color(0xFF280A48)
                ),
                startX = width - curtainWidth,
                endX = width
            ),
            topLeft = Offset(width - curtainWidth, 0f),
            size = androidx.compose.ui.geometry.Size(curtainWidth, height)
        )
    }
}

/**
 * Animated Marquee Bulbs around the bezel:
 * Chasing and twinkling in Halloween Orange and Purple.
 * Flashes rapidly when a team wins!
 */
@Composable
fun MarqueeBulbsBorder(
    isGameWon: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "marqueeChase")

    // Chasing phase (0 to 3) during normal play; fast strobe when won
    val chasePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isGameWon) 250 else 900,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "chasePhase"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val bulbRadius = 3.5.dp.toPx()
        val spacing = 18.dp.toPx()
        val phaseInt = chasePhase.toInt()

        // Top edge bulbs
        val numTop = (w / spacing).toInt()
        for (i in 0 until numTop) {
            val x = (i + 0.5f) * (w / numTop)
            val bulbState = (i + phaseInt) % 3
            drawMarqueeBulb(Offset(x, 4.dp.toPx()), bulbState, isGameWon, bulbRadius)
        }

        // Bottom edge bulbs
        for (i in 0 until numTop) {
            val x = (i + 0.5f) * (w / numTop)
            val bulbState = (i + phaseInt + 1) % 3
            drawMarqueeBulb(Offset(x, h - 4.dp.toPx()), bulbState, isGameWon, bulbRadius)
        }

        // Left edge bulbs
        val numSide = ((h - 20.dp.toPx()) / spacing).toInt()
        for (i in 0 until numSide) {
            val y = 10.dp.toPx() + (i + 0.5f) * ((h - 20.dp.toPx()) / numSide)
            val bulbState = (i + phaseInt + 2) % 3
            drawMarqueeBulb(Offset(4.dp.toPx(), y), bulbState, isGameWon, bulbRadius)
        }

        // Right edge bulbs
        for (i in 0 until numSide) {
            val y = 10.dp.toPx() + (i + 0.5f) * ((h - 20.dp.toPx()) / numSide)
            val bulbState = (i + phaseInt) % 3
            drawMarqueeBulb(Offset(w - 4.dp.toPx(), y), bulbState, isGameWon, bulbRadius)
        }
    }
}

private fun DrawScope.drawMarqueeBulb(
    center: Offset,
    bulbState: Int,
    isGameWon: Boolean,
    radius: Float
) {
    val (coreColor, glowColor) = when {
        isGameWon -> {
            if (bulbState % 2 == 0) {
                HalloweenOrangeBright to HalloweenOrange.copy(alpha = 0.85f)
            } else {
                HalloweenPurpleLight to HalloweenPurple.copy(alpha = 0.85f)
            }
        }
        bulbState == 0 -> HalloweenOrangeBright to HalloweenOrange.copy(alpha = 0.65f)
        bulbState == 1 -> HalloweenPurpleLight to HalloweenPurpleDeep.copy(alpha = 0.65f)
        else -> Color(0xFF383540) to Color.Transparent
    }

    // Outer glow
    if (glowColor != Color.Transparent) {
        drawCircle(
            color = glowColor,
            radius = radius * 2.2f,
            center = center
        )
    }

    // Bulb body
    drawCircle(
        color = coreColor,
        radius = radius,
        center = center
    )

    // Center shine highlight
    if (bulbState != 2 || isGameWon) {
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = radius * 0.45f,
            center = Offset(center.x - radius * 0.3f, center.y - radius * 0.3f)
        )
    }
}

/**
 * 5) THE WIN: Animated Halloween confetti (pumpkins, bats, sparkles)
 * drifting and celebrating over the stage when a team wins!
 */
@Composable
fun HalloweenConfettiOverlay(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "confetti")
    val fallProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fallProgress"
    )

    val particles = remember {
        List(32) { index ->
            ConfettiParticle(
                xNorm = (index * 0.031f + Random.nextFloat() * 0.02f) % 1.0f,
                speed = 0.7f + Random.nextFloat() * 0.6f,
                swayFreq = 2.5f + Random.nextFloat() * 3.0f,
                swayAmp = 12.dp.value + Random.nextFloat() * 16.dp.value,
                phase = Random.nextFloat() * 2f * Math.PI.toFloat(),
                type = when (index % 3) {
                    0 -> ParticleType.PUMPKIN
                    1 -> ParticleType.BAT
                    else -> ParticleType.SPARKLE
                }
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (p in particles) {
            val totalY = (p.phase + fallProgress * p.speed) % 1.0f
            val y = totalY * (h + 40.dp.toPx()) - 20.dp.toPx()
            val xSway = sin(totalY * p.swayFreq * 2f * Math.PI.toFloat() + p.phase) * p.swayAmp
            val x = (p.xNorm * w + xSway).coerceIn(10f, w - 10f)

            when (p.type) {
                ParticleType.PUMPKIN -> {
                    // Mini orange pumpkin particle
                    drawCircle(
                        color = HalloweenOrangeBright,
                        radius = 4.5.dp.toPx(),
                        center = Offset(x, y)
                    )
                    drawCircle(
                        color = HalloweenOrangeDark,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                    // Stem
                    drawLine(
                        color = Color(0xFF2E6B18),
                        start = Offset(x, y - 4.5.dp.toPx()),
                        end = Offset(x + 1.dp.toPx(), y - 7.dp.toPx()),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }

                ParticleType.BAT -> {
                    // Mini purple bat silhouette
                    val batColor = HalloweenPurpleLight.copy(alpha = 0.85f)
                    val r = 5.dp.toPx()
                    drawLine(batColor, Offset(x - r, y - r * 0.5f), Offset(x, y + r * 0.3f), strokeWidth = 2.dp.toPx())
                    drawLine(batColor, Offset(x, y + r * 0.3f), Offset(x + r, y - r * 0.5f), strokeWidth = 2.dp.toPx())
                    drawCircle(batColor, radius = 2.dp.toPx(), center = Offset(x, y))
                }

                ParticleType.SPARKLE -> {
                    // Diamond sparkle
                    val sColor = Color.White.copy(alpha = 0.9f)
                    val r = 3.5.dp.toPx()
                    drawLine(sColor, Offset(x - r, y), Offset(x + r, y), strokeWidth = 1.5.dp.toPx())
                    drawLine(sColor, Offset(x, y - r), Offset(x, y + r), strokeWidth = 1.5.dp.toPx())
                }
            }
        }
    }
}

private enum class ParticleType { PUMPKIN, BAT, SPARKLE }

private data class ConfettiParticle(
    val xNorm: Float,
    val speed: Float,
    val swayFreq: Float,
    val swayAmp: Float,
    val phase: Float,
    val type: ParticleType
)
