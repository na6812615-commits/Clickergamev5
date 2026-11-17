package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CyberDroneId
import com.example.data.CyberRuneId
import com.example.data.GameStateEntity
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.AmberGoldDim
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.NeonEmeraldDim
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameUiState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DroneAndRuneScreen(
    uiState: GameUiState,
    onUpgradeDrone: (CyberDroneId) -> Unit,
    onActivateDroneOverclock: () -> Unit,
    onUpgradeRune: (CyberRuneId) -> Unit,
    onToggleEquipRune: (CyberRuneId) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game
    var selectedTab by remember { mutableIntStateOf(0) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("drone_rune_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ElevatedCard,
                            contentColor = CyberCyan
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_drone_rune_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Trạm Drone & Rune")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TRẠM DRONE AI & RUNE",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Đồng hành tự động & Ma trận Rune 3-Slot",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberCyanDim,
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = "${game.voidEssence}🌌",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Trạm Drone AI (LV.${game.totalDroneLevel})", fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Rounded.PrecisionManufacturing, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CyberCyan,
                        selectedLabelColor = ObsidianBgDeep,
                        containerColor = CarbonSurface,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("tab_drone_hangar")
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Ma Trận Rune (${game.equippedRuneIds.size}/3)", fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricViolet,
                        selectedLabelColor = Color.White,
                        containerColor = CarbonSurface,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("tab_rune_matrix")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                DroneHangarContent(
                    game = game,
                    overclockRemainingSec = uiState.droneOverclockRemainingSec,
                    onUpgradeDrone = onUpgradeDrone,
                    onActivateDroneOverclock = onActivateDroneOverclock,
                    modifier = Modifier.weight(1f)
                )
            } else {
                RuneMatrixContent(
                    game = game,
                    onUpgradeRune = onUpgradeRune,
                    onToggleEquipRune = onToggleEquipRune,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DroneHangarContent(
    game: GameStateEntity,
    overclockRemainingSec: Long,
    onUpgradeDrone: (CyberDroneId) -> Unit,
    onActivateDroneOverclock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverclock = overclockRemainingSec > 0L
    val swarmMult = if (isOverclock) 3.0 else 1.0

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, if (isOverclock) AmberGold else CyberCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOverclock) "BẦY ĐÀN ÉP XUNG x3 (${overclockRemainingSec}s)" else "TỔNG HỢP DRONE AI",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isOverclock) AmberGold else CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Điểm/s: +${Formatters.formatPoints(game.dronePointsPerSec * swarmMult)} • Boss DPS: +${Formatters.formatPoints(game.droneBossDps * swarmMult)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Text(
                                text = "Tài nguyên hiện có: ${Formatters.formatDollars(game.dollars)} • ${game.techChips}⚙ • ${game.quantumCores}⚛",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonEmerald
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onActivateDroneOverclock,
                            enabled = !isOverclock && game.totalDroneLevel > 0,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AmberGold,
                                contentColor = ObsidianBgDeep
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("activate_drone_overclock_button")
                        ) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOverclock) "${overclockRemainingSec}s" else "ÉP XUNG $25+2⚙",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        items(CyberDroneId.entries, key = { it.id }) { drone ->
            val lv = drone.levelIn(game)
            val isMax = lv >= drone.maxLevel
            val dCost = drone.dollarCostAt(lv)
            val cCost = drone.chipCostAt(lv)
            val qCost = drone.coreCostAt(lv)
            val canAfford = !isMax && game.dollars >= dCost && game.techChips >= cCost && game.quantumCores >= qCost
            val accent = when (drone) {
                CyberDroneId.PULSE_DRONE -> CyberCyan
                CyberDroneId.MINER_DRONE -> NeonEmerald
                CyberDroneId.STRIKER_DRONE -> CoralCrimson
                CyberDroneId.CHRONO_DRONE -> ElectricViolet
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, if (lv > 0) accent.copy(alpha = 0.55f) else CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        DroneRotorEmblem(active = lv > 0, accent = accent, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = drone.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "LV.$lv / ${drone.maxLevel}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "[${drone.roleLabel}] ${drone.benefitSummary}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { (lv.toFloat() / drone.maxLevel.toFloat()).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(99.dp)),
                        color = accent,
                        trackColor = ElevatedCard
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    val costLabel = if (isMax) {
                        "ĐÃ ĐẠT CẤP TỐI ĐA"
                    } else {
                        buildString {
                            append("NÂNG CẤP (${Formatters.formatDollars(dCost)} + ${cCost}⚙")
                            if (qCost > 0L) append(" + ${qCost}⚛")
                            append(")")
                        }
                    }
                    Button(
                        onClick = { onUpgradeDrone(drone) },
                        enabled = canAfford,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = accent,
                            contentColor = ObsidianBgDeep,
                            disabledContainerColor = ElevatedCard,
                            disabledContentColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("upgrade_drone_${drone.id}")
                    ) {
                        Text(costLabel, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun RuneMatrixContent(
    game: GameStateEntity,
    onUpgradeRune: (CyberRuneId) -> Unit,
    onToggleEquipRune: (CyberRuneId) -> Unit,
    modifier: Modifier = Modifier
) {
    val equipped = game.equippedRuneIds
    val synergyBonusPct = ((game.equippedRuneSynergyMultiplier - 1.0) * 100).toInt()

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.65f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "KHAY MA TRẬN (${equipped.size} / 3 SLOT)",
                            style = MaterialTheme.typography.titleMedium,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(shape = RoundedCornerShape(8.dp), color = AmberGoldDim) {
                            Text(
                                text = if (synergyBonusPct > 0) "CỘNG HƯỞNG +$synergyBonusPct%" else "Lắp ≥2 Rune để Cộng Hưởng",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (slotIdx in 0 until 3) {
                            val runeId = equipped.getOrNull(slotIdx)
                            val runeDef = CyberRuneId.entries.firstOrNull { it.id == runeId }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                color = if (runeDef != null) ElevatedCard else ObsidianBgDeep,
                                border = BorderStroke(1.dp, if (runeDef != null) ElectricViolet else CardBorder)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(6.dp)) {
                                    Text(
                                        text = if (runeDef != null) "${runeDef.tag}\nBậc ${runeDef.rankIn(game)}" else "Ô Trống ${slotIdx + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (runeDef != null) CyberCyan else TextMuted,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tài nguyên rèn: ${game.shards}✦ Mảnh • ${game.quantumCores}⚛ Lõi • ${game.voidEssence}🌌 Tinh Chất",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonEmerald
                    )
                }
            }
        }

        items(CyberRuneId.entries, key = { it.id }) { rune ->
            val rank = rune.rankIn(game)
            val isMax = rank >= rune.maxRank
            val isEquipped = game.isRuneEquipped(rune)
            val sCost = rune.shardCostAt(rank)
            val qCost = rune.coreCostAt(rank)
            val vCost = rune.voidCostAt(rank)
            val canForge = !isMax && game.shards >= sCost && game.quantumCores >= qCost && game.voidEssence >= vCost
            val accent = when (rune) {
                CyberRuneId.BERSERK -> CoralCrimson
                CyberRuneId.MIDAS -> AmberGold
                CyberRuneId.VAMPIRE -> Color(0xFFFF6E9C)
                CyberRuneId.CHRONO -> CyberCyan
                CyberRuneId.HARVEST -> NeonEmerald
                CyberRuneId.SINGULARITY -> ElectricViolet
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, if (isEquipped) accent else CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        HexRuneGlyph(unlocked = rank > 0, accent = accent, modifier = Modifier.size(46.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rune.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = accent,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (rank == 0) "Chưa rèn" else "Bậc $rank/${rune.maxRank}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = if (rank > 0) TextPrimary else TextMuted,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = rune.effectSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val forgeText = when {
                            isMax -> "TỐI ĐA"
                            rank == 0 -> "RÈN (${sCost}✦ + ${qCost}⚛)"
                            else -> "THĂNG BẬC (${sCost}✦ + ${qCost}⚛ + ${vCost}🌌)"
                        }
                        Button(
                            onClick = { onUpgradeRune(rune) },
                            enabled = canForge,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = accent,
                                contentColor = ObsidianBgDeep,
                                disabledContainerColor = ElevatedCard,
                                disabledContentColor = TextMuted
                            ),
                            modifier = Modifier
                                .weight(1.4f)
                                .heightIn(min = 48.dp)
                                .testTag("upgrade_rune_${rune.id}")
                        ) {
                            Text(forgeText, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        if (rank > 0) {
                            OutlinedButton(
                                onClick = { onToggleEquipRune(rune) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, if (isEquipped) NeonEmerald else CyberCyan),
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("equip_rune_${rune.id}")
                            ) {
                                if (isEquipped) {
                                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = NeonEmerald, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (isEquipped) "Đang Lắp" else "Lắp Slot",
                                    color = if (isEquipped) NeonEmerald else CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DroneRotorEmblem(
    active: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "drone_spin")
    val angle by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(if (active) 3200 else 9000, easing = LinearEasing), RepeatMode.Restart),
        label = "rotor"
    )
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width / 2f, size.height / 2f)
            val r = size.minDimension / 2f - 3.dp.toPx()
            rotate(degrees = if (active) angle else 0f, pivot = c) {
                drawCircle(
                    color = if (active) accent.copy(alpha = 0.25f) else ElevatedCard,
                    radius = r
                )
                drawCircle(
                    color = if (active) accent else CardBorder,
                    radius = r,
                    style = Stroke(width = 2.dp.toPx())
                )
                drawLine(
                    color = if (active) accent else TextMuted,
                    start = Offset(c.x - r * 0.7f, c.y),
                    end = Offset(c.x + r * 0.7f, c.y),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = if (active) accent else TextMuted,
                    start = Offset(c.x, c.y - r * 0.7f),
                    end = Offset(c.x, c.y + r * 0.7f),
                    strokeWidth = 2.dp.toPx()
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.Memory,
            contentDescription = null,
            tint = if (active) accent else TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun HexRuneGlyph(
    unlocked: Boolean,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.minDimension / 2f - 2.dp.toPx()
            val c = Offset(size.width / 2f, size.height / 2f)
            val path = Path()
            for (i in 0 until 6) {
                val a = (i * 60.0 - 90.0) * PI / 180.0
                val pt = Offset(c.x + r * cos(a).toFloat(), c.y + r * sin(a).toFloat())
                if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
            }
            path.close()
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(
                        (if (unlocked) accent else TextMuted).copy(alpha = 0.35f),
                        ObsidianBgDeep
                    )
                )
            )
            drawPath(
                path = path,
                color = if (unlocked) accent else CardBorder,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = null,
            tint = if (unlocked) accent else TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}
