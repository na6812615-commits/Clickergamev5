package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.Formatters
import com.example.ui.components.ParticleExplosionCanvas
import com.example.ui.components.rememberParticleExplosionController
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CoralCrimsonDim
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldDim
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.FloatingPopup
import com.example.viewmodel.GameUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.FlightTakeoff
import com.example.data.CyberSkillId

@Composable
fun TapCoreScreen(
    uiState: GameUiState,
    onTapCore: () -> Unit,
    onActivateOverdrive: () -> Unit,
    onToggleAuto: () -> Unit,
    onOpenCashShop: () -> Unit,
    onCastSkill: (CyberSkillId) -> Unit = {},
    onCollectSupplyDrop: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val game = uiState.game
    val haptic = LocalHapticFeedback.current
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "reactor_orbit")
    val outerOrbitAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (uiState.isOverdriveActive) 4000 else 14000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_orbit"
    )
    val innerPolygonAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (uiState.isOverdriveActive) 3000 else 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_poly"
    )

    val particleController = rememberParticleExplosionController()
    var lastTapOriginNormalized by remember { androidx.compose.runtime.mutableStateOf(Offset(0.5f, 0.5f)) }
    var buttonPixelSize by remember { androidx.compose.runtime.mutableStateOf(IntSize.Zero) }

    val comboAccent by animateColorAsState(
        targetValue = if (uiState.isOverdriveActive) AmberGold else comboColorFor(uiState.comboCount),
        animationSpec = tween(durationMillis = 220),
        label = "combo_accent"
    )

    LaunchedEffect(uiState.tapBurstCounter) {
        if (uiState.tapBurstCounter > 0L) {
            particleController.spawnBurst(
                originNormalized = lastTapOriginNormalized,
                accentColor = comboAccent,
                isCrit = uiState.lastTapWasCrit,
                isOverdrive = uiState.isOverdriveActive,
                comboCount = uiState.comboCount,
                highGraphicsEnabled = uiState.settings.highGraphicsEnabled
            )
        }
    }

    val coreScale by animateFloatAsState(
        targetValue = if (uiState.isCooldownActive) 0.93f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "core_scale"
    )

    val autoPulse by animateFloatAsState(
        targetValue = if (uiState.autoTickCounter % 2L == 0L) 1f else 1.08f,
        animationSpec = tween(durationMillis = 180),
        label = "auto_pulse"
    )

    val overdriveBoost = if (uiState.isOverdriveActive) 2.0 else 1.0
    val goldenBoost = if (uiState.goldenFeverRemainingSec > 0L) 3.0 else 1.0
    val tapSurgeBoost = if (uiState.tapSurgeRemainingSec > 0L) 4.0 else 1.0
    val hyperFocusBoost = if (uiState.hyperActiveRemainingSec > 0L) 2.5 else 1.0
    val currentTapGain = game.effectivePointsPerClick * uiState.comboMultiplier * overdriveBoost * goldenBoost * tapSurgeBoost * hyperFocusBoost

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Click Combo HUD Banner
        ClickComboBanner(
            comboCount = uiState.comboCount,
            comboMultiplier = uiState.comboMultiplier,
            comboRemainingFraction = uiState.comboRemainingFraction,
            comboTierLabel = uiState.comboTierLabel,
            bestCombo = game.bestCombo,
            accent = comboAccent
        )

        // Active Timed Boosters / Fever / Hyper Focus / Drone Overclock
        if (uiState.goldenFeverRemainingSec > 0L ||
            uiState.tapSurgeRemainingSec > 0L ||
            uiState.critStormRemainingSec > 0L ||
            uiState.autoFrenzyRemainingSec > 0L ||
            uiState.hyperActiveRemainingSec > 0L ||
            uiState.droneOverclockRemainingSec > 0L
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (uiState.goldenFeverRemainingSec > 0L) {
                    ActiveBuffPill("Hoàng Kim x3", uiState.goldenFeverRemainingSec, AmberGold, Modifier.weight(1f))
                }
                if (uiState.tapSurgeRemainingSec > 0L) {
                    ActiveBuffPill("Tap x4", uiState.tapSurgeRemainingSec, CoralCrimson, Modifier.weight(1f))
                }
                if (uiState.hyperActiveRemainingSec > 0L) {
                    ActiveBuffPill("Hội Tụ x2.5", uiState.hyperActiveRemainingSec, CyberCyan, Modifier.weight(1f))
                }
                if (uiState.droneOverclockRemainingSec > 0L) {
                    ActiveBuffPill("Drone x3", uiState.droneOverclockRemainingSec, ElectricViolet, Modifier.weight(1f))
                }
                if (uiState.critStormRemainingSec > 0L) {
                    ActiveBuffPill("Crit 100%", uiState.critStormRemainingSec, ElectricViolet, Modifier.weight(1f))
                }
                if (uiState.autoFrenzyRemainingSec > 0L) {
                    ActiveBuffPill("Auto x2.5", uiState.autoFrenzyRemainingSec, NeonEmerald, Modifier.weight(1f))
                }
            }
        }

        uiState.activeSupplyDrop?.let { drop ->
            Button(
                onClick = onCollectSupplyDrop,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan,
                    contentColor = ObsidianBgDeep
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("collect_supply_drop_button")
            ) {
                Icon(Icons.Rounded.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "NHẬN TIẾP TẾ: ${drop.label}",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Central Multi-Layer Reactor Core
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(266.dp),
            contentAlignment = Alignment.Center
        ) {
            // Multi-stop ambient reactor aura
            Box(
                modifier = Modifier
                    .size(258.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                comboAccent.copy(alpha = if (uiState.isOverdriveActive) 0.36f else 0.24f),
                                ElectricViolet.copy(alpha = 0.08f),
                                ObsidianBgDeep.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )

            val ringProgress = 1f - uiState.cooldownRemainingFraction
            val sides = game.reactorTier.sides

            Canvas(modifier = Modifier.size(248.dp)) {
                val center = Offset(size.width / 2f, size.height / 2f)

                // Layer 1: Outer rotating radar ticks (36 ticks)
                rotate(degrees = outerOrbitAngle, pivot = center) {
                    val outerRadius = size.minDimension / 2f - 4.dp.toPx()
                    for (i in 0 until 36) {
                        val rad = i * 10.0 * PI / 180.0
                        val isMajor = i % 3 == 0
                        val innerLen = if (isMajor) 9.dp.toPx() else 4.dp.toPx()
                        val start = Offset(
                            x = center.x + (outerRadius - innerLen) * cos(rad).toFloat(),
                            y = center.y + (outerRadius - innerLen) * sin(rad).toFloat()
                        )
                        val end = Offset(
                            x = center.x + outerRadius * cos(rad).toFloat(),
                            y = center.y + outerRadius * sin(rad).toFloat()
                        )
                        drawLine(
                            color = comboAccent.copy(alpha = if (isMajor) 0.55f else 0.22f),
                            start = start,
                            end = end,
                            strokeWidth = (if (isMajor) 2.2f else 1.2f).dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Layer 2: Cooldown progress sweep arc
                val strokeWidth = 8.dp.toPx()
                val arcInset = 18.dp.toPx()
                val arcSize = Size(size.width - arcInset * 2, size.height - arcInset * 2)
                val arcTopLeft = Offset(arcInset, arcInset)

                drawArc(
                    color = CardBorder.copy(alpha = 0.7f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth)
                )

                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(comboAccent, NeonEmerald, ElectricViolet, comboAccent)
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * ringProgress.coerceIn(0f, 1f),
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Layer 3: Counter-rotating Reactor Tier polygon frame
                rotate(degrees = innerPolygonAngle, pivot = center) {
                    val polyRadius = size.minDimension / 2f - 28.dp.toPx()
                    val path = Path()
                    for (i in 0 until sides) {
                        val angle = (i * (360.0 / sides) - 90.0) * PI / 180.0
                        val pt = Offset(
                            x = center.x + polyRadius * cos(angle).toFloat(),
                            y = center.y + polyRadius * sin(angle).toFloat()
                        )
                        if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                    }
                    path.close()
                    drawPath(
                        path = path,
                        color = comboAccent.copy(alpha = 0.35f),
                        style = Stroke(width = 1.6.dp.toPx())
                    )
                }
            }

            // Layer 4: Central Interactive Reactor Core Button
            val tapCd = stringResource(R.string.tap_core_button_cd)
            Surface(
                modifier = Modifier
                    .size(168.dp)
                    .scale(coreScale)
                    .clip(CircleShape)
                    .onSizeChanged { buttonPixelSize = it }
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent(PointerEventPass.Initial)
                                val down = event.changes.firstOrNull { it.pressed }
                                if (down != null && buttonPixelSize.width > 0 && buttonPixelSize.height > 0) {
                                    // Map touch point inside 168.dp button into 266.dp reactor arena normalized space
                                    val bx = (down.position.x / buttonPixelSize.width.toFloat()) - 0.5f
                                    val by = (down.position.y / buttonPixelSize.height.toFloat()) - 0.5f
                                    lastTapOriginNormalized = Offset(
                                        x = 0.5f + bx * 0.55f,
                                        y = 0.5f + by * 0.55f
                                    )
                                }
                            }
                        }
                    }
                    .semantics { contentDescription = tapCd }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true, color = comboAccent),
                        role = Role.Button,
                        onClick = {
                            if (!uiState.isCooldownActive) {
                                if (uiState.settings.hapticsEnabled) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                onTapCore()
                            }
                        }
                    )
                    .testTag("tap_core_button"),
                shape = CircleShape,
                color = CyberCyanDim,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (uiState.isCooldownActive) comboAccent.copy(alpha = 0.45f) else comboAccent
                ),
                tonalElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    comboAccent.copy(alpha = 0.3f),
                                    CyberCyanDim,
                                    ObsidianBgDeep
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_bolt),
                            contentDescription = null,
                            tint = if (uiState.isCooldownActive) TextSecondary else comboAccent,
                            modifier = Modifier.size(38.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "+${Formatters.formatDecimal(currentTapGain)}",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = when {
                                uiState.isOverdriveActive -> "OVERDRIVE x2"
                                uiState.isCooldownActive -> stringResource(R.string.tap_core_cooldown)
                                else -> stringResource(R.string.tap_core_ready)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (uiState.isCooldownActive) AmberGold else comboAccent
                        )
                    }
                }
            }

            // Layer 5: Multi-burst Particle Explosion Overlay (renders above core rim without blocking touches)
            ParticleExplosionCanvas(
                controller = particleController,
                modifier = Modifier.size(266.dp)
            )

            // Floating +points / CRIT popups
            if (uiState.settings.floatingTextEnabled) {
                uiState.floatingPopups.forEach { popup ->
                    FloatingPointChip(popup = popup)
                }
            }
        }

        // OVERDRIVE Surge Bar
        OverdriveBar(
            chargePercent = game.overdriveCharge,
            isActive = uiState.isOverdriveActive,
            remainingFraction = uiState.overdriveRemainingFraction,
            durationSec = game.overdriveDurationSec,
            onActivate = onActivateOverdrive
        )

        // Active Cyber Skills Bar (EMP, Hyper Focus, Quantum Rain)
        ActiveSkillsBar(
            uiState = uiState,
            onCastSkill = onCastSkill
        )

        // Auto Clicker & Passive Core Status Card
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("auto_click_status_card"),
            shape = RoundedCornerShape(18.dp),
            color = CarbonSurface.copy(alpha = 0.92f),
            border = BorderStroke(
                width = 1.dp,
                color = if (game.autoUnlocked && game.autoEnabled) {
                    NeonEmerald.copy(alpha = 0.5f)
                } else {
                    CardBorder
                }
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        modifier = Modifier
                            .size(42.dp)
                            .scale(if (game.autoUnlocked && game.autoEnabled) autoPulse else 1f),
                        shape = CircleShape,
                        color = if (game.autoUnlocked && game.autoEnabled) NeonEmeraldDim else ElevatedCard,
                        border = BorderStroke(
                            1.dp,
                            if (game.autoUnlocked && game.autoEnabled) NeonEmerald else CardBorder
                        )
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                painter = painterResource(
                                    if (game.autoUnlocked) R.drawable.ic_auto else R.drawable.ic_lock
                                ),
                                contentDescription = null,
                                tint = if (game.autoUnlocked && game.autoEnabled) NeonEmerald else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = when {
                                !game.autoUnlocked -> stringResource(R.string.auto_status_locked)
                                game.autoEnabled -> stringResource(R.string.auto_status_active)
                                else -> stringResource(R.string.auto_status_paused)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (game.autoUnlocked) {
                                "+${Formatters.formatDecimal(game.effectiveAutoPointsPerTick)} điểm / ${Formatters.formatSeconds(game.autoCooldownSec)}"
                            } else {
                                "Mở khóa với giá $25 tại mục \$Shop\$"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (game.autoUnlocked) {
                    IconButton(
                        onClick = onToggleAuto,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (game.autoEnabled) NeonEmeraldDim else ElevatedCard,
                            contentColor = if (game.autoEnabled) NeonEmerald else AmberGold
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("auto_toggle_button")
                    ) {
                        Icon(
                            painter = painterResource(
                                if (game.autoEnabled) R.drawable.ic_pause else R.drawable.ic_play
                            ),
                            contentDescription = stringResource(R.string.auto_toggle_cd)
                        )
                    }
                } else {
                    TextButton(
                        onClick = onOpenCashShop,
                        modifier = Modifier.testTag("go_to_cash_shop_button")
                    ) {
                        Text(
                            text = "Mở \$Shop\$",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonEmerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun OverdriveBar(
    chargePercent: Double,
    isActive: Boolean,
    remainingFraction: Float,
    durationSec: Double,
    onActivate: () -> Unit
) {
    val ready = !isActive && chargePercent >= 100.0
    val accent = when {
        isActive -> CoralCrimson
        ready -> AmberGold
        else -> ElectricViolet
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("overdrive_card"),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, accent.copy(alpha = if (ready || isActive) 0.7f else 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isActive) "QUÁ TẢI OVERDRIVE ĐANG BẬT!" else "NẠP OVERDRIVE (${Formatters.formatSeconds(durationSec)})",
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isActive) "x2 TẤT CẢ" else "${chargePercent.roundToInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = {
                        if (isActive) remainingFraction.coerceIn(0f, 1f)
                        else (chargePercent / 100.0).toFloat().coerceIn(0f, 1f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(7.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = accent,
                    trackColor = ElevatedCard
                )
            }

            if (ready) {
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onActivate,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmberGold,
                        contentColor = ObsidianBgDeep
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("activate_overdrive_button")
                ) {
                    Text(
                        text = "KÍCH HOẠT",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun ClickComboBanner(
    comboCount: Int,
    comboMultiplier: Double,
    comboRemainingFraction: Float,
    comboTierLabel: String,
    bestCombo: Int,
    accent: Color
) {
    val isActive = comboCount > 0
    val comboScale by animateFloatAsState(
        targetValue = if (isActive && comboCount % 2 == 1) 1.03f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "combo_banner_scale"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(comboScale)
            .testTag("click_combo_banner"),
        shape = RoundedCornerShape(16.dp),
        color = if (isActive) comboContainerFor(comboCount) else CarbonSurface.copy(alpha = 0.92f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) accent.copy(alpha = 0.6f) else CardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_fire),
                        contentDescription = null,
                        tint = if (isActive) accent else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isActive) {
                        Text(
                            text = "${comboCount}x COMBO",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = accent,
                            modifier = Modifier.testTag("combo_count_text")
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ElevatedCard
                        ) {
                            val bonusText = if (comboMultiplier > 1.0) {
                                "$comboTierLabel • x${Formatters.formatDecimal(comboMultiplier)}"
                            } else {
                                "$comboTierLabel (5+ để thưởng)"
                            }
                            Text(
                                text = bonusText,
                                style = MaterialTheme.typography.labelSmall,
                                color = accent,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.combo_hint_idle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.combo_best_label, bestCombo),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }

            if (isActive) {
                Spacer(modifier = Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { comboRemainingFraction.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp)),
                    color = accent,
                    trackColor = ElevatedCard
                )
            }
        }
    }
}

private fun comboColorFor(combo: Int): Color = when {
    combo >= 50 -> CoralCrimson
    combo >= 20 -> AmberGold
    combo >= 5 -> NeonEmerald
    else -> CyberCyan
}

private fun comboContainerFor(combo: Int): Color = when {
    combo >= 50 -> CoralCrimsonDim
    combo >= 20 -> AmberGoldDim
    combo >= 5 -> NeonEmeraldDim
    else -> CyberCyanDim
}

@Composable
private fun FloatingPointChip(popup: FloatingPopup) {
    val riseAnim = remember(popup.id) { Animatable(0f) }
    val alphaAnim = remember(popup.id) { Animatable(1f) }

    LaunchedEffect(popup.id) {
        riseAnim.animateTo(
            targetValue = -115f,
            animationSpec = tween(durationMillis = 600)
        )
    }
    LaunchedEffect(popup.id) {
        alphaAnim.animateTo(
            targetValue = 0f,
            animationSpec = tween(durationMillis = 620)
        )
    }

    Text(
        text = popup.text,
        style = MaterialTheme.typography.titleLarge.copy(
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = if (popup.isCrit) 22.sp else 19.sp
        ),
        color = if (popup.isCrit) AmberGold else comboColorFor(popup.comboCount),
        modifier = Modifier
            .offset {
                IntOffset(
                    x = (popup.offsetXFraction * 180f).roundToInt(),
                    y = (-64f + riseAnim.value).roundToInt()
                )
            }
            .alpha(alphaAnim.value)
    )
}

@Composable
private fun ActiveBuffPill(
    label: String,
    remainingSec: Long,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val mins = remainingSec / 60L
    val secs = remainingSec % 60L
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = CarbonSurface.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.7f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = accent, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(
                text = String.format("%02d:%02d", mins, secs),
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun ActiveSkillsBar(
    uiState: GameUiState,
    onCastSkill: (CyberSkillId) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_skills_bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SkillActionButton(
            skill = CyberSkillId.EMP_BURST,
            cooldownSec = uiState.empCooldownSec,
            isActiveBuff = false,
            accent = AmberGold,
            onClick = { onCastSkill(CyberSkillId.EMP_BURST) },
            modifier = Modifier.weight(1f)
        )
        SkillActionButton(
            skill = CyberSkillId.HYPER_FOCUS,
            cooldownSec = uiState.hyperCooldownSec,
            isActiveBuff = uiState.hyperActiveRemainingSec > 0L,
            accent = CyberCyan,
            onClick = { onCastSkill(CyberSkillId.HYPER_FOCUS) },
            modifier = Modifier.weight(1f)
        )
        SkillActionButton(
            skill = CyberSkillId.QUANTUM_RAIN,
            cooldownSec = uiState.rainCooldownSec,
            isActiveBuff = false,
            accent = NeonEmerald,
            onClick = { onCastSkill(CyberSkillId.QUANTUM_RAIN) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SkillActionButton(
    skill: CyberSkillId,
    cooldownSec: Long,
    isActiveBuff: Boolean,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isReady = cooldownSec <= 0L
    Surface(
        modifier = modifier
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(enabled = isReady, onClick = onClick)
            .testTag("cast_${skill.id}"),
        shape = RoundedCornerShape(14.dp),
        color = when {
            isActiveBuff -> CyberCyanDim
            isReady -> CarbonSurface.copy(alpha = 0.96f)
            else -> ElevatedCard.copy(alpha = 0.7f)
        },
        border = BorderStroke(1.dp, if (isReady || isActiveBuff) accent else CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Bolt,
                    contentDescription = skill.title,
                    tint = if (isReady || isActiveBuff) accent else TextMuted,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = skill.shortTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isReady || isActiveBuff) TextPrimary else TextMuted,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
            Text(
                text = when {
                    isActiveBuff -> "ĐANG BẬT!"
                    isReady -> "Sẵn sàng"
                    else -> "${cooldownSec}s"
                },
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = if (isReady || isActiveBuff) accent else TextMuted,
                maxLines = 1
            )
        }
    }
}

