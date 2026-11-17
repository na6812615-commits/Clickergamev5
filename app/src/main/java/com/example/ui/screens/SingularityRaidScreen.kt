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
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Shield
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GameStateEntity
import com.example.data.RaidDungeonCatalog
import com.example.data.RaidDungeonDef
import com.example.data.SingularityPerkId
import com.example.ui.Formatters
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
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameUiState
import com.example.viewmodel.SingularityEngine

@Composable
fun SingularityRaidScreen(
    uiState: GameUiState,
    onRechargeRaidEnergy: () -> Unit,
    onEnterDungeon: (RaidDungeonDef) -> Unit,
    onStrikeDungeonEnemy: () -> Unit,
    onPerformSingularityAscension: () -> Unit,
    onUpgradeSingularityPerk: (SingularityPerkId) -> Unit,
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
            .testTag("singularity_raid_screen"),
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
                            contentColor = ElectricViolet
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_singularity_raid_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Kỳ Điểm & Ngục Tối")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "KỲ ĐIỂM & NGỤC TỐI RAID",
                            style = MaterialTheme.typography.titleLarge,
                            color = ElectricViolet,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Đột kích 3 Ải • Vượt Cấp Kỳ Điểm (${game.voidEssence}🌌)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberCyanDim,
                    border = BorderStroke(1.dp, ElectricViolet.copy(alpha = 0.7f))
                ) {
                    Text(
                        text = "KĐ.${game.singularityLv}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = ElectricViolet,
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
                    label = { Text("Ngục Tối Raid (${game.raidEnergy}/${RaidDungeonCatalog.MAX_RAID_ENERGY})", fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Rounded.Shield, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CoralCrimson,
                        selectedLabelColor = Color.White,
                        containerColor = CarbonSurface,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("tab_raid_dungeons")
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Kỳ Điểm (${game.voidEssence}🌌)", fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Rounded.Flare, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElectricViolet,
                        selectedLabelColor = Color.White,
                        containerColor = CarbonSurface,
                        labelColor = TextSecondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("tab_singularity_ascension")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (selectedTab == 0) {
                RaidDungeonsContent(
                    game = game,
                    lastRaidDamageText = uiState.lastRaidDamageText,
                    onRechargeRaidEnergy = onRechargeRaidEnergy,
                    onEnterDungeon = onEnterDungeon,
                    onStrikeDungeonEnemy = onStrikeDungeonEnemy,
                    modifier = Modifier.weight(1f)
                )
            } else {
                SingularityAscensionContent(
                    game = game,
                    onPerformSingularityAscension = onPerformSingularityAscension,
                    onUpgradeSingularityPerk = onUpgradeSingularityPerk,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RaidDungeonsContent(
    game: GameStateEntity,
    lastRaidDamageText: String?,
    onRechargeRaidEnergy: () -> Unit,
    onEnterDungeon: (RaidDungeonDef) -> Unit,
    onStrikeDungeonEnemy: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDungeon = RaidDungeonCatalog.findById(game.activeDungeonId)

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
                border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.55f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Rounded.Key, contentDescription = null, tint = AmberGold, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CHÌA KHÓA RAID: ${game.raidEnergy} / ${RaidDungeonCatalog.MAX_RAID_ENERGY}",
                                style = MaterialTheme.typography.titleSmall,
                                color = AmberGold,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "Tap ${game.raidTapCounter}/${RaidDungeonCatalog.TAPS_PER_ENERGY} (+1 Chìa) • Đã vượt: ${game.totalDungeonsCleared} Ngục Tối",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(
                        onClick = onRechargeRaidEnergy,
                        enabled = game.raidEnergy < RaidDungeonCatalog.MAX_RAID_ENERGY && game.dollars >= 20.0,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, AmberGold),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("recharge_raid_energy_button")
                    ) {
                        Text("+2 Chìa ($20)", color = AmberGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (activeDungeon != null) {
            item {
                val maxHp = SingularityEngine.stageHp(activeDungeon, game.dungeonStage)
                val hpFrac = (game.dungeonEnemyHp / maxHp).toFloat().coerceIn(0f, 1f)
                val stageName = activeDungeon.stageNames.getOrElse(game.dungeonStage - 1) { "Trùm Cuối" }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_dungeon_panel"),
                    shape = RoundedCornerShape(18.dp),
                    color = CoralCrimsonDim,
                    border = BorderStroke(1.5.dp, CoralCrimson)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${activeDungeon.title} • Ải ${game.dungeonStage}/3",
                                style = MaterialTheme.typography.titleMedium,
                                color = AmberGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "+${activeDungeon.rewardVoidEssence}🌌",
                                style = MaterialTheme.typography.labelMedium,
                                color = ElectricViolet,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stageName,
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { hpFrac },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = CoralCrimson,
                            trackColor = ObsidianBgDeep
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "HP: ${Formatters.formatPoints(game.dungeonEnemyHp)} / ${Formatters.formatPoints(maxHp)}",
                                style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                color = TextPrimary
                            )
                            if (lastRaidDamageText != null) {
                                Text(
                                    text = lastRaidDamageText,
                                    style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                                    color = AmberGold,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = onStrikeDungeonEnemy,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CoralCrimson,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 52.dp)
                                .testTag("strike_dungeon_enemy_button")
                        ) {
                            Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TẤN CÔNG ĐỘT KÍCH!", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        items(RaidDungeonCatalog.dungeons, key = { it.id }) { dungeon ->
            val canEnter = activeDungeon == null && game.raidEnergy >= dungeon.energyCost
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, if (canEnter) CyberCyan.copy(alpha = 0.55f) else CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dungeon.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(shape = RoundedCornerShape(8.dp), color = AmberGoldDim) {
                            Text(
                                text = "${dungeon.energyCost} Chìa",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberGold,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dungeon.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Thưởng 3 Ải: +$${dungeon.rewardDollars.toLong()} • +${dungeon.rewardChips}⚙ • +${dungeon.rewardShards}✦ • +${dungeon.rewardCores}⚛ • +${dungeon.rewardVoidEssence}🌌",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonEmerald,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onEnterDungeon(dungeon) },
                        enabled = canEnter,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = ObsidianBgDeep,
                            disabledContainerColor = ElevatedCard,
                            disabledContentColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("enter_dungeon_${dungeon.id}")
                    ) {
                        Text(
                            text = if (activeDungeon != null) "ĐANG TRONG NGỤC TỐI" else "TIẾN VÀO NGỤC TỐI (${dungeon.energyCost} Chìa)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SingularityAscensionContent(
    game: GameStateEntity,
    onPerformSingularityAscension: () -> Unit,
    onUpgradeSingularityPerk: (SingularityPerkId) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = CarbonSurface,
                border = BorderStroke(1.5.dp, if (game.canAscendSingularity) ElectricViolet else CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SingularityVortexOrb(active = game.canAscendSingularity, modifier = Modifier.size(76.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CẤP KỲ ĐIỂM HIỆN TẠI: ${game.singularityLv}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ElectricViolet,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hệ Số Kỳ Điểm: x${Formatters.formatDecimal(game.singularityGlobalMultiplier)} Toàn Sản Lượng",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Điều kiện: Restart LV.${game.singularityRequirementRestartLv} (hiện LV.${game.restartLv}) hoặc ${Formatters.formatPoints(game.singularityRequirementPoints)}đ trọn đời",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = "Giữ nguyên: 💎 Kim Cương, Shop VIP, Huy Hiệu, Drone AI, Rune, Đế Chế & Hạm Đội",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonEmerald
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onPerformSingularityAscension,
                        enabled = game.canAscendSingularity,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricViolet,
                            contentColor = Color.White,
                            disabledContainerColor = ElevatedCard,
                            disabledContentColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 50.dp)
                            .testTag("perform_singularity_button")
                    ) {
                        val preview = game.pendingVoidEssenceOnSingularity
                        Text(
                            text = if (game.canAscendSingularity) "VƯỢT CẤP KỲ ĐIỂM (+${preview}🌌)" else "CHƯA ĐỦ ĐIỀU KIỆN KỲ ĐIỂM",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        items(SingularityPerkId.entries, key = { it.id }) { perk ->
            val lv = perk.levelIn(game)
            val isMax = lv >= perk.maxLevel
            val cost = perk.voidCostAt(lv)
            val canUpgrade = !isMax && game.voidEssence >= cost

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CarbonSurface,
                border = BorderStroke(1.dp, if (lv > 0) ElectricViolet.copy(alpha = 0.55f) else CardBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = perk.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = AmberGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "LV.$lv / ${perk.maxLevel}",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = perk.benefitSummary,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { onUpgradeSingularityPerk(perk) },
                        enabled = canUpgrade,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonEmerald,
                            contentColor = ObsidianBgDeep,
                            disabledContainerColor = ElevatedCard,
                            disabledContentColor = TextMuted
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("upgrade_singularity_perk_${perk.id}")
                    ) {
                        Text(
                            text = if (isMax) "ĐÃ TỐI ĐA" else "THĂNG HOA (${cost}🌌 Tinh Chất)",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SingularityVortexOrb(
    active: Boolean,
    modifier: Modifier = Modifier
) {
    val inf = rememberInfiniteTransition(label = "singularity_vortex")
    val rot by inf.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Restart),
        label = "vortex_rot"
    )
    Canvas(modifier = modifier) {
        val c = Offset(size.width / 2f, size.height / 2f)
        val r = size.minDimension / 2f - 4.dp.toPx()
        val accent = if (active) ElectricViolet else CyberCyan
        rotate(degrees = rot, pivot = c) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ObsidianBgDeep, accent.copy(alpha = 0.55f), Color.Transparent)
                ),
                radius = r
            )
            drawCircle(
                color = accent,
                radius = r * 0.72f,
                style = Stroke(width = 2.4.dp.toPx())
            )
            drawCircle(
                color = AmberGold.copy(alpha = 0.7f),
                radius = r * 0.44f,
                style = Stroke(width = 1.6.dp.toPx())
            )
        }
    }
}
