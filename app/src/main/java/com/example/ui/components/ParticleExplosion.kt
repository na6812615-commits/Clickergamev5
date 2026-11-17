package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RoyalGold
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

internal enum class ParticleKind {
    SPARK_STREAK,
    EMBER_ORB,
    DIAMOND_SHARD
}

internal data class ParticleSpec(
    val angleRad: Float,
    val speedPxPerSec: Float,
    val baseRadiusDp: Float,
    val color: Color,
    val kind: ParticleKind,
    val drag: Float,
    val gravityPxPerSec2: Float,
    val spinSpeedDegPerSec: Float,
    val maxLifeSec: Float
)

internal data class ExplosionBurst(
    val id: Long,
    val originNormalized: Offset,
    val primaryColor: Color,
    val isCrit: Boolean,
    val isOverdrive: Boolean,
    val particles: List<ParticleSpec>,
    val durationSec: Float,
    var ageSec: Float = 0f
)

@Stable
class ParticleExplosionController {
    private val bursts = mutableStateListOf<ExplosionBurst>()
    private var nextId = 1L

    internal fun currentBursts(): List<ExplosionBurst> = bursts.toList()
    internal fun hasActiveBursts(): Boolean = bursts.isNotEmpty()
    internal fun advanceFrame(dtSec: Float) {
        val iterator = bursts.listIterator()
        while (iterator.hasNext()) {
            val burst = iterator.next()
            burst.ageSec += dtSec
            if (burst.ageSec >= burst.durationSec) {
                iterator.remove()
            }
        }
    }

    fun spawnBurst(
        originNormalized: Offset = Offset(0.5f, 0.5f),
        accentColor: Color,
        isCrit: Boolean,
        isOverdrive: Boolean,
        comboCount: Int,
        highGraphicsEnabled: Boolean
    ) {
        val count = when {
            highGraphicsEnabled && isCrit -> 40
            highGraphicsEnabled && isOverdrive -> 34
            highGraphicsEnabled -> 26 + (comboCount / 10).coerceAtMost(8)
            isCrit -> 20
            else -> 14
        }

        val palette = when {
            isCrit -> listOf(AmberGold, RoyalGold, CoralCrimson, Color.White, accentColor)
            isOverdrive -> listOf(AmberGold, CoralCrimson, ElectricViolet, CyberCyan)
            else -> listOf(accentColor, CyberCyan, NeonEmerald, ElectricViolet, Color.White)
        }

        val seed = nextId++
        val rng = Random(seed * 7919L + System.nanoTime())
        val particles = List(count) { idx ->
            val baseAngle = (idx.toFloat() / count) * (2f * PI.toFloat())
            val jitter = (rng.nextFloat() - 0.5f) * 0.42f
            val angle = baseAngle + jitter
            val speedScale = if (isCrit) 1.32f else if (isOverdrive) 1.18f else 1f
            val speed = (220f + rng.nextFloat() * 440f) * speedScale
            val kind = when {
                isCrit && idx % 3 == 0 -> ParticleKind.DIAMOND_SHARD
                idx % 2 == 0 -> ParticleKind.SPARK_STREAK
                else -> ParticleKind.EMBER_ORB
            }
            ParticleSpec(
                angleRad = angle,
                speedPxPerSec = speed,
                baseRadiusDp = when (kind) {
                    ParticleKind.DIAMOND_SHARD -> 4.2f + rng.nextFloat() * 2.8f
                    ParticleKind.SPARK_STREAK -> 2.4f + rng.nextFloat() * 1.8f
                    ParticleKind.EMBER_ORB -> 2.6f + rng.nextFloat() * 3.0f
                },
                color = palette[idx % palette.size],
                kind = kind,
                drag = 2.6f + rng.nextFloat() * 1.8f,
                gravityPxPerSec2 = 160f + rng.nextFloat() * 180f,
                spinSpeedDegPerSec = (rng.nextFloat() - 0.5f) * 520f,
                maxLifeSec = (0.38f + rng.nextFloat() * 0.28f) * if (isCrit) 1.15f else 1f
            )
        }

        if (bursts.size >= MAX_ACTIVE_BURSTS) {
            bursts.removeAt(0)
        }
        bursts.add(
            ExplosionBurst(
                id = seed,
                originNormalized = Offset(
                    x = originNormalized.x.coerceIn(0.15f, 0.85f),
                    y = originNormalized.y.coerceIn(0.15f, 0.85f)
                ),
                primaryColor = if (isCrit) AmberGold else accentColor,
                isCrit = isCrit,
                isOverdrive = isOverdrive,
                particles = particles,
                durationSec = if (isCrit) 0.72f else 0.58f
            )
        )
    }

    private companion object {
        const val MAX_ACTIVE_BURSTS = 6
    }
}

@Composable
fun rememberParticleExplosionController(): ParticleExplosionController {
    return remember { ParticleExplosionController() }
}

