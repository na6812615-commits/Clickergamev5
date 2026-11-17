package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Upgrade
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ExpeditionCatalog
import com.example.data.ExpeditionZoneDef
import com.example.data.FleetShipType
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberCyanDim
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameUiState

@Composable
fun GalaxyFleetScreen(
    uiState: GameUiState,
    onUpgradeShip: (FleetShipType) -> Unit,
    onStartExpedition: (ExpeditionZoneDef) -> Unit,
    onClaimExpedition: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game
    var subTab by remember { mutableIntStateOf(0) } // 0 = Hạm Đội Phi Thuyền, 1 = Thám Hiểm Hành Tinh
    val activeZone = ExpeditionCatalog.findById(game.activeExpeditionId)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 640.dp)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onClose,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = ElevatedCard,
                            contentColor = CyberCyan
                        ),
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_galaxy_fleet_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Hạm Đội Thiên Hà")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HẠM ĐỘI & THÁM HIỂM",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${Formatters.formatDollars(game.dollars)} • ${game.shards}✦ • ${game.techChips}⚙",
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = JetBrainsMonoFontFamily),
                            color = TextSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CyberCyanDim,
                    border = BorderStroke(1.dp, CyberCyan.copy(alpha = 0.65f))
                ) {
                    Text(
                        text = "PWR ${game.fleetTotalPower}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = CyberCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.img_galaxy_fleet_banner),
                    contentDescription = "Hạm Đội Thiên Hà TapCore",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                listOf(ObsidianBgDeep.copy(alpha = 0.88f), Color.Transparent)
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = "KỶ NGUYÊN THIÊN HÀ",
                        style = MaterialTheme.typography.labelMedium,
                        color = AmberGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "+${Formatters.formatPoints(game.fleetPointsPerSec)}đ/s • +${Formatters.formatDollars(game.fleetDollarsPer10Sec)}/10s",
                        style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "DPS Boss tự động: ${Formatters.formatPoints(game.fleetBossDps)}/2s • +${game.fleetScoutLv * 1.5}% Crit",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonEmerald
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-tab switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FleetTabChip(
                    label = "Phi Thuyền (${game.fleetTotalPower} PWR)",
                    selected = subTab == 0,
                    accent = CyberCyan,
                    onClick = { subTab = 0 },
                    tag = "fleet_tab_ships",
                    modifier = Modifier.weight(1f)
                )
                val expTagLabel = when {
                    uiState.isExpeditionReady -> "Thám Hiểm (XONG!)"
                    activeZone != null -> "Thám Hiểm (${uiState.expeditionRemainingSec}s)"
                    else -> "Thám Hiểm (${game.totalExpeditionsCompleted})"
                }
                FleetTabChip(
                    label = expTagLabel,
                    selected = subTab == 1,
                    accent = AmberGold,
                    onClick = { subTab = 1 },
                    tag = "fleet_tab_expeditions",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 18.dp)
            ) {
                if (subTab == 0) {
                    items(FleetShipType.entries, key = { it.id }) { ship ->
                        FleetShipCard(
                            ship = ship,
                            currentLv = ship.levelIn(game),
                            dollars = game.dollars,
                            shards = game.shards,
                            onUpgrade = { onUpgradeShip(ship) }
                        )
                    }
                } else {
                    if (activeZone != null) {
                        item(key = "active_expedition_card") {
                            ActiveExpeditionCard(
                                zone = activeZone,
                                remainingSec = uiState.expeditionRemainingSec,
                                isReady = uiState.isExpeditionReady,
                                onClaim = onClaimExpedition
                            )
                        }
                    }

                    items(ExpeditionCatalog.zones, key = { it.id }) { zone ->
                        ExpeditionZoneCard(
                            zone = zone,
                            fleetPower = game.fleetTotalPower,
                            bonusMult = (1.0 + game.fleetTotalPower * 0.04) *
                                game.techExpeditionBonusMultiplier *
                                (if (game.hasVipPass) 1.5 else 1.0),
                            isAnyExpeditionActive = activeZone != null,
                            isCurrentZone = activeZone?.id == zone.id,
                            onLaunch = { onStartExpedition(zone) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FleetTabChip(
    label: String,
    selected: Boolean,
    accent: Color,
    onClick: () -> Unit,
    tag: String,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) accent else ElevatedCard,
            contentColor = if (selected) ObsidianBgDeep else TextSecondary
        ),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(tag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun FleetShipCard(
    ship: FleetShipType,
    currentLv: Int,
    dollars: Double,
    shards: Long,
    onUpgrade: () -> Unit
) {
    val isMax = currentLv >= ship.maxLevel
    val dollarCost = ship.dollarCostAt(currentLv)
    val shardCost = ship.shardCostAt(currentLv)
    val canAfford = !isMax && dollars >= dollarCost && shards >= shardCost
    val accent = when (ship) {
        FleetShipType.SCOUT -> CyberCyan
        FleetShipType.HARVESTER -> NeonEmerald
        FleetShipType.CRUISER -> CoralCrimson
        FleetShipType.MOTHERSHIP -> ElectricViolet
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fleet_ship_card_${ship.id}"),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface,
        border = BorderStroke(1.dp, if (currentLv > 0) accent.copy(alpha = 0.55f) else CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(
                        imageVector = Icons.Rounded.RocketLaunch,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = ship.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = ship.roleLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = accent
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElevatedCard,
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "LV.$currentLv/${ship.maxLevel}",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = ship.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (currentLv.toFloat() / ship.maxLevel.toFloat()).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = accent,
                trackColor = ElevatedCard
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onUpgrade,
                enabled = !isMax,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) accent else ElevatedCard,
                    contentColor = if (canAfford) ObsidianBgDeep else TextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("upgrade_ship_${ship.id}")
            ) {
                Icon(Icons.Rounded.Upgrade, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                val btnText = if (isMax) {
                    "ĐÃ ĐẠT CẤP TỐI ĐA"
                } else {
                    val shardPart = if (shardCost > 0L) " + $shardCost✦" else ""
                    "Nâng LV.${currentLv + 1} (${Formatters.formatDollars(dollarCost)}$shardPart)"
                }
                Text(text = btnText, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActiveExpeditionCard(
    zone: ExpeditionZoneDef,
    remainingSec: Long,
    isReady: Boolean,
    onClaim: () -> Unit
) {
    val elapsedFrac = ((zone.durationSec - remainingSec).toFloat() / zone.durationSec.coerceAtLeast(1L).toFloat())
        .coerceIn(0f, 1f)
    val accent = if (isReady) NeonEmerald else AmberGold

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_expedition_card"),
        shape = RoundedCornerShape(16.dp),
        color = ElevatedCard,
        border = BorderStroke(1.5.dp, accent)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Rounded.Explore, contentDescription = null, tint = accent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isReady) "HOÀN TẤT: ${zone.title}" else "ĐANG THÁM HIỂM: ${zone.title}",
                        style = MaterialTheme.typography.titleSmall,
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = if (isReady) "SẴN SÀNG" else "${remainingSec}s",
                    style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (isReady) 1f else elapsedFrac },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = accent,
                trackColor = CarbonSurface
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onClaim,
                enabled = isReady,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isReady) NeonEmerald else CarbonSurface,
                    contentColor = if (isReady) ObsidianBgDeep else TextMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("claim_expedition_button")
            ) {
                Icon(Icons.Rounded.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isReady) "THU HOẠCH KHO BÁU THÁM HIỂM" else "Đang bay về căn cứ...",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ExpeditionZoneCard(
    zone: ExpeditionZoneDef,
    fleetPower: Int,
    bonusMult: Double,
    isAnyExpeditionActive: Boolean,
    isCurrentZone: Boolean,
    onLaunch: () -> Unit
) {
    val unlocked = fleetPower >= zone.minFleetPower
    val accent = if (unlocked) AmberGold else TextMuted

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("expedition_zone_${zone.id}"),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface,
        border = BorderStroke(1.dp, if (isCurrentZone) AmberGold else CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = zone.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = zone.subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElevatedCard
                ) {
                    Text(
                        text = "${zone.durationSec}s",
                        style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFontFamily),
                        color = CyberCyan,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Quà dự kiến: +${Formatters.formatPoints(zone.basePoints * bonusMult)}đ • +$${(zone.baseDollars * bonusMult).toLong()} • +${zone.baseShards}✦ • +${zone.baseTechChips}⚙ • +${zone.seasonXp} XP",
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
                color = NeonEmerald
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Yêu cầu Hạm đội: PWR ≥ ${zone.minFleetPower} (Hiện tại: $fleetPower)",
                style = MaterialTheme.typography.labelSmall,
                color = if (unlocked) TextSecondary else CoralCrimson
            )

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onLaunch,
                enabled = unlocked && !isAnyExpeditionActive,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (unlocked && !isAnyExpeditionActive) accent else ElevatedCard,
                    contentColor = if (unlocked && !isAnyExpeditionActive) ObsidianBgDeep else TextMuted
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("launch_expedition_${zone.id}")
            ) {
                Text(
                    text = when {
                        isCurrentZone -> "HẠM ĐỘI ĐANG Ở ĐÂY"
                        isAnyExpeditionActive -> "ĐANG CÓ CHUYẾN BAY KHÁC"
                        !unlocked -> "CHƯA ĐỦ LỰC CHIẾN (PWR ${zone.minFleetPower})"
                        else -> "PHÓNG HẠM ĐỘI (${zone.durationSec}s)"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
