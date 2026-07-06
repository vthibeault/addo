package com.addo.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import com.addo.app.ui.theme.AddoColors
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val spin: Float,
    val isRect: Boolean
)

private val palette = listOf(
    AddoColors.coral, AddoColors.sunshine, AddoColors.mint,
    AddoColors.grape, AddoColors.bubblegum, Color(0xFF7DD3FC)
)

/**
 * A short, joyful burst from the bottom center of the screen.
 * [trigger] increments to replay. Purely decorative — no touch interception.
 */
@Composable
fun ConfettiBurst(trigger: Int, modifier: Modifier = Modifier) {
    var playingFor by remember { mutableStateOf(0) }
    val animatable = remember { Animatable(1f) }
    LaunchedEffect(trigger) {
        if (trigger > 0) {
            playingFor = trigger
            animatable.snapTo(0f)
            animatable.animateTo(1f, tween(durationMillis = 1900, easing = LinearEasing))
        }
    }
    if (playingFor == 0) return

    val particles = remember(playingFor) {
        val rng = Random(playingFor)
        List(120) {
            Particle(
                angle = (-90f + (rng.nextFloat() - 0.5f) * 130f) * (Math.PI.toFloat() / 180f),
                speed = 0.45f + rng.nextFloat() * 1.4f,
                size = 7f + rng.nextFloat() * 16f,
                color = palette[rng.nextInt(palette.size)],
                spin = (rng.nextFloat() - 0.5f) * 1080f,
                isRect = rng.nextBoolean()
            )
        }
    }
    val progress = animatable.value
    if (progress >= 1f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val origin = Offset(size.width / 2f, size.height * 0.92f)
        val reach = size.height * 0.95f
        particles.forEach { p ->
            val t = progress
            val distance = p.speed * reach * t
            val x = origin.x + cos(p.angle) * distance
            val y = origin.y + sin(p.angle) * distance + (t * t * size.height * 0.6f)
            val alpha = (1f - t).coerceIn(0f, 1f)
            if (p.isRect) {
                rotate(degrees = p.spin * t, pivot = Offset(x, y)) {
                    drawRect(
                        color = p.color.copy(alpha = alpha),
                        topLeft = Offset(x - p.size / 2, y - p.size / 4),
                        size = Size(p.size, p.size / 2)
                    )
                }
            } else {
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = p.size / 2 * (1f - t * 0.3f),
                    center = Offset(x, y)
                )
            }
        }
    }
}
