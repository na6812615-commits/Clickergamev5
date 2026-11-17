package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import kotlin.math.PI
import kotlin.math.sin

private data class StarMote(
    val xFrac: Float,
    val yFrac: Float,
    val radiusDp: Float,
    val speed: Float,
    val phase: Float
)

@Composable
fun CyberBackground(
    isOverdriveActive: Boolean,
    comboCount: Int,
    highGraphicsEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val auraTargetColor = when {
        isOverdriveActive -> CoralCrimson
        comboCount >= 20 -> AmberGold
        comboCount >= 5 -> NeonEmerald
        else -> CyberCyan
    }
    val auraColor by animateColorAsState(
        targetValue = auraTargetColor,
        animationSpec = tween(durationMillis = 450),
        label = "bg_aura_color"
    )

    if (!highGraphicsEnabled) {
        Canvas(modifier = modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(ObsidianBg, ObsidianBgDeep)
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        auraColor.copy(alpha = if (isOverdriveActive) 0.14f else 0.07f),
                        Color.Transparent
                    ),
                    center = Offset(size.width * 0.5f, size.height * 0.42f),
                    radius = size.minDimension * 0.85f
                ),
                center = Offset(size.width * 0.5f, size.height * 0.42f),
                radius = size.minDimension * 0.85f
            )
        }
        return
    }

    val transition = rememberInfiniteTransition(label = "cyber_bg")
    val gridPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "grid_phase"
    )
    val particlePhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle_phase"
    )

    val motes = remember {
        List(20) { index ->
            StarMote(
                xFrac = ((index * 37) % 100) / 100f,
                yFrac = ((index * 53) % 100) / 100f,
                radiusDp = 1.2f + (index % 3) * 0.8f,
                speed = 0.6f + (index % 4) * 0.25f,
                phase = index * 0.7f
            )
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(ObsidianBg, ObsidianBgDeep)
            )
        )

        // Atmospheric top-center energy glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    auraColor.copy(alpha = if (isOverdriveActive) 0.18f else 0.09f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.5f, size.height * 0.42f),
                radius = size.minDimension * 0.85f
            ),
            center = Offset(size.width * 0.5f, size.height * 0.42f),
            radius = size.minDimension * 0.85f
        )

        // Subtle scrolling cyber coordinate grid
        val spacing = 42.dp.toPx()
        val lineAlpha = if (isOverdriveActive) 0.09f else 0.045f
        var x = 0f
        while (x <= size.width) {
            drawLine(
                color = auraColor.copy(alpha = lineAlpha),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1.dp.toPx()
            )
            x += spacing
        }

        val yOffset = gridPhase * spacing
        var y = -spacing + yOffset
        while (y <= size.height) {
            drawLine(
                color = auraColor.copy(alpha = lineAlpha),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1.dp.toPx()
            )
            y += spacing
        }

        // Drifting neon motes
        motes.forEach { mote ->
            val driftY = ((mote.yFrac - (gridPhase * mote.speed)) % 1f + 1f) % 1f
            val swayX = sin(particlePhase + mote.phase) * 14.dp.toPx()
            val alpha = (0.18f + 0.25f * (0.5f + 0.5f * sin(particlePhase + mote.phase)))
            drawCircle(
                color = auraColor.copy(alpha = alpha),
                radius = mote.radiusDp.dp.toPx(),
                center = Offset(
                    x = (mote.xFrac * size.width + swayX).coerceIn(0f, size.width),
                    y = driftY * size.height
                )
            )
        }
    }
}
