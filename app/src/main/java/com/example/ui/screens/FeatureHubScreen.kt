package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Flare
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material.icons.rounded.MilitaryTech
import androidx.compose.material.icons.rounded.PrecisionManufacturing
import androidx.compose.material.icons.rounded.RocketLaunch
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AchievementCatalog
import com.example.data.CyberStockTicker
import com.example.ui.Formatters
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CarbonSurface
import com.example.ui.theme.CoralCrimson
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.ElevatedCard
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianBgDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.GameUiState

@Composable
fun FeatureHubScreen(
    uiState: GameUiState,
    onOpenDailyRewards: () -> Unit,
    onOpenSeasonPass: () -> Unit,
    onOpenBadgeOverlay: () -> Unit,
    onOpenBossAndRelics: () -> Unit,
    onOpenCosmicArena: () -> Unit,
    onOpenSingularityRaid: () -> Unit,
    onOpenCyberEmpire: () -> Unit,
    onOpenGalaxyFleet: () -> Unit,
    onOpenTechTree: () -> Unit,
    onOpenQuantumAlchemy: () -> Unit,
    onOpenDroneAndRune: () -> Unit,
    onOpenCyberExchange: () -> Unit,
    onOpenCasino: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val game = uiState.game
    val unlockedBadges = AchievementCatalog.all.count { def ->
        (uiState.achievementRecords[def.id]?.isUnlocked == true) || def.progressSelector(game) >= def.targetValue
    }
    val fleetBadge = when {
        uiState.isExpeditionReady -> "✓"
        game.activeExpeditionId.isNotBlank() -> "${uiState.expeditionRemainingSec}s"
        else -> null
    }
    val corePrice = uiState.marketSnapshot.prices[CyberStockTicker.CORE] ?: CyberStockTicker.CORE.initialPrice

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ObsidianBg.copy(alpha = 0.98f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .testTag("feature_hub_screen"),
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
                            .testTag("close_feature_hub_button")
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Đóng Trung Tâm Tính Năng")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "TRUNG TÂM TÍNH NĂNG",
                            style = MaterialTheme.typography.titleLarge,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tất cả sự kiện, chiến đấu, đế chế và giải trí",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    HubSectionHeader("SỰ KIỆN & THÀNH TỰU", AmberGold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HubFeatureCard(
                                title = "Điểm Danh",
                                subtitle = "Chuỗi ${game.dailyStreak} ngày",
                                accent = AmberGold,
                                badgeText = if (uiState.canClaimDailyToday) "!" else null,
                                icon = Icons.Rounded.CardGiftcard,
                                tag = "open_daily_rewards_button",
                                onClick = {
                                    onClose()
                                    onOpenDailyRewards()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            HubFeatureCard(
                                title = "Season Pass",
                                subtitle = "Mùa 1 • Cấp ${game.seasonLevel}",
                                accent = ElectricViolet,
                                badgeText = if (uiState.unclaimedSeasonRewardsCount > 0) "${uiState.unclaimedSeasonRewardsCount}" else null,
                                icon = Icons.Rounded.AutoAwesome,
                                tag = "open_season_pass_button",
                                onClick = {
                                    onClose()
                                    onOpenSeasonPass()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HubFeatureCard(
                            title = "Tủ Huy Hiệu Thành Tựu",
                            subtitle = "Đã mở khóa $unlockedBadges / ${AchievementCatalog.all.size} huy hiệu (Room DB)",
                            accent = NeonEmerald,
                            badgeText = if (uiState.unclaimedAchievementCount > 0) "${uiState.unclaimedAchievementCount}" else null,
                            icon = Icons.Rounded.MilitaryTech,
                            tag = "open_badge_overlay_home_button",
                            onClick = {
                                onClose()
                                onOpenBadgeOverlay()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    HubSectionHeader("CHIẾN ĐẤU & KỲ ĐIỂM", CoralCrimson)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HubFeatureCard(
                                title = "Boss & Cổ Vật",
                                subtitle = "Boss LV.${game.bossLevel}",
                                accent = CoralCrimson,
                                badgeText = if (uiState.unclaimedDailyQuestCount > 0) "${uiState.unclaimedDailyQuestCount}" else null,
                                icon = Icons.Rounded.Whatshot,
                                tag = "open_boss_relics_button",
                                onClick = {
                                    onClose()
                                    onOpenBossAndRelics()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            HubFeatureCard(
                                title = "Đấu Trường & Tháp",
                                subtitle = "Đợt ${game.arenaWave} • Tầng ${game.towerHighestFloorClaimed}",
                                accent = AmberGold,
                                badgeText = if (uiState.readyTowerFloorCount > 0) "!" else null,
                                icon = Icons.Rounded.EmojiEvents,
                                tag = "open_cosmic_arena_button",
                                onClick = {
                                    onClose()
                                    onOpenCosmicArena()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HubFeatureCard(
                            title = "Kỳ Điểm & Ngục Tối Raid",
                            subtitle = "KĐ.${game.singularityLv} • ${game.voidEssence}🌌 • ${game.raidEnergy} Chìa khóa Raid",
                            accent = ElectricViolet,
                            badgeText = when {
                                game.canAscendSingularity -> "★"
                                game.activeDungeonId.isNotBlank() -> "⚔"
                                else -> null
                            },
                            icon = Icons.Rounded.Flare,
                            tag = "open_singularity_raid_button",
                            onClick = {
                                onClose()
                                onOpenSingularityRaid()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    HubSectionHeader("ĐẾ CHẾ & CÔNG NGHỆ", CyberCyan)
                    Spacer(modifier = Modifier.height(6.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HubFeatureCard(
                                title = "Đế Chế Cyber",
                                subtitle = "Tổng cấp LV.${game.totalEmpireLevel}",
                                accent = AmberGold,
                                badgeText = if (uiState.hasPendingEmpireTreasury) "$" else null,
                                icon = Icons.Rounded.Apartment,
                                tag = "open_cyber_empire_button",
                                onClick = {
                                    onClose()
                                    onOpenCyberEmpire()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            HubFeatureCard(
                                title = "Hạm Đội Vũ Trụ",
                                subtitle = "Lực chiến ${game.fleetTotalPower}",
                                accent = CyberCyan,
                                badgeText = fleetBadge,
                                icon = Icons.Rounded.RocketLaunch,
                                tag = "open_galaxy_fleet_button",
                                onClick = {
                                    onClose()
                                    onOpenGalaxyFleet()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            HubFeatureCard(
                                title = "Cây Công Nghệ",
                                subtitle = "${game.techChips}⚙ Chip",
                                accent = ElectricViolet,
                                badgeText = if (game.techChips >= 2L) "${game.techChips}" else null,
                                icon = Icons.Rounded.Memory,
                                tag = "open_tech_tree_button",
                                onClick = {
                                    onClose()
                                    onOpenTechTree()
                                },
                                modifier = Modifier.weight(1f)
                            )
                            HubFeatureCard(
                                title = "Lò Giả Kim & Đồ",
                                subtitle = "${game.quantumCores}⚛ Lõi Lượng Tử",
                                accent = NeonEmerald,
                                badgeText = if (game.quantumCores >= 1L) "${game.quantumCores}" else null,
                                icon = Icons.Rounded.Science,
                                tag = "open_quantum_alchemy_button",
                                onClick = {
                                    onClose()
                                    onOpenQuantumAlchemy()
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HubFeatureCard(
                            title = "Trạm Drone AI & Ma Trận Rune",
                            subtitle = "Drone LV.${game.totalDroneLevel} • Rune ${game.equippedRuneIds.size}/3 Slot",
                            accent = CyberCyan,
                            badgeText = if (uiState.droneOverclockRemainingSec > 0L) "x3" else null,
                            icon = Icons.Rounded.PrecisionManufacturing,
                            tag = "open_drone_rune_button",
                            onClick = {
                                onClose()
                                onOpenDroneAndRune()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    HubSectionHeader("THỊ TRƯỜNG & GIẢI TRÍ", NeonEmerald)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        HubFeatureCard(
                            title = "Sàn Chứng Khoán",
                            subtitle = "CORE ${Formatters.formatDollars(corePrice)}",
                            accent = NeonEmerald,
                            badgeText = null,
                            icon = Icons.Rounded.ShowChart,
                            tag = "open_cyber_exchange_button",
                            onClick = {
                                onClose()
                                onOpenCyberExchange()
                            },
                            modifier = Modifier.weight(1f)
                        )
                        HubFeatureCard(
                            title = "Cyber Casino",
                            subtitle = "777 Hũ & Quay Số",
                            accent = AmberGold,
                            badgeText = null,
                            icon = Icons.Rounded.Casino,
                            tag = "open_casino_button",
                            onClick = {
                                onClose()
                                onOpenCasino()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HubSectionHeader(title: String, accent: Color) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = accent,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun HubFeatureCard(
    title: String,
    subtitle: String,
    accent: Color,
    badgeText: String?,
    icon: ImageVector,
    tag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 66.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(16.dp),
        color = CarbonSurface,
        border = BorderStroke(1.dp, accent.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BadgedBox(
                badge = {
                    if (badgeText != null) {
                        Badge(containerColor = AmberGold, contentColor = ObsidianBgDeep) {
                            Text(badgeText, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accent.copy(alpha = 0.14f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = title,
                            tint = accent,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
