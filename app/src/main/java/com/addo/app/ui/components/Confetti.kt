package com.addo.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class Particle(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val color: Color,
    val spin: Float
)

private val palette = listOf(
    Color(0xFF2E6B5E), Color(0xFFF0BD8F), Color(0xFF9A6A3B),
    Color(0xFFA0D1C4), Color(0xFFC3CB94), Color(0xFFE8927C)
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
            animatable.animateTo(1f, tween(durationMillis = 1600, easing = LinearEasing))
        }
    }
    if (playingFor == 0) return

    val particles = remember(playingFor) {
        val rng = Random(playingFor)
        List(80) {
            Particle(
                angle = (-90f + rng.nextFloat() * 120f - 60f) * (Math.PI.toFloat() / 180f),
                speed = 0.5f + rng.nextFloat() * 1.3f,
                size = 8f + rng.nextFloat() * 14f,
                color = palette[rng.nextInt(palette.size)],
                spin = rng.nextFloat() * 8f
            )
        }
    }
    val progress = animatable.value
    if (progress >= 1f) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val origin = Offset(size.width / 2f, size.height * 0.9f)
        val reach = size.height * 0.9f
        particles.forEach { p ->
            val t = progress
            val distance = p.speed * reach * t
            val x = origin.x + cos(p.angle) * distance
            val y = origin.y + sin(p.angle) * distance + (t * t * size.height * 0.55f)
            val alpha = (1f - t).coerceIn(0f, 1f)
            drawCircle(
                color = p.color.copy(alpha = alpha),
                radius = p.size * (1f - t * 0.4f),
                center = Offset(x, y)
            )
        }
    }
}
