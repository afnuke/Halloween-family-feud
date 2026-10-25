package com.afnuke.halloweenfamilyfeud

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtBorderMonochrome
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtGrayDark
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtGrayMid
import com.afnuke.halloweenfamilyfeud.ui.theme.CrtWhite
import com.afnuke.halloweenfamilyfeud.ui.theme.HalloweenOrangeBright

enum class HostExpression {
    GRINNING,
    SHOCKED,
    LAUGHING,
    TALKING
}

/**
 * 4) THE HOST: Bigger and alive with dynamic body animations:
 * - Bouncing with excitement on a reveal
 * - Shaking head and doubling over on a strike
 * - Throwing his arms up on a win
 */
@Composable
fun HostCharacterView(
    expression: HostExpression,
    hostReactionText: String?,
    isWinning: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "hostAnim")

    // 1. Excitement vertical bounce on reveal / laughter
    val revealBounceY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -7f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "revealBounce"
    )

    // 2. Disbelief head shake on strike
    val headShakeX by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "headShake"
    )

    // 3. Doubling over posture on strike
    val doubleOverY by animateFloatAsState(
        targetValue = if (expression == HostExpression.SHOCKED) 5f else 0f,
        animationSpec = tween(180),
        label = "doubleOver"
    )

    // Micro-bobbing on mic for talking/idle
    val micBob by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "micBob"
    )

    val currentBounceY = when {
        isWinning -> revealBounceY * 1.3f
        expression == HostExpression.LAUGHING -> revealBounceY
        else -> 0f
    }

    val currentShakeX = if (expression == HostExpression.SHOCKED) headShakeX else 0f

    Column(
        modifier = modifier
            .testTag("host_character_widget"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Host Speech Reaction Bubble (if speaking)
        if (!hostReactionText.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1B1A22),
                border = BorderStroke(1.dp, HalloweenOrangeBright.copy(alpha = 0.85f)),
                modifier = Modifier
                    .padding(bottom = 3.dp)
                    .testTag("host_reaction_bubble")
            ) {
                Text(
                    text = "\"$hostReactionText\"",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.5.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = HalloweenOrangeBright,
                    maxLines = 2,
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                )
            }
        }

        // Host Figure + Nameplate
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = currentBounceY.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .size(width = 72.dp, height = 92.dp)
                    .offset(x = currentShakeX.dp, y = doubleOverY.dp)
                    .testTag("host_canvas_${expression.name.lowercase()}")
            ) {
                drawAnimatedHostFigure(expression, micBob, isWinning)
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = CrtGrayDark,
                    border = BorderStroke(1.dp, CrtBorderMonochrome)
                ) {
                    Text(
                        text = "JACK",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = CrtWhite,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = when {
                        isWinning -> "🏆 WINNER!"
                        expression == HostExpression.GRINNING -> "😄 Grinning"
                        expression == HostExpression.SHOCKED -> "😲 Oh no!"
                        expression == HostExpression.LAUGHING -> "🎉 Big Reaction!"
                        expression == HostExpression.TALKING -> "🎙️ On Air"
                        else -> "🎙️ On Air"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 7.5.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isWinning || expression == HostExpression.LAUGHING) HalloweenOrangeBright else CrtGrayMid,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Custom Canvas drawing for the showman host with dynamic poses:
 * - Arms raised on win
 * - Disbelief head/hands on strike
 * - Smiling game-show host on reveal/idle
 */
private fun DrawScope.drawAnimatedHostFigure(
    expression: HostExpression,
    micBob: Float,
    isWinning: Boolean
) {
    val centerX = size.width / 2f
    val headRadius = 16.5.dp.toPx()
    val headCenterY = 23.dp.toPx()

    val suitColor = Color(0xFF1B1A22)
    val lapelColor = Color(0xFF2E2C38)
    val shirtColor = Color(0xFFF0F0F6)
    val skinColor = Color(0xFFE8C8AC)
    val hairColor = Color(0xFF261D1A)

    // 1. TUXEDO BODY
    val suitPath = Path().apply {
        moveTo(centerX - 26.dp.toPx(), size.height)
        lineTo(centerX - 23.dp.toPx(), 46.dp.toPx())
        lineTo(centerX - 11.dp.toPx(), 37.dp.toPx())
        lineTo(centerX + 11.dp.toPx(), 37.dp.toPx())
        lineTo(centerX + 23.dp.toPx(), 46.dp.toPx())
        lineTo(centerX + 26.dp.toPx(), size.height)
        close()
    }
    drawPath(suitPath, suitColor, style = Fill)
    drawPath(suitPath, Color(0xFF383544), style = Stroke(width = 1.5.dp.toPx()))

    // 2. White Shirt V-insert
    val shirtPath = Path().apply {
        moveTo(centerX - 9.dp.toPx(), 37.dp.toPx())
        lineTo(centerX, 57.dp.toPx())
        lineTo(centerX + 9.dp.toPx(), 37.dp.toPx())
        close()
    }
    drawPath(shirtPath, shirtColor, style = Fill)

    // 3. Narrow Tie with gold tie clip
    val tiePath = Path().apply {
        moveTo(centerX - 3.dp.toPx(), 41.dp.toPx())
        lineTo(centerX + 3.dp.toPx(), 41.dp.toPx())
        lineTo(centerX + 3.5.dp.toPx(), 64.dp.toPx())
        lineTo(centerX, 68.dp.toPx())
        lineTo(centerX - 3.5.dp.toPx(), 64.dp.toPx())
        close()
    }
    drawPath(tiePath, Color(0xFF0F0E13), style = Fill)
    // Gold tie clip
    drawLine(
        color = HalloweenOrangeBright,
        start = Offset(centerX - 4.dp.toPx(), 52.dp.toPx()),
        end = Offset(centerX + 4.dp.toPx(), 52.dp.toPx()),
        strokeWidth = 2.dp.toPx()
    )

    // Satin Lapels
    val leftLapel = Path().apply {
        moveTo(centerX - 11.dp.toPx(), 37.dp.toPx())
        lineTo(centerX - 3.dp.toPx(), 57.dp.toPx())
        lineTo(centerX - 15.dp.toPx(), 48.dp.toPx())
        close()
    }
    drawPath(leftLapel, lapelColor, style = Fill)

    val rightLapel = Path().apply {
        moveTo(centerX + 11.dp.toPx(), 37.dp.toPx())
        lineTo(centerX + 3.dp.toPx(), 57.dp.toPx())
        lineTo(centerX + 15.dp.toPx(), 48.dp.toPx())
        close()
    }
    drawPath(rightLapel, lapelColor, style = Fill)

    // 4. Arms & Pose
    if (isWinning) {
        // THROWING ARMS UP ON WIN!
        val leftArm = Path().apply {
            moveTo(centerX - 23.dp.toPx(), 46.dp.toPx())
            lineTo(centerX - 28.dp.toPx(), 16.dp.toPx())
            lineTo(centerX - 22.dp.toPx(), 14.dp.toPx())
            lineTo(centerX - 18.dp.toPx(), 42.dp.toPx())
            close()
        }
        drawPath(leftArm, suitColor, style = Fill)
        drawCircle(skinColor, radius = 5.dp.toPx(), center = Offset(centerX - 25.dp.toPx(), 12.dp.toPx()))

        val rightArm = Path().apply {
            moveTo(centerX + 23.dp.toPx(), 46.dp.toPx())
            lineTo(centerX + 28.dp.toPx(), 16.dp.toPx())
            lineTo(centerX + 22.dp.toPx(), 14.dp.toPx())
            lineTo(centerX + 18.dp.toPx(), 42.dp.toPx())
            close()
        }
        drawPath(rightArm, suitColor, style = Fill)
        drawCircle(skinColor, radius = 5.dp.toPx(), center = Offset(centerX + 25.dp.toPx(), 12.dp.toPx()))
    }

    // 5. Head & Neck
    drawRect(
        color = skinColor,
        topLeft = Offset(centerX - 4.5.dp.toPx(), headCenterY + headRadius * 0.7f),
        size = Size(9.dp.toPx(), 9.dp.toPx())
    )
    drawCircle(
        color = skinColor,
        radius = headRadius,
        center = Offset(centerX, headCenterY)
    )

    // Retro Slick Pompadour Hair
    val hairPath = Path().apply {
        moveTo(centerX - headRadius * 0.95f, headCenterY - 2.dp.toPx())
        cubicTo(
            centerX - headRadius, headCenterY - headRadius * 1.55f,
            centerX + headRadius * 0.5f, headCenterY - headRadius * 1.65f,
            centerX + headRadius * 1.05f, headCenterY - 4.dp.toPx()
        )
        lineTo(centerX + headRadius * 0.9f, headCenterY - headRadius * 0.4f)
        lineTo(centerX - headRadius * 0.7f, headCenterY - headRadius * 0.5f)
        close()
    }
    drawPath(hairPath, hairColor, style = Fill)

    // 6. Facial Expressions
    val eyeColor = Color(0xFF1B1A20)
    val eyeY = headCenterY - 1.dp.toPx()
    val mouthY = headCenterY + 7.5.dp.toPx()

    when {
        expression == HostExpression.SHOCKED -> {
            // Wide circular shocked eyes
            drawCircle(Color.White, radius = 3.8.dp.toPx(), center = Offset(centerX - 5.5.dp.toPx(), eyeY))
            drawCircle(eyeColor, radius = 1.6.dp.toPx(), center = Offset(centerX - 5.5.dp.toPx(), eyeY))
            drawCircle(Color.White, radius = 3.8.dp.toPx(), center = Offset(centerX + 5.5.dp.toPx(), eyeY))
            drawCircle(eyeColor, radius = 1.6.dp.toPx(), center = Offset(centerX + 5.5.dp.toPx(), eyeY))

            // Raised arched disbelief eyebrows
            drawLine(eyeColor, Offset(centerX - 9.dp.toPx(), eyeY - 8.dp.toPx()), Offset(centerX - 3.dp.toPx(), eyeY - 6.dp.toPx()), strokeWidth = 1.5.dp.toPx())
            drawLine(eyeColor, Offset(centerX + 9.dp.toPx(), eyeY - 8.dp.toPx()), Offset(centerX + 3.dp.toPx(), eyeY - 6.dp.toPx()), strokeWidth = 1.5.dp.toPx())

            // Dropped jaw 'O' mouth in shock
            drawRoundRect(
                color = Color(0xFF1C1A22),
                topLeft = Offset(centerX - 4.dp.toPx(), mouthY - 1.dp.toPx()),
                size = Size(8.dp.toPx(), 10.dp.toPx()),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )
        }

        expression == HostExpression.LAUGHING || isWinning -> {
            // Squeezed laughing eyes (^ ^)
            val leftEye = Path().apply {
                moveTo(centerX - 8.dp.toPx(), eyeY + 1.dp.toPx())
                lineTo(centerX - 5.5.dp.toPx(), eyeY - 3.5.dp.toPx())
                lineTo(centerX - 3.dp.toPx(), eyeY + 1.dp.toPx())
            }
            drawPath(leftEye, eyeColor, style = Stroke(width = 1.6.dp.toPx()))

            val rightEye = Path().apply {
                moveTo(centerX + 3.dp.toPx(), eyeY + 1.dp.toPx())
                lineTo(centerX + 5.5.dp.toPx(), eyeY - 3.5.dp.toPx())
                lineTo(centerX + 8.dp.toPx(), eyeY + 1.dp.toPx())
            }
            drawPath(rightEye, eyeColor, style = Stroke(width = 1.6.dp.toPx()))

            // Big open laughing grin with pearly white teeth
            val mouthPath = Path().apply {
                moveTo(centerX - 8.5.dp.toPx(), mouthY)
                quadraticBezierTo(centerX, mouthY + 9.5.dp.toPx(), centerX + 8.5.dp.toPx(), mouthY)
                close()
            }
            drawPath(mouthPath, Color(0xFF221A26), style = Fill)
            drawRect(
                color = Color.White,
                topLeft = Offset(centerX - 6.5.dp.toPx(), mouthY),
                size = Size(13.dp.toPx(), 4.dp.toPx())
            )
            drawPath(mouthPath, eyeColor, style = Stroke(width = 1.dp.toPx()))
        }

        else -> {
            // Grinning / Confident Showman Smile
            drawCircle(eyeColor, radius = 2.2.dp.toPx(), center = Offset(centerX - 5.5.dp.toPx(), eyeY))
            drawCircle(eyeColor, radius = 2.2.dp.toPx(), center = Offset(centerX + 5.5.dp.toPx(), eyeY))

            drawLine(eyeColor, Offset(centerX - 7.5.dp.toPx(), eyeY - 5.5.dp.toPx()), Offset(centerX - 3.dp.toPx(), eyeY - 4.dp.toPx()), strokeWidth = 1.5.dp.toPx())
            drawLine(eyeColor, Offset(centerX + 7.5.dp.toPx(), eyeY - 5.5.dp.toPx()), Offset(centerX + 3.dp.toPx(), eyeY - 4.dp.toPx()), strokeWidth = 1.5.dp.toPx())

            // Dazzling wide smile
            val mouthPath = Path().apply {
                moveTo(centerX - 7.5.dp.toPx(), mouthY)
                quadraticBezierTo(centerX, mouthY + 7.5.dp.toPx(), centerX + 7.5.dp.toPx(), mouthY)
                close()
            }
            drawPath(mouthPath, Color.White, style = Fill)
            drawPath(mouthPath, eyeColor, style = Stroke(width = 1.dp.toPx()))
        }
    }

    // 7. Handheld Chrome Microphone
    val micX = centerX + 20.dp.toPx()
    val micY = 58.dp.toPx() + micBob

    drawLine(
        color = Color(0xFF282830),
        start = Offset(micX, micY),
        end = Offset(micX + 4.dp.toPx(), micY + 24.dp.toPx()),
        strokeWidth = 3.dp.toPx()
    )
    drawCircle(color = Color(0xFFDCDCE4), radius = 5.5.dp.toPx(), center = Offset(micX, micY))
    drawCircle(color = Color(0xFF706F7C), radius = 4.2.dp.toPx(), center = Offset(micX, micY), style = Stroke(width = 1.dp.toPx()))
}