@Composable
fun ParticleExplosionCanvas(
    controller: ParticleExplosionController,
    modifier: Modifier = Modifier
) {
    var frameTick by remember { mutableLongStateOf(0L) }

    LaunchedEffect(controller) {
        while (isActive) {
            if (!controller.hasActiveBursts()) {
                snapshotFlow { controller.hasActiveBursts() }.first { it }
            }
            var lastFrameNs = 0L
            while (isActive && controller.hasActiveBursts()) {
                withFrameNanos { frameTimeNs ->
                    val dtSec = if (lastFrameNs == 0L) {
                        0.016f
                    } else {
                        ((frameTimeNs - lastFrameNs) / 1_000_000_000.0).toFloat().coerceIn(0.001f, 0.05f)
                    }
                    lastFrameNs = frameTimeNs
                    controller.advanceFrame(dtSec)
                    frameTick = frameTimeNs
                }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Read frameTick so DrawScope invalidates every animation frame
        @Suppress("UNUSED_EXPRESSION")
        frameTick

        val burstsSnapshot = controller.currentBursts()
        burstsSnapshot.forEach { burst ->
            val progress = (burst.ageSec / burst.durationSec).coerceIn(0f, 1f)
            val origin = Offset(
                x = size.width * burst.originNormalized.x,
                y = size.height * burst.originNormalized.y
            )
            drawBurst(
                burst = burst,
                origin = origin,
                progress = progress
            )
        }
    }
}

private fun DrawScope.drawBurst(
    burst: ExplosionBurst,
    origin: Offset,
    progress: Float
) {
    val t = burst.ageSec
    val invProgress = (1f - progress).coerceIn(0f, 1f)

    // 1. Initial impact flash flare
    if (progress < 0.35f) {
        val flashAlpha = (1f - progress / 0.35f).coerceIn(0f, 1f) * if (burst.isCrit) 0.72f else 0.5f
        val flashRadius = size.minDimension * (0.14f + progress * 0.55f)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = flashAlpha),
                    burst.primaryColor.copy(alpha = flashAlpha * 0.65f),
                    Color.Transparent
                ),
                center = origin,
                radius = flashRadius
            ),
            radius = flashRadius,
            center = origin
        )
    }

    // 2. Primary + Secondary Shockwave Rings
    val primaryRingRadius = size.minDimension * (0.20f + 0.34f * easeOutCubic(progress))
    drawCircle(
        color = burst.primaryColor.copy(alpha = invProgress * 0.78f),
        radius = primaryRingRadius,
        center = origin,
        style = Stroke(width = ((if (burst.isCrit) 5f else 3.5f) * invProgress + 0.8f).dp.toPx())
    )

    if (progress > 0.08f) {
        val secondaryProgress = ((progress - 0.08f) / 0.92f).coerceIn(0f, 1f)
        val secondaryAlpha = (1f - secondaryProgress).coerceIn(0f, 1f) * 0.45f
        val secondaryRadius = size.minDimension * (0.16f + 0.32f * easeOutCubic(secondaryProgress))
        drawCircle(
            color = if (burst.isCrit) CoralCrimson.copy(alpha = secondaryAlpha) else ElectricViolet.copy(alpha = secondaryAlpha),
            radius = secondaryRadius,
            center = origin,
            style = Stroke(width = (2.2f * (1f - secondaryProgress) + 0.5f).dp.toPx())
        )
    }

    // 3. Individual Physics Particles (sparks, embers, diamond shards)
    val densityScale = (size.minDimension / 260.dp.toPx()).coerceIn(0.8f, 1.35f)
    burst.particles.forEach { p ->
        val lifeFrac = (t / p.maxLifeSec).coerceIn(0f, 1f)
        if (lifeFrac >= 1f) return@forEach

        val alpha = (1f - lifeFrac.pow(1.4f)).coerceIn(0f, 1f)
        // Analytical damped displacement: s(t) = v0 * (1 - e^(-drag * t)) / drag
        val dampFactor = (1f - kotlin.math.exp(-p.drag * t)) / p.drag
        val dist = p.speedPxPerSec * densityScale * dampFactor
        val px = origin.x + dist * cos(p.angleRad)
        val py = origin.y + dist * sin(p.angleRad) + 0.5f * p.gravityPxPerSec2 * t * t

        val radiusPx = p.baseRadiusDp.dp.toPx() * (0.35f + 0.65f * (1f - lifeFrac))
        val pos = Offset(px, py)

        when (p.kind) {
            ParticleKind.SPARK_STREAK -> {
                val prevT = (t - 0.026f).coerceAtLeast(0f)
                val prevDamp = (1f - kotlin.math.exp(-p.drag * prevT)) / p.drag
                val prevDist = p.speedPxPerSec * densityScale * prevDamp
                val prevPos = Offset(
                    x = origin.x + prevDist * cos(p.angleRad),
                    y = origin.y + prevDist * sin(p.angleRad) + 0.5f * p.gravityPxPerSec2 * prevT * prevT
                )
                drawLine(
                    color = p.color.copy(alpha = alpha * 0.9f),
                    start = prevPos,
                    end = pos,
                    strokeWidth = radiusPx * 1.15f,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White.copy(alpha = alpha * 0.85f),
                    radius = radiusPx * 0.45f,
                    center = pos
                )
            }
            ParticleKind.EMBER_ORB -> {
                drawCircle(
                    color = p.color.copy(alpha = alpha * 0.32f),
                    radius = radiusPx * 2.1f,
                    center = pos
                )
                drawCircle(
                    color = p.color.copy(alpha = alpha),
                    radius = radiusPx,
                    center = pos
                )
            }
            ParticleKind.DIAMOND_SHARD -> {
                val r = radiusPx * 1.45f
                val path = Path().apply {
                    moveTo(pos.x, pos.y - r)
                    lineTo(pos.x + r * 0.72f, pos.y)
                    lineTo(pos.x, pos.y + r)
                    lineTo(pos.x - r * 0.72f, pos.y)
                    close()
                }
                rotate(degrees = p.spinSpeedDegPerSec * t, pivot = pos) {
                    drawPath(
                        path = path,
                        color = p.color.copy(alpha = alpha)
                    )
                }
            }
        }
    }
}

private fun easeOutCubic(x: Float): Float {
    val inv = 1f - x.coerceIn(0f, 1f)
    return 1f - inv * inv * inv
}
